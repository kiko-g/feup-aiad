package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;


public class AgentWindow {
    Stage stage;

    public AgentWindow() {
        stage = new Stage();
        stage.setTitle("Agent Window");


        Button closeButton = new Button();
        closeButton.setText("Close Window");
        closeButton.setOnAction(e -> stage.close());
        Label label = new Label("Agent Information Window");
        label.setPadding(new Insets(100));

        VBox vbox = new VBox();
        vbox.getChildren().add(label);
        vbox.getChildren().add(closeButton);

        //Styling layout
        vbox.setAlignment(Pos.CENTER);
        vbox.getStylesheets().add("/gui/style.css");
        vbox.getStylesheets().add("https://fonts.googleapis.com/css?family=Open+Sans");
        vbox.applyCss();

        Scene scene = new Scene(vbox, 300, 300);
        stage.setScene(scene);
    }

    public void display() {
        stage.show();
    }
}
