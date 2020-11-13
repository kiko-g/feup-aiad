package gui;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import java.util.Arrays;


public class AgentWindow {
    Stage stage;

    public AgentWindow(String playerName, String role, String info, boolean isAlive) {
        String[] result = info.split("#");
        String[] susRates = Arrays.copyOfRange(info.split("#"), 1, info.split("#").length);
        String status = isAlive ? "Alive" : "Dead";

        // TOP COMPONENT
        HBox[] topHBox = new HBox[4];
        topHBox[0] = new HBox(new Label("Name:   \t"), new Label(playerName));
        topHBox[1] = new HBox(new Label("Role:   \t"), new Label(role));
        topHBox[2] = new HBox(new Label("Trait:   \t"), new Label(result[0]));
        topHBox[3] = new HBox(new Label("Status:   \t"), new Label(status));
        VBox vbox1 = new VBox(topHBox);
        vbox1.getStyleClass().add("top");
        vbox1.setPadding(new Insets(0, 0, 50, 0));

        // TABLE
        HBox[] tableHBox = new HBox[susRates.length];
        for(int i = 0; i < susRates.length; ++i) {
            String[] pair = susRates[i].split(":");

            Label peer = new Label(pair[0]+"\t\t");
            int percentage = (int) (Math.floor(Float.parseFloat(pair[1])*100));
            Label peerSusRate = new Label(percentage + "%\t\t");

            tableHBox[i] = new HBox(peer, peerSusRate);
            tableHBox[i].setAlignment(Pos.CENTER);
        }

        // TABLE TITLE
        VBox vbox2 = new VBox();
        Label l1 = new Label("NAME");
        l1.getStyleClass().clear();
        l1.getStyleClass().add("headers");
        Label l2 = new Label("SUS RATE");
        l2.getStyleClass().clear();
        l2.getStyleClass().add("headers");
        HBox hbox = new HBox(l1, l2);
        hbox.setAlignment(Pos.CENTER);
        hbox.getStyleClass().add("table-title");
        vbox2.setAlignment(Pos.CENTER);
        vbox2.getStyleClass().add("table-title");
        vbox2.getChildren().add(hbox);

        // TABLE BODY
        VBox vbox3 = new VBox(tableHBox);
        vbox3.setAlignment(Pos.CENTER);
        vbox3.getStyleClass().add("table-body");


        // CLOSE BUTTON
        Button closeButton = new Button();
        closeButton.setText("Close");
        closeButton.setOnAction(e -> stage.close());
        VBox vbox4 = new VBox(closeButton);
        vbox4.setPadding(new Insets(30, 0, 0, 0));
        vbox4.setAlignment(Pos.CENTER);

        // Final layout
        stage = new Stage();
        stage.setTitle(playerName.toUpperCase() + " | " + role.toUpperCase());
        VBox layout = new VBox(vbox1);
        layout.getChildren().add(vbox2);
        layout.getChildren().add(vbox3);
        layout.getChildren().add(vbox4);
        layout.getStylesheets().add("/gui/style.css");
        layout.getStylesheets().add("https://fonts.googleapis.com/css?family=Open+Sans");
        layout.getStyleClass().add("agent-bg");
        layout.applyCss();

        Scene scene = new Scene(layout, 600, 700);
        stage.setScene(scene);
        stage.getIcons().add(new Image("/resources/icon.png"));
    }

    public void display() {
        stage.show();
    }
}
