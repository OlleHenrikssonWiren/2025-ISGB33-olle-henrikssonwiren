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
    //Initera mongodb
    public void initMongo() {
        mongoClient = MongoClients.create(uri);
        database = mongoClient.getDatabase("sample_mflix");
        collection = database.getCollection("movies");
        //Example filter
            //Document doc = collection.find(eq("title", "Back to the Future")).first();
    }
    String uri = "mongodb://localhost:27017/";

    //Filtrera och sök
    public String fetch(String input) {
        String result = "";
        try {
            System.out.println(input);
            if(input != null && input.trim().isEmpty()) {
                //Saknas input!
                result = "Ingen film matchade kategorin";
                throw new Exception("Error - Rutan får inte vara tom!");
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
            if (output.first() == null) { // Om första ojektet är tomt!
                System.out.println("Tom Rad");
                result = "Ingen film matchade kategorin"; // uppdatera variable för return
                throw new Exception(" ERROR - Ingen film matchade kategorin");
            }
            for(Document doc : output) { // Loopa igenom alla dokument
                String title = doc.getString("title");
                String year = String.valueOf(doc.getInteger("year"));
                result += title + ", " + year + "\n"; // Lägg ihopp dem och returna i slutet om allt gick bra
            }



            //return result;
        }
        catch(Exception e) { // om fel hittades kasta stringen
            return result;
        }
        return result; // annars gå vidare
    }
}
