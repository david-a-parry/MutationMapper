/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/*
TO DO
give option to only output protein coding transcript results
give option to translate ensembl IDs to RefSeq where possible
maybe give option to automatically remove non-DNA characters from input boxes

CDS -> mutation 
sequence -> mutation
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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
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
    MenuItem quitMenuItem;
    
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
            
        } catch (Exception ex) {
            Logger.getLogger(MutationMapper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tableLoader = new FXMLLoader(getClass().
                                       getResource("MutationMapperResultView.fxml"));
        Platform.runLater(() -> {
            geneTextField.requestFocus();
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
        
        quitMenuItem.setOnAction((ActionEvent e) -> {
            Platform.exit();
        });
        
        runButton.setDefaultButton(true);
        runButton.setOnAction((ActionEvent actionEvent) -> {
            mapMutation();
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
            if (!cdsTextField.getText().matches("\\d+")){
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Mutation Mapper Error");
                alert.setHeaderText("CDS Input Error");
                alert.setContentText("CDS input must only be a whole number");
                alert.showAndWait();
                return;
            }
            if (mutationTextField.getText().isEmpty()){
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
                        alert.showAndWait();
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
                alert.showAndWait();
                return;
            }
            if (mutationTextField.getText().isEmpty()){
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
                    alert.showAndWait();
                    return;
                }
                if (seq.equalsIgnoreCase(mutSeq)){
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Mutation Mapper Error");
                    alert.setHeaderText("Mutant Sequence Error");
                    alert.setContentText("Mutant Sequence is the same as Matching Sequence");
                    alert.showAndWait();
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
                }
                if (tableScene == null){
                    tableScene = new Scene(tablePane);
                    tableStage = new Stage();
                    tableStage.setScene(tableScene);
                    tableStage.initModality(Modality.NONE);
                }Platform.runLater(() -> {
                    resultView.displayData(results);
                    tableStage.setTitle("MutationMapper Results");
                    tableStage.getIcons().add(new Image(this.getClass()
                            .getResourceAsStream("icon.png")));
                    if (!tableStage.isShowing()){
                        tableStage.show();
                    }else{
                        tableStage.requestFocus();
                    }
                });
            }catch(Exception ex){
                //TO DO - show error dialog
                ex.printStackTrace();
            }
        });
        
        mapperTask.setOnCancelled((WorkerStateEvent e) -> {
            Platform.runLater(() -> {
                setRunning(false);
                progressLabel.textProperty().unbind();
                progressLabel.setText("Cancelled");
                progressIndicator.progressProperty().unbind();
                progressIndicator.progressProperty().set(0);
            });
        });
                 
         mapperTask.setOnFailed((WorkerStateEvent e) -> {
             Platform.runLater(() -> {
                setRunning(false);
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Mutation Mapper Error");
                alert.setHeaderText("Run Failed");
                alert.setContentText(e.getSource().getException().getMessage());
                alert.showAndWait();
                
                progressLabel.textProperty().unbind();
                progressLabel.setText("Failed!");
                progressIndicator.progressProperty().unbind();
                progressIndicator.progressProperty().set(0);
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
        String chrom;
        int start;
        int end; 
        //updateMessage("Searching Genes");
        if (isTranscriptId(gene)){
            TranscriptDetails t = rest.getTranscriptDetails(gene);
            chrom = t.getChromosome();
            start = t.getTxStart();
            end = t.getTxEnd();
            transcripts.add(t);
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
        List<Integer> indices = searchDna(dna, seq);
        List<Integer> rcIndices = searchDna(
                dna, ReverseComplementDNA.reverseComplement(seq));
        if (!rcIndices.isEmpty()){
            revCompMatches = true;
            indices.addAll(rcIndices);
        }
        if (indices.isEmpty()){
            throw new RuntimeException("No matches found for sequence or its "
                    + "reverse complement.");
        }
        if (indices.size() > 1){
            throw new RuntimeException("ERROR: Seq matches multiple (" + indices.size() + ") times.");
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
            MutationMapperResult result = putBasicTranscriptInfo(t);
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
        for (TranscriptDetails t: transcripts){
            HashMap<String, String> g = rest.codingToGenomicTranscript(
                    species, t.getTranscriptId(), Integer.parseInt(cdsCoordinate));
            MutationMapperResult result = putBasicTranscriptInfo(t);
            if (g != null){
                result.setCdsCoordinate(cdsCoordinate);
                result.setChromosome(g.get("chromosome"));
                if (! g.get("coordinate").isEmpty()){
                    result.setCoordinate(Integer.parseInt(g.get("coordinate")));
                }
                result.setGenome(g.get("assembly"));
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
            }
            results.add(result);
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
                            replaceAll("[\\[\\]]", ""));
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
                        r.setMostSeverConsequence(cons.get(k).get("most_severe_consequence"));
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
            cdsRef = getDna(t.getChromosome(), genomicPos - span, genomicPos, species, -1);
            gRef = getDna(t.getChromosome(), genomicPos - span, genomicPos, species, 1);
        }else{
            cdsRef = getDna(t.getChromosome(), genomicPos, genomicPos + span, species, 1);
            gRef = cdsRef;
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
    
    
    private MutationMapperResult putBasicTranscriptInfo(TranscriptDetails t){
        MutationMapperResult result = new MutationMapperResult();
        result.setGeneSymbol(t.getSymbol());
        result.setGeneId(t.getId());
        result.setTranscript(t.getTranscriptId());
        result.setBiotype(t.getBiotype());
        return result;
    }
    
    private List<TranscriptDetails>  getTranscriptsForGene(String gene, String species) 
            throws ParseException, MalformedURLException, IOException, InterruptedException{
        List<TranscriptDetails> transcripts = new ArrayList<>();
        String id;
        if(isTranscriptId(gene)){
            transcripts.add(rest.getTranscriptDetails(gene));
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
        }
        if (running){
            runButton.setText("Cancel");
            runButton.setCancelButton(true);
        }else{
            runButton.setText("Run");
            runButton.setDefaultButton(true);
            runButton.setOnAction((ActionEvent actionEvent) -> {
                mapMutation();
            });
        }
    }
                
    EventHandler<KeyEvent> checkNumeric(){
        return (KeyEvent ke) -> {
            if (!ke.getCharacter().matches("\\d")){
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
