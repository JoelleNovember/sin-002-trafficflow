package co.wethinkcode.trafficflow;

import javax.jms.*;

import co.wethinkcode.trafficflow.mq.MqConfig;
import io.javalin.Javalin;
import org.apache.activemq.ActiveMQConnectionFactory;

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
            publishLevelChange(level);
            ctx.status(204);
        });
    }
    private static void publishLevelChange(int level) {
        try {
            ConnectionFactory factory = new ActiveMQConnectionFactory(MqConfig.BROKER_URL);
            try (Connection conn = factory.createConnection()) {
                conn.start();
                Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Topic topic = session.createTopic(MqConfig.TOPIC);
                MessageProducer producer = session.createProducer(topic);
                String json = "{\"level\": " + level + "}";
                producer.send(session.createTextMessage(json));
                System.out.println("Published congestion level " + level + " to " + MqConfig.TOPIC);
            }
        } catch (JMSException e) {
            System.err.println("WARNING: failed to publish congestion update — " + e.getMessage());
        }
    }
}

// MQ TODO: publishes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig)
