package com.aruistar.citylibrary

import com.aruistar.citylibrary.verticle.CronVerticle
import com.aruistar.citylibrary.verticle.DatabaseVerticle
import com.aruistar.citylibrary.verticle.HttpVerticle
import com.aruistar.citylibrary.verticle.SpiderVerticle
import groovy.util.logging.Slf4j
import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.json.JsonObject

@Slf4j
class MainVerticle extends AbstractVerticle {

    JsonObject config

    @Override
    void start() throws Exception {
        config = config()
        log.info("config is :" + config.toString())

        vertx.deployVerticle(CronVerticle.newInstance(), buildDeploymentOptions("cron"))
        vertx.deployVerticle(SpiderVerticle.newInstance())
        vertx.deployVerticle(HttpVerticle.newInstance(), buildDeploymentOptions("http"))
        vertx.deployVerticle(DatabaseVerticle.newInstance(), buildDeploymentOptions("database"))

    }

    DeploymentOptions buildDeploymentOptions(String name) {
        new DeploymentOptions([config: config.getJsonObject(name)])
    }
}
