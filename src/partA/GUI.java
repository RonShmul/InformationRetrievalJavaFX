package partA;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GUI extends Application {
    private String dataSetPath;
    private String locationPath;
    private Controller controller;
    private String queriesFilePath;
    private String searchText;
    private boolean isDocno;
    private static List<File> savedFiles = new ArrayList<>();
    private HashMap<String, List<String>> queriesFileResults;
    private List<String> queryResults;
    @Override
    public void start(Stage primaryStage) throws Exception {
        //initialize controller and docno boolean field
        controller = new Controller();
        isDocno = false;
        //Creating containers
        VBox vBox = new VBox();
        HBox hBox = new HBox();

        //the start button
        Button startBTN = new Button("Start");

        //change buttons settings
        /*startBTN.setDefaultButton(true);
        startBTN.setMinWidth(100);
        startBTN.setPadding(new Insets(5));
        startBTN.setDisable(true);*/
        //add the side grid to the hBox
        hBox.getChildren().add(createSideGridPane(startBTN));

        //add the buttons grid to vBox and the vBox to hBox
        vBox.getChildren().add(createButtonsGridPane(startBTN));
        vBox.getChildren().add(searchGrid());
        vBox.setPrefWidth(1000);
        vBox.setPrefHeight(1000);
        VBox.setVgrow(vBox, Priority.ALWAYS);
        HBox.setHgrow(vBox, Priority.ALWAYS);
        hBox.getChildren().add(vBox);

        //Styling start button
        startBTN.setStyle("-fx-background-color: #4a8af4; -fx-textfill: black; -fx-color: black; -fx-alignment: top-center;");

        //Creating a scene object
        FXMLLoader fxl = new FXMLLoader();

        //set the stage and scene
        primaryStage.setTitle("Information Retrieval - Part A");
        Scene scene = new Scene(hBox);
        primaryStage.setScene(scene);

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        //set the stage properties
        primaryStage.setResizable(true);
        primaryStage.setX(primaryScreenBounds.getMinX());
        primaryStage.setY(primaryScreenBounds.getMinY());
        primaryStage.setWidth(primaryScreenBounds.getWidth());
        primaryStage.setHeight(primaryScreenBounds.getHeight());

        primaryStage.setMaximized(true);
        primaryStage.show();
    }
    public void setIsDocno(boolean isDocno) {
        this.isDocno = isDocno;
    }

    public boolean isDocno() {
        return isDocno;
    }

    GridPane createButtonsGridPane(Button startBTN) {
        GridPane gridPaneforButtons = new GridPane();
        //create buttons
        Button showDictionary = new Button("Show Dictionary");
        Button showCache = new Button("Show Cache");
        Button load = new Button("Load");
        Button save = new Button("Save");
        Button reset = new Button("Reset");

        //set properties to buttons
        load.setMinWidth(100);
        save.setMinWidth(100);
        reset.setMinWidth(100);
        load.setPadding(new Insets(5));
        save.setPadding(new Insets(5));
        reset.setPadding(new Insets(5));

        //events to buttons
        //Part A:
        /*startBTN.setOnMouseClicked((new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {

                if (!showCache.isVisible() || !showDictionary.isVisible() || !save.isVisible()) {
                    showCache.setVisible(true);
                    showDictionary.setVisible(true);
                    save.setVisible(true);
                }
                if (reset.isDisable()) {
                    reset.setDisable(false);
                }
                controller.startIndexing(dataSetPath, locationPath);
            }
        }));
        save.setOnMouseClicked((new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                controller.saveDictionaryAndCache();
            }
        }));*/
        save.setOnMouseClicked((new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                File file = controller.chooseSaveFile();
                if(queryResults == null && queriesFileResults == null)
                    openAlertMessage("nothing has run yet..");
                else {
                    if(queriesFileResults != null) {

                        controller.writeQueriesFileResultToFile(file, queriesFileResults);
                    }
                    else{
                        controller.writeQueryResultToFile(file, queryResults);
                    }
                }
            }
        }));
        load.setOnMouseClicked((new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {

                controller.loadIndex();
                showCache.setVisible(true);
                showDictionary.setVisible(true);

            }
        }));
        reset.setOnMouseClicked((new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                if (savedFiles.isEmpty()) {
                    openAlertMessage("Nothing has saved yet..");
                } else {
                    for (int i = 0; i < savedFiles.size(); i++) {
                        savedFiles.get(i).delete();
                    }
                    savedFiles.clear();
                    controller.getIndexer().getDictionary().clear();
                    controller.getIndexer().getCache().clear();
                    controller.getIndexer().getDocuments().clear();
                }
            }
        }));
        showDictionary.setOnMouseClicked((new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                Stage stage = new Stage();
                Scene scene = new Scene(createDictionaryView(), 400, 600);
                stage.setScene(scene);
                stage.show();
            }
        }));
        showCache.setOnMouseClicked((new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                Stage stage = new Stage();
                Scene scene = new Scene(getCacheTable(), 400, 600);
                stage.setScene(scene);
                stage.show();
            }
        }));

        //default not shown
        //save.setVisible(false);
        //reset.setDisable(true);
        showCache.setVisible(false);
        showDictionary.setVisible(false);

        //add buttons to grid
        gridPaneforButtons.add(startBTN, 0, 0);
        gridPaneforButtons.add(reset, 1, 0);
        gridPaneforButtons.add(load, 2, 0);
        gridPaneforButtons.add(save, 3, 0);
        gridPaneforButtons.add(showDictionary, 6, 0);
        gridPaneforButtons.add(showCache, 7, 0);

        //setting the grid properties
        gridPaneforButtons.setAlignment(Pos.TOP_LEFT);
        gridPaneforButtons.setPadding(new Insets(50, 10, 10, 10));
        gridPaneforButtons.setVgap(5);
        gridPaneforButtons.setHgap(5);
        gridPaneforButtons.setAlignment(Pos.CENTER);

        //set the style
        load.setStyle("-fx-background-color: #4a8af4; -fx-textfill: black; -fx-color: black; -fx-alignment: top-center;");
        save.setStyle("-fx-background-color: #4a8af4; -fx-textfill: black; -fx-color: black; -fx-alignment: top-center;");
        reset.setStyle("-fx-background-color: #4a8af4; -fx-textfill: black; -fx-color: black; -fx-alignment: top-center;");
        showDictionary.setStyle("-fx-background-color: white; -fx-border-color: #9e9e9e; -fx-border-radius: 5");
        showCache.setStyle("-fx-background-color: white; -fx-border-color: #9e9e9e; -fx-border-radius: 5");

        return gridPaneforButtons;
    }

    GridPane createSideGridPane(Button startBTN) {
        GridPane mainGridPane = new GridPane();

        //Setting the grid properties
        mainGridPane.setPadding(new Insets(10, 10, 10, 10));
        mainGridPane.setVgap(20);
        mainGridPane.setHgap(20);
        mainGridPane.setAlignment(Pos.TOP_LEFT);

        //create the browse buttons
        Button dataSetBrowse = new Button("Browse");
        Button filesLocationBrowse = new Button("Browse");
        //Label dataset
        Text datasetLabel = new Text("Choose DataSet");

        //Text field for dataset
        TextField dataSetTextField = new TextField();

        //Label for location
        Text locationFileLabel = new Text("Choose Files Location");

        //Text field for location
        TextField locationTextField = new TextField();

        //check box for stemming
        CheckBox stemmingCheckBox = new CheckBox("Enable Stemming");
        stemmingCheckBox.setIndeterminate(false);

        //Arranging all the nodes in the grid
        mainGridPane.add(datasetLabel, 0, 1);
        mainGridPane.add(dataSetTextField, 1, 1);
        mainGridPane.add(dataSetBrowse, 2, 1);
        mainGridPane.add(locationFileLabel, 0, 2);
        mainGridPane.add(locationTextField, 1, 2);
        mainGridPane.add(filesLocationBrowse, 2, 2);
        mainGridPane.add(stemmingCheckBox, 0, 3);

        //buttons events
        dataSetBrowse.setOnMouseClicked((new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {

                dataSetPath = controller.chooseFolder();
                dataSetTextField.setText(dataSetPath);
                if (locationPath != null)
                    startBTN.setDisable(false);

            }
        }));

        filesLocationBrowse.setOnMouseClicked((new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {

                locationPath = controller.chooseFolder();
                locationTextField.setText(locationPath);
                if (dataSetPath != null)
                    startBTN.setDisable(false);
            }
        }));
        dataSetTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            dataSetPath = newValue;
            controller.setCorpusPath(dataSetPath);

            if (locationPath != null)
                startBTN.setDisable(false);
        });

        locationTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            locationPath = newValue;

            if (dataSetPath != null)
                startBTN.setDisable(false);
        });
        stemmingCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                controller.setToStemm(newValue);
            }
        });

        //styling the buttons
        dataSetBrowse.setStyle("-fx-background-color: white; -fx-border-color: #9e9e9e; -fx-border-radius: 5");
        filesLocationBrowse.setStyle("-fx-background-color: white; -fx-border-color: #9e9e9e; -fx-border-radius: 5");
        datasetLabel.setStyle("-fx-font: normal 15px 'serif' ");
        locationFileLabel.setStyle("-fx-font: normal 15px 'serif' ");
        dataSetTextField.setStyle("-fx-font: normal 15px 'serif' ");
        locationTextField.setStyle("-fx-font: normal 15px 'serif' ");
        mainGridPane.setStyle("-fx-background-color: whitesmoke;");

        return mainGridPane;
    }

    public TableView<Term> createDictionaryView() {

        //create the Table
        TableView<Term> tableDict = new TableView<Term>();
        //create the observable list from the dictionary
        ObservableList<Term> data = FXCollections.observableArrayList(controller.showDictionary());

        //create the Term Column
        TableColumn<Term, String> termName = new TableColumn<>("Term");
        termName.setMinWidth(200);
        termName.setCellValueFactory(new PropertyValueFactory<>("term"));

        //create the df Column
        TableColumn<Term, Integer> termDF = new TableColumn<>("Document Frequency");
        termDF.setMinWidth(200);
        termDF.setCellValueFactory(new PropertyValueFactory<>("df"));

        //create the Term Column
        TableColumn<Term, Integer> termFIC = new TableColumn<>("frequency In Corpus");
        termFIC.setMinWidth(200);
        termFIC.setCellValueFactory(new PropertyValueFactory<>("frequencyInCorpus"));

        tableDict.setItems(data);
        tableDict.getColumns().addAll(termName, termDF, termFIC);


        return tableDict;
    }

    TableView<Map.Entry<String, String>> getCacheTable() {

        // use fully detailed type for Map.Entry<String, String>
        TableColumn<Map.Entry<String, String>, String> column1 = new TableColumn<>("Term");
        column1.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry<String, String>, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Map.Entry<String, String>, String> p) {
                // this callback returns property for just one cell, you can't use a loop here
                // for first column we use key
                return new SimpleStringProperty(p.getValue().getKey());
            }
        });

        TableColumn<Map.Entry<String, String>, String> column2 = new TableColumn<>("Posting");
        column2.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry<String, String>, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Map.Entry<String, String>, String> p) {
                // for second column we use value
                return new SimpleStringProperty(p.getValue().getValue());
            }
        });

        ObservableList<Map.Entry<String, String>> items = FXCollections.observableArrayList(controller.showCache().entrySet());
        TableView<Map.Entry<String, String>> table = new TableView<>(items);

        table.getColumns().setAll(column1, column2);
        return table;
    }
    GridPane searchGrid() {
        //create the grid
        GridPane mainGrid = new GridPane();
        //setting the grid properties
        mainGrid.setAlignment(Pos.TOP_LEFT);
        mainGrid.setAlignment(Pos.CENTER);
        mainGrid.setPadding(new Insets(50, 10, 10, -200));
        mainGrid.setVgap(5);
        mainGrid.setHgap(5);
        //Label and text field  for search box
        Text searchLabel = new Text("Enter Search Query Or DOCNO:");
        TextField searchTextBox = new TextField();
        searchLabel.setStyle("-fx-font: normal 15px 'serif' ");

        //check box for Docno
        CheckBox docnoCheckBox = new CheckBox("Check It For DOCNO");
        docnoCheckBox.setIndeterminate(false);

        //Label, text field and Browse button for Queries file
        Text QueriesFileLabel = new Text("Choose Queries File:");
        TextField QueriesFileTextBox = new TextField();
        Button qureiesFileBrowse = new Button("Browse");
        qureiesFileBrowse.setStyle("-fx-background-color: white; -fx-border-color: #9e9e9e; -fx-border-radius: 5");
        QueriesFileLabel.setStyle("-fx-font: normal 15px 'serif' ");

        //create the run button
        Button run = new Button("Run");
        run.setDisable(true);
        run.setMinWidth(100);
        run.setPadding(new Insets(5));
        run.setStyle("-fx-background-color: #4a8af4; -fx-textfill: black; -fx-color: black; -fx-alignment: top-center;");
        //events
        qureiesFileBrowse.setOnMouseClicked((new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                queriesFilePath = controller.chooseFile();
                QueriesFileTextBox.setText(queriesFilePath);
                if (queriesFilePath != null &&(searchText == null || searchText.length() == 0 ))
                    run.setDisable(false);
                else  run.setDisable(true);
            }
        }));
        QueriesFileTextBox.textProperty().addListener((observable, oldValue, newValue) -> {
            queriesFilePath = newValue;
            if (queriesFilePath != null)
                run.setDisable(false);
        });
        searchTextBox.textProperty().addListener((observable, oldValue, newValue) -> {
            searchText = newValue;
            if (searchText != null && (queriesFilePath == null || queriesFilePath.length() == 0 ))
                run.setDisable(false);
            else  run.setDisable(true);
        });
        docnoCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                setIsDocno(newValue);
            }
        });
        run.setOnMouseClicked((new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
               handleForRunButton();
            }
        }));

        //add to grid and return it
        mainGrid.add(searchLabel, 0, 1);
        mainGrid.add(searchTextBox, 1, 1);
        mainGrid.add(docnoCheckBox, 0, 2);
        mainGrid.add(QueriesFileLabel, 0, 4);
        mainGrid.add(QueriesFileTextBox, 1, 4);
        mainGrid.add(qureiesFileBrowse, 2, 4);
        mainGrid.add(run, 1, 6);
        return mainGrid;
    }

    private void handleForRunButton() {
        if((searchText != null) && (queriesFilePath == null || queriesFilePath.length() == 0)) {
            if(isDocno) {
                try {
                    Stage stage = new Stage();
                    Scene scene = new Scene(createFiveSentencesView(), 800, 600);
                    stage.setScene(scene);
                    stage.show();
                } catch (Exception e) {
                    openAlertMessage("there is no such Docno as: " + searchText);
                }
            }
            else {
                Stage stage = new Stage();
                Scene scene = new Scene(createDocnoListView(), 800, 600);
                stage.setScene(scene);
                stage.show();
            }
        }
        else if((queriesFilePath != null) && (searchText == null || searchText.length() == 0)) {
            Stage stage = new Stage();
            Scene scene = new Scene(getDocnosQueriesFileView(), 800, 600);
            stage.setScene(scene);
            stage.show();
        }
        else {
            if(searchText == null) {
                openAlertMessage("There are no parameters to run");
            }
            else {
                openAlertMessage("Too many parameters to run. Please delete either DOCNO/Query text or Query file path");
            }
        }
    }

    public ListView<String> createFiveSentencesView() {

        //create the Table
        ListView<String> table = new ListView<String>();
        //create the observable list from the dictionary
        ObservableList<String> data = FXCollections.observableArrayList(controller.getSentencesForDocno(searchText));
        table.setItems(data);
        return table;
    }

    public VBox createDocnoListView() {
        long startTime = System.currentTimeMillis();
        VBox vBox = new VBox();
        List<String> list = controller.getDocnosListForAQuery(searchText);
        queryResults = list;
        int size = list.size();
        ListView<String> table = createListView(list);
        vBox.getChildren().add(new Label("query: " + searchText));
        vBox.getChildren().add(table);
        long endTime = System.currentTimeMillis();
        long timeTakes = (endTime - startTime)/1000;
        vBox.getChildren().add(new Label("Time: " + timeTakes + " seconds"));
        vBox.getChildren().add(new Label("Number Of Documents Returned: " + size));
        return vBox;
    }
    public ListView<String> createListView(List<String> list) {

        //create the Table
        ListView<String> table = new ListView<String>();
        //create the observable list from the dictionary
        ObservableList<String> data = FXCollections.observableArrayList(list);
        table.setItems(data);
        return table;
    }

    public VBox getDocnosQueriesFileView() {
        VBox vBox = new VBox();
        long startTime = System.currentTimeMillis();
        HashMap<String, List<String>> results = controller.getDocnosListsForQueriesFile(queriesFilePath);
        queriesFileResults = results;
        List<String> totalList = new ArrayList<>();
        for(Map.Entry<String, List<String>> queryResult : results.entrySet()) {
            String query = queryResult.getKey();
            List<String> docnos = queryResult.getValue();
            String NumOfDocnos = "Number Of Documents Returned: " + docnos.size();
            totalList.add(query);
            totalList.addAll(docnos);
            totalList.add(NumOfDocnos);
            totalList.add("");
        }
        ListView<String> listView = createListView(totalList);
        vBox.getChildren().add(listView);
        long endTime = System.currentTimeMillis();
        long timeTakes = (endTime - startTime)/1000;
        Label time = new Label("Time: " + timeTakes + " seconds");
        vBox.getChildren().add(time);
        return vBox;
    }
    public void openAlertMessage(String text) {
        Alert alert = new Alert(Alert.AlertType.WARNING, text, ButtonType.OK, ButtonType.CANCEL);
       alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
