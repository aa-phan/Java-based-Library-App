import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;


public class LibraryServer {
    private static Map<Item, Boolean> library = null;
    private ServerSocket serverSocket;
    public LibraryServer(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }
    public void startServer(){
        //String pathName = "C:\\Users\\Aaron\\Documents\\Spring24\\ECE422C\\Final Project - Server\\jsonFiles\\itemList.json";
        String pathName = "/itemList.json";
        library = jsonHelpers.populateLibrary(pathName);

        /*library = new HashMap<>();
        library.put(new Item("hi","123"), true);*/
        try{
            while(!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                System.out.println("new user");
                ClientHandler client = new ClientHandler(socket);
                Thread thread = new Thread(client);
                thread.start();
            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    public void closeServerSocket(){
        try{
            if(serverSocket != null){
                serverSocket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public static Map<Item, Boolean> getLibrary(){
        return library;
    }
    /*public static synchronized void updateLibrary(Item item, boolean status) {
        // Update the library map
        library.put(item, status);
        // Send updated library to all clients
        sendLibtoClients();
    }*/
    //call this when any changes are made to the library
    /*public static void sendLibtoClients(){
        for (ClientHandler clientHandler : ClientHandler.getClientHandlers()) {
            try {
                ObjectOutputStream oos = new ObjectOutputStream(clientHandler.getSocket().getOutputStream());
                oos.writeObject(library);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/
    public static void setLibraryUpdate(Map<Item, Boolean> newLib){
        library = newLib;
    }
    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv("PORT"));
        ServerSocket serverSocket = new ServerSocket(port);
        LibraryServer server = new LibraryServer(serverSocket);
        server.startServer();
    }


}
