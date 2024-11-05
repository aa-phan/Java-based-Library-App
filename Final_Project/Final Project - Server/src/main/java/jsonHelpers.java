import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class jsonHelpers {
    static Map<Item, Boolean> populateLibrary(String pathName) {
        Map<Item, Boolean> result = new HashMap<>();
        InputStream inputStream = null;
        InputStreamReader reader = null;
        try {
            inputStream = jsonHelpers.class.getResourceAsStream(pathName);
            if (inputStream != null) {
                reader = new InputStreamReader(inputStream);
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(Item.class, new ItemDeserializer())
                        .create();
                List<Item> items = gson.fromJson(reader, new TypeToken<List<Item>>(){}.getType());
                for (Item item : items) {
                    result.put(item, true);
                }
            } else {
                System.out.println("Resource not found: " + pathName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    static class ItemDeserializer implements JsonDeserializer<Item> {
        @Override
        public Item deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String type = jsonObject.get("type").getAsString();

            switch (type) {
                case "Book":
                    return context.deserialize(jsonObject, Book.class);
                case "DVD":
                    return context.deserialize(jsonObject, DVD.class);
                case "Audiobook":
                    return context.deserialize(jsonObject, Audiobook.class);
                case "Game":
                    return context.deserialize(jsonObject, Game.class);
                case "comicBook":
                    return context.deserialize(jsonObject, comicBook.class);
                default:
                    throw new JsonParseException("Unknown type: " + type);
            }
        }
    }


}
