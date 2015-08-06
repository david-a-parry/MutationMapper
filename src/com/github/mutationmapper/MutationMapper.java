/*
 * Copyright (C) 2015 David A. Parry <d.a.parry@leeds.ac.uk>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.mutationmapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
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
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
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
    ProgressIndicator progressIndicator;
    @FXML
    Button runButton;
    @FXML
    MenuItem saveMenuItem;
    @FXML
    MenuItem showResultsMenuItem;
    @FXML
    MenuItem quitMenuItem;
    @FXML
    MenuItem aboutMenuItem;
    @FXML
    MenuItem helpMenuItem;
    @FXML
    CheckMenuItem canonicalOnlyMenu;
    @FXML
    CheckMenuItem codingOnlyMenu;
    @FXML
    RadioMenuItem noRefSeqMenu;
    @FXML
    RadioMenuItem refSeqMenu;
    @FXML
    RadioMenuItem refSeqOnlyMenu;
    @FXML
    CheckMenuItem grch37Menu;
    
    //Result display window
    FXMLLoader tableLoader;
    Pane tablePane;
    Scene tableScene;
    Stage tableStage;
    MutationMapperResultViewController resultView;
    
    static HashMap<String, String> speciesTable;
    final static EnsemblRest rest = new EnsemblRest();
    final static String VERSION = "2.2";
    
    @Override
    public void start(final Stage primaryStage) {
        try{
            AnchorPane page;
            if (System.getProperty("os.name").equals("Mac OS X")){
                page = (AnchorPane) FXMLLoader.load(
                        com.github.mutationmapper.MutationMapper.class.
                                getResource("MutationMapperMac.fxml"));  
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
            primaryStage.getIcons().add(new Image(this.getClass().
                    getResourceAsStream("icon.png")));
            scene.getStylesheets().add(com.github.mutationmapper.MutationMapper.class.getResource("mmapper.css").toExternalForm());
            primaryStage.setOnCloseRequest((WindowEvent e) -> {
                Platform.exit();
                System.exit(0);
            });
            final KeyCombination macCloseKeyComb = 
                new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN);
            if (System.getProperty("os.name").equals("Mac OS X")){
               scene.addEventHandler(
                    KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent ev){
                        if (macCloseKeyComb.match(ev)){
                            primaryStage.close();
                        }
                    }
                });
            }
        } catch (Exception ex) {
            Logger.getLogger(MutationMapper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        menuBar.setUseSystemMenuBar(true);
        if (System.getProperty("os.name").equals("Mac OS X")){
            tableLoader = new FXMLLoader(getClass().
                                       getResource("MutationMapperResultViewMac.fxml"));
        }else{
            tableLoader = new FXMLLoader(getClass().
                                       getResource("MutationMapperResultView.fxml"));
        }
        Platform.runLater(() -> {
            geneTextField.requestFocus();
        });
        speciesChoiceBox.getSelectionModel().selectedItemProperty().addListener(
            (new ChangeListener<String>(){
                @Override
                public void changed (ObservableValue ov, String value, final String new_value){ 
                    if (    new_value.equalsIgnoreCase("Human")  ){
                        grch37Menu.setDisable(false);
                    }else{
                        grch37Menu.setDisable(true);
                    }
                }
            })
        );
        speciesChoiceBox.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN) {
                    event.consume();
                }else if (event.getCode().isLetterKey()){
                    String c = event.getText().toLowerCase();
                    List items = speciesChoiceBox.getItems();
                    int s = speciesChoiceBox.getSelectionModel().selectedIndexProperty().get();
                    for (int i = s + 1; i < items.size(); i++){
                        if (items.get(i).toString().toLowerCase().startsWith(c)){
                            speciesChoiceBox.getSelectionModel().select(i);
                            return;
                        }
                    }
                    for (int i = 0; i < s; i++){//wrap around
                        if (items.get(i).toString().toLowerCase().startsWith(c)){
                            speciesChoiceBox.getSelectionModel().select(i);
                            return;
                        }
                    }
                }
            }
        });
        
        cdsTextField.addEventFilter(KeyEvent.KEY_TYPED, checkNumeric());
        
        
        cdsTextField.textProperty().addListener(
                (ObservableValue<? extends String> observable, final String oldValue, final String newValue) -> {
            //newValue = newValue.trim();
            Platform.runLater(() -> {
                sequenceTextField.setDisable(!newValue.isEmpty());
            });
        });
        
        sequenceTextField.textProperty().addListener(
                (ObservableValue<? extends String> observable, final String oldValue, final String newValue) -> {
            //newValue = newValue.trim();
            Platform.runLater(() -> {
                cdsTextField.setDisable(!newValue.isEmpty());
            });
        });
        saveMenuItem.setDisable(true);
        showResultsMenuItem.setDisable(true);
        showResultsMenuItem.setOnAction((ActionEvent e) -> {
            if (tableStage != null){
                if (!tableStage.isShowing()){
                    tableStage.show();
                }else{
                    tableStage.requestFocus();
                }
            }
        });
        quitMenuItem.setOnAction((ActionEvent e) -> {
            Platform.exit();
        });
        helpMenuItem.setOnAction(new EventHandler(){
            @Override
            public void handle (Event ev){
                showHelp();
            }
        });
        aboutMenuItem.setOnAction(new EventHandler(){
            @Override
            public void handle (Event ev){
                showAbout();
            }
        });
        ToggleGroup refseqToggleGroup = new ToggleGroup();
        refSeqMenu.setToggleGroup(refseqToggleGroup);
        refSeqOnlyMenu.setToggleGroup(refseqToggleGroup);
        noRefSeqMenu.setToggleGroup(refseqToggleGroup);
        
        runButton.setDefaultButton(true);
        runButton.setOnAction((ActionEvent actionEvent) -> {
            mapMutation();
        });
        getAvailableSpecies();
        
    }
    
    private void mapMutation() {
        final Task<List<MutationMapperResult>> mapperTask;
        final String gene = geneTextField.getText().trim();
        if (gene.isEmpty()){
            complainAndCancel("Please enter a gene symbol/id");
            return;
        }else if (gene.split("\\s+").length > 1){
            complainAndCancel("Please only enter one gene symbol/id");
            return;
        }
        if (cdsTextField.getText().trim().isEmpty() && 
                sequenceTextField.getText().trim().isEmpty()){
            complainAndCancel("Please enter coordinate or matching sequence to search");
            return;
        }
        String speciesSelection = (String) speciesChoiceBox.getSelectionModel().getSelectedItem();
        speciesSelection = speciesSelection.trim();
        //Some species common names do not work so get scientific name
        if (speciesTable.containsKey(speciesSelection)){
            speciesSelection = speciesTable.get(speciesSelection);
        }
        final String species = speciesSelection.replaceAll("\\s+", "_");       
        if (species.isEmpty()){
            complainAndCancel("You must select a species");
            return;
        }
        if (species.equalsIgnoreCase("Homo_sapiens") && grch37Menu.isSelected()){
            rest.setGRCh37Server();
        }else{
            rest.setDefaultServer();
        }
        
        final String cdsCoordinate = cdsTextField.getText().trim();
        final String sequence = sequenceTextField.getText().trim();
        if (!cdsCoordinate.isEmpty()){
            if (species.equalsIgnoreCase("Saccharomyces_cerevisiae")){
                Alert alert = new Alert(AlertType.ERROR);
                alert.getDialogPane().setPrefSize(420, 180);
                alert.setTitle("Mutation Mapper Error");
                alert.setHeaderText("Species Not Supported");
                alert.setContentText("CDS mapping not supported for this species." 
                        +" Please try using matching sequences instead");
                alert.setResizable(true);
                alert.showAndWait();
                Platform.runLater(() -> {
                    runButton.getScene().getWindow().requestFocus();
                });
                return;
            }
            if (!cdsTextField.getText().matches("\\d+([+-]\\d+)?+")){
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Mutation Mapper Error");
                alert.setHeaderText("CDS Input Error");
                alert.setContentText("CDS input must only be a whole number");
                System.out.println(alert.getContentText());
                alert.setResizable(true);
                alert.showAndWait();
                Platform.runLater(() -> {
                    runButton.getScene().getWindow().requestFocus();
                });
                return;
            }
            if (mutationTextField.getText().trim().isEmpty()){
                mapperTask = 
                        new Task<List<MutationMapperResult>>(){
                    @Override
                    protected List<MutationMapperResult> call() throws 
                            ParseException, MalformedURLException, IOException, InterruptedException {
                        updateProgress(-1, -1);
                        return codingToGenomic(gene, species, cdsCoordinate);
                    }
                };
            }else{
                final String mutSeq = mutationTextField.getText().replaceAll("[\\s]", "").toUpperCase();//remove whitespace chars
                    if (!mutSeq.isEmpty() && !cdsMutationIsOk(mutSeq)){
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.getDialogPane().setPrefSize(420, 180);
                        alert.setTitle("Mutation Mapper Error");
                        alert.setHeaderText("Mutation Sequence Error");
                        alert.setContentText("Mutation sequence must either be "
                                + "DNA or be in the format \"ins,<seq>\" or \"del,<seq/number>\".");
                        alert.setResizable(true);
                        System.out.println(alert.getContentText());
                        alert.showAndWait();
                        Platform.runLater(() -> {
                            runButton.getScene().getWindow().requestFocus();
                        });
                        return;
                    }

                mapperTask = new Task<List<MutationMapperResult>>(){
                    @Override
                    protected List<MutationMapperResult> call() throws 
                            ParseException, MalformedURLException, IOException, InterruptedException {
                        return codingToGenomic(gene, species, cdsCoordinate, mutSeq);
                    }
                };
            }
        }else{
            final String seq = sequence.replaceAll("[\\W]", "").toUpperCase();//remove non-word chars
            if (! sequenceIsDna(seq)){
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Mutation Mapper Error");
                alert.setHeaderText("Matching Sequence Error");
                alert.setContentText("Non-DNA characters found in Matching Sequence field");
                System.out.println(alert.getContentText());
                alert.setResizable(true);
                alert.showAndWait();
                Platform.runLater(() -> {
                    runButton.getScene().getWindow().requestFocus();
                });
                return;
            }
            if (mutationTextField.getText().trim().isEmpty()){
                mapperTask = new Task<List<MutationMapperResult>>() {
                    @Override
                    protected List<MutationMapperResult> call()
                            throws ParseException, MalformedURLException, IOException, InterruptedException{                        
                        updateProgress(-1, -1);
                        return sequenceToGenomic(gene, species, seq);
                    }
                };
                
            }else{
                final String mutSeq = mutationTextField.getText().replaceAll("[\\W]", "").toUpperCase();//remove non-word chars
                if (! sequenceIsDna(mutSeq)){
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Mutation Mapper Error");
                    alert.setHeaderText("Mutant Sequence Error");
                    alert.setContentText("Non-DNA characters found in Mutant Sequence field");
                    System.out.println(alert.getContentText());
                    alert.setResizable(true);
                    alert.showAndWait();
                    Platform.runLater(() -> {
                        runButton.getScene().getWindow().requestFocus();
                    });
                    return;
                }
                if (seq.equalsIgnoreCase(mutSeq)){
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Mutation Mapper Error");
                    alert.setHeaderText("Mutant Sequence Error");
                    alert.setContentText("Mutant Sequence is the same as Matching Sequence");
                    System.out.println(alert.getContentText());
                    alert.setResizable(true);
                    alert.showAndWait();
                    Platform.runLater(() -> {
                        runButton.getScene().getWindow().requestFocus();
                    });
                    return;
                }
                mapperTask = new Task<List<MutationMapperResult>>(){
                    @Override
                    protected List<MutationMapperResult> call()throws ParseException, 
                            MalformedURLException, IOException, InterruptedException {
                        updateProgress(-1, -1);
                        return sequenceToGenomic(gene, species, seq, mutSeq);
                
                    }
                };
            }
        }
        mapperTask.setOnSucceeded((WorkerStateEvent e) -> {
            Platform.runLater(() -> {
                setRunning(false);
                progressLabel.textProperty().unbind();
                progressLabel.setText("");
                progressIndicator.progressProperty().unbind();
                progressIndicator.progressProperty().set(0);
            });
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
                    resultView.canonicalOnlyMenu.selectedProperty().bindBidirectional(canonicalOnlyMenu.selectedProperty());
                    resultView.codingOnlyMenu.selectedProperty().bindBidirectional(codingOnlyMenu.selectedProperty());
                    resultView.refSeqOnlyMenu.selectedProperty().bindBidirectional(refSeqOnlyMenu.selectedProperty());
                    resultView.refSeqMenu.selectedProperty().bindBidirectional(refSeqMenu.selectedProperty());
                    saveMenuItem.disableProperty().bind(resultView.saveMenuItem.disableProperty());
                    saveMenuItem.setOnAction((ActionEvent actionEvent) -> {
                        resultView.saveMenuItem.fire();
                    });
                    showResultsMenuItem.setDisable(false);
                }
                if (tableScene == null){
                    tableScene = new Scene(tablePane);
                    tableStage = new Stage();
                    tableStage.setScene(tableScene);
                    tableStage.initModality(Modality.NONE);
                    tableStage.setResizable(true);
                }
                Platform.runLater(() -> {
                    resultView.displayData(results);
                    tableStage.setTitle("MutationMapper Results");
                    tableStage.getIcons().add(new Image(this.getClass()
                            .getResourceAsStream("icon.png")));
                    if (!tableStage.isShowing()){
                        tableStage.show();
                    }else{
                        tableStage.requestFocus();
                    }
                    FXMLLoader rstLoader = new FXMLLoader(this.getClass().
                                       getResource("ResultSummary.fxml"));
                    try{
                        Pane rstPane = (Pane) rstLoader.load();
                        ResultSummaryController rst = (ResultSummaryController) 
                                rstLoader.getController();
                        Scene rstScene = new Scene(rstPane);
                        Stage rstStage = new Stage();
                        rstStage.setScene(rstScene);
                        rstStage.setTitle("MutationMapper Results");
                        rstStage.getIcons().add(new Image(this.getClass()
                                .getResourceAsStream("icon.png")));
                        if (results.get(0).getMostSevereConsequence() == null){
                            rst.setMessage("Mapper Result");
                        }else{
                            rst.setMessage("Most Severe Consequence: " + 
                                    results.get(0).getMostSevereConsequence());
                        }
                        rst.setDetails(results.get(0).getDescription());
                        rstStage.initModality(Modality.APPLICATION_MODAL);
                        rstStage.show();
                    }catch(IOException ex){
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Mutation Mapper Error");
                        alert.setHeaderText("Error Displaying Summary");
                        alert.setContentText(ex.getMessage());
                        alert.setResizable(true);
                        System.out.println(alert.getContentText());
                        alert.showAndWait();
                    }
                });
            }catch(Exception ex){
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Mutation Mapper Error");
                alert.setHeaderText("Error Displaying Results");
                alert.setContentText(ex.getMessage());
                alert.setResizable(true);
                System.out.println(alert.getContentText());
                alert.showAndWait();
            }
        });
        
        mapperTask.setOnCancelled((WorkerStateEvent e) -> {
            Platform.runLater(() -> {
                setRunning(false);
                progressLabel.textProperty().unbind();
                progressLabel.setText("Cancelled");
                progressIndicator.progressProperty().unbind();
                progressIndicator.progressProperty().set(0);
                Platform.runLater(() -> {
                    runButton.getScene().getWindow().requestFocus();
                });
            });
        });
                 
         mapperTask.setOnFailed((WorkerStateEvent e) -> {
             Platform.runLater(() -> {
                setRunning(false);
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Mutation Mapper Error");
                alert.setHeaderText("Run Failed");
                alert.setContentText(e.getSource().getException().getMessage());
                alert.setResizable(true);
                System.out.println(alert.getContentText());
                alert.showAndWait();
                
                progressLabel.textProperty().unbind();
                progressLabel.setText("Failed!");
                progressIndicator.progressProperty().unbind();
                progressIndicator.progressProperty().set(0);
                Platform.runLater(() -> {
                    runButton.getScene().getWindow().requestFocus();
                });
             });
        });
        progressIndicator.progressProperty().bind(mapperTask.progressProperty());
        progressLabel.setText("Running");
        //progressLabel.textProperty().bind(mapperTask.messageProperty());
        runButton.setOnAction((ActionEvent actionEvent) -> {
            mapperTask.cancel();
        });
        new Thread(mapperTask).start();
        setRunning(true);
        
        
    }
    private List<MutationMapperResult> sequenceToGenomic(String gene, String species, 
            String seq)throws ParseException, MalformedURLException, 
            IOException, InterruptedException{
        return sequenceToGenomic(gene, species, seq, null);
    }
    
    private List<MutationMapperResult> sequenceToGenomic(String gene, String species, 
            String seq, String mutSeq)throws ParseException, MalformedURLException, 
            IOException, InterruptedException{
        List<MutationMapperResult> results = new ArrayList<>();
        List<TranscriptDetails> transcripts = new ArrayList<>();
        
        String chrom = null;
        Integer start = null;
        Integer end = null; 
        //updateMessage("Searching Genes");
        if (isTranscriptId(gene, species)){
            TranscriptDetails t = rest.getTranscriptDetails(gene);
            chrom = t.getChromosome();
            start = t.getTxStart();
            end = t.getTxEnd();
            transcripts.add(t);
        }else if (isRefSeqId(gene)){
            HashMap<String, String> ids = rest.getEnsemblFromRefSeqId(gene);
            if (ids.containsKey("transcript")){
                TranscriptDetails t = rest.getTranscriptDetails(ids.get("transcript"));
                chrom = t.getChromosome();
                start = t.getTxStart();
                end = t.getTxEnd();
                transcripts.add(t);
            }
        }else{
            GeneDetails g;
            if (isGeneId(gene, species)){
                g = rest.getGeneDetails(gene);
            }else{
                String id = rest.getGeneID(species, gene);
                g = rest.getGeneDetails(id);
            }
            chrom = g.getChromosome();
            start = g.getStart();
            end = g.getEnd();
            transcripts = g.getTranscripts();   
        }
        if (transcripts.isEmpty()){
            throw new RuntimeException("No transcripts identified for gene "
                    + "input " + gene);
        }
        //updateMessage("Found transcripts - getting DNA");
        String dna = getDna(chrom, start, end, species);
        //updateMessage("Searching DNA for input sequence...");
        boolean revCompMatches = false;
        boolean seqSwapped = false;
        List<Integer> indices = searchDna(dna, seq);
        List<Integer> rcIndices = searchDna(
                dna, ReverseComplementDNA.reverseComplement(seq));
        if (!rcIndices.isEmpty()){
            revCompMatches = true;
            indices.addAll(rcIndices);
        }
        if (indices.isEmpty()){
            if (mutSeq != null){
                indices = searchDna(dna, mutSeq);
                rcIndices = searchDna(dna, 
                        ReverseComplementDNA.reverseComplement(mutSeq));
                if (!rcIndices.isEmpty()){
                    revCompMatches = true;
                    indices.addAll(rcIndices);
                }
                if (!indices.isEmpty()){
                    seqSwapped = true;
                }
            }
        }
        if (indices.isEmpty()){
            throw new RuntimeException("No matches found for sequence or its "
                    + "reverse complement.");
        }
        if (indices.size() > 1){
            if (seqSwapped){
                throw new RuntimeException("ERROR: Sequence matches 0 times, mutated "
                        + "sequence matches multiple (" + indices.size() + ") times.");        
            }else{
                throw new RuntimeException("ERROR: Sequence matches multiple (" + 
                        indices.size() + ") times. Please try a longer sequence.");
            }
        }
        if (seqSwapped){
            String tempSeq = seq;
            seq = mutSeq;
            mutSeq = tempSeq;
        }
        //Calculate start and end coordinates of input sequence
        int matchPos;
        int matchEnd;
        /*  
            if using mutant seq report the pos of the trimmed REF allele, not 
            the position of the matching sequence. Store appropriate pos in 
            shiftedPos.

            we shouldn't report the span from the length of input sequence if 
            we've shifted our position genomic coordinate from the start of the 
            matching sequence, so use reportSpan boolean to specify appropriate
            behaviour for results.
        */
        int shiftedPos;
        boolean reportSpan = false;
        
        matchPos = indices.get(0) + start;
        matchEnd = matchPos + seq.length() - 1;
        
        HashMap<String, HashMap<String, String>> cons = new HashMap<>();
        HashMap<String, String> trim = new HashMap<>();
        if (mutSeq != null && !mutSeq.isEmpty()){//get mutation consequences
            //trim ref and alt alleles and get relative position of variant
            if (revCompMatches){
                trim = trimRefAlt(ReverseComplementDNA.reverseComplement(seq), 
                        ReverseComplementDNA.reverseComplement(mutSeq));
            }else{
                trim = trimRefAlt(seq, mutSeq);
            }
            shiftedPos = Integer.parseInt(trim.get("shift")) + matchPos;
            matchEnd = shiftedPos;
            //for a mutation we report the pos of the REF allele only, like in VCF format
            cons = getVepConsequences(chrom, shiftedPos, species, 
                    trim.get("ref"), trim.get("alt"));
        }else{
            shiftedPos = matchPos;//no mutant seq and no shift - report matching positions of seq
            reportSpan = true;
        }
        //updateMessage("Formatting results.");
        //ArrayList<String> t_ids = new ArrayList<>();
        for (TranscriptDetails t: transcripts){
            StringBuilder description = new StringBuilder();
            MutationMapperResult result = putBasicTranscriptInfo(t);
            result.setReportSpan(reportSpan);
            result.setHostServices(getHostServices());
            result.setSpecies(species);
            if ( species.equalsIgnoreCase("homo_sapiens") && grch37Menu.isSelected()){
                result.setEnsemblSite("http://grch37.ensembl.org/");
            }
            if (cons.containsKey("Consequence")){
                result.setMostSevereConsequence(
                        cons.get("Consequence").get("most_severe_consequence"));
            }
            if (seqSwapped){
                description.append("Mutant sequence matches, matching sequence "
                        + "does not. Sequences swapped. ");
            }
            if (revCompMatches){
                description.append("Reverse complement matched ");
            }else{
                description.append("Sequence matched ");
            }
            description.append("at genomic coordinate ").append(chrom)
                    .append(":").append(matchPos).append(" (")
                    .append(t.getGenomeBuild()).append(")\n");
            result.setDescription(description.toString());
            if (!trim.isEmpty()){
                result.setRefAllele(trim.get("ref"));
                result.setVarAllele(trim.get("alt"));
            }else{
                if (revCompMatches){
                    result.setRefAllele(ReverseComplementDNA.reverseComplement(seq));
                }else{
                    result.setRefAllele(seq);
                }
            }
            
            // calculate CDS position from genomic position for each transcript
            String cds_pos_match = t.getCdsPosition(chrom, shiftedPos);
            String cds_pos_end = t.getCdsPosition(chrom, matchEnd);
            if (cds_pos_match.equals(cds_pos_end)){
                /*
                this should only occur if we haven't got a position but instead
                transcript is non-coding or seq is outside transcribed sites
                */
                result.setCdsCoordinate(cds_pos_match);
            }else{
                result.setCdsCoordinate(String.format("%s-%s", cds_pos_match, cds_pos_end));
            }
            result.setGenome(t.getGenomeBuild());
            result.setChromosome(chrom);
            result.setCoordinate(shiftedPos);
            result.setMatchingSequence(seq);
            result.setMutation(mutSeq);
            addVepConsequenceToMutationMapperResult(result, t, cons);
            results.add(result);
            //t_ids.add(t.getTranscriptId());
        }
        return results;
    }
    
    private List<MutationMapperResult> codingToGenomic(String gene, String species, 
            String cdsCoordinate) throws ParseException, MalformedURLException, IOException, InterruptedException{
        return  codingToGenomic(gene, species, cdsCoordinate, null);
    }
    
    private List<MutationMapperResult> codingToGenomic(String gene, String species, 
            String cdsCoordinate, String mutSeq) throws ParseException, 
            MalformedURLException, IOException, InterruptedException{
        //TO DO implement mutation checking!
        
        List<MutationMapperResult> results = new ArrayList<>();
        List<TranscriptDetails> transcripts = getTranscriptsForGene(gene, species);
        
        if (transcripts.isEmpty()){
            //we should have already thrown an error from EnsemblRests methods
            throw new RuntimeException(String.format("Could not get transcripts "
                  + "for %s in species %s.\n", gene, species));
        }
        int spliceCoord = 0;
        int cdsCoord;
        if (cdsCoordinate.matches("\\d+\\+\\d+")){
            String[] c = cdsCoordinate.split("\\+");
            cdsCoord = Integer.parseInt(c[0]);
            spliceCoord = Integer.parseInt(c[1]);
        }else if (cdsCoordinate.matches("\\d+-\\d+")){
            String[] c = cdsCoordinate.split("-");
            cdsCoord = Integer.parseInt(c[0]);
            spliceCoord = -1 * Integer.parseInt(c[1]);
        }else{
            cdsCoord = Integer.parseInt(cdsCoordinate);
        }
        ArrayList<String> consequences = new ArrayList<>();
        StringBuilder description = new StringBuilder();
        for (TranscriptDetails t: transcripts){
            HashMap<String, String> g = rest.codingToGenomicTranscript(
                    species, t.getTranscriptId(), cdsCoord);
            MutationMapperResult result = putBasicTranscriptInfo(t);
            result.setHostServices(getHostServices());
            result.setSpecies(species);
            if ( species.equalsIgnoreCase("homo_sapiens") && grch37Menu.isSelected()){
                result.setEnsemblSite("http://grch37.ensembl.org/");
            }
            if (g != null){
                result.setCdsCoordinate(cdsCoordinate);
                result.setChromosome(g.get("chromosome"));
                if (! g.get("coordinate").isEmpty()){
                    int c = Integer.parseInt(g.get("coordinate"));
                    if (t.getStrand() < 0){
                        c -= spliceCoord;
                    }else{
                        c += spliceCoord;
                    }
                    result.setCoordinate(c);
                }
                result.setGenome(g.get("assembly"));
                if (refSeqMenu.isSelected() || refSeqOnlyMenu.isSelected()){
                    description.append(result.getRefSeqIfAvailable());
                }else{
                    description.append(t.getTranscriptId());
                }
                description.append(" c.")
                        .append(cdsCoordinate).append(" => ")
                        .append(result.getGenomicCoordinate()).append("\n");
            }
            if (mutSeq != null && result.getCoordinate() != null){
                /*
                we use a rather convoluted means of getting VEP consequence so
                that we can use the regions POST method as other methods don't support
                more complex var alleles
                */
                List<String> alleles = getCdsVarAlleles(t, species, 
                        result.getCoordinate(), mutSeq);
                // TO DO check deleted allele matches if supplied
                int genomicCoordinate = result.getCoordinate();
                if (t.getStrand() < 0){
                    genomicCoordinate -= getMutationSpan(mutSeq);
                }else if (mutSeq.matches("(?i)del,[\\dACTG]+")){
                    //for deletions we will have retrieved preceding base for the Ref allele
                    genomicCoordinate -= 1;
                }
                HashMap<String, String> trim = trimRefAlt(alleles.get(2), alleles.get(3));
                result.setRefAllele(trim.get("ref"));
                result.setVarAllele(trim.get("alt"));
                HashMap<String, HashMap<String, String>> cons = 
                        getVepConsequences(t.getChromosome(), 
                                Integer.parseInt(trim.get("shift")) + genomicCoordinate, 
                                species, trim.get("ref"), trim.get("alt"));
                addVepConsequenceToMutationMapperResult(result, t, cons);
            }else if (result.getCoordinate() != null){
                String gRef = getDna(t.getChromosome(), result.getCoordinate(), 
                        result.getCoordinate(), species, 1);
                result.setRefAllele(gRef);
            }
            if (result.getConsequence() != null){
                consequences.addAll(Arrays.asList(result.getConsequence().split(",")));
            }
            results.add(result);
        }
        consequences.sort(new ConsequenceComparator());
        for (MutationMapperResult r: results){
            r.setDescription(description.toString());
            if (!consequences.isEmpty()){
                r.setMostSevereConsequence(consequences.get(0));
            }
        }
        return results;
    }
    
    private void addVepConsequenceToMutationMapperResult(MutationMapperResult r,
            TranscriptDetails t, HashMap<String, HashMap<String, String>> cons){
        if (!cons.isEmpty()){
            if (cons.containsKey(t.getTranscriptId())){
                if (cons.get(t.getTranscriptId()).containsKey("hgvsc")){
                    r.setCdsConsequence(cons.get(t.getTranscriptId()).get("hgvsc"));
                }
                if (cons.get(t.getTranscriptId()).containsKey("hgvsp")){
                    String pCons = cons.get(t.getTranscriptId()).get("hgvsp").replaceAll("%3D", "=");
                    r.setProteinConsequence(pCons);
                }
                if(cons.get(t.getTranscriptId()).containsKey("polyphen_prediction")
                 && cons.get(t.getTranscriptId()).containsKey("polyphen_score")){
                    r.setPolyphenResult(String.format("%s (%s)", 
                            cons.get(t.getTranscriptId()).get("polyphen_prediction"),
                            cons.get(t.getTranscriptId()).get("polyphen_score")));
                }
                if(cons.get(t.getTranscriptId()).containsKey("sift_prediction")
                 && cons.get(t.getTranscriptId()).containsKey("sift_score")){
                    r.setSiftResult(String.format("%s (%s)", 
                            cons.get(t.getTranscriptId()).get("sift_prediction"),
                            cons.get(t.getTranscriptId()).get("sift_score")));
                }
                if (cons.get(t.getTranscriptId()).containsKey("consequence_terms")){
                    r.setConsequence(cons.get(t.getTranscriptId()).get("consequence_terms").
                            replaceAll("[\\[\\]\"]", ""));
                }
                /*if (cons.get(t.getTranscriptId()).containsKey("refseq_transcript_ids")){
                    r.setRefSeqIds(cons.get(t.getTranscriptId()).get("refseq_transcript_ids").
                            replaceAll("[\\[\\]\"]", ""));
                }
                if (cons.get(t.getTranscriptId()).containsKey("canonical")){
                    if (Integer.parseInt(cons.get(t.getTranscriptId()).get("canonical")) > 0){
                        r.setIsCanonical(true);
                    }else{
                        r.setIsCanonical(false);
                    }
                }*/
                if (cons.get(t.getTranscriptId()).containsKey("exon")){
                    r.setExonIntronNumber("exon " + cons.get(t.getTranscriptId()).get("exon"));
                }else if (cons.get(t.getTranscriptId()).containsKey("intron")){
                    r.setExonIntronNumber("intron " + cons.get(t.getTranscriptId()).get("intron"));
                }
                
                
                StringBuilder snpIds = new StringBuilder();
                for (String k: cons.keySet()){
                    if (k.startsWith("snps_")){
                        if(snpIds.length() > 0){
                            snpIds.append("/");
                        }
                        snpIds.append(cons.get(k).get("id"));
                        if (cons.get(k).containsKey("clin_sig")){
                            if (!cons.get(k).get("clin_sig").isEmpty()){
                                snpIds.append(" (clin_sig=").append(cons.get(k)
                                        .get("clin_sig").replaceAll("[\\[\\]\"]", "")
                                ).append(")");
                            }
                        }
                    }else if (k.equals("Consequence")){
                        r.setMostSevereConsequence(cons.get(k).get("most_severe_consequence"));
                    }
                }
                if (snpIds.length() > 0){
                    r.setKnownIds(snpIds.toString());
                }
                //r.setRefAllele(cons.get(t.getTranscriptId()).get("ref"));
                //r.setVarAllele(cons.get(t.getTranscriptId()).get("alt"));
            }
        }
    }
    
    private int getMutationSpan(String mut){
        if (mut.matches("(?i)del,[ACTG]+")){
            String[] split = mut.split(",");
            return split[1].length() ;
        }else if (mut.matches("(?i)del,\\d+")){
            String[] split = mut.split(",");
            return Integer.parseInt(split[1]); 
        }else if (mut.matches("(?i)[ACTG]+")){
            return mut.length() - 1;
        }
        return 0;
    }
    
    
    /*
    returns a list, in order of: 
        CDS reference allele
        CDS variant allele
        Genomic reference allele
        Genomic variant allele
    */
    private List<String> getCdsVarAlleles(TranscriptDetails t, String species, int genomicPos, String mut)
            throws ParseException, MalformedURLException, IOException, InterruptedException{
        int span = getMutationSpan(mut);
        Integer strand = t.getStrand();
        if (strand == null){
            strand = 1; 
        }
        String cdsRef;
        String cdsAlt;
        String gRef;
        String gAlt;
        if (strand < 0){
            //cdsRef = getDna(t.getChromosome(), genomicPos - span, genomicPos, species, -1);
            gRef = getDna(t.getChromosome(), genomicPos - span, genomicPos, species, 1);
            cdsRef = ReverseComplementDNA.reverseComplement(gRef);
        }else{
            if (mut.matches("(?i)del,[\\dACTG]+")){
                //for deletions get preceding base to allow VCF style Ref/Alt alleles
                gRef = getDna(t.getChromosome(), genomicPos - 1, genomicPos + span - 1, species, 1);    
            }else{
                gRef = getDna(t.getChromosome(), genomicPos, genomicPos + span, species, 1);
            }
            cdsRef = gRef;
        }
        if (mut.matches("(?i)del,[\\dACTG]+")){
            cdsAlt = cdsRef.substring(0, 1);
            gAlt = gRef.substring(0, 1);
        }else if (mut.matches("(?i)ins,[ACTG]+")){
            String[] split = mut.split(",");
            cdsAlt = cdsRef + split[1];
            if (strand < 0){
                gAlt = ReverseComplementDNA.reverseComplement(cdsAlt);
            }else{
                gAlt = cdsAlt;
            }
        }else{
            cdsAlt = mut;
            if (strand < 0){
                gAlt = ReverseComplementDNA.reverseComplement(mut);
            }else{
                gAlt = mut;
            }
        }
        return Arrays.asList(cdsRef, cdsAlt, gRef, gAlt);
    }
    
    private HashMap<String, String> trimRefAlt(String ref, String alt){
        HashMap<String, String> trimmed = new HashMap<>();
        String refTrim = ref.toUpperCase();
        String altTrim = alt.toUpperCase();
        int posShift = 0;//need to shunt coordinate up by one for every character trimmed from start of seq
        // trim identical suffixes
        while (refTrim.length() > 1 && altTrim.length() > 1 && 
                refTrim.substring(refTrim.length()-1).equals(
                altTrim.substring(altTrim.length()-1))){
            refTrim = refTrim.substring(0, refTrim.length() -1);
            altTrim = altTrim.substring(0, altTrim.length() -1);
        }
        // trim identical prefixes
        while (refTrim.length() > 1 && altTrim.length() > 1 && 
                refTrim.substring(0, 1).equals(altTrim.substring(0, 1))){
            refTrim = refTrim.substring(1);
            altTrim = altTrim.substring(1);
            posShift++;
        }
        trimmed.put("ref", refTrim);
        trimmed.put("alt", altTrim);
        trimmed.put("shift", String.valueOf(posShift));
        return trimmed;
    }
    
    private HashMap<String, HashMap<String, String>> getVepConsequences(String chrom, int pos, 
            String species, String ref, String alt)throws ParseException, 
            MalformedURLException, IOException, InterruptedException {
        /*
        reduce sequences to simplest possible representations before submitting to VEP
        */
        
        HashMap<String, HashMap<String, String>> results = 
                rest.getVepConsequence(chrom, pos, species, ref, alt);
        return results;
        
    }
    
    private String getDna(String chrom, int start, int end, String species)
            throws ParseException, MalformedURLException, IOException, InterruptedException{
        return rest.getDna(chrom, start, end, species);
    }
    
    private String getDna(String chrom, int start, int end, String species, int strand)
            throws ParseException, MalformedURLException, IOException, InterruptedException{
        return rest.getDna(chrom, start, end, species, strand);
    }
    
    private List<Integer> searchDna(String dna, String seq){
        String lcDna = dna.toLowerCase();
        String lcSeq = seq.toLowerCase();
        int index = lcDna.indexOf(lcSeq);
        List<Integer> indices = new ArrayList<>();
        while (index >= 0){
            indices.add(index);
            index = lcDna.indexOf(lcSeq, index + 1);
        }
        return indices;
    }
    
    
    private MutationMapperResult putBasicTranscriptInfo(TranscriptDetails t)
            throws ParseException, MalformedURLException, IOException, InterruptedException{
        MutationMapperResult result = new MutationMapperResult();
        
        result.setGeneSymbol(t.getSymbol());
        result.setGeneId(t.getId());
        result.setTranscript(t.getTranscriptId());
        result.setBiotype(t.getBiotype());
        result.setIsCanonical(t.getIsCanonical());
        result.setRefSeqIds(String.join("\n", rest.getRefSeqIds(t.getTranscriptId()) ) );
        return result;
    }
    
    private List<TranscriptDetails>  getTranscriptsForGene(String gene, String species) 
            throws ParseException, MalformedURLException, IOException, InterruptedException{
        List<TranscriptDetails> transcripts = new ArrayList<>();
        String id;
        if(isTranscriptId(gene, species)){
            transcripts.add(rest.getTranscriptDetails(gene));
        }else if (isRefSeqId(gene)){
            HashMap<String, String> ids = rest.getEnsemblFromRefSeqId(gene);
            if (ids.containsKey("transcript")){
                transcripts.add(rest.getTranscriptDetails(ids.get("transcript")));
            }
        }else{
            if (isGeneId(gene, species)){
                id = gene;
            }else{//assume gene symbol
                id = rest.getGeneID(species, gene);
            }
            transcripts = rest.getGeneDetails(id).getTranscripts();
        }
        return transcripts;
    }
    
    private boolean cdsMutationIsOk(String seq){
        if (seq == null || seq.isEmpty()){
            return false;
        }
        String[] split = seq.split(",");
        if (split.length > 2){
            return false;
        }
        if (split.length == 1){
            return sequenceIsDna(seq);
        }
        if (split.length == 2){
            if (!split[0].matches("(?i)ins|del")){
                return false;
            }
            if (sequenceIsDna(split[1])){
                return true;
            }else{
                if (split[0].equalsIgnoreCase("del")){
                    return split[1].matches("\\d+");
                }
                return false;
            }
        }
        return false;//should only get here if seq is a , (?)
    }
    
    private boolean sequenceIsDna(String seq){
        return seq.matches("(?i)[ACTG]+");
    }
    
    private boolean isTranscriptId(String id, String species){
        if (species.equalsIgnoreCase("drosophila_melanogaster") 
                && id.matches("FBtr\\d+")){
            return true;
        }
        if (species.equalsIgnoreCase("Saccharomyces_cerevisiae") && id.matches("Y\\w{2}\\d{3}\\w")){
            return true;
        }
        return id.matches("ENS\\w*T\\d{11}.*\\d*");
    }
    
    private boolean isGeneId(String id, String species){
        if (species.equalsIgnoreCase("drosophila_melanogaster") && id.matches("FBgn\\d+")){
            return true;
        }
        if (species.equalsIgnoreCase("Caenorhabditis_elegans") && id.matches("WBGene\\d+")){
            return true;
        }
        return id.matches("ENS\\w*G\\d{11}.*\\d*");
    }
    private boolean isRefSeqId(String id){//tests for both protein or RNA IDs
        return id.matches("[NX][MRP]_\\d+(.\\d+)*");
    }
    
    
    private void getAvailableSpecies(){
        setRunning(true);
        progressLabel.textProperty().unbind();
        progressLabel.setText("Retrieving available species...");
        progressIndicator.progressProperty().unbind();
        progressIndicator.progressProperty().set(-1);
        final Task<HashMap<String, String> > getSpeciesTask = new Task<HashMap<String, String>>(){
            @Override
            protected HashMap<String, String> call() 
                    throws ParseException, MalformedURLException, IOException, InterruptedException{
                System.out.println("Getting species.");
                HashMap<String, String> species = rest.getAvailableSpecies();
                return species;
            }
        };
        
        getSpeciesTask.setOnSucceeded((WorkerStateEvent e) -> {
            speciesTable = (HashMap<String, String> ) e.getSource().getValue();
            final ArrayList<String> names = new ArrayList<>();
            names.addAll(speciesTable.keySet());
            SpeciesComparator comp = new SpeciesComparator();
            Collections.sort(names, comp);        
            Platform.runLater(() -> {
                speciesChoiceBox.getItems().clear();
                speciesChoiceBox.getItems().addAll(names);
                speciesChoiceBox.getSelectionModel().selectFirst();
            });
            setRunning(false);
            progressLabel.textProperty().unbind();
            progressLabel.setText("");
            progressIndicator.progressProperty().unbind();
            progressIndicator.progressProperty().set(0);
        });
        getSpeciesTask.setOnCancelled((WorkerStateEvent e) -> {
            setRunning(false);
            progressLabel.textProperty().unbind();
            progressLabel.setText("Cancelled species retrieval");
            progressIndicator.progressProperty().unbind();
            progressIndicator.progressProperty().set(0);
            showNoSpeciesError("User cancelled species retrieval. Mutation Mapper"
                    + " must connect to Ensembl's REST server to identify "
                    + "available species for mapping.");
        });
        getSpeciesTask.setOnFailed((WorkerStateEvent e) -> {
            setRunning(false);
            progressLabel.textProperty().unbind();
            progressLabel.setText("Failed to retrieve species");
            progressIndicator.progressProperty().unbind();
            progressIndicator.progressProperty().set(0);
            e.getSource().getException().printStackTrace();
            showNoSpeciesError(e.getSource().getException());
        });
        runButton.setOnAction((ActionEvent actionEvent) -> {
            getSpeciesTask.cancel();
        });
        new Thread(getSpeciesTask).start();
    }
    
    public void complainAndCancel(String msg){
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Mutation Mapper Error");
        alert.setHeaderText(msg);
        alert.setContentText("Please correct this error and try again.");
        alert.setResizable(true);
        alert.showAndWait();
    }
    
    private void canRun(boolean can){
        //runButton.setDisable(!can);
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
            cdsTextField.setDisable(!sequenceTextField.getText().isEmpty());
            String species = (String) speciesChoiceBox.getSelectionModel().getSelectedItem();
            if (species != null && species.equalsIgnoreCase("Human") ){
                grch37Menu.setDisable(false);
            }
        }
        if (running){
            grch37Menu.setDisable(true);
            runButton.setText("Cancel");
            runButton.setDefaultButton(false);
            runButton.setCancelButton(true);
        }else{
            runButton.setText("Run");
            runButton.setDefaultButton(true);
            runButton.setCancelButton(false);
            runButton.setOnAction((ActionEvent actionEvent) -> {
                mapMutation();
            });
        }
    }
                
    EventHandler<KeyEvent> checkNumeric(){
        return (KeyEvent ke) -> {
            if (!ke.getCharacter().matches("[\\d-+]")){
                ke.consume();
            }
        };
    }
    
    private void showNoSpeciesError(String msg){
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.getDialogPane().setPrefSize(420, 200);
        ButtonType cButton = ButtonType.CANCEL;
        ButtonType okButton = ButtonType.OK;
        alert.getButtonTypes().setAll(okButton, cButton);
        alert.setTitle("Mutation Mapper Error");
        alert.setHeaderText("Error retrieving species.");
        alert.setContentText(msg + " Click OK to attempt connecting again or "
                + "Cancel to quit.");
        Optional<ButtonType> response = alert.showAndWait();
        if (response.get() == okButton){
            getAvailableSpecies();
        }else{
            Platform.exit();
        }
    }    
    
    private void showNoSpeciesError(Throwable ex){
        // Create expandable Exception.
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.getDialogPane().setPrefSize(420, 200);
        ButtonType cButton = ButtonType.CANCEL;
        ButtonType okButton = ButtonType.OK;
        alert.getButtonTypes().setAll(okButton, cButton);
        alert.setTitle("Mutation Mapper Error");
        alert.setHeaderText("Error retrieving species.");
        alert.setContentText("Exception encountered when attempting "
                    + "to retrieve available species. Check your internet "
                + "connection and select OK to try again or Cancel to quit.");
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
        alert.setResizable(true);
        Optional<ButtonType> response = alert.showAndWait();
        if (response.get() == okButton){
            getAvailableSpecies();
        }else{
            Platform.exit();
        }
    }    
    
    public void showHelp(){
        try{
            File instructionsPdf = File.createTempFile("MutationMapper_Instructions", ".pdf" );
            instructionsPdf.deleteOnExit();
            InputStream inputStream = this.getClass().
                    getResourceAsStream("instructions.pdf");
            OutputStream outputStream = new FileOutputStream(instructionsPdf);
            int read = 0;
            byte[] bytes = new byte[1024];    
            while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
            }
            inputStream.close();
            outputStream.close();
            openFile(instructionsPdf);
        }catch(IOException ex){
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Mutation Mapper Error");
            alert.setHeaderText("Could not open instructions PDF");
            alert.setContentText(ex.getMessage());
            System.out.println(alert.getContentText());
            alert.setResizable(true);
            alert.showAndWait();
        }
    }
    
    private void openFile(File f) throws IOException{
        String command;
        //Desktop.getDesktop().open(f);
        if (System.getProperty("os.name").equals("Linux")) {
            command = "xdg-open " + f;
        }else if (System.getProperty("os.name").equals("Mac OS X")) {
            command = "open " + f;
        }else if (System.getProperty("os.name").contains("Windows")){
            command = "cmd /C start " + f;
        }else {
            return;
        }
        Runtime.getRuntime().exec(command);
    }
    
    public void showAbout(){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("about.fxml"));
            Pane page = (Pane) loader.load();
            Scene scene = new Scene(page);
            Stage stage = new Stage();
            stage.setScene(scene);
            //scene.getStylesheets().add(AutoPrimer3.class
            //            .getResource("autoprimer3.css").toExternalForm());
            AboutController controller = loader.getController();
            controller.setVersion(VERSION);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(this.getClass().
                    getResourceAsStream("icon.png")));
            stage.setTitle("About MutationMapper");
            
            stage.show();
        }catch(IOException ex){
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Mutation Mapper Error");
            alert.setHeaderText("Could not display about dialog");
            alert.setContentText(ex.getMessage());
            ex.printStackTrace();
            alert.setResizable(true);
            alert.showAndWait();
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
