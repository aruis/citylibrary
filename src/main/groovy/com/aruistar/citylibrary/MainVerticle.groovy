package com.aruistar.citylibrary

import com.aruistar.citylibrary.verticle.CronVerticle
import groovy.util.logging.Slf4j
import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.json.JsonObject

@Slf4j
class MainVerticle extends AbstractVerticle {

    @Override
    void start() throws Exception {
        def config = config()
        log.info("config is :" + config.toString())

        def cron_expression = config.getString("cron", "0 15 10 ? * *")


        vertx.deployVerticle(CronVerticle.newInstance(), new DeploymentOptions().setConfig(new JsonObject([
                cron_expression: cron_expression
        ])))


    }
}
