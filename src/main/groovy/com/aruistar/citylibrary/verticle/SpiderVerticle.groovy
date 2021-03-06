package com.aruistar.citylibrary.verticle

import com.aruistar.citylibrary.MainVerticle
import groovy.util.logging.Slf4j
import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import org.jsoup.Jsoup

@Slf4j
class SpiderVerticle extends AbstractVerticle {

    final static String CRON_EVENTBUS_ADDRESS_CREPRICE = "cron_eventbus_address_creprice"

    /*有几个定时爬虫，list就有几个eventbus地址*/
    final static List<String> CRON_EVENTBUS_ADDRESSES = [CRON_EVENTBUS_ADDRESS_CREPRICE]

    EventBus eventBus

    @Override
    void start() throws Exception {
        log.info("verticle starting...")
        eventBus = vertx.eventBus()

        if (MainVerticle.isDebug) {
            accessCreprice()
        }

        vertx.eventBus().consumer(CRON_EVENTBUS_ADDRESS_CREPRICE, { handler ->
            log.info("cron is call me.")
            accessCreprice()
        })
    }

    //TODO 可以优化下，考虑一次抓去不成功，可以每天多次抓取，尽可能保证数据足够。 另外，这个数据是 每日变动还是每月变动？
    def accessCreprice() {
        def jsonCity = new JsonObject(vertx.fileSystem().readFileBlocking("city.json"))
        def url = "http://www.creprice.cn/city/"
        // 获取creprice城市列表
        WebClient webClientCity = WebClient.create(vertx, new WebClientOptions().setTryUseCompression(true).setMaxPoolSize(10))
        jsonCity.getJsonArray("cities").eachWithIndex { JsonObject city, int cityIndex ->
            // 防止超时，等待1s再请求，351个城市351s在可接受范围
            vertx.setTimer(1000 * cityIndex + 1, {
                webClientCity.getAbs("${url + city.getString("cityCode")}.html")
                        .timeout(10000)
                        .send({
                    if (it.succeeded()) {
                        def html = it.result().bodyAsString()
                        def doc = Jsoup.parse(html)
                        def cityName = doc.getElementsByClass("citySelect_header")[0].text()

                        def elements = doc.getElementsByClass("u_area")
                        def cityPrice = [:]
                        cityPrice.city = cityName
                        cityPrice.price = []
                        elements.eachWithIndex { div, index ->
                            // class tith 的标题
                            def tiths = div.getElementsByClass("bt")

                            def u_lists = div.getElementsByClass("u_list")
                            // 上个月，这个月，今日和未来的价格
                            u_lists.eachWithIndex { u_list, int i ->
                                def lis = u_list.getElementsByTag("li")
                                lis.each { li ->
                                    def children = li.childNodes()
                                    def text = tiths[i].text()
                                    children.each { child ->
                                        text += child.text()
                                    }
                                    /**
                                     * 上月9,331 元/㎡ 同比 +26.03%
                                     * 上月平均总价： 109万元
                                     * 上月售租比： 42年
                                     * 上月挂牌市值： 176.8亿元
                                     * 上月挂牌数量： 1.6万套
                                     * 近一月9,398 元/㎡ 环比 +0.72%
                                     * 近一月新增房源 16,356 套
                                     * 今日9,628 元/㎡ 环比 +2.44%
                                     * 预测未来一月 9,520 元/㎡
                                     *
                                     */
                                    cityPrice.price << text
                                }
                            }
                        }
                        // todo save to database
                        println(cityPrice)

                        eventBus.send(DatabaseVerticle.DATABASE_EVENTBUS_ADDRESS, new JsonObject(cityPrice), new DeliveryOptions().addHeader("action", "insert.daliy_creprice"))

                    } else {
                        println(it.cause().message)
                    }
                })
            })
        }
    }
}
