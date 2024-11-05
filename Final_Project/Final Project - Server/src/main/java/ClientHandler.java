
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class ClientHandler implements Runnable{
    private String username;
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    //private Map<Item, Boolean> map;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;


    public ClientHandler(Socket socket){
        try{
            this.socket = socket;
//            this.map =  map;
            ois = new ObjectInputStream(socket.getInputStream());
            oos = new ObjectOutputStream(socket.getOutputStream());
            clientHandlers.add(this);
            //sendLibraryToClient();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public void broadcastMessage(String msg) {
        synchronized (clientHandlers) {
            Iterator<ClientHandler> iterator = clientHandlers.iterator();
            while (iterator.hasNext()) {
                ClientHandler clientHandler = iterator.next();
                try {
                    if (!clientHandler.username.equals(username)) {
                        clientHandler.oos.writeInt(msg.length());
                        clientHandler.oos.writeUTF(msg);
                        clientHandler.oos.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    iterator.remove(); // Remove the client on exception
                }
            }
        }
    }
    public void removeClient() {
        synchronized (clientHandlers) {
            clientHandlers.remove(this);
            broadcastMessage(username + " has left the fray!");
            System.out.println(username + " disconnected");

        }
    }
    public void closeEverything(Socket socket, ObjectInputStream reader, ObjectOutputStream writer){
        removeClient();
        try{
            if(reader!=null){
                reader.close();
            }
            if(writer!=null){
                writer.close();
            }
            if(socket!=null){
                socket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        try {
            oos.reset();
            oos.writeInt(-1);
            oos.writeObject(LibraryServer.getLibrary());
            oos.flush();
            //reading in username first
            int dataType = ois.readInt();
            if(dataType != -1){
                username = ois.readUTF();
            }
            broadcastMessage(username + " has joined the fray!");
            //reading object or string
            while (socket.isConnected()) {
                if(ois.available()>0) {
                    dataType = ois.readInt();
                    if (dataType == -1) {
                        sendLibraryToClient(dataType);
                    } else {
                        String msgFromClient = ois.readUTF();
                        if (msgFromClient.equalsIgnoreCase("bye")) {
                            break;
                        } else {
                            broadcastMessage(username + " : " + msgFromClient);
                            System.out.println(username + " : " + msgFromClient);

                        }
                    }
                }
            }
            closeEverything(socket, ois, oos);

            //close socket and buffers
            System.out.println("client closed");
        } catch (IOException e) {
            System.out.println("client handler oopsie");
            closeEverything(socket, ois, oos);
        }
    }
    private synchronized void sendLibraryToClient(int dataType) throws IOException {
        try {
            if(dataType == -1){
                Map<Item, Boolean> map = (Map<Item, Boolean>) ois.readObject();
                for(Item item : map.keySet()){
                    System.out.println(item + map.get(item).toString());

                }
                LibraryServer.setLibraryUpdate(map);
                for(ClientHandler clientHandler: clientHandlers){
                    if(!clientHandler.equals(this)){
//                        clientHandler.oos.reset();
                        clientHandler.oos.writeInt(-1);
                        clientHandler.oos.writeObject(LibraryServer.getLibrary());
                        clientHandler.oos.flush();
                    }

                }
                System.out.println("client handlers sent lib");
            }
        }
        catch (IOException e) {
            e.printStackTrace();

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
