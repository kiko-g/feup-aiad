//import agents.town.*;
//import agents.mafia.*;
//import agents.neutral.*;
//import agents.GameMaster;
//import jade.core.Profile;
//import jade.core.ProfileImpl;
//import sajas.core.Runtime;
//import sajas.wrapper.AgentController;
//import sajas.wrapper.ContainerController;
//import jade.wrapper.StaleProxyException;
//import gui.AgentWindow;
//import javafx.application.Platform;
//import javafx.scene.Scene;
//import javafx.stage.Stage;
//import javafx.geometry.Pos;
//import javafx.geometry.Insets;
//import javafx.concurrent.Task;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.VBox;
//import javafx.scene.image.Image;
//import javafx.scene.control.Label;
//import javafx.scene.control.Button;
//import javafx.application.Application;
//import java.util.*;
//import utils.ConfigReader;
//import utils.GameLobby;
//import utils.Util;
//
//
//public class GameLauncher extends Application {
//    List<String> names;
//    GameMaster gameMaster;
//
//
//    public static void main(String[] args) {
//        launch(); // launch Java FX GUI
//    }
//
//    @Override
//    public void start(Stage stage) throws Exception {
//        launchGame();
//        stage.setTitle("MAFIA");
//        stage.getIcons().add(new Image("/resources/icon.png"));
//
//        int maxButtonsPerRow = 5;
//        int playerAmount = gameMaster.getGameLobby().getCapacity();
//        int rowAmount = (int) Math.ceil((float)playerAmount/maxButtonsPerRow);
//        String[] namesArray = gameMaster.getGameLobby().getAllPlayerNames();
//
//        HBox[] HBoxes = new HBox[rowAmount];
//        for(int i = 0; i < rowAmount; i++) {
//            int buttonsPerRow = (i == rowAmount-1) ? (playerAmount % maxButtonsPerRow) : maxButtonsPerRow;
//            if(buttonsPerRow == 0) buttonsPerRow = maxButtonsPerRow;
//
//            Button[] rowButtons = new Button[buttonsPerRow];
//            for(int j = 0; j < buttonsPerRow; j++) {
//                String currentName = namesArray[i*maxButtonsPerRow+j];
//                rowButtons[j] = new Button(currentName + ", the " +  gameMaster.getGameLobby().getPlayerRole(currentName));
//                rowButtons[j].setMinWidth(140.0);
//                rowButtons[j].setMinHeight(45.0);
//                rowButtons[j].setOnAction(e -> this.buttonAction(currentName));
//            }
//
//            HBoxes[i] = new HBox(rowButtons);
//            HBoxes[i].setSpacing(10);
//            HBoxes[i].setAlignment(Pos.CENTER);
//            HBoxes[i].setPadding(new Insets(10, 5, 10, 5));
//        }
//
//        VBox vbox = new VBox(HBoxes);
//        Label label = new Label("Player buttons");
//        vbox.getChildren().add(label);
//        vbox.setAlignment(Pos.CENTER);
//        vbox.setPadding(new Insets(20));
//        vbox.getStylesheets().add("/gui/style.css"); // Main stylesheet
//        vbox.getStylesheets().add("https://fonts.googleapis.com/css?family=Open+Sans"); // Open Sans font
//        vbox.applyCss();
//
//        stage.setScene(new Scene(vbox, 1200, 600));
//        stage.show();
//    }
//
//    private void buttonAction(String playerName) {
//        String role = gameMaster.getGameLobby().getRoleByName(playerName);
//        String info = gameMaster.requestPlayerPersonalInformation(playerName);
//        boolean isAlive = gameMaster.getGameLobby().isAlive(playerName);
//
//        AgentWindow agentWindow = new AgentWindow(playerName, role, info, isAlive);
//        agentWindow.display();
//    }
//
//
//
//    public void launchGame() throws Exception {
//        names = ConfigReader.importNames("src/resources/names.txt"); // Loads available names
//        List<String> roles = ConfigReader.importRoles("src/resources/gamemodes/test.txt"); // Loads roles
//
//        Profile p1 = new ProfileImpl(); // Create the main container
//        Profile p2 = new ProfileImpl(); // Create additional container
//        Runtime runtime = Runtime.instance(); // Get a JADE runtime
//        ContainerController mainController = runtime.createMainContainer(p1);
//        ContainerController container = runtime.createAgentContainer(p2);
//
//        this.gameMaster = new GameMaster(roles.size()); // Launch GameMaster
//        AgentController gameMasterController = container.acceptNewAgent("GameMaster", gameMaster);
//        gameMasterController.start();
//
//        Thread.sleep(1000);
//        launchAgents(roles, names, container);
//        Thread.sleep(1000);
//    }
//
//    public void launchAgents(List<String> roles, List<String> names, ContainerController container) throws StaleProxyException {
//        // Role handling
//        List<String> tempRoles = new ArrayList<>(roles);
//        Collections.shuffle(tempRoles);
//        Queue<String> remainingRoles = new LinkedList<>(tempRoles);
//
//        // Name handling
//        List<String> tempNames = new ArrayList<>(names);
//        Collections.shuffle(tempNames);
//        Queue<String> remainingNames = new LinkedList<>(tempNames);
//
//        // Launches Agents
//        for (int i = 0; i < roles.size(); i++) {
//            String role = remainingRoles.poll();
//            String name = remainingNames.poll();
//
//            if (role != null && name != null) {
//                launchAgent(role, name, container);
//            }
//        }
//    }
//
//    public void launchAgent(String role, String name, ContainerController container) throws StaleProxyException {
//        AgentController ac;
//        switch(role) {
//            case "Villager" : {
//                ac = container.acceptNewAgent(name, new Villager());
//                ac.start();
//                break;
//            }
//            case "Killing" : {
//                ac = container.acceptNewAgent(name, new Killing());
//                ac.start();
//                break;
//            }
//            case "Leader" : {
//                ac = container.acceptNewAgent(name, new Leader());
//                ac.start();
//                break;
//            }
//            case "Jester" : {
//                ac = container.acceptNewAgent(name, new Jester());
//                ac.start();
//                break;
//            }
//            case "Healer" : {
//                ac = container.acceptNewAgent(name, new Healer());
//                ac.start();
//                break;
//            }
//            case "Detective" : {
//                ac = container.acceptNewAgent(name, new Detective());
//                ac.start();
//                break;
//            }
//            default : {
//                System.out.println(role + " is still not implemented! Skipping...");
//                break;
//            }
//        }
//    }
//}
