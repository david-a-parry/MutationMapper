/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mutationmapper;

import com.github.mutationmapper.TranscriptDetails.Exon;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
                        cdsTextField.setDisable(!newValue.isEmpty());
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
                 Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                setRunning(false);
                                progressLabel.textProperty().unbind();
                                progressBar.progressProperty().unbind();
                                progressBar.progressProperty().set(0);
                                
                            }
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
                    }Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                            resultView.displayData(results);
                            tableStage.setTitle("MutationMapper Results");
                            //tableStage.getIcons().add(new Image(this.getClass()
                            //        .getResourceAsStream("icon.png")));
                            if (!tableStage.isShowing()){
                                    tableStage.show();
                            }else{
                                tableStage.requestFocus();
                            }
                            }
                    });
                }catch(Exception ex){
                    //TO DO - show error dialog
                    ex.printStackTrace();
                }

                
            }

        });
        
        mapperTask.setOnCancelled(new EventHandler<WorkerStateEvent>(){
                @Override
                public void handle (WorkerStateEvent e){
                     Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                            setRunning(false);
                            progressLabel.textProperty().unbind();
                            progressBar.progressProperty().unbind();
                            progressBar.progressProperty().set(0);

                        }
                     });
                }
        });
                 
         mapperTask.setOnFailed(new EventHandler<WorkerStateEvent>(){
            @Override
                public void handle (WorkerStateEvent e){
                     Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                            setRunning(false);
                            progressLabel.textProperty().unbind();
                            progressBar.progressProperty().unbind();
                            progressBar.progressProperty().set(0);

                        }
                     });
                }
        });
         
        new Thread(mapperTask).start();
        setRunning(true);
        
        
    }
    
    private List<MutationMapperResult> sequenceToCoordinates(String gene, String species, String seq)
            throws ParseException, MalformedURLException, IOException, InterruptedException{
        List<MutationMapperResult> results = new ArrayList<>();
        List<TranscriptDetails> transcripts = new ArrayList<>();
        String chrom;
        int start;
        int end; 
        int strand;
        if (isTranscriptId(gene)){
            TranscriptDetails t = rest.getTranscriptDetails(gene);
            chrom = t.getChromosome();
            start = t.getTxStart();
            end = t.getTxEnd();
            strand = t.getStrand();
            transcripts.add(t);
        }else{
            GeneDetails g = new GeneDetails();
            if (isGeneId(gene)){
                g = rest.getGeneDetails(gene);
            }else{
                String id = rest.getGeneID(species, gene);
                g = rest.getGeneDetails(id);
            }
            chrom = g.getChromosome();
            start = g.getStart();
            end = g.getEnd();
            strand = g.getStrand();
            transcripts = g.getTranscripts();   
        }
        if (transcripts.isEmpty()){
            return null;
        }
        String dna = rest.getDna(chrom, start, end, species);
        int index = dna.indexOf(seq);
        List<Integer> indices = new ArrayList<>();
        boolean revCompMatches = false;
        while (index >= 0){
            indices.add(index);
            index = dna.indexOf(seq, index + 1);
        }
        index = dna.indexOf(ReverseComplementDNA.reverseComplement(seq));
        while (index >= 0){
            revCompMatches = true;
            indices.add(index);
            index = dna.indexOf(ReverseComplementDNA.reverseComplement(seq), index + 1);
        }
        if (indices.isEmpty()){
            //TO DO - throw an error
            System.out.println("ERROR: No matches for seq");
            return null;
        }
        if (indices.size() > 1){
            //TO DO - throw an error
            System.out.println("ERROR: Seq matches multiple (" + indices.size() + ") times.");
            return null;
        }
        //Calculate start and end coordinates of input sequence
        int matchPos;
        int matchEnd;
        
        if(revCompMatches){
            matchEnd = indices.get(0) + start;
            matchPos = matchEnd + seq.length() - 1;
        }else{
            matchPos = indices.get(0) + start;
            matchEnd = matchPos + seq.length() - 1;
        
        }
        
        ArrayList<String> t_ids = new ArrayList<>();
        for (TranscriptDetails t: transcripts){
            MutationMapperResult result = putBasicTranscriptInfo(t);
            // calculate CDS position from genomic position for each transcript
            String cds_pos_match = getCdsPosition(chrom, matchPos, t);
            String cds_pos_end = getCdsPosition(chrom, matchEnd, t);
            result.setCdsCoordinate(String.format("%s-%s", cds_pos_match, cds_pos_end));
            result.setGenome(t.getGenomeBuild());
            result.setChromosome(chrom);
            result.setCoordinate(matchPos);
            result.setMatchingSequence(seq);
            results.add(result);
            t_ids.add(t.getTranscriptId());
        }
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
            result.setCdsCoordinate(cdsCoordinate);
            if (g != null){
                result.setChromosome(g.get("chromosome"));
                result.setCoordinate(Integer.parseInt(g.get("coordinate")));
                result.setGenome(g.get("assembly"));
            }
            results.add(result);
        }
        return results;
    }
    
    private String getCdsPosition(String chrom, int pos, TranscriptDetails t){
        if (! t.isCoding()){
            return "";
        }
        if (pos < t.getTxStart() || pos > t.getTxEnd()){
            return "";
        }
        if (pos < t.getCdsStart() || pos  > t.getCdsEnd()){
            return getUtrPosition(chrom, pos, t);
        }
        ArrayList<Exon> cds = t.getCodingRegions();
        if (!t.getChromosome().equals(chrom)){
            return "";
        }
        
        int cds_pos = 0;
        int intron_pos = 0;
        StringBuilder cds_string = new StringBuilder();

        boolean isExonic = false;
        
        for (int i = 0; i < cds.size(); i++){
            if ( pos < cds.get(i).getStart()){//pos is before this exons start
                break;
            }
            if (pos > cds.get(i).getEnd()){//pos is after this exon
                cds_pos += cds.get(i).getLength();
            }else{//pos is within exon
                cds_pos += pos - cds.get(i).getStart() + 1;
                isExonic = true; 
                break;
            }
        }
        
        if (!isExonic){
            for (int i = 0; i < cds.size() -1; i++){
                if (pos >= cds.get(i).getEnd() && pos < cds.get(i+1).getStart()){
                    Integer donor_pos = pos - cds.get(i).getEnd();
                    Integer acceptor_pos = pos - cds.get(i+1).getStart();
                    if (Math.abs(donor_pos) <= Math.abs(acceptor_pos)){
                        intron_pos = donor_pos;
                    }else{
                        intron_pos = acceptor_pos;
                        if (cds_pos < t.getCodingLength()){
                            cds_pos++;
                        }else{
                            /*
                            intronic position is actually after STOP codon
                            but pre the first UTR exon
                            */
                            if (t.getStrand() < 0){// on - strand so actually 5'UTR
                                intron_pos *= -1;
                                cds_string.append("c.-1");
                            }else{//if on + strand is 3' UTR
                                cds_string.append("c.*1");
                            }
                            if (intron_pos > 0){
                                cds_string.append("+");
                            }
                            cds_string.append(intron_pos);
                            return cds_string.toString();
                        }
                    }
                }
            }
        }
        
        if (t.getStrand() < 0){
            intron_pos *= -1;
            cds_pos = t.getCodingLength() - cds_pos + 1;
        }
        cds_string.append("c.").append(cds_pos);
        if (intron_pos != 0){
            if (intron_pos > 0){
                cds_string.append("+");
            }
            cds_string.append(intron_pos);
        }
        return cds_string.toString();
    }
    
    private String getUtrPosition(String chrom, int pos, TranscriptDetails t){
        if (! t.isCoding()){
            return "";
        }
        if (pos > t.getCdsStart() && pos  <= t.getCdsEnd()){
            //Should we throw an exepction here?
            return "";
        }
        if (!t.getChromosome().equals(chrom)){
            return "";
        }
        StringBuilder utr_string = new StringBuilder();
        int utr_pos = 0;
        int intron_pos = 0;
        
        ArrayList<Exon> exons = t.getExons();
        boolean isExonic = false;
        if (pos < t.getCdsStart()){
            int utr_length = 0;
            for (int i = 0; i < exons.size(); i++){
                if (exons.get(i).getStart() > t.getCdsStart()){//exon is in CDS
                    break;
                }
                if (exons.get(i).getEnd() < t.getCdsStart()){//whole exon is pre-CDS
                    utr_length += exons.get(i).getLength();
                    if (pos > exons.get(i).getEnd()){
                        utr_pos += exons.get(i).getLength();
                    }else if(pos >= exons.get(i).getStart()){
                        utr_pos += pos - exons.get(i).getStart() ;
                        isExonic = true;
                    }
                }else{//cds start is within exon
                    utr_length += t.getCdsStart() - exons.get(i).getStart() ;
                    if (pos >= exons.get(i).getStart()){
                        utr_pos += pos - exons.get(i).getStart() ;
                        isExonic = true;
                    }
                    break;
                }
            }
            utr_pos = utr_length - utr_pos;
            utr_pos *= -1;
        }else{
            for (int i = 0; i < exons.size(); i++){
                if (exons.get(i).getEnd() < t.getCdsEnd()){//exon is before or within CDS
                    continue;
                }
                if (exons.get(i).getStart() > t.getCdsEnd()){//whole exon is post CDS
                    if (pos > exons.get(i).getEnd()){
                        utr_pos += exons.get(i).getLength();
                    }else if (pos >= exons.get(i).getStart() ){
                        utr_pos += pos - exons.get(i).getStart();
                        isExonic = true;
                        break;
                    }
                }else{//cds end is in exon
                    if (pos >= t.getCdsEnd() && pos <= exons.get(i).getEnd()){
                        utr_pos += pos - t.getCdsEnd();
                        isExonic = true;
                        break;
                    }
                }
            }
        }
        
        if (!isExonic){
            for (int i = 0; i < exons.size() -1; i++){
                if (pos >= exons.get(i).getEnd() && pos < exons.get(i+1).getStart()){
                    Integer donor_pos = pos - exons.get(i).getEnd();
                    Integer acceptor_pos = pos - exons.get(i+1).getStart();
                    if (Math.abs(donor_pos) <= Math.abs(acceptor_pos)){
                        intron_pos = donor_pos;
                    }else{
                        intron_pos = acceptor_pos;
                        if (utr_pos != -1){
                            utr_pos++;
                        }else{
                            /*
                            intronic position is actually before first coding base
                            */
                            if (t.getStrand() < 0){// on - strand so actually just after last coding base
                                intron_pos *= -1;
                                utr_string.append("c.").append(t.getCodingLength());
                            }else{//if on + strand is 3' UTR
                                utr_string.append("c.1");
                            }
                            if (intron_pos > 0){
                                utr_string.append("+");
                            }
                            utr_string.append(intron_pos);
                            return utr_string.toString();
                        }
                    }
                }
            }
        }
        
        if (t.getStrand() < 0){
            intron_pos *= -1;
            utr_pos = t.getCodingLength() - utr_pos + 1;
        }
        if (utr_pos > 0){
            utr_string.append("c.*").append(utr_pos);
        }else{
            utr_string.append("c.").append(utr_pos);
        }
        if (intron_pos != 0){
            if (intron_pos > 0){
                utr_string.append("+");
            }
            utr_string.append(intron_pos);
        }
        return utr_string.toString();
        
        
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
    
    private boolean sequenceIsDna(String seq){
        return !seq.matches("(?i)[^ACTG]");
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
            runButton.setDefaultButton(true);
        }else{
            runButton.setText("Run");
            runButton.setDefaultButton(true);
        }
    }
                
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
