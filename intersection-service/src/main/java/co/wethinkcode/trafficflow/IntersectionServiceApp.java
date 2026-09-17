package co.wethinkcode.trafficflow;

import io.javalin.Javalin;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.ActiveMQConnectionFactory;
import co.wethinkcode.trafficflow.mq.MqConfig;

import javax.jms.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IntersectionServiceApp {

    public static void main(String[] args) {

        Map<String, Intersection> store = new ConcurrentHashMap<>();
        loadFromIngestionService(store);

        Javalin app = Javalin.create().start(7021);

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/intersections/{id}", ctx -> {
            String id = ctx.pathParam("id").toUpperCase();
            Intersection found = store.get(id);
            if (found == null) {
                ctx.status(404).json(Map.of("error", "intersection not found"));
            } else {
                ctx.json(found);
            }
        });

        startHeartbeat();
    }

    private static void startHeartbeat() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(IntersectionServiceApp::publishHeartbeat, 0, 5, TimeUnit.SECONDS);
    }

    private static void publishHeartbeat() {
        try {
            ConnectionFactory factory = new ActiveMQConnectionFactory(MqConfig.BROKER_URL);
            try (Connection conn = factory.createConnection()) {
                conn.start();
                Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Queue queue = session.createQueue(MqConfig.HEARTBEAT_QUEUE);
                MessageProducer producer = session.createProducer(queue);
                producer.send(session.createTextMessage("heartbeat:" + System.currentTimeMillis()));
            }
        } catch (JMSException e) {
            System.err.println("WARNING: failed to publish heartbeat — " + e.getMessage());
        }
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
