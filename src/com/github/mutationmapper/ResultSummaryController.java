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
        okButton.setDefaultButton(true);
        okButton.setCancelButton(true);
    } 
    public void setMessage(String msg){
        messageLabel.setText(msg);
    }
    public void setDetails(String details){
        detailsLabel.setText(details);
    }
    
}
