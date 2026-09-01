package co.wethinkcode.trafficflow;

import io.javalin.Javalin;
import java.util.List;

public class IngestionServiceApp {

    public static void main(String[] args) throws Exception{
        List<Intersection> cleanedRecords =
                CsvCleaner.loadAndClean("/intersection-legacy.csv");


        Javalin app = Javalin.create().start(7020);
        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/intersections", ctx -> ctx.json(cleanedRecords));

        // TODO: read and clean src/main/resources/intersections-legacy.csv (intersections, districts, signal types data —
        // trim whitespace, fix casing, normalize dates/booleans) and expose the
        // cleaned records here for the other services to consume.

    }
}
