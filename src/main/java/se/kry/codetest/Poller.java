package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.WebClient;
import java.util.ArrayList;
import java.util.List;

public class Poller {
  private final WebClient client;
  private final Logger log = LoggerFactory.getLogger(Poller.class);

  public Poller(Vertx vertx) {
    client = WebClient.create(vertx);
  }

  public List<Future<JsonObject>> pollServices(List<JsonObject> services) {
      List<Future<JsonObject>> result = new ArrayList<>();
      for (JsonObject service : services){
        result.add(checkService(service));
      }
      return result;
  }

  private Future<JsonObject> checkService(JsonObject service) {
    String url = service.getString("url");
    log.info("Check service: " + url);
    Future<JsonObject> statusFuture = Future.future();
    try {
      client.getAbs(url)
              .send(response -> {
                if (response.succeeded()) {
                  String responseText = (200 == response.result().statusCode() ? "OK" : "FAIL");
                  statusFuture.complete(service.put("status", responseText));
                } else {
                  statusFuture.complete(service.put("status", "FAIL"));
                }
              });
    } catch (Exception e) {
      log.error("Service check failed " + url, e);
      statusFuture.complete(service.put("status", "FAIL"));
    }
    return statusFuture;
  }
}
