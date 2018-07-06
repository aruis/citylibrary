package com.aruistar.citylibrary.verticle

import groovy.util.logging.Slf4j
import io.vertx.core.AbstractVerticle

@Slf4j
class HttpVerticle extends AbstractVerticle {

    @Override
    void start() throws Exception {
        log.info("HttpVerticle starting...")
    }
}
