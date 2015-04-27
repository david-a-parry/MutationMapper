/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mutationmapper;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author david
 */
public class MutationMapperResultViewController implements Initializable {
    
   @FXML
   AnchorPane resultPane;
   @FXML
   MenuBar menuBar;
   @FXML
   TableView<MutationMapperResult> resultTable;
   @FXML
   TableColumn indexCol;
   @FXML
   TableColumn symbolCol;
   @FXML
   TableColumn geneCol;
   @FXML
   TableColumn transcriptCol;
   @FXML
   TableColumn cdsCol;
   @FXML
   TableColumn genomicCol;
   @FXML
   TableColumn refCol;
   @FXML
   TableColumn varCol;
   @FXML
   TableColumn consequenceCol;
   @FXML
   TableColumn cdsConsequenceCol;
   @FXML
   TableColumn proteinConsequenceCol;
   @FXML
   TableColumn polyphenCol;
   @FXML
   TableColumn siftCol;
   @FXML
   TableColumn knownVarCol;
   @FXML
   TableColumn seqInputCol;
   @FXML
   Button closeButton;
   @FXML 
   Label summaryLabel;
   @FXML
   TextArea resultTextArea;
   @FXML
   MenuItem closeMenuItem;
   @FXML
   MenuItem clearAndCloseMenuItem;
   @FXML
   MenuItem clearMenuItem;
   
   private final ObservableList<MutationMapperResult> data = FXCollections.observableArrayList();
   Integer lastIndex = 0;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        indexCol.setCellValueFactory(new PropertyValueFactory<>("index"));
        symbolCol.setCellValueFactory(new PropertyValueFactory<>("geneSymbol"));
        geneCol.setCellValueFactory(new PropertyValueFactory<>("geneId"));
        transcriptCol.setCellValueFactory(new PropertyValueFactory<>("transcript"));
        cdsCol.setCellValueFactory(new PropertyValueFactory<>("cdsCoordinate"));
        genomicCol.setCellValueFactory(new PropertyValueFactory<>("genomicCoordinate"));
        refCol.setCellValueFactory(new PropertyValueFactory<>("refAllele"));
        varCol.setCellValueFactory(new PropertyValueFactory<>("varAllele"));
        proteinConsequenceCol.setCellValueFactory(new PropertyValueFactory<>("proteinConsequence"));
        cdsConsequenceCol.setCellValueFactory(new PropertyValueFactory<>("cdsConsequence"));
        consequenceCol.setCellValueFactory(new PropertyValueFactory<>("consequence"));
        knownVarCol.setCellValueFactory(new PropertyValueFactory<>("knownIds"));
        seqInputCol.setCellValueFactory(new PropertyValueFactory<>("seqInput"));
        polyphenCol.setCellValueFactory(new PropertyValueFactory<>("polyphenResult"));
        siftCol.setCellValueFactory(new PropertyValueFactory<>("siftResult"));
        
        resultTable.setFixedCellSize(-1);
        resultTable.setRowFactory(param -> {
            return new TableRow() {
                @Override
                public void updateIndex(int i) {
                    super.updateIndex(i);

                    setMinHeight(35);// * i);
                }
            };
        });
        /*
        cdsConsequenceCol.setCellFactory(new Callback<TableColumn<String,String>, TableCell<String,String>>() {
               @Override
               public TableCell<String, String> call( TableColumn<String, String> param) {
                    final TableCell<String, String> cell = new TableCell<String, String>() {
                         private Text text;
                         @Override
                         public void updateItem(String item, boolean empty) {
                              super.updateItem(item, empty);
                              if (item != null && !isEmpty()) {
                                   text = new Text(item.toString());
                                   text.wrappingWidthProperty().bind(cdsConsequenceCol.widthProperty()); // Setting the wrapping width to the Text
                                   setGraphic(text);
                              }
                         }
                    };
                    return cell;
               }
          });
        */
        resultTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        resultTable.getSelectionModel().setCellSelectionEnabled(true);
        resultTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ObservableList<TablePosition> posList = resultTable.getSelectionModel().getSelectedCells();
                int old_r = -1;
                StringBuilder clipboardString = new StringBuilder();
                for (TablePosition p : posList) {
                    int r = p.getRow();
                    int c = p.getColumn();
                    Object cell = resultTable.getColumns().get(c).getCellData(r);
                    if (cell == null)
                        cell = "";
                    if (old_r == r)
                        clipboardString.append('\t');
                    else if (old_r != -1)
                        clipboardString.append('\n');
                    clipboardString.append(cell);
                    old_r = r;
                    
                }
                final ClipboardContent content = new ClipboardContent();
                content.putString(clipboardString.toString());
                Clipboard.getSystemClipboard().setContent(content);
            }
        });
        ContextMenu menu = new ContextMenu();
        menu.getItems().add(copyItem);
        copyItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN));
        resultTable.setContextMenu(menu);
        
        closeMenuItem.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent e){
                Platform.runLater(new Runnable(){
                    @Override
                    public void run(){
                        Stage stage = (Stage) resultPane.getScene().getWindow();
                        stage.close();
                    }
                });
            }
        });
        
        clearAndCloseMenuItem.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent e){
                Platform.runLater(new Runnable(){
                    @Override
                    public void run(){
                        clearTable();
                        Stage stage = (Stage) resultPane.getScene().getWindow();
                        stage.close();
                    }
                });
            }
        });
        
        clearMenuItem.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent e){
                Platform.runLater(new Runnable(){
                    @Override
                    public void run(){
                        clearTable();
                    }
                });
            }
        });
        
    } 
    
    
    public void displayData(ArrayList<MutationMapperResult> results){
        String index = String.valueOf(lastIndex + 1);
        char sub = 'A';
        for (MutationMapperResult r: results){
            if (sub == 'Z'){
                index = (index + sub);
                sub = 'A';
            }
            r.setIndex(index + sub);
            data.add(r);
            sub++;
        }
        lastIndex++;
        resultTable.setItems(data);
    } 
    
    
    private void clearTable(){
        lastIndex = 0;
        data.clear();
        resultTable.getItems().clear();
    }
}
