package web;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;

import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;

/**
 * MongoDB manager for both:
 *  - Energy history records
 *  - Device inventory (shared by SOAP + REST)
 */
public class MongoDBManager {

    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "smart_energy_db";

    // Collections
    private static final String ENERGY_COLLECTION = "energy_history";
    private static final String DEVICES_COLLECTION = "devices";

    private MongoClient mongoClient;
    private MongoDatabase database;

    private MongoCollection<Document> energyCollection;
    private MongoCollection<Document> devicesCollection;

    public void init() {
        try {
            mongoClient = MongoClients.create(CONNECTION_STRING);
            database = mongoClient.getDatabase(DATABASE_NAME);

            energyCollection = database.getCollection(ENERGY_COLLECTION);
            devicesCollection = database.getCollection(DEVICES_COLLECTION);

            // Ensure unique device names
            devicesCollection.createIndex(
                    Indexes.ascending("name"),
                    new IndexOptions().unique(true)
            );

            System.out.println("Connected to MongoDB database: " + DATABASE_NAME);
        } catch (Exception e) {
            System.err.println("Failed to connect to MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /* -------------------- ENERGY HISTORY -------------------- */

    public void saveEnergyRecord(Map<String, Object> data) {
        try {
            Document document = new Document(data);
            energyCollection.insertOne(document);
            System.out.println("[DEBUG] Record saved to MongoDB: " + data);
        } catch (Exception e) {
            System.err.println("Failed to save record to MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> getEnergyHistory() {
        List<Map<String, Object>> history = new ArrayList<>();
        try {
            for (Document doc : energyCollection.find().sort(Sorts.descending("timestamp")).limit(100)) {
                history.add(new HashMap<>(doc));
            }
            System.out.println("[DEBUG] Retrieved " + history.size() + " records from MongoDB.");
        } catch (Exception e) {
            System.err.println("Failed to retrieve energy history from MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
        return history;
    }

    public Map<String, Object> getLatestEnergyRecord() {
        try {
            Document doc = energyCollection.find().sort(Sorts.descending("timestamp")).first();
            if (doc != null) {
                return new HashMap<>(doc);
            }
        } catch (Exception e) {
            System.err.println("Failed to retrieve latest record from MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /* -------------------- DEVICES -------------------- */

    public boolean deviceExistsByName(String name) {
        try {
            return devicesCollection.find(eq("name", name)).first() != null;
        } catch (Exception e) {
            System.err.println("Failed to check device existence: " + e.getMessage());
            return false;
        }
    }

    /**
     * Insert a device. Returns true if inserted, false if duplicate.
     */
    public boolean insertDevice(String name, double baseConsumption, boolean isOn) {
        try {
            Document doc = new Document("name", name)
                    .append("baseConsumption", baseConsumption)
                    .append("isOn", isOn)
                    .append("createdAt", System.currentTimeMillis());
            devicesCollection.insertOne(doc);
            return true;
        } catch (Exception e) {
            // Duplicate key or other insert issues
            return false;
        }
    }

    /**
     * Upsert (insert or update) a device by name.
     */
    public void upsertDevice(String name, double baseConsumption, boolean isOn) {
        UpdateOptions options = new UpdateOptions().upsert(true); // standard MongoDB upsert option [web:217]
        devicesCollection.updateOne(
                eq("name", name),
                Updates.combine(
                        Updates.set("baseConsumption", baseConsumption),
                        Updates.set("isOn", isOn),
                        Updates.set("updatedAt", System.currentTimeMillis()),
                        Updates.setOnInsert("createdAt", System.currentTimeMillis())
                ),
                options
        );
    }

    public boolean removeDevice(String name) {
        try {
            return devicesCollection.deleteOne(eq("name", name)).getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("Failed to remove device: " + e.getMessage());
            return false;
        }
    }

    public boolean setDevicePower(String name, boolean isOn) {
        try {
            return devicesCollection.updateOne(eq("name", name), Updates.set("isOn", isOn)).getMatchedCount() > 0;
        } catch (Exception e) {
            System.err.println("Failed to set device power: " + e.getMessage());
            return false;
        }
    }

    public boolean toggleDevicePower(String name) {
        Document doc = devicesCollection.find(eq("name", name)).first();
        if (doc == null) return false;
        boolean current = doc.getBoolean("isOn", false);
        return setDevicePower(name, !current);
    }

    public boolean updateDeviceConsumption(String name, double newConsumption) {
        try {
            return devicesCollection.updateOne(eq("name", name), Updates.set("baseConsumption", newConsumption)).getMatchedCount() > 0;
        } catch (Exception e) {
            System.err.println("Failed to update device consumption: " + e.getMessage());
            return false;
        }
    }

    public List<Map<String, Object>> getAllDevices() {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            for (Document doc : devicesCollection.find().sort(Sorts.ascending("name"))) {
                list.add(new HashMap<>(doc));
            }
        } catch (Exception e) {
            System.err.println("Failed to list devices: " + e.getMessage());
        }
        return list;
    }

    public List<Map<String, Object>> getDevicesByStatus(boolean isOn) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            for (Document doc : devicesCollection.find(eq("isOn", isOn)).sort(Sorts.ascending("name"))) {
                list.add(new HashMap<>(doc));
            }
        } catch (Exception e) {
            System.err.println("Failed to list devices by status: " + e.getMessage());
        }
        return list;
    }

    public double getTotalConsumptionForOnDevices() {
        double total = 0.0;
        try {
            for (Document doc : devicesCollection.find(eq("isOn", true))) {
                total += doc.getDouble("baseConsumption");
            }
        } catch (Exception e) {
            System.err.println("Failed to compute total consumption: " + e.getMessage());
        }
        return total;
    }

    public void seedDefaultDevicesIfEmpty() {
        try {
            long count = devicesCollection.countDocuments();
            if (count > 0) return;

            insertDevice("Heating System", 25.0, true);
            insertDevice("Air Conditioner", 30.0, false);
            insertDevice("Water Heater", 15.0, true);
            insertDevice("Lighting Grid", 10.0, true);
            insertDevice("Entertainment System", 5.0, false);

            System.out.println("Seeded default devices into MongoDB.");
        } catch (Exception e) {
            System.err.println("Failed to seed devices: " + e.getMessage());
        }
    }

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
