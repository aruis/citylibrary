package com.aruistar.citylibrary.verticle

import groovy.util.logging.Slf4j
import io.reactiverse.pgclient.PgClient
import io.reactiverse.pgclient.PgPoolOptions
import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject

@Slf4j
class DatabaseVerticle extends AbstractVerticle {


    PgClient pgClient

    @Override
    void start() throws Exception {
        log.info("verticle starting...")
        pgClient = buildDBClient()

//        pgClient.query("select 1", { ar ->
//            if (ar.succeeded()) {
//                log.info(new Date().toString() + " : " + ar.result().size())
//            } else {
//                log.info("数据库访问异常：${ar.cause().getMessage()}")
//            }
//        })
    }

    PgClient buildDBClient() {
        JsonObject config = config()
        String dbHost = config.getString('dbHost', '127.0.0.1')
        String dbName = config.getString('dbName', 'citylibrary')
        String dbUser = config.getString('dbUser', 'postgres')
        String dbPassword = config.getString('dbPassword', '')
        int dbPort = config.getInteger('dbPort', 5432)

        PgPoolOptions pgOptions = new PgPoolOptions()
                .setPort(dbPort)
                .setHost(dbHost)
                .setDatabase(dbName)
                .setUser(dbUser)
                .setPassword(dbPassword)
                .setMaxSize(5)

        return PgClient.pool(vertx, pgOptions)
    }

}
