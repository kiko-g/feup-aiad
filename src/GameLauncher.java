import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

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
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Network2DDisplay;
import uchicago.src.sim.gui.OvalNetworkItem;
import uchicago.src.sim.network.DefaultDrawableNode;
import utils.ConfigReader;

public class GameLauncher extends Repast3Launcher {

	private static final boolean BATCH_MODE = true;

	private ContainerController mainContainer;
	private GameMaster gameMaster;


	private List<String> names;
	private List<String> roles;

	private boolean runInBatchMode;

	private static List<DefaultDrawableNode> nodes;

	GameLauncher(boolean batchMode) {
		this.runInBatchMode = batchMode;
		nodes = new ArrayList<DefaultDrawableNode>();

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
					launchAgent(role, name, mainContainer, i);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private DefaultDrawableNode generateNode(String label, Color color, int x, int y) {
		OvalNetworkItem oval = new OvalNetworkItem(x,y);
		oval.allowResizing(false);
		oval.setHeight(60);
		oval.setWidth(60);
		oval.setLabelColor(Color.BLACK);

		DefaultDrawableNode node = new DefaultDrawableNode(label, oval);
		node.setColor(color);

		return node;
	}

	public void launchAgent(String role, String name, ContainerController container, int agentNumber) throws StaleProxyException {
		AgentController ac;
		Random random = new Random(System.currentTimeMillis());
		double agentAnglePos = (2 * Math.PI / roles.size());

		switch (role) {
		case "Villager": {
			Villager villager = new Villager();
			ac = container.acceptNewAgent(name, villager);
			ac.start();
			DefaultDrawableNode node =
					generateNode("Villager", Color.GREEN,
							(int) (Math.cos(agentAnglePos * agentNumber) * 250) + WIDTH/2, (int) (Math.sin(agentAnglePos * agentNumber) * 250) + HEIGHT/2);
			nodes.add(node);
			break;
		}
		case "Killing": {
			ac = container.acceptNewAgent(name, new Killing());
			ac.start();
			DefaultDrawableNode node =
					generateNode("Killing", Color.RED,
							(int) (Math.cos(agentAnglePos * agentNumber) * 250) + WIDTH/2, (int) (Math.sin(agentAnglePos * agentNumber) * 250) + HEIGHT/2);
			nodes.add(node);
			break;
		}
		case "Leader": {
			ac = container.acceptNewAgent(name, new Leader());
			ac.start();
			DefaultDrawableNode node =
					generateNode("Leader", Color.RED,
							(int) (Math.cos(agentAnglePos * agentNumber) * 250) + WIDTH/2, (int) (Math.sin(agentAnglePos * agentNumber) * 250) + HEIGHT/2);
			nodes.add(node);
			break;
		}
		case "Jester": {
			ac = container.acceptNewAgent(name, new Jester());
			ac.start();
			DefaultDrawableNode node =
					generateNode("Jester", Color.WHITE,
							(int) (Math.cos(agentAnglePos * agentNumber) * 250) + WIDTH/2, (int) (Math.sin(agentAnglePos * agentNumber) * 250) + HEIGHT/2);
			nodes.add(node);
			break;
		}
		case "Healer": {
			ac = container.acceptNewAgent(name, new Healer());
			ac.start();
			DefaultDrawableNode node =
					generateNode("Healer", Color.GREEN,
							(int) (Math.cos(agentAnglePos * agentNumber) * 250) + WIDTH/2, (int) (Math.sin(agentAnglePos * agentNumber) * 250) + HEIGHT/2);
			nodes.add(node);
			break;
		}
		case "Detective": {
			ac = container.acceptNewAgent(name, new Detective());
			ac.start();
			DefaultDrawableNode node =
					generateNode("Detective", Color.GREEN,
							(int) (Math.cos(agentAnglePos * agentNumber) * 250) + WIDTH/2, (int) (Math.sin(agentAnglePos * agentNumber) * 250) + HEIGHT/2);
			nodes.add(node);
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
		else {
			buildAndScheduleDisplay();
		}
	}

	private DisplaySurface dsurf;
	private int WIDTH = 800, HEIGHT = 750;

	private void buildAndScheduleDisplay() {
		// display surface
		if (dsurf != null) dsurf.dispose();
		dsurf = new DisplaySurface(this, "Service Consumer/Provider Display");
		registerDisplaySurface("Service Consumer/Provider Display", dsurf);
		Network2DDisplay display = new Network2DDisplay(nodes,WIDTH,HEIGHT);
		dsurf.addDisplayableProbeable(display, "Network Display");
		dsurf.addZoomable(display);
		addSimEventListener(dsurf);
		dsurf.display();

		//getSchedule().scheduleActionAtInterval(1, dsurf, "updateDisplay", Schedule.LAST);
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
		boolean runMode = !BATCH_MODE;
		SimInit init = new SimInit();
		init.setNumRuns(10); // works only in batch mode
		init.loadModel(new GameLauncher(runMode), null, runMode);
	}

}
