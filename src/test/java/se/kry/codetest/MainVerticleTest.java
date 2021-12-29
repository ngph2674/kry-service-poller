package se.kry.codetest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class MainVerticleTest {

    @BeforeEach
    void deployVerticle(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
    }

    @Test
    void insertService(Vertx vertx, VertxTestContext testContext) {
    WebClient.create(vertx)
        .post(8080, "::1", "/service")
        .sendJsonObject(
            new JsonObject().put("url", "http://www.test.com").put("name", "testname"),
            response -> {
              assertEquals(200, response.result().statusCode());

              WebClient.create(vertx)
                  .get(8080, "::1", "/service")
                  .send(
                      ret -> {
                        assertEquals(200, ret.result().statusCode());
                        JsonArray bodyArray = ret.result().bodyAsJsonArray();
                        for (int i = 0; i < bodyArray.size(); i++) {
                          JsonObject service = bodyArray.getJsonObject(i);
                          if (service.getString("url").equals("http://www.test.com")) {
                            assertEquals("testname", service.getString("name"));
                          }
                        }
                        testContext.completeNow();
                      });
            });
    }
    @Test
    void deleteService(Vertx vertx, VertxTestContext testContext) throws Exception {
        WebClient.create(vertx)
                .post(8080, "::1", "/delete")
                .sendJsonObject(new JsonObject().put("url", "http://www.test.com"),response -> {
                    assertEquals(200, response.result().statusCode());
                   WebClient.create(vertx)
                            .get(8080, "::1", "/service")
                            .send(ret ->  {
                                assertEquals(200, ret.result().statusCode());
                                JsonArray bodyArray = ret.result().bodyAsJsonArray();
                                for(int i =0; i < bodyArray.size(); i++){
                                    JsonObject service = bodyArray.getJsonObject(i);
                                    assertNotEquals("http://www.test.com", service.getString("url"));
                                }
                                testContext.completeNow();
                            });
                });
    }

}