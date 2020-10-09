/*
 * Copyright 2020 AppsFlyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appsflyer.donkey.server;

import com.appsflyer.donkey.Routes;
import com.appsflyer.donkey.server.route.RouteDefinition;
import com.appsflyer.donkey.server.route.RouteList;
import com.appsflyer.donkey.server.ring.route.RingRouteCreatorFactory;
import com.appsflyer.donkey.server.exception.ServerInitializationException;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.appsflyer.donkey.TestUtil.assert200;
import static com.appsflyer.donkey.TestUtil.doGet;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("integration")
@ExtendWith(VertxExtension.class)
class ServerTest {
  
  private static final int port = 16969;
  
  private static ServerConfig newServerConfig(Vertx vertx, RouteList routeList) {
    return ServerConfig.builder()
                       .vertx(vertx)
                       .instances(4)
                       .serverOptions(new HttpServerOptions().setPort(port))
                       .routeCreatorFactory(new RingRouteCreatorFactory())
                       .routerDefinition(routeList)
                       .build();
  }
  
  private Server server;
  
  @AfterEach
  void tearDown() throws InterruptedException {
    if (server != null) {
      var latch = new CountDownLatch(1);
      server.vertx().close(v -> latch.countDown());
      latch.await(2, TimeUnit.SECONDS);
    }
  }
  
  @Test
  void testServerAsyncLifecycle(Vertx vertx, VertxTestContext testContext) {
    RouteDefinition route = Routes.helloWorld();
    server = Server.create(newServerConfig(vertx, RouteList.from(route)));
    server.start()
          .onFailure(testContext::failNow)
          .onSuccess(startResult -> doGet(vertx, route.path().value())
              .onComplete(testContext.succeeding(
                  response -> testContext.verify(() -> {
                    assert200(response);
                    assertEquals("Hello, World!", response.bodyAsString());
              
                    server.shutdown().onComplete(stopResult -> {
                      if (stopResult.failed()) {
                        testContext.failNow(stopResult.cause());
                      }
                      testContext.completeNow();
                    });
                  }))));
  }
  
  @Test
  void testServerSyncLifecycle(Vertx vertx, VertxTestContext testContext) throws
                                                                          ServerInitializationException {
    RouteDefinition route = Routes.helloWorld();
    server = Server.create(newServerConfig(vertx, RouteList.from(route)));
    server.startSync();
    
    doGet(vertx, route.path().value())
        .onComplete(testContext.succeeding(
            response -> testContext.verify(() -> {
              assert200(response);
              assertEquals("Hello, World!", response.bodyAsString());
              testContext.completeNow();
            })));
  }
  
  @Test
  void testRingCompliantRequest() {
  
  }
}
