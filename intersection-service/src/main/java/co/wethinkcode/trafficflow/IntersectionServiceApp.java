package co.wethinkcode.trafficflow;

import io.javalin.Javalin;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IntersectionServiceApp {

    public static void main(String[] args) {

        Map<String, Intersection> store = new ConcurrentHashMap<>();
        loadFromIngestionService(store);

        Javalin app = Javalin.create().start(7021);

        app.get("/health", ctx -> ctx.result("OK"));

        // TODO (Validates intersection/district names (source of truth).)
        // Add domain endpoints for intersection-service here.
        app.get("/intersections/{id}", ctx -> {
            String id = ctx.pathParam("id").toUpperCase();
            Intersection found = store.get(id);
            if (found == null) {
                ctx.status(404).json(Map.of("error", "intersection not found"));
            } else {
                ctx.json(found);
            }
        });
    }

    private static void loadFromIngestionService(Map<String, Intersection> store) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:7020/intersections"))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            List<Intersection> records = mapper.readValue(
                    response.body(),
                    mapper.getTypeFactory().constructCollectionType(List.class, Intersection.class)
            );

            for (Intersection record : records) {
                store.put(record.getId(), record);
            }

            System.out.println("Loaded " + records.size() + " intersections from ingestion-service.");
        } catch (Exception e) {
            System.err.println("WARNING: could not load from ingestion-service — " +
                    "starting with an empty store. Lookups will 404 until this is fixed. Cause: " + e.getMessage());
        }
    }
}



// MQ TODO: publishes a periodic heartbeat to ActiveMQ queue MqConfig.HEARTBEAT_QUEUE at
// MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig), consumed by intersection-watchdog.
