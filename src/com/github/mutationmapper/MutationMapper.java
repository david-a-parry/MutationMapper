/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/*
TO DO
Make about dialog
Add some missing error dialogs
Handle no internet connection
Write manual

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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
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
    ProgressIndicator progressIndicator;
    @FXML
    Button runButton;
    @FXML
    MenuItem saveMenuItem;
    @FXML
    MenuItem quitMenuItem;
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
        tableLoader = new FXMLLoader(getClass().
                                       getResource("MutationMapperResultView.fxml"));
        Platform.runLater(() -> {
            geneTextField.requestFocus();
        });
        speciesChoiceBox.getSelectionModel().selectedItemProperty().addListener(
            (new ChangeListener<String>(){
                @Override
                public void changed (ObservableValue ov, String value, final String new_value){ 
                    if (new_value.equalsIgnoreCase("Human")){
                        grch37Menu.setDisable(false);
                    }else{
                        grch37Menu.setDisable(true);
                    }
                }
            })
        );
        
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
        saveMenuItem.setDisable(false);
        quitMenuItem.setOnAction((ActionEvent e) -> {
            Platform.exit();
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
            //TO DO please enter gene dialog
            return;
        }
        if (cdsTextField.getText().trim().isEmpty() && 
                sequenceTextField.getText().trim().isEmpty()){
            //TO DO please ender coordinate or matching sequence dialog
            return;
        }
        final String species = (String) speciesChoiceBox.getSelectionModel().getSelectedItem();
        if (species.isEmpty()){
            //TO DO you must select a species dialog
            return;
        }
        if (species.equalsIgnoreCase("Human") && grch37Menu.isSelected()){
            rest.setGRCh37Server();
        }else{
            rest.setDefaultServer();
        }
        
        final String cdsCoordinate = cdsTextField.getText().trim();
        final String sequence = sequenceTextField.getText().trim();
        if (!cdsCoordinate.isEmpty()){
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
                        alert.setTitle("Mutation Mapper Error");
                        alert.setHeaderText("Mutation Sequence Error");
                        alert.setContentText("Mutation sequence must either be "
                                + "DNA or be in the format \"ins,<seq>\" or \"del,<seq/number>\".");
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
                }
                if (tableScene == null){
                    tableScene = new Scene(tablePane);
                    tableStage = new Stage();
                    tableStage.setScene(tableScene);
                    tableStage.initModality(Modality.NONE);
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
        progressLabel.textProperty().bind(mapperTask.messageProperty());
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
        if (isTranscriptId(gene)){
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
            if (isGeneId(gene)){
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
                throw new RuntimeException("ERROR: Seq matches 0 times, mutated "
                        + "sequence matches multiple (" + indices.size() + ") times.");        
            }else{
                throw new RuntimeException("ERROR: Seq matches multiple (" + 
                        indices.size() + ") times.");
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
            cons = getVepConsequences(chrom, matchPos + Integer.parseInt(trim.get("shift")), 
                    species, trim.get("ref"), trim.get("alt"));
        }
        //updateMessage("Formatting results.");
        //ArrayList<String> t_ids = new ArrayList<>();
        for (TranscriptDetails t: transcripts){
            StringBuilder description = new StringBuilder();
            MutationMapperResult result = putBasicTranscriptInfo(t);
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
            String cds_pos_match = t.getCdsPosition(chrom, matchPos);
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
            result.setCoordinate(matchPos);
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
                                snpIds.append(" (clin_sig=").append(cons.get(k).get("clin_sig")).append(")");
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
            gRef = getDna(t.getChromosome(), genomicPos, genomicPos + span, species, 1);
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
        if(isTranscriptId(gene)){
            transcripts.add(rest.getTranscriptDetails(gene));
        }else if (isRefSeqId(gene)){
            HashMap<String, String> ids = rest.getEnsemblFromRefSeqId(gene);
            if (ids.containsKey("transcript")){
                transcripts.add(rest.getTranscriptDetails(ids.get("transcript")));
            }
        }else{
            if (isGeneId(gene)){//is gene id
                id = gene;
            }else{
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
    
    private boolean isTranscriptId(String id){
        return id.matches("ENS\\w*T\\d{11}.*\\d*"); 
    }
    private boolean isGeneId(String id){
        return id.matches("ENS\\w*G\\d{11}.*\\d*");
    }
    private boolean isRefSeqId(String id){//tests for both protein or RNA IDs
        return id.matches("[NX][MRP]_\\d+(.\\d+)*");
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
        
        getSpeciesTask.setOnSucceeded((WorkerStateEvent e) -> {
            List<String> species = (List<String>) e.getSource().getValue();
            Platform.runLater(() -> {
                speciesChoiceBox.getItems().clear();
                speciesChoiceBox.getItems().addAll(species);
                speciesChoiceBox.getSelectionModel().selectFirst();
            });
        });
        getSpeciesTask.setOnCancelled(null);
        getSpeciesTask.setOnFailed((WorkerStateEvent e) -> {
            //TO DO - ERROR DIALOG
            e.getSource().getException().printStackTrace();
        });
        new Thread(getSpeciesTask).start();
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
            if (species.equalsIgnoreCase("Human")){
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
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
