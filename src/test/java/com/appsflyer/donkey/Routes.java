package com.appsflyer.donkey;

import com.appsflyer.donkey.server.route.RouteDefinition;
import io.vertx.ext.web.RoutingContext;

public final class Routes {
  
  private Routes() {}
  
  private static void returnRequest(RoutingContext ctx) {
    ctx.response().end(ctx.request().toString());
  }
  
  public static RouteDefinition helloWorld() {
    return RouteDefinition.create()
                          .path("/")
                          .handler(ctx -> ctx.response().end("Hello, World!"));
  }
  
  public static RouteDefinition echo() {
    return RouteDefinition.create().path("/echo").handler(Routes::returnRequest);
  }
}
