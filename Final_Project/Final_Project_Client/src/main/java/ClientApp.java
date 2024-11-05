import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

public class ClientApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Connect to the server and create a LibraryClient instance



        // Connect to the server and create a LibraryClient instance
        Socket socket = new Socket("localhost", 1234);
        LibraryClient client = new LibraryClient(socket, "");
        client.listenForMessage();
        //client.sendMessage();
        FXMLLoader loginLoader = new FXMLLoader(getClass().getClassLoader().getResource("LoginScreen.fxml"));
        Parent root = loginLoader.load();

        // Get the controller
        LoginController controller = loginLoader.getController();
        controller.setStage(primaryStage);

        // Pass LibraryClient instance to the controller
        controller.setLibraryClient(client);


        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
        double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();
        double loginWidth = 662;
        double loginHeight = 400;
        // Set the scene dimensions to match the screen size
        // Create and show the scene
        Scene scene = new Scene(root, loginWidth, loginHeight);
        primaryStage.setTitle("Hello!");
        primaryStage.setScene(scene);
        //primaryStage.setMaximized(true);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> controller.setLogOff());
    }
}
