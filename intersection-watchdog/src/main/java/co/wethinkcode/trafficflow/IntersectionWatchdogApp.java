package co.wethinkcode.trafficflow;

import io.javalin.Javalin;
import org.apache.activemq.ActiveMQConnectionFactory;
import co.wethinkcode.trafficflow.mq.MqConfig;

import javax.jms.*;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class IntersectionWatchdogApp  {


    private static final long HEARTBEAT_INTERVAL_MS = 5000;
    private static final long ALERT_THRESHOLD_MS = HEARTBEAT_INTERVAL_MS * 3; // miss 3 in a row

    private static final AtomicLong lastHeartbeatAt = new AtomicLong(System.currentTimeMillis());
    private static final AtomicBoolean alerting = new AtomicBoolean(false);


    public static void main(String[] args) throws JMSException {
        subscribeToHeartbeatQueue();
        startMonitor();
        Javalin app = Javalin.create().start(7024);

        app.get("/health", ctx -> ctx.result("OK"));

        // TODO (Cries for help if the Intersection Service crashes, since routes can no longer be validated.)
        // Mechanism: ActiveMQ Queue heartbeat/dead-letter
        app.get("/alert", ctx -> ctx.json(Map.of(
                "alerting", alerting.get(),
                "lastHeartbeatAt", lastHeartbeatAt.get()
        )));

    }

    private static void subscribeToHeartbeatQueue() throws JMSException {
        ConnectionFactory factory = new ActiveMQConnectionFactory(MqConfig.BROKER_URL);
        Connection conn = factory.createConnection();
        conn.start();
        Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue(MqConfig.HEARTBEAT_QUEUE);
        MessageConsumer consumer = session.createConsumer(queue);

        consumer.setMessageListener(message -> {
            lastHeartbeatAt.set(System.currentTimeMillis());
            if (alerting.compareAndSet(true, false)) {
                System.out.println("Heartbeat resumed — clearing alert.");
            }
        });

        System.out.println("Subscribed to " + MqConfig.HEARTBEAT_QUEUE);
    }

    private static void startMonitor() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            long since = System.currentTimeMillis() - lastHeartbeatAt.get();
            if (since > ALERT_THRESHOLD_MS && alerting.compareAndSet(false, true)) {
                System.err.println("ALERT: no heartbeat from intersection-service in " + since + "ms");
            }
        }, ALERT_THRESHOLD_MS, 2000, TimeUnit.MILLISECONDS);
    }


}

// MQ TODO: subscribes to ActiveMQ queue MqConfig.HEARTBEAT_QUEUE at MqConfig.BROKER_URL
// (see co.wethinkcode.trafficflow.mq.MqConfig) and alerts if a heartbeat from
// intersection-service is missed or a message lands in the dead-letter queue.
