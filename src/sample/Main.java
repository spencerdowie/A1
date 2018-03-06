package sample;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.geometry.Insets;

import java.io.File;

// **************************************************//

public class Main extends Application {

    private TableView<TestFile> table;
    private ObservableList<TestFile> files;
    private BorderPane layout;
    private SpamTrainer trainer = new SpamTrainer();

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("Assignment 1");

        // Get Folder that contains necessary ham and spam folder
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("."));
        File mainDirectory = directoryChooser.showDialog(primaryStage);

        if(mainDirectory != null && mainDirectory.isDirectory()) {

            File[] content = mainDirectory.listFiles();

            // Training Set
            for (File current : content) {
                if (current.getName().contains("train")) {
                    trainer.processTrainFolder(current);
                }
            }
            // Testing Set + Results
            for (File current : content) {
                if (current.getName().contains("test")) {
                    files = trainer.processTestFolder(current);
                }
            }

        }

        table = new TableView<>();
        table.setItems(files);
        table.setEditable(false);

        TableColumn<TestFile, String> fileColumn = null;
        fileColumn = new TableColumn<>("File");
        fileColumn.setMinWidth(300);
        fileColumn.setCellValueFactory(new PropertyValueFactory<>("filename"));

        TableColumn<TestFile, String> actualClassColumn = null;
        actualClassColumn = new TableColumn<>("Actual Class");
        actualClassColumn.setMinWidth(100);
        actualClassColumn.setCellValueFactory(new PropertyValueFactory<>("actualClass"));

        TableColumn<TestFile, Double> spamColumn = null;
        spamColumn = new TableColumn<>("Spam Probability");
        spamColumn.setMinWidth(200);
        spamColumn.setCellValueFactory(new PropertyValueFactory<>("spamProbRounded"));

        table.getColumns().add(fileColumn);
        table.getColumns().add(actualClassColumn);
        table.getColumns().add(spamColumn);

        GridPane summary = new GridPane();
        summary.setPadding(new Insets(10, 10, 10, 10));
        summary.setVgap(10);
        summary.setHgap(10);

        Label accuLabel = new Label("Accuracy: ");
        summary.add(accuLabel, 0, 0);
        TextField accuField = new TextField();
        accuField.setText(trainer.accuracy.toString());
        summary.add(accuField, 1, 0);

        Label precLabel = new Label("Precision: ");
        summary.add(precLabel, 0, 1);
        TextField precField = new TextField();
        precField.setText(trainer.precision.toString());
        summary.add(precField, 1, 1);

        layout = new BorderPane();
        // Place UI elements
        layout.setCenter(table);
        layout.setBottom(summary);

        primaryStage.setScene(new Scene(layout, 600, 500));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
