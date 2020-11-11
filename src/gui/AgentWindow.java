package gui;

import javafx.application.Application;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AgentWindow {
    Stage window;

    //DFAgentDescription agentDescription
    public AgentWindow() {
        window = new Stage();
        window.setTitle("Agent Window");

        Button button = new Button();
        button.setText("Close Window");
        button.setOnAction(e -> window.close());

        StackPane layout = new StackPane();
        layout.getChildren().add(button);

        Scene scene = new Scene(layout, 200, 300);
        window.setScene(scene);
    }

    private void buttonAction() {
        System.out.println("Button pressed");
    }

    public void display() {
        window.show();
    }
}
