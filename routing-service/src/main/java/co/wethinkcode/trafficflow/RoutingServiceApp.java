package co.wethinkcode.trafficflow;

import io.javalin.Javalin;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class RoutingServiceApp {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7023);

        app.get("/health", ctx -> ctx.result("OK"));

        // TODO (Provides estimated travel times based on congestion and intersection.)
        // Add domain endpoints for routing-service here.
        app.get("/routes/estimate", ctx -> {
            String from = ctx.queryParam("from");
            String to = ctx.queryParam("to");

            if (from == null || to == null) {
                ctx.status(400).json(Map.of("error", "both 'from' and 'to' query params are required"));
                return;
            }

            Intersection fromIntersection;
            Intersection toIntersection;
            try {
                fromIntersection = fetchIntersection(from);
                toIntersection = fetchIntersection(to);
            } catch (NotFoundException e) {
                ctx.status(404).json(Map.of("error", e.getMessage()));
                return;
            } catch (Exception e) {
                ctx.status(503).json(Map.of("error", "intersection-service unavailable: " + e.getMessage()));
                return;
            }

            int congestionLevel;
            try {
                congestionLevel = fetchCongestionLevel();
            } catch (Exception e) {
                ctx.status(503).json(Map.of("error", "congestion-service unavailable: " + e.getMessage()));
                return;
            }

            double baseMinutes = baseMinutesFor(fromIntersection.getSignalType());
            double estimatedMinutes = baseMinutes * (1 + congestionLevel / 8.0);

            ctx.json(Map.of(
                    "from", fromIntersection.getId(),
                    "to", toIntersection.getId(),
                    "congestionLevel", congestionLevel,
                    "estimatedMinutes", Math.round(estimatedMinutes * 10.0) / 10.0
            ));
        });
    }

    private static double baseMinutesFor(String signalType) {
        if (signalType == null) return 2.0;
        return switch (signalType) {
            case "roundabout" -> 1.5;
            case "stop-sign" -> 1.0;
            case "4-way" -> 2.0;
            default -> 2.0;
        };
    }

    private static Intersection fetchIntersection(String id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:7021/intersections/" + id))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 404) {
            throw new NotFoundException("unknown intersection: " + id);
        }
        if (response.statusCode() != 200) {
            throw new RuntimeException("unexpected status " + response.statusCode());
        }
        return mapper.readValue(response.body(), Intersection.class);
    }

    private static int fetchCongestionLevel() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:7022/congestion"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Map<?, ?> body = mapper.readValue(response.body(), Map.class);
        return (Integer) body.get("level");
    }

    private static class NotFoundException extends Exception {
        NotFoundException(String message) { super(message); }
    }
}

// MQ TODO: subscribes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig)
