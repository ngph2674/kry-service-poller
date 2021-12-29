package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainVerticle extends AbstractVerticle {

  private ServiceStore serviceStore;
  private final Logger log = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public void start(Future<Void> startFuture) {

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    Poller poller = new Poller(vertx);
    serviceStore = new ServiceStore(vertx);
    serviceStore.initialize().setHandler(result -> {
      if (result.succeeded()) {
        vertx.setPeriodic(3000, timerId -> poller.pollServices(serviceStore.getAllServices()));
        configureRoutes(router);
        vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(8080, req -> {
                  if (req.succeeded()) {
                    startFuture.complete();
                  } else {
                    startFuture.fail(req.cause());
                  }
                });
      } else {
        startFuture.fail(result.cause());
      }
    });

  }
  private void sendResponse(RoutingContext routingContext,String obj,String contentType){
    routingContext.response()
            .putHeader("content-type", contentType)
            .end(obj);
  }

  private void configureRoutes(Router router) {
    staticHandlerRoute(router);
    getServices(router);
    insertService(router);
    deleteService(router);
  }

  private void staticHandlerRoute(Router router) {
    router.route("/*").handler(StaticHandler.create());
  }

  private void getServices(Router router) {
    router.get("/service").handler(req -> {
      List<JsonObject> jsonServices = serviceStore.getAllServices();
      sendResponse(req,new JsonArray(jsonServices).encode(),"application/json");
    });
  }




  private void insertService(Router router) {
    router.post("/service").handler(req -> {
      JsonObject jsonBody = req.getBodyAsJson();
      JsonObject service;
      try {
        service = createServiceFromReq(jsonBody.getString("url"), jsonBody.getString("name"));
      } catch (MalformedURLException e) {
        sendResponse(req,"ERROR","text/plain");
        return;
      }
      serviceStore.insert(service).setHandler(result -> {
        if (result.succeeded()) {
          log.info("Service inserted/updated successfully.");
          sendResponse(req,"OK","text/plain");
        } else {
          log.error("Service can not be inserted", result.cause());
          sendResponse(req,"ERROR","text/plain");
        }
      });
    });
  }

  private void deleteService(Router router) {
    router.post("/delete").handler(req -> {
      try {
        JsonObject jsonBody = req.getBodyAsJson();
        String service =  jsonBody.getString("url");
        serviceStore.delete(service).setHandler(result -> {
          if (result.succeeded()) {
            log.info("successful delete");
            sendResponse(req,"OK","text/plain");
          } else {
            log.error("Delete error : ", result.cause());
            sendResponse(req,"ERROR","text/plain");
          }
        });
      }catch (Exception e){
        log.error("Delete error.");
        sendResponse(req,"ERROR","text/plain");
      }
    });
  }


  private JsonObject createServiceFromReq(String url, String name) throws MalformedURLException {
    return  new JsonObject()
            .put("url", new URL(url).toString())
            .put("name", name)
            .put("createdAt", DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now()))
            .put("status", "UNKNOWN");
  }
}



