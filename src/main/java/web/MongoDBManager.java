package web;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MongoDBManager {

    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "smart_energy_db";
    private static final String COLLECTION_NAME = "energy_history";

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    /**
     * Initializes the MongoDB connection and collection.
     */
    public void init() {
        try {
            mongoClient = MongoClients.create(CONNECTION_STRING);
            database = mongoClient.getDatabase(DATABASE_NAME);
            collection = database.getCollection(COLLECTION_NAME);
            System.out.println("Connected to MongoDB database: " + DATABASE_NAME);
        } catch (Exception e) {
            System.err.println("Failed to connect to MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Saves a single energy data record to the MongoDB collection.
     * @param data A map containing the energy data for one timestamp.
     */
    public void saveEnergyRecord(Map<String, Object> data) {
        try {
            Document document = new Document(data);
            collection.insertOne(document);
            System.out.println("[DEBUG] Record saved to MongoDB: " + data);
        } catch (Exception e) {
            System.err.println("Failed to save record to MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the last 100 energy history records from the MongoDB collection.
     * @return A list of maps, where each map is a historical record.
     */
    public List<Map<String, Object>> getEnergyHistory() {
        List<Map<String, Object>> history = new ArrayList<>();
        try {
            for (Document doc : collection.find().sort(Sorts.descending("timestamp")).limit(100)) {
                history.add(doc);
            }
            System.out.println("[DEBUG] Retrieved " + history.size() + " records from MongoDB.");
        } catch (Exception e) {
            System.err.println("Failed to retrieve energy history from MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
        return history;
    }

    /**
     * Retrieves the single most recent energy record from the MongoDB collection.
     * @return A map representing the latest record, or null if the collection is empty.
     */
    public Map<String, Object> getLatestEnergyRecord() {
        try {
            Document doc = collection.find().sort(Sorts.descending("timestamp")).first();
            if (doc != null) {
                return doc;
            }
        } catch (Exception e) {
            System.err.println("Failed to retrieve latest record from MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Closes the MongoDB connection gracefully.
     */
    public void close() {
        try {
            if (mongoClient != null) {
                mongoClient.close();
                System.out.println("MongoDB connection closed.");
            }
        } catch (Exception e) {
            System.err.println("Failed to close MongoDB connection: " + e.getMessage());
        }
    }
}