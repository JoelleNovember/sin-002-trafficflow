package co.wethinkcode.trafficflow;

import io.javalin.Javalin;

public class IntersectionServiceApp {

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7021);

        app.get("/health", ctx -> ctx.result("OK"));

        // TODO (Validates intersection/district names (source of truth).)
        // Add domain endpoints for intersection-service here.
    }
}
