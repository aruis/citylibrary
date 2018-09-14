package com.aruistar.citylibrary.verticle

import groovy.util.logging.Slf4j
import io.reactiverse.pgclient.PgClient
import io.reactiverse.pgclient.PgPool
import io.reactiverse.pgclient.PgPoolOptions
import io.reactiverse.pgclient.Tuple
import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject

@Slf4j
class DatabaseVerticle extends AbstractVerticle {


    PgPool pgClient

    @Override
    void start() throws Exception {
        log.info("verticle starting...")
        pgClient = buildDBClient()

        pgClient.query("SELECT * FROM USERS", { ar ->
            if (ar.succeeded()) {
                log.info(new Date().toString() + " : " + ar.result().size())
            } else {
                log.info("数据库访问异常：${ar.cause().getMessage()}")
            }
        })

        pgClient.getConnection({
            def conn = it.result()
            conn.preparedQuery('SELECT * FROM USERS WHERE id=$1', Tuple.of('1'), { ar ->
                if (ar.succeeded()) {
                    log.info(new Date().toString() + " : " + ar.result().size())
                } else {
                    log.info("数据库访问异常：${ar.cause().getMessage()}")
                }
                conn.close()
            })
        })

        def batch = []
        batch.add(Tuple.of("julien", "Julien Viet"))
        batch.add(Tuple.of("emad", "Emad Alblueshi"))
        // Execute the prepared batch
        pgClient.preparedBatch('INSERT INTO USERS (id, name) VALUES ($1, $2)', batch, { res ->
            if (res.succeeded()) {
                // Process rows
                def rows = res.result()
            } else {
                log.info("Batch failed ${res.cause()}")
            }
        })

        pgClient.getConnection({ res ->
            if (res.succeeded()) {
                // Transaction must use a connection
                def conn = res.result()
                // Begin the transaction
                def tx = conn.begin().abortHandler({ v ->
                    log.info("Transaction failed => rollbacked")
                })

                conn.preparedQuery('INSERT INTO Users (id,name) VALUES ($1,$2)', Tuple.of(123, 123), { ar ->
                    // Works fine of course
                    if (ar.succeeded()) {

                    } else {
                        log.info(ar.cause().getMessage())
                        tx.rollback()
                        conn.close()
                    }
                })
                conn.query("INSERT INTO Users (id,name) VALUES ('123','456')", { ar1 ->
                    // Fails and triggers transaction aborts
                })

                // Attempt to commit the transaction
                tx.commit({ ar2 ->
                    // But transaction abortion fails it
                    if (ar2.succeeded()) {
                        log.info("Transaction succeeded")
                    } else {
                        log.info("Transaction failed " + ar2.cause().getMessage())
                    }
                    // Return the connection to the pool
                    conn.close()
                })
            }
        })
    }

    PgPool buildDBClient() {
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
