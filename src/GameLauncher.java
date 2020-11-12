import agents.GameMaster;
import agents.mafia.Killing;
import agents.mafia.Leader;
import agents.neutral.Jester;
import agents.town.Detective;
import agents.town.Healer;
import agents.town.Villager;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import utils.ConfigReader;
import utils.Util;

import java.io.IOException;
import java.util.*;

public class GameLauncher {

    public static void launchAgent(String role, String name, ContainerController container) throws StaleProxyException {
        AgentController ac;

        switch (role) {
            case "Villager": {
                ac = container.acceptNewAgent(name, new Villager());
                ac.start();
                break;
            }
            case "Killing": {
                ac = container.acceptNewAgent(name, new Killing());
                ac.start();
                break;
            }
            case "Leader": {
                ac = container.acceptNewAgent(name, new Leader());
                ac.start();
                break;
            }
            case "Jester": {
                ac = container.acceptNewAgent(name, new Jester());
                ac.start();
                break;
            }
            case "Healer": {
                ac = container.acceptNewAgent(name, new Healer());
                ac.start();
                break;
            }
            case "Detective": {
                ac = container.acceptNewAgent(name, new Detective());
                ac.start();
                break;
            }
            default: {
                System.out.println(role + " is still not implemented! Skipping...");
            }
        }
    }

    public static void launchAgents(List<String> roles, List<String> names, ContainerController container) throws StaleProxyException {
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

    public static void main(String[] args) {

        // Loads available names
        List<String> names;
        try {
            names = ConfigReader.importNames("src/resources/names.txt");
        } catch (IOException e) {
            System.out.println("An Error occurred while importing names from config. Aborting");
            return;
        }

        // Loads roles
        List<String> roles;
        try {
            roles = ConfigReader.importRoles("src/resources/gamemodes/test.txt");
            System.out.println("Number roles imported: " + roles.size());
        } catch (IOException e) {
            System.out.println("An Error occurred while importing roles from config. Aborting");
            return;
        }

        // Get a JADE runtime
        Runtime rt = Runtime.instance();

        // Create the main container
        Profile p1 = new ProfileImpl();
        ContainerController mainController = rt.createMainContainer(p1);

        // Create additional container
        Profile p2 = new ProfileImpl();
        ContainerController container = rt.createAgentContainer(p2);

        try {
            // Gui Agent
            AgentController gui = mainController.createNewAgent("GUI", "jade.tools.rma.rma", null);
//            gui.start();

            // Launch GameMaster
            AgentController gm_c = container.acceptNewAgent("GameMaster", new GameMaster(roles.size()));
            gm_c.start();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            launchAgents(roles, names, container);

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
