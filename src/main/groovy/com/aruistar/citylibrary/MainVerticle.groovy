package com.aruistar.citylibrary

import com.aruistar.citylibrary.verticle.CronVerticle
import com.aruistar.citylibrary.verticle.SpiderVerticle
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

        def cron_expression = config.getString("cron", "0 48 9 ? * *")  //"0 48 9 ? * *" 每天上午9:48 just a example


        vertx.deployVerticle(CronVerticle.newInstance(), new DeploymentOptions().setConfig(new JsonObject([
                cron_expression: cron_expression
        ])), { handler ->
            if (handler.succeeded()) {//定时任务部署完毕后，部署爬虫服务
                vertx.deployVerticle(SpiderVerticle.newInstance())
            }
        })


    }
}
