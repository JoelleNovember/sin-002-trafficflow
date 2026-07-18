package co.wethinkcode.trafficflow;

import io.javalin.Javalin;

public class IntersectionWatchdogApp {

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7024);

        app.get("/health", ctx -> ctx.result("OK"));

        // TODO (Cries for help if the Intersection Service crashes, since routes can no longer be validated.)
        // Mechanism: ActiveMQ Queue heartbeat/dead-letter
    }
}

// MQ TODO: subscribes to ActiveMQ queue MqConfig.HEARTBEAT_QUEUE at MqConfig.BROKER_URL
// (see co.wethinkcode.trafficflow.mq.MqConfig) and alerts if a heartbeat from
// intersection-service is missed or a message lands in the dead-letter queue.
