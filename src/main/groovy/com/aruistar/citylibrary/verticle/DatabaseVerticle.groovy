package com.aruistar.citylibrary.verticle

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.reactiverse.pgclient.Json
import io.reactiverse.pgclient.PgClient
import io.reactiverse.pgclient.PgPoolOptions
import io.reactiverse.pgclient.Tuple
import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject

@Slf4j
@CompileStatic
class DatabaseVerticle extends AbstractVerticle {


    final static String DATABASE_EVENTBUS_ADDRESS = "citylibrary.database.operation"

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

        def eb = vertx.eventBus()

        eb.consumer(DATABASE_EVENTBUS_ADDRESS, this.&insertDaliyCreprice)

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

//    Handler<Message<T>> handler
    def insertDaliyCreprice(Message message) {

        def action = message.headers().get("action")
        def json = message.body() as JsonObject

        switch (action) {
            case "insert.daliy_creprice":
                pgClient.preparedQuery('insert into daliy_creprice (v_city, j_data) VALUES ($1,$2)',
                        Tuple.of(json.getString("city"), Json.create(json.getJsonArray("price"))), {

                })
                break
        }

    }

}
