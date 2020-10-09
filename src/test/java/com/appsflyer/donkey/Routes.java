package com.appsflyer.donkey;

import com.appsflyer.donkey.server.ring.handler.RingHandler;
import com.appsflyer.donkey.server.route.RouteDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;

public final class Routes {
  
  private Routes() {}
  
  private static void returnRequest(RoutingContext ctx) {
    try {
      ctx.response()
         .end(Buffer.buffer(
             ClojureObjectMapper
                 .mapper()
                 .writeValueAsBytes(
                     ctx.get(RingHandler.RING_HANDLER_RESULT))));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
  
  public static RouteDefinition helloWorld() {
    return RouteDefinition.create()
                          .path("/")
                          .handler((RingHandler) ctx -> ctx.response().end("Hello, World!"));
  }
  
  public static RouteDefinition echo() {
    return RouteDefinition.create().path("/echo").handler((RingHandler) Routes::returnRequest);
  }
}
