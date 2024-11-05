
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;
import java.time.LocalDate;
import java.util.*;

public class LibraryClient {
    private Socket socket;
    private String username;
    private Map<Item, Boolean> localLib;
    private ArrayList<Item> checkedItems;
    ObjectOutputStream oos;
    ObjectInputStream ois;
    //private Boolean updateFlag = false;
    Scanner input;
    public LibraryClient(Socket socket, String username){
        try{
            this.socket = socket;
            //sender = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            //receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            input = new Scanner(System.in);
            this.username = username;
            sendUsername(this.username);
            //
            if(ois.readInt()==-1){
                localLib = (Map<Item, Boolean>) ois.readObject();
                System.out.println("library received");
                for(Map.Entry<Item, Boolean> entry: localLib.entrySet()){
                    entry.getKey().setDateAdded(LocalDate.now().toString());
                    System.out.println(entry.getKey().toString() + "\nstatus: " + entry.getValue());
                }
            }

        }
        catch(IOException e){
            closeEverything(socket, ois, oos);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public void sendUsername(String username){
        try{
            //sending username first
            oos.writeInt(username.length());
            oos.writeUTF(username);
            oos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void sendMessage(String msgtoSend){
        try{
            //Scanner input = new Scanner(System.in);
            //sending map or messages
            //while(socket.isConnected()){
//                String msgtoSend = input.nextLine();
                if(msgtoSend.startsWith("/checkout")){
                    String itemName = msgtoSend.substring(msgtoSend.indexOf(" ") + 1);
                    for(Item item : localLib.keySet()){
                        if(item.getTitle().equals(itemName)){
                            itemCheckout(item);
                            break;
                        }
                    }
                    //updateLibrary();
                    //continue;
                }
                else if(msgtoSend.startsWith("/return")){
                    String itemName = msgtoSend.substring(msgtoSend.indexOf(" ") + 1);
                    for(Item item : localLib.keySet()){
                        if(item.getTitle().equals(itemName)){
                            itemReturn(item, item.getTitle());
                            break;
                        }
                    }
                    //continue;
                }
                else if(msgtoSend.equalsIgnoreCase("/view")){
                    for(Map.Entry<Item, Boolean> entry: localLib.entrySet()){
                        System.out.println(entry.getKey().toString() + "\nstatus: " + entry.getValue());
                    }
                    //continue;
                }
                else if(msgtoSend.equalsIgnoreCase("/checked")){
                    for(Item item : checkedItems){
                        System.out.println(item);
                    }
                }
                else if(msgtoSend.equalsIgnoreCase("bye")){
                    oos.writeInt("bye".length());
                    oos.writeUTF("bye");
                    oos.flush();
                    //break;
                    closeEverything(socket, ois, oos);
                    System.out.println("client closed");
                }else{
                    oos.writeInt(msgtoSend.length());
                    oos.writeUTF(msgtoSend);
                    oos.flush();
                }
           // }
           // closeEverything(socket, ois, oos);
           // System.out.println("client closed");
        }catch (IOException | ClassNotFoundException e){
            System.out.println("send message exception");
            closeEverything(socket, ois, oos);
        }
    }
    public void refreshLibrary() throws IOException, ClassNotFoundException {
        //ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.reset();
        oos.writeInt(-1);
        oos.writeObject(localLib);
        oos.flush();
    }


    public void listenForMessage(){
        new Thread(new Runnable(){
            @Override
            public void run(){
                String msgFromServer;
                boolean isUpdateMessage = false;
                try {
                    while (socket.isConnected()) {
                        if(ois.available()>0) {
                            int dataType = ois.readInt();
                            if (dataType == -1) {
                                localLib = (Map<Item, Boolean>) ois.readObject();
                                System.out.println("local lib updated");
                                for (Map.Entry<Item, Boolean> entry : localLib.entrySet()) {
                                    System.out.println(entry.getKey().toString() + "\nstatus: " + entry.getValue());
                                }
                            } else{
                                msgFromServer = ois.readUTF();
                                System.out.println(msgFromServer);
                            }

                        }
                    }
                }
                catch (IOException e){
                    System.out.println("listen message exception");
                    closeEverything(socket, ois, oos);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, ObjectInputStream reader, ObjectOutputStream writer){
        try{
            //writeArrayListToMongo(checkedItems);
            if(reader!=null){
                reader.close();
            }
            if(writer!=null){
                writer. close();
            }
            if(socket!=null){
                socket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            System.out.println("done closing!");
        }
    }
    public void itemCheckout(Item item) throws IOException, ClassNotFoundException {
        if (localLib.get(item)) {
            item.setDueDate(LocalDate.now().plusWeeks(1).toString());
            localLib.put(item, false);
            checkedItems.add(item);
            //updateFlag = true;
            System.out.println("Item checked out successfully");
            refreshLibrary();

            // Exit the method after sending the library
        }
        else System.out.println("Item is already checked out, please choose another item.");
    }
    public void itemReturn(Item item, String itemTitle) throws IOException, ClassNotFoundException {
        Optional<Item> foundItem = checkedItems.stream()
                .filter(itemM -> itemM.getTitle().equals(itemTitle))
                .findFirst();
        if(!localLib.get(item) && foundItem.isPresent()){
            checkedItems.remove(foundItem.get());
            item.setDueDate("");
            item.setDateAdded(LocalDate.now().toString());
            localLib.put(item, true);
            System.out.println("item returned successfully");
            refreshLibrary();

        }
        else if(localLib.get(item) && foundItem.isPresent()){
            System.out.println("returning duplicate");
            checkedItems.remove(foundItem.get());
        }
        else System.out.println("item is already in library");

    }
    public ArrayList<Item> getCheckedItems(){
        return checkedItems;
    }
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter username:");
        String username = scanner.nextLine();
        Socket socket = new Socket("localhost", 1234);
        LibraryClient client = new LibraryClient(socket, username);
        client.listenForMessage();
        while(socket.isConnected()){
            client.sendMessage(scanner.nextLine());
        }
        System.out.println("done");
    }
    private static void writeArrayListToJson(ArrayList<Item> items) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            File file = new File("checkedItems.json");
            boolean fileExists = file.exists();

            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(items, writer);
                if (fileExists)
                    System.out.println("Array list successfully overwritten in JSON file.");
                else
                    System.out.println("Array list successfully written to JSON file.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error writing array list to JSON file: " + e.getMessage());
        }
    }
    static ArrayList<Item> readArrayListFromJson() {
        try (FileReader reader = new FileReader("checkedItems.json")) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Item.class, new ItemDeserializer())
                    .create();
            ArrayList<Item> items = gson.fromJson(reader, new TypeToken<List<Item>>(){}.getType());
            return items;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.err.println("File not found");
            return new ArrayList<>(); // return empty list if file not found
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error reading JSON file: " + e.getMessage());
            return new ArrayList<>(); // return empty list if error occurs during reading
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setCheckedItems(ArrayList<Item> checkedItems) {
        this.checkedItems = checkedItems;
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
    public Map<Item, Boolean> getLocalLib() {
        return localLib;
    }
}
