import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import agents.GameMaster;
import agents.mafia.Killing;
import agents.mafia.Leader;
import agents.neutral.Jester;
import agents.town.Detective;
import agents.town.Healer;
import agents.town.Villager;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;

import sajas.core.Runtime;
import sajas.sim.repast3.Repast3Launcher;
import sajas.wrapper.AgentController;
import sajas.wrapper.ContainerController;

import uchicago.src.sim.analysis.DataRecorder;
import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.Recorder;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import utils.ConfigReader;

public class GameLauncher extends Repast3Launcher {

	private static final boolean BATCH_MODE = true;

	private ContainerController mainContainer;
	private GameMaster gameMaster;


	private List<String> names;
	private List<String> roles;

	private boolean runInBatchMode;

	GameLauncher(boolean batchMode) {
		this.runInBatchMode = batchMode;

		try {
			this.names = ConfigReader.importNames("src/resources/names.txt"); // Loads available names
			this.roles = ConfigReader.importRoles("src/resources/gamemodes/test.txt"); // Loads roles
		} catch (IOException e) { e.printStackTrace(); }
	}

	@Override
	public String[] getInitParam() {
		return new String[0];
	}

	@Override
	public String getName() {
		return "SAJaS Project";
	}

	@Override
	protected void launchJADE() {

		Runtime rt = Runtime.instance();
		Profile p1 = new ProfileImpl();
		mainContainer = rt.createMainContainer(p1);

		launchAgents();
	}

	private void launchAgents() {

		try {
			// Launch GM
			this.gameMaster = new GameMaster(roles.size());
			mainContainer.acceptNewAgent("GameMaster", this.gameMaster).start();

			// Launch player agents

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
					launchAgent(role, name, mainContainer);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void launchAgent(String role, String name, ContainerController container) throws StaleProxyException {
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
			break;
		}
		}
	}

	@Override
	public void setup() {
		super.setup();

		// property descriptors
		// ...
	}

	@Override
	public void begin() {
		buildModel();

		super.begin();

		// Record Data if in batchMode only
		if(this.runInBatchMode) {
			getSchedule().scheduleActionAtEnd(new BasicAction() {
				@Override
				public void execute() {
					recorder.record();
					recorder.writeToFile();
				}
			});
		}

		// display surfaces, spaces, displays, plots, ...
		// ...
	}

	private DataRecorder recorder;

	class GameOutcomeSource implements DataSource {
		@Override
		public String execute() {
			return gameMaster.getGameStateExport();
		}
	}

	private void buildModel() {
		this.recorder = new DataRecorder("./gameOutcomes.csv", this);
		recorder.addObjectDataSource("gameOutcome", new GameOutcomeSource());
	}


	public static void main(String[] args) {
		boolean runMode = BATCH_MODE;
		SimInit init = new SimInit();
		init.setNumRuns(10); // works only in batch mode
		init.loadModel(new GameLauncher(runMode), null, runMode);
	}

}
