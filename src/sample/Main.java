package sample;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
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
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import org.controlsfx.control.Notifications;
import org.json.JSONException;
import org.json.JSONObject;


public class Main extends Application {
    JSONObject data;
    Label definition;
    Parent root;
    javafx.scene.control.ListView<String> listView;
    Label titleLabel;

    ObservableList<String> words;
    
    FileChooser fileChooser = new FileChooser();
    File file;
    String path;
    
    Preferences prefs;

    @Override
    public void start(Stage primaryStage) throws Exception{
        prefs = Preferences.userNodeForPackage(Main.class);
        String filePath = prefs.get("filePath", "sampleData.json");
        loadJson(filePath);
        
                
        root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        listView = (javafx.scene.control.ListView<String>) root.lookup("#listView");
        titleLabel = (Label) root.lookup("#label");
        titleLabel.setFont(new Font(titleLabel.getFont().getFamily(),30));
        definition = (Label) root.lookup("#definition");
        addWordsToList();

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


        Scene scene = new Scene(root, 600, 500);
        setupInputHandling(scene);
        setupMenu(scene ,primaryStage);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Dictionnaire des anglicismes");
        primaryStage.show();
        
        Notifications.create()
              .title("Load")
              .text("Loaded " + filePath).showInformation();
        
        
        
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("Quitting");
                alert.setHeaderText("You are about to quit");
                alert.setContentText("Would you like to save first?");

                alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.YES){
                    try {
                        saveWork();
                    } catch (Exception ex) {
                        showExceptionDialog(ex);
                    }
                } if (result.get() == ButtonType.CANCEL )
                    event.consume();
                }
        });
        
        
         

    }

    private void setupInputHandling(Scene scene) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
            if(key.getCode()== KeyCode.DELETE) {
                String word = listView.getSelectionModel().getSelectedItem();
                data.remove(word);
                words.remove(word);
                Notifications.create()
              .title("Delete")
              .text("Deleted " + word).showInformation();
            }
        });
    }

    private void setupMenu(Scene scene, Stage primaryStage) {
        MenuBar menuBar = new MenuBar();

        // --- Menu File
        Menu menuFile = new Menu("File");

        MenuItem newWord = new MenuItem("New Word");
        newWord.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showAddNewWordDialog("","");
            }
        });
        
        MenuItem save = new MenuItem("Save");
        save.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
               try {
                    saveWork();
                    Notifications.create()
              .title("Save")
              .text("File successfuly saved!").showInformation();
                } catch (Exception ex) {
                    showExceptionDialog(ex);
                }
            }
        });
        
        MenuItem open = new MenuItem("Open");
        open.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                file = fileChooser.showOpenDialog(primaryStage);
                try {
                    loadJson(file);
                    addWordsToList();
                } catch (IOException ex) {
                    showExceptionDialog(ex);
                }
            }
        });

        menuFile.getItems().addAll(newWord, open, save);

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

    GridPane gridPane = new GridPane();
    gridPane.setPrefSize(300, 200);

    Label motLabel = new Label("Mot");
    TextField motTextField = new TextField();
    motTextField.setPromptText("Mot");
    motTextField.setText(word);
    
    Label defLabel = new Label("Définition");
    TextField defTextField = new TextField();
    defTextField.setText(def);
    defTextField.setPromptText("Définition");

    gridPane.add(motLabel, 0, 0);
    gridPane.add(motTextField, 1, 0);
    gridPane.add(defLabel, 0, 1);
    gridPane.add(defTextField, 1, 1);
    dialog.getDialogPane().setContent(gridPane);

    // Request focus on the word field field by default.
    Platform.runLater(() -> motTextField.requestFocus());

    dialog.setResultConverter(dialogButton -> {
        if (dialogButton == loginButtonType) {
            addNewWord(motTextField.getText(), defTextField.getText());
            return dialogButton;
        }
        return null;
    });
    
    final Button btOk = (Button) dialog.getDialogPane().lookupButton(loginButtonType);
 btOk.addEventFilter(ActionEvent.ACTION, event -> {
     if (motTextField.getText().isEmpty()) {
         Alert alert = new Alert(Alert.AlertType.ERROR, "Vous devez rentrez un mot");
         alert.showAndWait();
         event.consume();
     }
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

    public void loadJson(String filePath) throws IOException {
        data = parseJSONFile(filePath);
        path = filePath;
    }
    
    public void loadJson(File file) throws IOException {
        data = parseJSONFile(file);
        path = file.getPath();
    }
    
   

    public JSONObject parseJSONFile(String filename) throws JSONException, IOException {
        
        String content = new String(Files.readAllBytes(Paths.get(filename)));
        return new JSONObject(content);
    }
    
    public JSONObject parseJSONFile(File file) throws JSONException, IOException {
        String content = new String(Files.readAllBytes(file.toPath()));
        return new JSONObject(content);
    }


    public static void main(String[] args) {
        launch(args);
        
 
                
    }

    @Override
    public void stop()  {
        
        prefs.put("filePath", path);
    }
        

    private void saveWork() throws Exception {
        FileWriter fileWriter = new FileWriter(path);
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
    
    public void showExceptionDialog(Exception ex) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Exception Dialog");
        alert.setHeaderText("Look, an Exception Dialog");
        alert.setContentText(ex.getMessage());


        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }

    private boolean showConfirmationToQuitDialog() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Quitting");
        alert.setHeaderText("You are about to quit");
        alert.setContentText("Would you like to save first?");
        
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            return true;
        } else {
            return false;
        }
    }

    private void addWordsToList() {
        ArrayList<String> temp = new ArrayList<>();
        data.keys().forEachRemaining(new Consumer<String>() {
            @Override
            public void accept(String s) {
                temp.add(s);
            }
        });
        Collections.sort(temp);
        words = FXCollections.observableArrayList(temp);
        listView.setItems(words);
        listView.getSelectionModel().select(0);
    }
}
