package com.aruistar.citylibrary.verticle

import com.diabolicallabs.vertx.cron.CronEventSchedulerVertical
import groovy.util.logging.Slf4j
import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject

@Slf4j
class CronVerticle extends AbstractVerticle {


    @Override
    void start() throws Exception {
        log.info("verticle starting...")

        vertx.deployVerticle(CronEventSchedulerVertical.newInstance(), { result ->
            if (result.succeeded()) {
                log.info("deploy CronEventSchedulerVertical OK")

                SpiderVerticle.CRON_EVENTBUS_ADDRESSES.each {
                    vertx.eventBus().send("cron.schedule", buildCronEvent(it))
                }

            }
        })
    }

    JsonObject buildCronEvent(String address) {

        def cron_expression = config().getString("cron", "0 30 15 ? * *")  //"0 48 9 ? * *" 每天上午9:48 just a example

        return new JsonObject()
                .put("cron_expression", cron_expression)
                .put("address", address)
                .put("message", "squid")
                .put("action", "publish")
                .put("timezone_name", "Asia/Shanghai")
    }
}
