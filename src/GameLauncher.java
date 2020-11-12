import agents.town.*;
import agents.mafia.*;
import agents.neutral.*;
import agents.GameMaster;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import gui.AgentWindow;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.application.Application;
import java.util.*;
import utils.ConfigReader;
import utils.GameLobby;


public class GameLauncher extends Application {
    GameMaster gameMaster;

    public static void main(String[] args) {
        launch(); // launch Java FX GUI
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.launchGame();
        stage.setTitle("Game Window");
        stage.getIcons().add(new Image("/resources/icon.png"));


        int maxButtonsPerRow = 5;
        int playerAmount = gameMaster.getGameLobby().getCapacity();
        int rowAmount = (int) Math.ceil((float)playerAmount/maxButtonsPerRow);

        HBox[] HBoxes = new HBox[rowAmount];
        for(int i = 0; i < rowAmount; i++) {
            int buttonsPerRow = (i == rowAmount-1) ? (playerAmount%maxButtonsPerRow) : maxButtonsPerRow;

            Button[] rowButtons = new Button[buttonsPerRow];
            for(int j = 0; j < buttonsPerRow; j++) {
                rowButtons[j] = new Button("" + (i*maxButtonsPerRow+j));
                rowButtons[j].setOnAction(e -> this.buttonAction());
            }

            HBoxes[i] = new HBox(rowButtons);
            HBoxes[i].setSpacing(10);
            HBoxes[i].setAlignment(Pos.CENTER);
            HBoxes[i].setPadding(new Insets(5));
        }

        VBox vbox = new VBox(HBoxes);
        Label label = new Label("Player buttons");
        vbox.getChildren().add(label);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));
        vbox.getStylesheets().add("/gui/style.css");
        vbox.applyCss();

        stage.setScene(new Scene(vbox, 600, 500));
        stage.show();
    }

    private void buttonAction() {
        AgentWindow agentWindow = new AgentWindow();
        agentWindow.display();
    }



    public void launchGame() throws Exception {
        List<String> names = ConfigReader.importNames("src/resources/names.txt"); // Loads available names
        List<String> roles = ConfigReader.importRoles("src/resources/gamemodes/test.txt"); // Loads roles

        Runtime runtime = Runtime.instance(); // Get a JADE runtime
        Profile p1 = new ProfileImpl(); // Create the main container
        Profile p2 = new ProfileImpl(); // Create additional container
        ContainerController mainController = runtime.createMainContainer(p1);
        ContainerController container = runtime.createAgentContainer(p2);

        this.gameMaster = new GameMaster(roles.size()); // Launch GameMaster
        AgentController gameMasterController = container.acceptNewAgent("GameMaster", gameMaster);
        gameMasterController.start();

        Thread.sleep(1000);

        launchAgents(roles, names, container);
    }

    public void launchAgent(String role, String name, ContainerController container) throws StaleProxyException {
        AgentController ac;
        switch(role) {
            case "Villager" -> {
                ac = container.acceptNewAgent(name, new Villager());
                ac.start();
            }
            case "Killing" -> {
                ac = container.acceptNewAgent(name, new Killing());
                ac.start();
            }
            case "Leader" -> {
                ac = container.acceptNewAgent(name, new Leader());
                ac.start();
            }
            case "Jester" -> {
                ac = container.acceptNewAgent(name, new Jester());
                ac.start();
            }
            case "Healer" -> {
                ac = container.acceptNewAgent(name, new Healer());
                ac.start();
            }
            case "Detective" -> {
                ac = container.acceptNewAgent(name, new Detective());
                ac.start();
            }
            default -> System.out.println(role + " is still not implemented! Skipping...");
        }
    }
    public void launchAgents(List<String> roles, List<String> names, ContainerController container) throws StaleProxyException {
        // Role handling
        List<String> tempRoles = new ArrayList<>(roles);
        Collections.shuffle(tempRoles);
        Queue<String> remainingRoles = new LinkedList<>(tempRoles);

        // Name handling
        List<String> tempNames = new ArrayList<>(names);
        Collections.shuffle(tempNames);
        Queue<String> remainingNames = new LinkedList<>(tempNames);

        // Launches Agents
        for (int i = 0; i < roles.size(); i++) {
            String role = remainingRoles.poll();
            String name = remainingNames.poll();

            if (role != null && name != null) {
                launchAgent(role, name, container);
            }
        }
    }
}
