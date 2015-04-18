/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mutationmapper;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import javafx.animation.Animation.Status;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.Callback;

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
   TableColumn knownVarCol;
   @FXML
   Button closeButton;
   @FXML 
   Label summaryLabel;
   @FXML
   TextArea resultTextArea;
   
   private final ObservableList<MutationMapperResult> data = FXCollections.observableArrayList();
   
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
        consequenceCol.setCellValueFactory(new PropertyValueFactory<>("consequences"));
        consequenceCol.setCellFactory(new Callback<TableColumn<String,String>, TableCell<String,String>>() {
               @Override
               public TableCell<String, String> call( TableColumn<String, String> param) {
                    final TableCell<String, String> cell = new TableCell<String, String>() {
                         private Text text;
                         @Override
                         public void updateItem(String item, boolean empty) {
                              super.updateItem(item, empty);
                              if (item != null && !isEmpty()) {
                                   text = new Text(item.toString());
                                   text.wrappingWidthProperty().bind(consequenceCol.widthProperty()); // Setting the wrapping width to the Text
                                   setGraphic(text);
                              }
                         }
                    };
                    return cell;
               }
          });
        knownVarCol.setCellValueFactory(new PropertyValueFactory<>("knownVar"));
        
    } 
    public void displayData(ArrayList<MutationMapperResult> results){
        for (MutationMapperResult r: results){
            r.setIndex(data.size() + 1);
            data.add(r);
        }
        resultTable.setItems(data);
    } 
        
}
