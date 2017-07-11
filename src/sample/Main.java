package sample;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.json.JSONException;
import org.json.JSONObject;


public class Main extends Application {
    JSONObject data;
    Label definition;
    Parent root;
    javafx.scene.control.ListView<String> listView;
    Label titleLabel;

    ObservableList<String> words;

    @Override
    public void start(Stage primaryStage) throws Exception{
        loadJson();
        root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        listView = (javafx.scene.control.ListView<String>) root.lookup("#listView");
        ArrayList<String> temp = new ArrayList<>();
        data.keys().forEachRemaining(new Consumer<String>() {
            @Override
            public void accept(String s) {
                temp.add(s);
            }
        });
        words = FXCollections.observableArrayList(temp);
        primaryStage.setTitle("Dictionnaire des anglicismes");
        listView.setItems(words);
        titleLabel = (Label) root.lookup("#label");
        definition = (Label) root.lookup("#definition");
        titleLabel.setFont(new Font(titleLabel.getFont().getFamily(),30));


        listView.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<String>() {
                    public void changed(ObservableValue<? extends String> ov,
                                        String old_val, String new_val) {
                        titleLabel.setText(new_val);
                        definition.setText(data.getString(new_val));
                    }
                });
        
        listView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            
    @Override
    public void handle(MouseEvent click) {

        if (click.getClickCount() == 2) {
           String word = listView.getSelectionModel().getSelectedItem();
           String def = data.getString(word);
            showAddNewWordDialog(word, def);
        }
    }
        });


        Scene scene = new Scene(root, 600, 800);
        setupInputHandling(scene);
        setupMenu(scene ,primaryStage);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        
         

    }

    private void setupInputHandling(Scene scene) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
            if(key.getCode()== KeyCode.DELETE) {
                String word = listView.getSelectionModel().getSelectedItem();
                data.remove(word);
                words.remove(word);
            }
        });
        // Textarea inputs
        /*TextField updateTitle = (TextField) scene.getRoot().lookup("#updateTitle");
        TextField updateDefinition = (TextField) scene.getRoot().lookup("#updateDefinition");
        updateTitle.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    String oldValue = titleLabel.getText();
                    data.put(updateTitle.getText(), data.remove(oldValue));
                    words.remove(oldValue);
                    words.add(updateTitle.getText());
                    updateTitle.clear();
                }
            }
        });
        updateDefinition.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    String key = listView.getSelectionModel().getSelectedItem();
                    System.out.println("Key " + key);
                    String newDefinition = updateDefinition.getText();
                    data.put(key, newDefinition);
                    definition.setText(newDefinition);
                    updateDefinition.clear();
                }
            }
        });*/
    }

    private void setupMenu(Scene scene, Stage primaryStage) {
        MenuBar menuBar = new MenuBar();

        // --- Menu File
        Menu menuFile = new Menu("File");

        MenuItem newWord = new MenuItem("New Word");
        newWord.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //addNewWord();
                showAddNewWordDialog("","");
            }
        });

        menuFile.getItems().addAll(newWord);

        // --- Menu Edit
        Menu menuEdit = new Menu("Edit");

        // --- Menu View
        Menu menuView = new Menu("View");

        menuBar.getMenus().addAll(menuFile, menuEdit, menuView);

        menuBar.prefWidthProperty().bind(primaryStage.widthProperty());
        ((BorderPane)scene.getRoot()).setTop(menuBar);


    }
    
    private void showAddNewWordDialog(String word, String def) {
    // Create the custom dialog.
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Add New Word");

    // Set the button types.
    ButtonType loginButtonType = new ButtonType("OK", ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

    VBox vbox = new VBox();
    vbox.setPrefSize(300, 200);

    TextField from = new TextField();
    from.setPromptText("From");
    from.setText(word);
    TextField to = new TextField();
    to.setText(def);
    to.setPromptText("To");

    vbox.getChildren().addAll(from, to);
    dialog.getDialogPane().setContent(vbox);

    // Request focus on the username field by default.
    Platform.runLater(() -> from.requestFocus());

    // Convert the result to a username-password-pair when the login button is clicked.
    dialog.setResultConverter(dialogButton -> {
        if (dialogButton == loginButtonType) {
            addNewWord(from.getText(), to.getText());
            return dialogButton;
        }
        return null;
    });

    dialog.showAndWait();

    
    }

    private void addNewWord() {
        int i = 0;
        while (true) {
            if (!data.has("temp" + i)) break;
            i++;
        }
        data.put("temp" + i, "temp");
        words.add("temp" + i);
        //refreshListView();
    }
    
    private void addNewWord(String key, String value) {
        boolean refresh = false;
        if(!data.has(key)) {
            words.add(key);
        } else {
            titleLabel.setText(key);
            definition.setText(value);
        }
        data.put(key,value);
        
    }

    private void refreshListView() {

    }

    public void loadJson() throws IOException {
        data = parseJSONFile("sampleData.json");
    }

    public static JSONObject parseJSONFile(String filename) throws JSONException, IOException {
        String content = new String(Files.readAllBytes(Paths.get(filename)));
        return new JSONObject(content);
    }


    public static void main(String[] args) {
        launch(args);
        
 
                
    }

    @Override
    public void stop() throws Exception {
        FileWriter fileWriter = new FileWriter("sampleData.json");
        try {

            fileWriter.write(data.toString());

            System.out.println("Successfully Copied JSON Object to File...");
        } catch (Exception e) {
            System.out.println("Failed saving file");
            e.printStackTrace();
        } finally {
            fileWriter.flush();
            fileWriter.close();
        }
    }
}
