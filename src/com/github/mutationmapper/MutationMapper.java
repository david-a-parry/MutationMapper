/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mutationmapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.minidev.json.parser.ParseException;




/**
 *
 * @author david
 */
public class MutationMapper extends Application implements Initializable{
    
    @FXML 
    MenuBar menuBar;
    @FXML
    Menu fileMenu;
    @FXML
    Menu helpMenu;
    @FXML
    ChoiceBox speciesChoiceBox;
    @FXML
    TextField geneTextField;
    @FXML
    TextField cdsTextField;
    @FXML
    TextField sequenceTextField;
    @FXML
    TextField mutationTextField;
    @FXML
    Label progressLabel;
    @FXML
    ProgressBar progressBar;
    @FXML
    Button runButton;
    
    //Result display window
    FXMLLoader tableLoader;
    Pane tablePane;
    Scene tableScene;
    Stage tableStage;
    MutationMapperResultViewController resultView;
    
    final static EnsemblRest rest = new EnsemblRest();
    
    @Override
    public void start(final Stage primaryStage) {
        try{
            AnchorPane page;
            if (System.getProperty("os.name").equals("Mac OS X")){
                page = (AnchorPane) FXMLLoader.load(
                        com.github.mutationmapper.MutationMapper.class.
                                getResource("MutationMapper.fxml"));  
            }else{
                page = (AnchorPane) FXMLLoader.load(
                        com.github.mutationmapper.MutationMapper.class.
                                getResource("MutationMapper.fxml"));
            }
            Scene scene = new Scene(page);
            primaryStage.setScene(scene);
            primaryStage.setTitle("MutationMapper");
            primaryStage.setResizable(false);
            primaryStage.show();
            //primaryStage.getIcons().add(new Image(this.getClass().
            //        getResourceAsStream("icon.png")));
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
               @Override
               public void handle(WindowEvent e) {
                  Platform.exit();
                  System.exit(0);
               }
            });
            
        } catch (Exception ex) {
            Logger.getLogger(MutationMapper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tableLoader = new FXMLLoader(getClass().
                                       getResource("MutationMapperResultView.fxml"));
        Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    geneTextField.requestFocus();
                }
            }
        );
        
        cdsTextField.textProperty().addListener(new ChangeListener<String>(){
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    final String oldValue, final String newValue ){
                //newValue = newValue.trim();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        sequenceTextField.setDisable(!newValue.isEmpty());
                    }                
                });
                
            }
        });
        
        sequenceTextField.textProperty().addListener(new ChangeListener<String>(){
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    final String oldValue, final String newValue ){
                //newValue = newValue.trim();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (! newValue.isEmpty()){ 
                            cdsTextField.setDisable(newValue.isEmpty());
                        }
                    }
                });
                
            }
        });
        
        runButton.setDefaultButton(true);
        runButton.setOnAction(new EventHandler<ActionEvent>(){
           @Override
           public void handle(ActionEvent actionEvent){
                mapMutation();
            }
       });
        
        getAvailableSpecies();
        
    }
    
    private void mapMutation() {
        final Task<List<MutationMapperResult>> mapperTask;
        final String gene = geneTextField.getText();
        if (gene.isEmpty()){
            //TO DO please enter gene dialog
            return;
        }
        if (cdsTextField.getText().isEmpty() && sequenceTextField.getText().isEmpty()){
            //TO DO please ender coordinate or matching sequence dialog
            return;
        }
        final String species = (String) speciesChoiceBox.getSelectionModel().getSelectedItem();
        if (species.isEmpty()){
            //TO DO you must select a species dialog
            return;
        }
        final String cdsCoordinate = cdsTextField.getText();
        final String sequence = sequenceTextField.getText();
        if (!cdsCoordinate.isEmpty()){
            if (mutationTextField.getText().isEmpty()){
                mapperTask = 
                        new Task<List<MutationMapperResult>>(){
                    @Override
                    protected List<MutationMapperResult> call() throws ParseException, MalformedURLException, IOException, InterruptedException {
                        
                        return codingToGenomic(gene, species, cdsCoordinate);
                    }
                };
            }else{
                mapperTask = new Task<List<MutationMapperResult>>(){
                    @Override
                    protected List<MutationMapperResult> call(){
                        List<MutationMapperResult> results = new ArrayList<>();
                        MutationMapperResult result = new MutationMapperResult();
                        //TO DO!
                        return results;
                    }
                };
            }
        }else{
            final String seq = sequence.replaceAll("[\\W]", "");//remove non-word chars
            if (! sequenceIsDna(seq)){
                System.out.println("Non-DNA characters in sequence field");
            } 
            if (mutationTextField.getText().isEmpty()){
                try{
                    sequenceToCoordinates(gene, species, seq);
                }catch(Exception ex){
                    ex.printStackTrace();
                }
               mapperTask = new Task<List<MutationMapperResult>>() {
                    @Override
                    protected List<MutationMapperResult> call()
                            throws ParseException, MalformedURLException, IOException, InterruptedException{
                        return sequenceToCoordinates(gene, species, seq);
                    }
                };
                
            }else{
                mapperTask = new Task<List<MutationMapperResult>>(){
                    @Override
                    protected List<MutationMapperResult> call(){
                        List<MutationMapperResult> results = new ArrayList<>();
                        MutationMapperResult result = new MutationMapperResult();
                        //TO DO!
                        return results;
                
                    }
                };
            }
        }
        mapperTask.setOnSucceeded(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle (WorkerStateEvent e){
                setRunning(false);
                progressLabel.textProperty().unbind();
                progressBar.progressProperty().unbind();
                progressBar.progressProperty().set(0);
                runButton.setText("Run");
                runButton.setDefaultButton(true);
                ArrayList<MutationMapperResult> results = 
                        (ArrayList<MutationMapperResult>) e.getSource().getValue();
                if (results == null){
                    return;
                }
                try{
                    if (tablePane == null){
                        tablePane = (Pane) tableLoader.load();
                    }
                    if (resultView == null){
                        resultView = 
                            (MutationMapperResultViewController) tableLoader.getController();
                    }
                    if (tableScene == null){
                        tableScene = new Scene(tablePane);
                        tableStage = new Stage();
                        tableStage.setScene(tableScene);
                        tableStage.initModality(Modality.NONE);
                    }
                    resultView.displayData(results);
                    tableStage.setTitle("MutationMapper Results");
                    //tableStage.getIcons().add(new Image(this.getClass()
                    //        .getResourceAsStream("icon.png")));
                    if (!tableStage.isShowing()){
                        tableStage.show();
                    }else{
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                tableStage.requestFocus();
                            }
                        });
                    }
                }catch(IOException ex){
                    //TO DO - show error dialog
                    ex.printStackTrace();
                }

                
            }

        });
        
        new Thread(mapperTask).start();
        
        runButton.setDefaultButton(false);
        runButton.setCancelButton(true);
        runButton.setText("Cancel");
        
        
        
        
        
    }
    
    private List<MutationMapperResult> sequenceToCoordinates(String gene, String species, String seq)
            throws ParseException, MalformedURLException, IOException, InterruptedException{
        List<MutationMapperResult> results = new ArrayList<>();
        List<TranscriptDetails> transcripts = getTranscriptsForGene(gene, species);
        if (transcripts.isEmpty()){
            return null;
        }
        ArrayList<String> t_ids = new ArrayList<>();
        for (TranscriptDetails t: transcripts){
            MutationMapperResult result = putBasicTranscriptInfo(t);
            results.add(result);
            t_ids.add(t.getTranscriptId());
        }
        rest.getTranscriptSequences(t_ids, "cds");
        return results;
    }
    
    
    private List<MutationMapperResult> codingToGenomic(String gene, String species, 
            String cdsCoordinate) throws ParseException, MalformedURLException, IOException, InterruptedException{
        List<MutationMapperResult> results = new ArrayList<>();
        List<TranscriptDetails> transcripts = getTranscriptsForGene(gene, species);
        
        if (transcripts.isEmpty()){
            return null;
        }
        for (TranscriptDetails t: transcripts){
            HashMap<String, String> g = rest.codingToGenomicTranscript(
                    species, t.getTranscriptId(), Integer.parseInt(cdsCoordinate));
            MutationMapperResult result = putBasicTranscriptInfo(t);
            result.setCdsCoordinate(Integer.parseInt(cdsCoordinate));
            result.setChromosome(g.get("chromosome"));
            result.setCoordinate(Integer.parseInt(g.get("coordinate")));
            result.setGenome(g.get("assembly"));
            results.add(result);
        }
        return results;
    }
    
    private MutationMapperResult putBasicTranscriptInfo(TranscriptDetails t){
        MutationMapperResult result = new MutationMapperResult();
        result.setGeneSymbol(t.getSymbol());
        result.setGeneId(t.getId());
        result.setTranscript(t.getTranscriptId());
        return result;
    }
    
    private List<TranscriptDetails>  getTranscriptsForGene(String gene, String species) 
            throws ParseException, MalformedURLException, IOException, InterruptedException{
        ArrayList<TranscriptDetails> transcripts = new ArrayList<>();
        String id;
        if(gene.matches("ENS\\w*T\\d{11}.*\\d*")){//is transcript id
            transcripts.add(rest.getTranscriptDetails(gene));
        }else{
            if(gene.matches("ENS\\w*G\\d{11}.*\\d*")){//is gene id
                id = gene;
            }else{
                id = rest.getGeneID(species, gene);
            }
            transcripts = rest.getGeneDetails(id);
        }
        return transcripts;
    }
    
    private boolean sequenceIsDna(String seq){
        return !seq.matches("(?i)[^ACTG]");
    }
    
    private void getAvailableSpecies(){
        final Task<List<String>> getSpeciesTask = new Task<List<String>>(){
            @Override
            protected List<String> call() 
                    throws ParseException, MalformedURLException, IOException, InterruptedException{
                System.out.println("Getting species.");
                List<String> species = rest.getAvailableSpecies();
                return species;
            }
        };
        
        getSpeciesTask.setOnSucceeded(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle (WorkerStateEvent e){
                List<String> species = (List<String>) e.getSource().getValue();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        speciesChoiceBox.getItems().clear();
                        speciesChoiceBox.getItems().addAll(species);
                        speciesChoiceBox.getSelectionModel().selectFirst();
                    }
                });
            }
        });
        getSpeciesTask.setOnCancelled(null);
        getSpeciesTask.setOnFailed(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle (WorkerStateEvent e){
                //TO DO - ERROR DIALOG
                e.getSource().getException().printStackTrace();
            }
        });
        new Thread(getSpeciesTask).start();
    }
    
    private void canRun(boolean can){
        runButton.setDisable(!can);
    }
    
    private void setRunning(boolean running){
        canRun(!running);
        geneTextField.setDisable(running);
        sequenceTextField.setDisable(running);
        cdsTextField.setDisable(running);
        mutationTextField.setDisable(running);
        speciesChoiceBox.setDisable(running);
        if (!running){
            sequenceTextField.setDisable(!cdsTextField.getText().isEmpty());
            cdsTextField.setDisable(sequenceTextField.getText().isEmpty());
        }
    }
                
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
