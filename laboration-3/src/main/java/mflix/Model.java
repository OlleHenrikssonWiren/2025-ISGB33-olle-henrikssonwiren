package mflix;

import static com.mongodb.client.model.Filters.eq;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.bson.conversions.Bson;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.AggregateIterable;

import javax.print.Doc;


public class Model {
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;
    public void initMongo() {
        mongoClient = MongoClients.create(uri);
        database = mongoClient.getDatabase("sample_mflix");
        collection = database.getCollection("movies");
        //Example filter
            //Document doc = collection.find(eq("title", "Back to the Future")).first();
    }
    String uri = "mongodb://localhost:27017/";

    public String fetch(String input) {
        String result = "";
        try {
            System.out.println(input);
            if(input != null && input.trim().isEmpty()) {
                //Saknas input!
                result = "Ingen film matchade kategorin";
                throw new Exception("Error - Måste fylla i en kategori!");
            }
            //aggregera genom input variabel
            AggregateIterable<Document> output = collection.aggregate(Arrays.asList(new Document("$match",
                            new Document("genres", input)),
                    new Document("$project",
                            new Document("_id", 0L)
                                    .append("title", 1L)
                                    .append("year", 1L)),
                    new Document("$sort",
                            new Document("title", -1L)),
                    new Document("$limit", 10L))
            );
            for(Document doc : output) {
                String title = doc.getString("title");
                String year = String.valueOf(doc.getInteger("year"));
                result += title + ", " + year + "\n";
                System.out.println(doc.toJson());

            }



            //return result;
        }
        catch(Exception e) {
            return result;
        }
        return result;
    }
}
