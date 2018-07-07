package com.aruistar.citylibrary

import com.aruistar.citylibrary.verticle.CronVerticle
import com.aruistar.citylibrary.verticle.DatabaseVerticle
import com.aruistar.citylibrary.verticle.HttpVerticle
import com.aruistar.citylibrary.verticle.SpiderVerticle
import groovy.util.logging.Slf4j
import io.vertx.core.AbstractVerticle

@Slf4j
class MainVerticle extends AbstractVerticle {

    @Override
    void start() throws Exception {
        def config = config()
        log.info("config is :" + config.toString())

        vertx.deployVerticle(CronVerticle.newInstance())
        vertx.deployVerticle(SpiderVerticle.newInstance())
        vertx.deployVerticle(HttpVerticle.newInstance())
        vertx.deployVerticle(DatabaseVerticle.newInstance())

    }
}
