import agents.GameMaster;
import agents.NoRoleAgent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import utils.ConfigReader;

import java.io.IOException;
import java.util.List;

public class GameLauncher {

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
            roles = ConfigReader.importRoles("src/resources/roles.txt");
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
            AgentController gui =
                    mainController.createNewAgent("GUI", "jade.tools.rma.rma", null);
            gui.start();

            // Launch GameMaster
            AgentController gm_c = container.acceptNewAgent("GameMaster", new GameMaster(roles, names));
            gm_c.start();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // DEV ONLY
            for (int i = 0; i < 1; i++) {
                //Launch Player 1
                AgentController ac_1 = container.acceptNewAgent("Player" + (i + 1), new NoRoleAgent());
                ac_1.start();
            }

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
