package co.wethinkcode.trafficflow;

import io.javalin.Javalin;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CongestionServiceApp {

    public static void main(String[] args) {
        AtomicInteger congestionLevel = new AtomicInteger(0);

        Javalin app = Javalin.create().start(7022);

        app.get("/health", ctx -> ctx.result("OK"));

        // TODO (Tracks the city-wide Congestion Level (0-8).)
        // Add domain endpoints for congestion-service here.
        app.get("/congestion", ctx ->
                ctx.json(Map.of("level", congestionLevel.get()))
        );

        app.put("/congestion", ctx -> {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            Object rawLevel = body.get("level");

            if (!(rawLevel instanceof Integer)) {
                ctx.status(400).json(Map.of("error", "level must be an integer"));
                return;
            }

            int level = (Integer) rawLevel;
            if (level < 0 || level > 8) {
                ctx.status(400).json(Map.of("error", "level must be between 0 and 8"));
                return;
            }

            congestionLevel.set(level);
            ctx.status(204);
        });
    }
}

// MQ TODO: publishes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig)
