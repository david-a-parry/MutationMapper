/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mutationmapper;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author david
 */
public class ResultSummaryController implements Initializable {
    @FXML
    Label messageLabel;
    @FXML
    Label detailsLabel;
    @FXML
    HBox actionParent;
    @FXML
    HBox okParent;
    @FXML
    Button okButton;
    @FXML
    ImageView imageView;

    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        imageView.setImage(new Image(this.getClass()
                            .getResourceAsStream("icon.png")));
        okButton.setOnAction((ActionEvent e) -> {
            Platform.runLater(() -> {
                Stage stage = (Stage) okButton.getScene().getWindow();
                stage.close();
            });
        });
    } 
    public void setMessage(String msg){
        messageLabel.setText(msg);
    }
    public void setDetails(String details){
        detailsLabel.setText(details);
    }
    
}
