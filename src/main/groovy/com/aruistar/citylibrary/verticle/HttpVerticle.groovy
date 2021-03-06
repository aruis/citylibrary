package com.aruistar.citylibrary.verticle

import groovy.util.logging.Slf4j
import io.vertx.core.AbstractVerticle
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler

@Slf4j
class HttpVerticle extends AbstractVerticle {

    @Override
    void start() throws Exception {
        log.info("verticle starting...")

        def server = vertx.createHttpServer()

        def port = config().getInteger("port", 8888)
        def router = Router.router(vertx)

        router.route().handler(StaticHandler.create().setCachingEnabled(false))

//        router.route().handler({ routingContext ->
//
//            // This handler will be called for every request
//            def response = routingContext.response()
//            response.putHeader("content-type", "text/plain")
//
//            // Write to the response and end it
//            response.end("Hello World from Vert.x-Web!")
//        })


        server.requestHandler(router.&accept)
                .listen(port, { ar ->
            if (ar.succeeded()) {
                log.info("server is running on port " + port)
            } else {
                log.error("Could not start a HTTP server", ar.cause())
            }

        })

    }
}
