package gui;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;


public class AgentWindow {
    Stage stage;

    public AgentWindow() {
        stage = new Stage();
        stage.setTitle("Agent Window");

        Button button = new Button();
        button.setText("Close Window");
        button.setOnAction(e -> stage.close());

        StackPane layout = new StackPane();
        layout.getChildren().add(button);
        layout.getStylesheets().add("/gui/style.css");
        layout.applyCss();

        Scene scene = new Scene(layout, 300, 300);
        stage.setScene(scene);
    }

    public void display() {
        stage.show();
    }
}
