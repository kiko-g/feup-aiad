//package gui;
//
//import javafx.application.Application;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.image.Image;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.VBox;
//import javafx.stage.Stage;
//
//public class GameWindow extends Application {
//    int rowAmount;
//    int playerAmount;
//    int maxButtonsPerRow = 4;
//
//    public void setPlayerAmount(int playerAmount) { this.playerAmount = playerAmount; }
//
//    public GameWindow() {
//
//    }
//
//    public GameWindow(int playerAmount) {
//        this.playerAmount = playerAmount;
//        this.rowAmount = (int)Math.ceil((float) this.playerAmount/maxButtonsPerRow);
//    }
//
//    @Override
//    public void start(Stage stage) throws Exception {
//        stage.setTitle("Java FX Window");
//        stage.getIcons().add(new Image("/resources/icon.png"));
//
//        HBox[] HBoxes = new HBox[rowAmount];
//
//        for(int i = 0; i < rowAmount; i++) {
//            int buttonsPerRow = (i == rowAmount - 1) ? (playerAmount % maxButtonsPerRow) : maxButtonsPerRow;
//
//            Button[] rowButtons = new Button[buttonsPerRow];
//            for(int j = 0; j < buttonsPerRow; j++) {
//                rowButtons[j] = new Button("Button " + j);
//                rowButtons[j].setOnAction(e -> this.buttonAction());
//            }
//
//            HBoxes[i] = new HBox(rowButtons);
//            HBoxes[i].setSpacing(10);
//            HBoxes[i].setAlignment(Pos.CENTER);
//            HBoxes[i].setPadding(new Insets(5));
//        }
//
//        Label label = new Label("Player buttons");
//        VBox vbox = new VBox(HBoxes);
//        vbox.getChildren().add(label);
//        vbox.setAlignment(Pos.CENTER);
//        vbox.setPadding(new Insets(20));
//        vbox.getStylesheets().add("/gui/style.css");
//        vbox.applyCss();
//
//        stage.setScene(new Scene(vbox, 500, 300));
//        stage.show();
//    }
//
//
//
//
//    public void display() {
//        launch();
//    }
//}
