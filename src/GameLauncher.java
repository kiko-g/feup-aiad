import agents.GameMaster;
import agents.mafia.Killing;
import agents.town.Villager;
import gui.AgentWindow;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.ConfigReader;
import java.util.*;

public class GameLauncher extends Application {
    GameMaster gameMaster;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        launchGame();
        stage.setTitle("Java FX Window");
        stage.getIcons().add(new Image("/resources/icon.png"));

        int maxButtonsPerRow = 5;
        int playerAmount = this.gameMaster.getGameLobby().getCapacity();
        int rowAmount = (int)Math.ceil((float) playerAmount/maxButtonsPerRow);
        HBox[] HBoxes = new HBox[rowAmount];

        for(int i = 0; i < rowAmount; i++) {
            int buttonsPerRow = (i == rowAmount - 1) ? (playerAmount % maxButtonsPerRow) : maxButtonsPerRow;

            Button[] rowButtons = new Button[buttonsPerRow];
            for(int j = 0; j < buttonsPerRow; j++) {
                rowButtons[j] = new Button("Button " + j);
                rowButtons[j].setOnAction(e -> this.buttonAction());
            }

            HBoxes[i] = new HBox(rowButtons);
            HBoxes[i].setSpacing(10);
            HBoxes[i].setAlignment(Pos.CENTER);
            HBoxes[i].setPadding(new Insets(5));
        }

        Label label = new Label("Player buttons");
        VBox vbox = new VBox(HBoxes);
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
        // Loads available names
        List<String> names = ConfigReader.importNames("src/resources/names.txt");
        // Loads roles
        List<String> roles = ConfigReader.importRoles("src/resources/gamemodes/test.txt");

        // Get a JADE runtime
        Runtime rt = Runtime.instance();

        // Create the main container
        Profile p1 = new ProfileImpl();
        ContainerController mainController = rt.createMainContainer(p1);

        // Create additional container
        Profile p2 = new ProfileImpl();
        ContainerController container = rt.createAgentContainer(p2);

        // Launch GameMaster
        this.gameMaster = new GameMaster(roles.size()); //roles.size() is playerAmount
        AgentController gm_c = container.acceptNewAgent("GameMaster", gameMaster);
        gm_c.start();

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
