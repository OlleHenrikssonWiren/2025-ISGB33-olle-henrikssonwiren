import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;
import java.util.Arrays;

import com.mongodb.client.model.Collation;
import com.mongodb.client.model.CollationStrength;
import com.mongodb.client.model.Projections;
import kotlin.collections.ArrayDeque;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.AggregateIterable;


import io.javalin.Javalin;
import io.javalin.http.Context;

import javax.print.Doc;

public class Stub {

    private static final Logger logger = LoggerFactory.getLogger(Stub.class);

    private static MongoCollection<Document> myCollection;

    public static void main(String[] args) throws Exception {
        initMongo();
        startServer();
    }
    // connect till mongodb
    private static void initMongo() throws Exception {
        Properties prop = new Properties();

        try (InputStream input = new FileInputStream("connection.properties")) {
            prop.load(input);
        }

        String connString = prop.getProperty("db.connection_string");
        String dbName = prop.getProperty("db.name");

        ConnectionString connectionString = new ConnectionString(connString);

        MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .build();

        MongoClient mongoClient = MongoClients.create(settings);

        MongoDatabase database = mongoClient.getDatabase(dbName);

        myCollection = database.getCollection("movies");

        logger.info("Connected to MongoDB, using database '{}'", dbName);
    }
    //starta server via javelin
    private static void startServer() {
        Javalin app = Javalin.create().start(4567);

        //Get methods
        app.get("/title/{title}", Stub::getByTitle); // get title
        app.get("/fullplot/{title}", Stub::getFullplot); // get fullplot
        app.get("/cast/{title}", Stub::getCast);
        app.get("/genre/{genre}", Stub::getByGenre);
        app.get("/actor/{actor}", Stub::getByActor);
    }

    private static void getByTitle(Context ctx) {
        String inputTitle = ctx.pathParam("title");
        //String title = capitalizeFully(inputTitle.toLowerCase(Locale.ROOT));
        System.out.println(inputTitle);
        Collation col = ignoreCapsCollation();
        Document doc = myCollection.find(eq("title", inputTitle)).collation(col).first();

        if (doc != null) { // om objektet inte är tomt
            // ta bort keys
            doc.remove("_id");
            doc.remove("poser");
            doc.remove("cast");
            doc.remove("fullplot");
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(doc.toJson());
        } else {
            ctx.status(404);
            ctx.contentType("application/json");
            ctx.result(jsonError("movie not found.").toString());
        }
    }

    private static void getFullplot(Context ctx) {
        String input = ctx.pathParam("title");
        Collation col = ignoreCapsCollation();
        Document doc = myCollection.find(eq("title", input)).projection(Projections.include("fullplot", "title")).collation(col).first();

        if (doc != null) {
            doc.remove("_id");
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(doc.toJson());
        }
        else {
            ctx.status(404);
            ctx.contentType("application/json");
            ctx.result(jsonError("Plot from movie not found.").toString());
        }
    }

    private static void getCast(Context ctx) {
        String input = ctx.pathParam("title");
        Collation col = ignoreCapsCollation();
        Document doc = myCollection.find(eq("title", input)).projection(Projections.include("title", "cast")).collation(col).first();

        if (doc != null) {
            doc.remove("_id");
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(doc.toJson());
        }
        else {
            ctx.status(404);
            ctx.contentType("application/json");
            ctx.result(jsonError("Cast from movie not found.").toString());
        }
    }

    private static void getByGenre(Context ctx) {
        String input = ctx.pathParam("genre");
        Collation col = ignoreCapsCollation();
        //aggregation pipeline
        AggregateIterable<Document> doc = myCollection.aggregate(
                Arrays.asList(
                        new Document("$match",
                                new Document("genres", input)
                        ),
                        new Document("$limit", 10),
                        new Document("$project",
                                new Document("_id", 0)
                                        .append("poster", 0)
                                        .append("cast", 0)
                                        .append("fullplot", 0)
                        )
                )
        ).collation(col);
        // lista för varje film
        List<Document> list = new ArrayList<>();
        doc.into(list); //stoppa in dokumenten i en lista
        Document res = new Document("data", list); // skapa ett dokument med data från listan
        System.out.println(doc);
        if (doc.first() != null) { // om första dokumentet inte är null
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(res.toJson()); // dokumentet till json skickas som response
        }
        else {
            ctx.status(404);
            ctx.contentType("application/json");
            ctx.result(jsonError("Movies from genre not found.").toString());
        }
    }

    private static void getByActor(Context ctx) {
        String input = ctx.pathParam("actor");
        Collation col = ignoreCapsCollation();
        AggregateIterable<Document> doc = myCollection.aggregate(
                Arrays.asList(
                        new Document("$match",
                                new Document("cast", input)
                        ),
                        new Document("$limit", 10),
                        new Document("$project",
                                new Document("_id", 0)
                                        .append("title", 1)
                        )
                )
        ).collation(col);
        List<Document> list = new ArrayList<>();
        doc.into(list); //stoppa in dokumenten i en lista
        Document res = new Document("data", list); // skapa ett dokument med data från listan

        if (doc.first() != null) {
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(res.toJson()); // dokumentet till json skickas som response
        }
        else {
            ctx.status(404);
            ctx.contentType("application/json");
            ctx.result(jsonError("Movie titles from actor not found.").toString());
        }
    }

    // Metod för att skapa collation för att ignorera caps på mongodb med input.
    private static Collation ignoreCapsCollation() {
        return Collation.builder()
                .locale("en_US")
                .collationStrength(CollationStrength.SECONDARY)
                .build();
    }

    private static String capitalizeFully(String input) {
        String[] words = input.trim().split("\\s+");

        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                char first = Character.toUpperCase(word.charAt(0));
                String rest = word.substring(1).toLowerCase();

                result.append(first);
                result.append(rest);
                result.append(" ");
            }
        }

        return result.toString().trim();
    }

 

    private static JsonObject jsonError(String error) {
        JsonObject obj = new JsonObject();
        obj.addProperty("error", error);
        return obj;
    }
}