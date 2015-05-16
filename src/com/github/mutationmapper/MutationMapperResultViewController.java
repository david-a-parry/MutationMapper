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

import static com.github.mutationmapper.MutationMapper.VERSION;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import static java.lang.System.getProperty;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
   TableColumn exonIntronCol;
   @FXML
   TableColumn polyphenCol;
   @FXML
   TableColumn siftCol;
   @FXML
   TableColumn knownVarCol;
   @FXML
   TableColumn seqInputCol;
   @FXML 
   Label summaryLabel;
   @FXML
   TextArea resultTextArea;
   @FXML
   MenuItem saveMenuItem;
   @FXML
   MenuItem closeMenuItem;
   @FXML
   MenuItem quitMenuItem;
   @FXML
   MenuItem clearPreviousMenuItem;
   @FXML
   MenuItem clearAndCloseMenuItem;
   @FXML
   MenuItem clearMenuItem;
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
   MenuItem aboutMenuItem;
   @FXML
   MenuItem helpMenuItem;
   
   private final ObservableList<MutationMapperResult> data = FXCollections.observableArrayList();
   private final ObservableList<MutationMapperResult> displayData = FXCollections.observableArrayList();
   
   Integer lastIndex = 0;
   BooleanProperty refSeq = new SimpleBooleanProperty();
   BooleanProperty refSeqOnly = new SimpleBooleanProperty();
   BooleanProperty codingOnly = new SimpleBooleanProperty();
   BooleanProperty canonicalOnly = new SimpleBooleanProperty();
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        menuBar.setUseSystemMenuBar(true);
        
        indexCol.setCellValueFactory(new PropertyValueFactory<>("index"));
        symbolCol.setCellValueFactory(new PropertyValueFactory<>("geneSymbolLink"));
        geneCol.setCellValueFactory(new PropertyValueFactory<>("geneIdLink"));
        transcriptCol.setCellValueFactory(new PropertyValueFactory<>("transcriptLink"));
        cdsCol.setCellValueFactory(new PropertyValueFactory<>("cdsCoordinate"));
        genomicCol.setCellValueFactory(new PropertyValueFactory<>("regionLink"));
        refCol.setCellValueFactory(new PropertyValueFactory<>("refAllele"));
        varCol.setCellValueFactory(new PropertyValueFactory<>("varAllele"));
        proteinConsequenceCol.setCellValueFactory(new PropertyValueFactory<>("proteinConsequence"));
        cdsConsequenceCol.setCellValueFactory(new PropertyValueFactory<>("cdsConsequence"));
        exonIntronCol.setCellValueFactory(new PropertyValueFactory<>("exonIntronNumber"));
        consequenceCol.setCellValueFactory(new PropertyValueFactory<>("consequence"));
        knownVarCol.setCellValueFactory(new PropertyValueFactory<>("snpLink"));
        seqInputCol.setCellValueFactory(new PropertyValueFactory<>("seqInput"));
        polyphenCol.setCellValueFactory(new PropertyValueFactory<>("polyphenResult"));
        siftCol.setCellValueFactory(new PropertyValueFactory<>("siftResult"));
        
        resultTable.setFixedCellSize(-1);
        resultTable.setRowFactory(param -> {
            return new TableRow() {
                @Override
                public void updateIndex(int i) {
                    super.updateIndex(i);

                    setMinHeight(50);// * i);
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
                    if (cell instanceof Hyperlink){
                        Hyperlink link = (Hyperlink) cell;
                        cell = link.getText();
                    }
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
        
        closeMenuItem.setOnAction((ActionEvent e) -> {
            Platform.runLater(() -> {
                Stage stage = (Stage) resultPane.getScene().getWindow();
                stage.close();
            });
        });
        
        if (System.getProperty("os.name").equals("Mac OS X")){
           closeMenuItem.setAccelerator
                (new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN));
        }
        
        quitMenuItem.setOnAction((ActionEvent e) -> {
            Platform.runLater(() -> {
                Platform.exit();
                System.exit(0);
            });
        });
        
        saveMenuItem.setOnAction((ActionEvent e) -> {
            try{
                writeResultsToFile();
            }catch(IOException ex){
                Platform.runLater(() -> {
                    Alert error = getExceptionDialog(ex);
                    error.setTitle("Write Failed");
                    error.setHeaderText("Error writing file.");
                    error.setContentText("Exception encountered when attempting "
                                + "the saved file. See below:");
                    error.showAndWait();
                });
            }       
        });
        saveMenuItem.disableProperty().bind(Bindings.size(displayData).lessThan(1));
        clearAndCloseMenuItem.setOnAction((ActionEvent e) -> {
            Platform.runLater(() -> {
                clearTable();
                Stage stage = (Stage) resultPane.getScene().getWindow();
                stage.close();
            });
        });
        
        clearMenuItem.setOnAction((ActionEvent e) -> {
            Platform.runLater(() -> {
                clearTable();
            });
        });
        
        clearPreviousMenuItem.setOnAction((ActionEvent e) -> {
            Platform.runLater(()->{
                clearPreviousResults();
            });
        });
        
        ToggleGroup refseqToggleGroup = new ToggleGroup();
        refSeqMenu.setToggleGroup(refseqToggleGroup);
        refSeqOnlyMenu.setToggleGroup(refseqToggleGroup);
        noRefSeqMenu.setToggleGroup(refseqToggleGroup);
        refSeqMenu.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov,Boolean old_val, Boolean new_val) {
                        redisplayData();
                }
        });
        refSeqOnlyMenu.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov,Boolean old_val, Boolean new_val) {
                        redisplayData();
                }
        });
        noRefSeqMenu.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov,Boolean old_val, Boolean new_val) {
                        redisplayData();
                }
        });
        canonicalOnlyMenu.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov,Boolean old_val, Boolean new_val) {
                        redisplayData();
                }
        });
        codingOnlyMenu.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov,Boolean old_val, Boolean new_val) {
                        redisplayData();
                }
        });
        refSeq.bind(refSeqMenu.selectedProperty());
        refSeqOnly.bind(refSeqOnlyMenu.selectedProperty());
        canonicalOnly.bind(canonicalOnlyMenu.selectedProperty());
        codingOnly.bind(codingOnlyMenu.selectedProperty());
        aboutMenuItem.setOnAction(new EventHandler(){
            @Override
            public void handle (Event ev){
                showAbout();
            }
        });
        helpMenuItem.setOnAction(new EventHandler(){
            @Override
            public void handle (Event ev){
                showHelp();
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
        setTableItems();
    }
    
    private void setTableItems(){
        displayData.clear();
        if (refSeq.getValue() || refSeqOnly.getValue()){
            transcriptCol.setCellValueFactory(new PropertyValueFactory<>("refSeqTransciptLink"));
        }else{
            transcriptCol.setCellValueFactory(new PropertyValueFactory<>("transciptLink"));
        }
        for (MutationMapperResult r: data){
            if (canonicalOnly.getValue()){
                if (!r.getIsCanonical()){
                    continue;
                }
            }
            if (codingOnly.getValue()){
                if (!r.getBiotype().equals("protein_coding")){
                    continue;
                }
            }
            if (refSeqOnly.getValue()){
                if (r.getRefSeqIds() == null || r.getRefSeqIds().isEmpty()){
                    continue;
                }
            }
            displayData.add(r);
        }
        
        resultTable.setItems(displayData);
    }
    
    public void redisplayData(){
        resultTable.getItems().clear();
        setTableItems();
    }

    public void clearTable(){
        lastIndex = 0;
        data.clear();
        displayData.clear();
        resultTable.getItems().clear();
    }
    
    public void clearPreviousResults(){
        ArrayList<MutationMapperResult> lastRun = new ArrayList<>();
        for (MutationMapperResult r: data){
            if (r.getIndex().matches(lastIndex + "[A-Z]+")){
                r.setIndex(null);
                lastRun.add(r);
            }
        }
        clearTable();
        lastIndex = 0;
        displayData(lastRun);
    }
    
    private void writeResultsToFile() throws IOException{
        if (displayData.isEmpty()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Nothing to save");
            alert.setHeaderText("No results to save");
            alert.setContentText("No results are visible with current filters,"
                    + " no file can be saved.");
            alert.setResizable(true);
            alert.showAndWait();
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
               new FileChooser.ExtensionFilter("Excel  (*.xlsx)", "*.xlsx"),
               new FileChooser.ExtensionFilter("CSV  (*.csv)", "*.csv"),
               new FileChooser.ExtensionFilter("Text  (*.txt)", "*.txt")
       );
       fileChooser.setTitle("Write results to file...");
       fileChooser.setInitialDirectory(new File(getProperty("user.home")));
       File wFile = fileChooser.showSaveDialog(resultPane.getScene().getWindow());
       if (wFile == null){
           return;
       }else if (! wFile.getName().endsWith(".xlsx") && 
                 !wFile.getName().endsWith(".csv")   &&
                 !wFile.getName().endsWith(".txt")){
            String ext = //annoying bug with filechooser means extension might not be appended
                fileChooser.selectedExtensionFilterProperty().get().getExtensions().get(0).substring(1);
            wFile = new File(wFile.getAbsolutePath() + ext);
       }
       if (wFile.getName().endsWith(".xlsx")){
           writeResultsToExcel(wFile);
       }else if (wFile.getName().endsWith(".csv")){
           writeResultsToCsv(wFile);
       }else{
           writeResultsToTsv(wFile);
       }
    }
    
    private void writeResultsToExcel(final File f) throws IOException{
       final Service<Void> service = new Service<Void>(){
            @Override
            protected Task<Void> createTask(){
                return new Task<Void>(){
                    @Override
                    protected Void call() throws IOException {
                        BufferedOutputStream bo = new BufferedOutputStream(new 
                           FileOutputStream(f));
                        Workbook wb = new XSSFWorkbook();
                        //CellStyle hlink_style = wb.createCellStyle();
                        //Font hlink_font = wb.createFont();
                        //hlink_font.setUnderline(Font.U_SINGLE);
                        //hlink_font.setColor(IndexedColors.BLUE.getIndex());
                        //hlink_style.setFont(hlink_font);
                        //CreationHelper createHelper = wb.getCreationHelper();
                        Sheet sheet = wb.createSheet();
                        Row row = null;
                        int rowNo = 0;
                        row = sheet.createRow(rowNo++);
                        String header[] = {"#", "Symbol", "Gene", "Transcript", 
                            "Genomic Coordinate", "Ref", "Var", "CDS", "Consequence",
                            "CDS Consequence", "Protein Consequence", "Exon/Intron",
                            "Colocated Variants", "Polyphen", "Sift", "Seq Input"};
                        for (int col = 0; col < header.length; col ++){
                            Cell cell = row.createCell(col);
                            cell.setCellValue(header[col]);
                        }
                        
                        updateMessage("Writing results . . .");
                        updateProgress(0, displayData.size());
                        int n = 0;
                        for (MutationMapperResult r: displayData){
                            n++;
                            updateMessage("Writing result " + n + " . . .");
                            row = sheet.createRow(rowNo++);
                            int col = 0;
                            ArrayList<String> resultsToWrite = getResultArray(r);
                            for (String s: resultsToWrite){
                                Cell cell = row.createCell(col++);
                                cell.setCellValue(s);
                            }    
                            updateProgress(n, displayData.size());
                        }
                        
                        updateMessage("Wrote " + displayData.size() + " results to file.");
                        wb.write(bo);
                        bo.close();
                        return null;
                    }
                };
            }
            
        };
       
        service.setOnSucceeded(new EventHandler<WorkerStateEvent>(){
                @Override
                public void handle (WorkerStateEvent e){
                    Alert alert = new Alert(AlertType.CONFIRMATION);
                    alert.setTitle("File Saved");
                    alert.setHeaderText("Displayed results saved to " + f.getName());
                    alert.setContentText("Open this file now?");
                    ButtonType yesButton = ButtonType.YES;
                    ButtonType noButton = ButtonType.NO;
                    alert.getButtonTypes().setAll(yesButton, noButton);
                    Optional<ButtonType> response = alert.showAndWait();
                    if (response.get() == yesButton){
                        try{
                            openFile(f);
                        } catch (Exception ex) {
                            Alert openError = getExceptionDialog(ex);
                            openError.setTitle("Open Failed");
                            openError.setHeaderText("Could not open ouput file.");
                            openError.setContentText("Exception encountered when attempting to open "
                                        + "the saved file. See below:");
                            openError.showAndWait();
                        }
                    }
                }
        });
        service.setOnCancelled(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle (WorkerStateEvent e){
                Alert writeCancelled = new Alert(AlertType.INFORMATION);
                    writeCancelled.setTitle("Cancelled writing to file");
                    writeCancelled.setHeaderText("User cancelled writing primers to file.");
                    writeCancelled.showAndWait();
            }
        });
        service.setOnFailed(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle (WorkerStateEvent e){
                Alert writeFailed = getExceptionDialog(e.getSource().getException());
                writeFailed.setTitle("Write Failed");
                writeFailed.setHeaderText("Could not write ouput file.");
                writeFailed.setContentText("Exception encountered when attempting to write "
                            + "results to file. See below:");
                writeFailed.showAndWait();

            }
        });
        service.start();
    }
    
    private void writeResultsToCsv(final File f) throws IOException{
        writeResultsToText(f, ",");
    }
    
    private void writeResultsToTsv(final File f) throws IOException{
        writeResultsToText(f, "\t");
    }
    
    //takes output file and delimiter string as arguments
    private void writeResultsToText(final File f, final String d) throws IOException{
        final Service<Void> service = new Service<Void>(){
            @Override
            protected Task<Void> createTask(){
                return new Task<Void>(){
                    @Override
                    protected Void call() throws IOException {
                        FileWriter fw = new FileWriter(f.getAbsoluteFile());
                        BufferedWriter bw = new BufferedWriter(fw);
                        updateMessage("Writing primers . . .");
                        updateProgress(0, displayData.size());
                        String header[] = {"#", "Symbol", "Gene", "Transcript", 
                            "Genomic Coordinate", "Ref", "Var", "CDS", "Consequence",
                            "CDS Consequence", "Protein Consequence", "Exon/Intron",
                            "Colocated Variants", "Polyphen", "Sift", "Seq Input"
                        };
                        bw.write(String.join(d, header));
                        bw.newLine();
                        int n = 0;
                        for (MutationMapperResult r: displayData){
                            n++;
                            updateMessage("Writing result " + n + " . . .");
                            ArrayList<String> resultsToWrite = getResultArray(r);
                            bw.write(String.join(d, resultsToWrite));
                            bw.newLine();
                            updateProgress(n, displayData.size());
                        }
                        updateMessage("Wrote " + n + " results to file.");
                        bw.close();
                        return null;
                    }
                };
            }
            
        };
        
        
        service.setOnSucceeded(new EventHandler<WorkerStateEvent>(){
                @Override
                public void handle (WorkerStateEvent e){
                    Alert alert = new Alert(AlertType.CONFIRMATION);
                    alert.setTitle("File Saved");
                    alert.setHeaderText("Displayed results saved to " + f.getName());
                    alert.setContentText("Open this file now?");
                    ButtonType yesButton = ButtonType.YES;
                    ButtonType noButton = ButtonType.NO;
                    alert.getButtonTypes().setAll(yesButton, noButton);
                    Optional<ButtonType> response = alert.showAndWait();
                    if (response.get() == yesButton){
                        try{
                            openFile(f);
                        } catch (Exception ex) {
                            Alert openError = getExceptionDialog(ex);
                            openError.setTitle("Open Failed");
                            openError.setHeaderText("Could not open output file.");
                            openError.setContentText("Exception encountered when attempting to open "
                                        + "the saved file. See below:");
                            openError.showAndWait();
                        }
                    }
                }
        });
        service.setOnCancelled(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle (WorkerStateEvent e){
                Alert writeCancelled = new Alert(AlertType.INFORMATION);
                    writeCancelled.setTitle("Cancelled writing to file");
                    writeCancelled.setHeaderText("User cancelled writing primers to file.");
                    writeCancelled.showAndWait();
            }
        });
        service.setOnFailed(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle (WorkerStateEvent e){
                Alert writeFailed = getExceptionDialog(e.getSource().getException());
                writeFailed.setTitle("Write Failed");
                writeFailed.setHeaderText("Could not write output file.");
                writeFailed.setContentText("Exception encountered when attempting to write "
                            + "results to file. See below:");
                writeFailed.showAndWait();

            }
        });
           
        service.start();
    }
    
    private ArrayList<String> getResultArray(MutationMapperResult r){
        ArrayList<String> values = new ArrayList<>();
        values.add(r.getIndex());
        values.add(r.getGeneSymbol());
        values.add(r.getGeneId());
        if (refSeq.getValue() || refSeqOnly.getValue()){
            values.add(r.getRefSeqIfAvailable());
        }else{
            values.add(r.getTranscript());
        }
        values.add(r.getGenomicCoordinate());
        values.add(r.getRefAllele());
        values.add(r.getVarAllele());
        values.add(r.getCdsCoordinate());
        values.add(r.getConsequence());
        values.add(r.getCdsConsequence());
        values.add(r.getProteinConsequence());
        values.add(r.getExonIntronNumber());
        values.add(r.getKnownIds());
        values.add(r.getPolyphenResult());
        values.add(r.getSiftResult());
        values.add(r.getSeqInput());
        return values;
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
    
    private Alert getExceptionDialog(Throwable ex){
        // Create expandable Exception.
        Alert alert = new Alert(AlertType.ERROR);
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
        return alert;
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
            stage.setTitle("About AutoPrimer3");
            
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
}
