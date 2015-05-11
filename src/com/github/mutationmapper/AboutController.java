package com.github.mutationmapper;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author David A. Parry <d.a.parry at your.uk.ac.leeds>
 */
public class AboutController implements Initializable {

    
    @FXML
    Button closeButton; 
    @FXML
    Label versionLabel;
    String VERSION;
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        closeButton.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent e){
                Platform.runLater(new Runnable(){
                    @Override
                    public void run(){
                        Stage stage = (Stage) closeButton.getScene().getWindow();
                        stage.close();
                    }
                });
            }
        });
        closeButton.setDefaultButton(true);
        closeButton.setCancelButton(true);
        final KeyCombination macCloseKeyComb = 
                new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN);
            
        if (System.getProperty("os.name").equals("Mac OS X")){
                closeButton.addEventHandler(
                    KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent ev){
                        if (macCloseKeyComb.match(ev)){
                            closeButton.fire();
                        }
                    }
                });
        }
        
        
    }    
    
    public void setVersion(String version){
        VERSION = version;
        if (VERSION != null){
            versionLabel.setText("Version: " + VERSION);
        }
    }
}
