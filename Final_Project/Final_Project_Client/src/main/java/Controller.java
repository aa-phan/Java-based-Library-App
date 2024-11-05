import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;


import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Controller implements Initializable {

    @FXML
    private TableColumn<Item, String> titleCol;
    @FXML
    private TableColumn<Item, String> dateCol;
    @FXML
    private TableColumn<Item, String> typeCol;
    @FXML
    private TableColumn<Item, String> authorCol;
    @FXML
    private TableColumn<Item, String> narratorCol;
    @FXML
    private TableColumn<Item, String> productionCol;
    @FXML
    private TableColumn<Item, String> directorCol;
    @FXML
    private TableColumn<Item, String> gameDesignerCol;
    @FXML
    private TableColumn<Item, String> illustratorCol;
    @FXML
    private TableColumn<Item, Boolean> statusCol;
    @FXML
    private TableColumn<Item, String> dueDateCol;

    @FXML
    private TableView<Item> libraryCatalog;
    @FXML
    private MenuItem refreshMenu;
    @FXML
    private MenuItem logOff;
    @FXML
    private WebView webView;

    private Stage stage;

    private LibraryClient libraryClient;
    private MongoDBConnection mongoDB;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set cell value factories for columns
        webView.getEngine().setOnError((WebErrorEvent event) -> {
            System.out.println("Error: " + event.getMessage());});

        webView.getEngine().setOnAlert((WebEvent<String> event) -> {
            System.out.println("Alert: " + event.getData());
        });

        webView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                webView.getEngine().executeScript(
                        "window.onload = function() {" +
                                "   var player = document.getElementsByTagName('video')[0];" +
                                "   player.play();" +
                                "   player.muted = false;" +
                                "   player.webkitEnterFullScreen();" +
                                "};"
                );
            }
        });

        webView.getEngine().load("https://www.youtube.com/embed/L_fcrOyoWZ8?autoplay=1");
        resizeColumns();
        populateColumns();
        startLibraryDisplayUpdater();

        libraryCatalog.setRowFactory(tv -> {
            TableRow<Item> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    Item selectedItem = libraryCatalog.getSelectionModel().getSelectedItem();
                    System.out.println("Clicked on: " + selectedItem.getTitle());
                    Optional<Item> foundItem = libraryClient.getCheckedItems().stream()
                            .filter(item -> item.getTitle().equals(selectedItem.getTitle()))
                            .findFirst();
                    //you don't the item and its available, checkout
                    if(libraryClient.getLocalLib().get(selectedItem) && !foundItem.isPresent()){
                        /*libraryClient.oos.writeInt(selectedItem.getTitle().length());
                        libraryClient.oos.writeUTF("/checkout " + selectedItem.getTitle());
                        libraryClient.oos.flush();*/
                        libraryClient.sendMessage("/checkout " + selectedItem.getTitle());
                        System.out.println("/checkout " + selectedItem.getTitle());
                    }
                    //you have the item but it's already in the library, duplicate copy
                    else if(libraryClient.getLocalLib().get(selectedItem) && foundItem.isPresent()){
                        System.out.println("already in library");
                    }
                    //the item isn't in the library and you don't have it so you can't return it
                    else if(!libraryClient.getLocalLib().get(selectedItem) && !foundItem.isPresent()){
                        System.out.println("cannot return, user does not have item");
                    }
                    //you have the item and the library doesn't
                    else{
                       /* libraryClient.oos.writeInt(selectedItem.getTitle().length());
                        libraryClient.oos.writeUTF("/return " + selectedItem.getTitle());
                        libraryClient.oos.flush();*/
                        libraryClient.sendMessage("/return " + selectedItem.getTitle());
                    }
                    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.1), e -> {
                        updateLibraryDisplay();

                    }));
                    timeline.play();

                }
            });
            return row;
        });
    }


    public void populateColumns() {
        titleCol.setCellValueFactory(cellData -> Bindings.createStringBinding(() -> cellData.getValue().getTitle()));
        dateCol.setCellValueFactory(cellData -> Bindings.createStringBinding(() -> cellData.getValue().getDateAdded()));
        dueDateCol.setCellValueFactory(cellData -> Bindings.createStringBinding(() -> cellData.getValue().getDueDate()));
        typeCol.setCellValueFactory(cellData -> Bindings.createStringBinding(() -> cellData.getValue().getType()));
        statusCol.setCellValueFactory(cellData -> {
            Item item = cellData.getValue();
            boolean status = libraryClient.getLocalLib().getOrDefault(item, false);
            return Bindings.createObjectBinding(() -> status);
        });
        // For author column
        authorCol.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Book) {
                return Bindings.createStringBinding(() -> ((Book) cellData.getValue()).getAuthor());
            } else {
                return Bindings.createStringBinding(() -> "");
            }
        });

        // For narrator column
        narratorCol.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Audiobook) {
                return Bindings.createStringBinding(() -> ((Audiobook) cellData.getValue()).getNarrator());
            } else {
                return Bindings.createStringBinding(() -> "");
            }
        });

        // For production column
        productionCol.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof DVD) {
                return Bindings.createStringBinding(() -> ((DVD) cellData.getValue()).getProductionCompany());
            } else {
                return Bindings.createStringBinding(() -> "");
            }
        });

        // For director column
        directorCol.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof DVD) {
                return Bindings.createStringBinding(() -> ((DVD) cellData.getValue()).getDirector());
            } else {
                return Bindings.createStringBinding(() -> "");
            }
        });

        // For gameDesigner column
        gameDesignerCol.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Game) {
                return Bindings.createStringBinding(() -> ((Game) cellData.getValue()).getGameDesigner());
            } else {
                return Bindings.createStringBinding(() -> "");
            }
        });

        // For illustrator column
        illustratorCol.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof comicBook) {
                return Bindings.createStringBinding(() -> ((comicBook) cellData.getValue()).getIllustrator());
            } else {
                return Bindings.createStringBinding(() -> "");
            }
        });
    }

    public void resizeColumns(){
        int colNum = 11;
        libraryCatalog.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        titleCol.prefWidthProperty().bind(libraryCatalog.widthProperty().divide(colNum));
        dateCol.prefWidthProperty().bind(libraryCatalog.widthProperty().divide(colNum));
        typeCol.prefWidthProperty().bind(libraryCatalog.widthProperty().divide(colNum));
        authorCol.prefWidthProperty().bind(libraryCatalog.widthProperty().divide(colNum));
        narratorCol.prefWidthProperty().bind(libraryCatalog.widthProperty().divide(colNum));
        productionCol.prefWidthProperty().bind(libraryCatalog.widthProperty().divide(colNum));
        directorCol.prefWidthProperty().bind(libraryCatalog.widthProperty().divide(colNum));
        gameDesignerCol.prefWidthProperty().bind(libraryCatalog.widthProperty().divide(colNum));
        illustratorCol.prefWidthProperty().bind(libraryCatalog.widthProperty().divide(colNum));
        statusCol.prefWidthProperty().bind(libraryCatalog.widthProperty().divide(colNum));
        dueDateCol.prefWidthProperty().bind(libraryCatalog.widthProperty().divide(colNum));


    }
    // Setter method for LibraryClient instance
    public void setLibraryClient(LibraryClient libraryClient) {
        this.libraryClient = libraryClient;
        libraryClient.setCheckedItems(mongoDB.getCheckedItems(libraryClient.getUsername()));
        updateLibraryDisplay();
    }

    private void startLibraryDisplayUpdater() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::updateLibraryDisplay, 0, 1, TimeUnit.SECONDS);
    }
    // Update UI with localLib data
    public void updateLibraryDisplay() {
        if (libraryClient != null) {
            Map<Item, Boolean> localLib = libraryClient.getLocalLib();
            // Create an ObservableList to hold the items
            ObservableList<Item> itemList = FXCollections.observableArrayList();

            // Add all items from the localLib map to the ObservableList
            itemList.addAll(localLib.keySet());

            // Set the items to the TableView
            libraryCatalog.setItems(itemList);
            updateCheckout();
        }
    }
    public void updateCheckout() {
        statusCol.setCellValueFactory(cellData -> {
            Item item = cellData.getValue();
            boolean status = libraryClient.getLocalLib().getOrDefault(item, false);
            return Bindings.createObjectBinding(() -> status);
        });
        libraryCatalog.refresh();
    }
    public void setLogOff(){
        mongoDB.writeArrayListToMongo(libraryClient.getCheckedItems(), libraryClient.getUsername());
        libraryClient.sendMessage("bye");
        stage.close();
        mongoDB.close();
        Platform.exit();
        System.exit(0);
    }
    public void setStage(Stage primaryStage) {
        stage = primaryStage;
    }

    public void setMongoDBConnection(MongoDBConnection mongoDBConnection) {
        this.mongoDB = mongoDBConnection;
    }
}
