package se.kry.codetest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class PollerTest {

    @Test
    void check_valid_url(Vertx vertx, VertxTestContext testContext) {
        List<JsonObject> services = new ArrayList<>();
        services.add(new JsonObject().put("url", "https://www.vnexpress.net"));
        Future<JsonObject> future = new Poller(vertx).pollServices(services).get(0);
        future.setHandler(result ->  {
                    assertEquals("OK", result.result().getString("status"));
                    testContext.completeNow();
                });
    }

    @Test
    void check_invalid_url(Vertx vertx, VertxTestContext testContext) {
        List<JsonObject> services = new ArrayList<>();
        services.add(new JsonObject().put("url", "www.kzmdslfed.com.vjds"));
        Future<JsonObject> future = new Poller(vertx).pollServices(services).get(0);
        future.setHandler(result -> {
                    assertEquals("FAIL", result.result().getString("status"));
                    testContext.completeNow();
                });
    }

}
