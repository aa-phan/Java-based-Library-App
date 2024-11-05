import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;


import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
public class LoginController implements Initializable{
    @FXML
    private ImageView loginBG;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField usernameField;
    @FXML
    private Button loginButton;

    private ClientApp mainApp;
    private Stage stage;
    private LibraryClient libraryClient;
    private MongoDBConnection mongoDBConnection;


    public void setMainApp(ClientApp mainApp) {
        this.mainApp = mainApp;
    }



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set cell value factories for columns
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Library.png")));
        loginBG.setImage(image);
        loginBG.setFitWidth(700);
        loginBG.setFitHeight(400);
        mongoDBConnection = new MongoDBConnection();

    }
    public void loginAction() throws IOException {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Please enter username and password.");
            return;
        }

        boolean userExists = mongoDBConnection.userExists(username);
        if (userExists) {
            // Check if the password matches the username in MongoDB
            boolean passwordMatches = mongoDBConnection.passwordMatches(username, password);
            if (passwordMatches) {
                // Password matches, switch to main controller
                libraryClient.setUsername(username);
                switchToMainController();
            } else {
                // Password doesn't match, show error message
                showAlert("Incorrect password");
            }
        } else {
            // Username doesn't exist, show error message
            if (!isPasswordStrong(password)) {
                showAlert("Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character.");
                return;
            }
            byte[] salt = Password.generateSalt();
            mongoDBConnection.insertUser(username, Password.hashAndSaltPassword(password, salt), salt);
            switchToMainController();
            showAlert("New user added successfully");
        }
    }
    private void switchToMainController() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("CheckoutScreen.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMaximized(true);

            Controller controller = fxmlLoader.getController();
            controller.setMongoDBConnection(mongoDBConnection);
            controller.setStage(stage);
            controller.setLibraryClient(libraryClient);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error switching to main controller");
        }
    }
    public void setStage(Stage primaryStage) {
        stage = primaryStage;
    }

    public void setLibraryClient(LibraryClient client) {
        libraryClient = client;
    }

    public void setLogOff(){
        mongoDBConnection.writeArrayListToMongo(libraryClient.getCheckedItems(), libraryClient.getUsername());
        libraryClient.sendMessage("bye");
        stage.close();
        mongoDBConnection.close();
        Platform.exit();
        System.exit(0);
    }
    private void showAlert(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }
    private boolean isPasswordStrong(String password) {
        // Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*") &&
                password.matches(".*[!@#$%^&*()\\[\\]{}|<>?/~`.+=_-].*");
    }
}