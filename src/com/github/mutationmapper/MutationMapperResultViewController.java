/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mutationmapper;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

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
        refCol.setCellValueFactory(new PropertyValueFactory<>("ref"));
        varCol.setCellValueFactory(new PropertyValueFactory<>("var"));
        consequenceCol.setCellValueFactory(new PropertyValueFactory<>("consequences"));
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
