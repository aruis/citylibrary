package com.aruistar.citylibrary.verticle

import groovy.util.logging.Slf4j
import io.vertx.core.AbstractVerticle

@Slf4j
class SpiderVerticle extends AbstractVerticle {

    @Override
    void start() throws Exception {
        log.info("SpiderVerticle starting...")

        vertx.eventBus().consumer("scheduled.address", { handler ->
            log.info("scheduled is call me.")
        })
    }
}
