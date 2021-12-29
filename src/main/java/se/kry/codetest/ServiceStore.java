package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



class DBConnector {

    private final SQLClient client;

    public DBConnector(Vertx vertx){
       client = JDBCClient.createShared(vertx, new JsonObject()
                .put("url", "jdbc:sqlite:poller.db")
                .put("driver_class", "org.sqlite.JDBC")
                .put("max_pool_size", 50));
    }

    public Future<ResultSet> executeQuery(String query) {
        return executeQuery(query, new JsonArray());
    }


    public Future<ResultSet> executeQuery(String query, JsonArray params) {

        Future<ResultSet> queryResultFuture = Future.future();

        client.queryWithParams(query, params, result -> {
            if(result.failed()){
                queryResultFuture.fail(result.cause());
            } else {
                queryResultFuture.complete(result.result());
            }
        });
        return queryResultFuture;
    }
}


public class ServiceStore {
    private final Logger log = LoggerFactory.getLogger(ServiceStore.class);

    private final DBConnector dbConnector;

    private final HashMap<String, JsonObject> servicesMap = new HashMap<>();

    public ServiceStore(Vertx vertx) {
        DBConnector dbConnector = new DBConnector(vertx);
        this.dbConnector = dbConnector;
    }


    public void setupDB() {
        dbConnector.executeQuery("CREATE TABLE IF NOT EXISTS service (url VARCHAR(128) NOT NULL PRIMARY KEY, name VARCHAR(32), createdAt DATETIME);").setHandler(result -> {
            if(result.succeeded()){
                log.info("completed create db table");
            } else {
                log.error("Database connection is failed", result.cause());
            }
        });
    }

    public Future<Boolean> initialize() {
        setupDB();
        Future<Boolean> setupFuture = Future.future();
        String sqlQuery = "SELECT * FROM service;";
        dbConnector.executeQuery(sqlQuery).setHandler(result -> {
            if (result.succeeded()) {
                result.result().getRows().forEach(row -> servicesMap
                        .put(row.getString("url"), row.put("status", "UNKNOWN")));
                log.info("Services already in the store :" + servicesMap.keySet());
                setupFuture.complete(true);
            } else {
                log.error("Database connection is failed", result.cause());
                setupFuture.fail(result.cause());
            }
        });
        return setupFuture;
    }
    public Future<ResultSet> insert(JsonObject service) {
        servicesMap.put(service.getString("url"), service);
        String sqlQuery = "INSERT INTO service (url, name, createdAt)" +
                " values (?,?,DATE('now','localtime')" +
                ");";
        return dbConnector.executeQuery(sqlQuery,
                new JsonArray()
                        .add(service.getString("url"))
                        .add(service.getString("name"))
        );
    }


    public Future<ResultSet> delete(String service) {
        servicesMap.remove(service);
        String sqlQuery = "DELETE FROM service WHERE url=?;";
        return dbConnector.executeQuery(sqlQuery, new JsonArray().add(service));
    }
    public List<JsonObject> getAllServices() {
        return new ArrayList<>(servicesMap.values());
    }

}
