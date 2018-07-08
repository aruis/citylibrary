package com.aruistar.citylibrary.verticle

import groovy.util.logging.Slf4j
import io.vertx.core.AbstractVerticle

@Slf4j
class SpiderVerticle extends AbstractVerticle {

    final static String CRON_EVENTBUS_ADDRESS_CREPRICE = "cron_eventbus_address_creprice"


    /*有几个定时爬虫，list就有几个eventbus地址*/
    final static List<String> CRON_EVENTBUS_ADDRESSES = [CRON_EVENTBUS_ADDRESS_CREPRICE]

    @Override
    void start() throws Exception {
        log.info("verticle starting...")

        vertx.eventBus().consumer(CRON_EVENTBUS_ADDRESS_CREPRICE, { handler ->
            log.info("cron is call me.")
        })
    }
}
