import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.Binary;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class MongoDBConnection {

    // MongoDB connection URI
    private static final String MONGODB_URI = "mongodb+srv://atp:422C@librarycluster.sgterow.mongodb.net/?retryWrites=true&w=majority&appName=LibraryCluster";

    // MongoDB database and collection parameters
    private static final String DATABASE_NAME = "your_database_name";
    private static final String COLLECTION_NAME = "users";

    // Establish MongoDB connection
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;
    private Document user;
    public MongoDBConnection() {
        // Connect to MongoDB
        mongoClient = MongoClients.create(MONGODB_URI);
        database = mongoClient.getDatabase(DATABASE_NAME);
        collection = database.getCollection(COLLECTION_NAME);
        if (collection == null) {
            database.createCollection(COLLECTION_NAME);
            collection = database.getCollection(COLLECTION_NAME);
        }
    }

    public void insertUser(String username, String password, byte[] salt) {
        // Prepare data for insertion
        Document document = new Document();
        document.append("username", username);
        document.append("password", password);
        document.append("salt", salt);
        // Insert into MongoDB
        collection.insertOne(document);
    }
    public boolean userExists(String username) {
        // Check if the username exists in the collection
        return collection.countDocuments(Filters.eq("username", username)) > 0;
    }
    public boolean passwordMatches(String username, String password) {
        // Find the user document by username
        user = collection.find(Filters.eq("username", username)).first();

        if (user != null) {
            // Extract hashed password and salt from the user document
            String hashedPassword = user.getString("password");
            byte[] salt = user.get("salt", Binary.class).getData();

            // Convert stored salt back to byte array

            // Hash the input password with the retrieved salt
            String hashedInputPassword = Password.hashAndSaltPassword(password, salt);

            // Check if the stored hashed password matches the hashed input password
            return hashedPassword.equals(hashedInputPassword);
        } else {
            // User not found
            return false;
        }
    }

    public void writeArrayListToMongo(ArrayList<Item> items, String username) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Convert ArrayList of items to JSON string
        String json = gson.toJson(items);

        // Create a new document with username and checked out items JSON
        Document checkedItems = new Document("checkedItems", json);

        // Get the existing user document from MongoDB
        user = collection.find(Filters.eq("username", username)).first();

        if (user != null) {
            // Get the existing checked items array or create a new one if it doesn't exist
            ArrayList<Document> existingCheckedItems = user.get("checkedItems", ArrayList.class);
            if (existingCheckedItems == null) {
                existingCheckedItems = new ArrayList<>();
            }

            // Add the new checked items to the existing array
            existingCheckedItems.clear();
            existingCheckedItems.add(checkedItems);

            // Set the updated checked items array to the user document
            user.put("checkedItems", existingCheckedItems);

            // Update the user document in MongoDB
            collection.replaceOne(Filters.eq("username", username), user);
        } else {
            System.out.println("User not found in MongoDB.");
        }
    }
    public ArrayList<Item> getCheckedItems(String username) {
        ArrayList<Item> checkedItemsList = new ArrayList<>();
        user = collection.find(Filters.eq("username", username)).first();
        // Find the user document by username
        if (user != null) {
            // Retrieve the checked items array from the user document

            ArrayList<Document> userItemArray = user.get("checkedItems", ArrayList.class);
            if(userItemArray!=null){
                Document checkedItems = userItemArray.get(0);
                Gson gson = new GsonBuilder().registerTypeAdapter(Item.class, new ItemDeserializer()).create();
                Object value = checkedItems.get("checkedItems");

                List<Item> items = gson.fromJson((String) value, new TypeToken<List<Item>>(){}.getType());
                checkedItemsList.addAll(items);
            }



            /*if(checkedItems!=null){
                Gson gson = new GsonBuilder().registerTypeAdapter(Item.class, new ItemDeserializer()).create();
                for(Document checkedItemArray : checkedItems){
                    for(String key : checkedItemArray.keySet()){
                        Object value = checkedItemArray.get(key);
                        *//*String json = item.getString("checkedItems");
                        Item item = gson.fromJson(json, Item.class);
                        checkedItemsList.add(item);*//*
                    }
                }
            }*/



        }
         else {
            System.out.println("User not found in MongoDB.");
        }

        return checkedItemsList;
    }
    public void close() {
        // Close MongoDB connection
        mongoClient.close();
    }
    static class ItemDeserializer implements JsonDeserializer<Item> {
        @Override
        public Item deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            // Get the type field from the nested JSON object
            String type = jsonObject.get("type").getAsString();

            // Get the JSON object representing the item
            JsonObject itemObject = jsonObject.getAsJsonObject();

            switch (type) {
                case "Book":
                    return context.deserialize(itemObject, Book.class);
                case "DVD":
                    return context.deserialize(itemObject, DVD.class);
                case "Audiobook":
                    return context.deserialize(itemObject, Audiobook.class);
                case "Game":
                    return context.deserialize(itemObject, Game.class);
                case "comicBook":
                    return context.deserialize(itemObject, comicBook.class);
                default:
                    throw new JsonParseException("Unknown type: " + type);
            }
        }
    }

}
