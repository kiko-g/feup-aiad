package launcher;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import agents.GameMaster;
import agents.PlayerAgent;
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

import uchicago.src.sim.analysis.*;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Network2DDisplay;
import uchicago.src.sim.gui.OvalNetworkItem;
import uchicago.src.sim.network.DefaultDrawableNode;
import utils.ConfigReader;
import utils.Edge;
import utils.Util;

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
		nodes = new ArrayList<>();

		try {
			this.names = ConfigReader.importNames("src/resources/names.txt"); // Loads available names
		} catch (IOException e) { e.printStackTrace(); }

		this.roles = new ArrayList<>();
	}

	private Util.Trait playerTrait = Util.Trait.Mild;
	private boolean isTraitRandom = false;

	private boolean spawnLeader = true;
	private boolean spawnDetective = true;
	private boolean spawnJester = true;

	private int numberVillagers = 10;
	private int numberKillings = 2;
	private int numberHealers = 2;

	public String getPlayerTrait() {
		return playerTrait.toString();
	}

	public void setPlayerTrait(String playerTrait) {
		switch (playerTrait.toLowerCase()) {
			case "overtheline": {
				this.playerTrait = Util.Trait.OverTheLine;
				break;
			}
			case "agressive": {
				this.playerTrait = Util.Trait.Agressive;
				break;
			}
			case "mild": {
				this.playerTrait = Util.Trait.Mild;
				break;
			}
			case "peaceful": {
				this.playerTrait = Util.Trait.Peaceful;
				break;
			}
			default: {

				break;
			}
		}
	}

	public boolean getIsTraitRandom() {
		return isTraitRandom;
	}

	public void setIsTraitRandom(boolean traitRandom) {
		isTraitRandom = traitRandom;
	}

	public boolean isSpawnLeader() {
		return spawnLeader;
	}

	public void setSpawnLeader(boolean spawnLeader) {
		this.spawnLeader = spawnLeader;
	}

	public boolean isSpawnDetective() {
		return spawnDetective;
	}

	public void setSpawnDetective(boolean spawnDetective) {
		this.spawnDetective = spawnDetective;
	}

	public boolean isSpawnJester() {
		return spawnJester;
	}

	public void setSpawnJester(boolean spawnJester) {
		this.spawnJester = spawnJester;
	}

	public int getNumberVillagers() {
		return numberVillagers;
	}

	public void setNumberVillagers(int numberVillagers) {
		this.numberVillagers = Math.min(numberVillagers, 13);
	}

	public int getNumberKillings() {
		return numberKillings;
	}

	public void setNumberKillings(int numberKillings) {
		this.numberKillings = Math.min(numberKillings, 4);
	}

	public int getNumberHealers() {
		return numberHealers;
	}

	public void setNumberHealers(int numberHealers) {
		this.numberHealers = Math.min(numberHealers, 4);
	}

    @Override
	public String[] getInitParam() {
		return new String[] {"playerTrait", "isTraitRandom", "spawnLeader",
				"spawnDetective", "spawnJester", "numberVillagers", "numberKillings", "numberHealers"};
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
			this.roles = new ArrayList<>();

			// Builds roles list
			if(spawnDetective) this.roles.add("Detective");
			if(spawnJester) this.roles.add("Jester");
			if(spawnLeader) this.roles.add("Leader");

			for(int v = 0; v < numberVillagers; v++)
				this.roles.add("Villager");

			for(int h = 0; h < numberHealers; h++)
				this.roles.add("Healer");

			for(int k = 0; k < numberKillings; k++)
				this.roles.add("Killing");

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
		oval.setWidth(90);
		oval.setLabelColor(Color.BLACK);

		DefaultDrawableNode node = new DefaultDrawableNode(label, oval);
		node.setColor(color);

		return node;
	}

	public void launchAgent(String role, String name, ContainerController container, int agentNumber) throws StaleProxyException {
		AgentController ac;
		double agentAnglePos = (2 * Math.PI / roles.size());
		int x = (int) (WIDTH/2.2);
		int y = (int) (HEIGHT/2.2);

		switch (role) {
			case "Villager": {
				Villager villager;

				if(isTraitRandom) villager = new Villager();
				else villager = new Villager(playerTrait);

				ac = container.acceptNewAgent(name, villager);
				ac.start();
				DefaultDrawableNode node =
						generateNode(Util.buildNodeLabel(name, "Villager"), Color.GREEN,
								(int) (Math.cos(agentAnglePos * agentNumber) * 350) + x, (int) (Math.sin(agentAnglePos * agentNumber) * 300) + y);
				nodes.add(node);
				break;
			}
			case "Killing": {
				Killing killing;

				if(isTraitRandom) killing = new Killing();
				else killing = new Killing(playerTrait);

				ac = container.acceptNewAgent(name, killing);
				ac.start();
				DefaultDrawableNode node =
						generateNode(Util.buildNodeLabel(name, "Killing"), Color.RED,
								(int) (Math.cos(agentAnglePos * agentNumber) * 350) + x, (int) (Math.sin(agentAnglePos * agentNumber) * 300) + y);
				nodes.add(node);
				break;
			}
			case "Leader": {
				Leader leader;

				if(isTraitRandom) leader = new Leader();
				else leader = new Leader(playerTrait);

				ac = container.acceptNewAgent(name, leader);
				ac.start();
				DefaultDrawableNode node =
						generateNode(Util.buildNodeLabel(name, "Leader"), Color.RED,
								(int) (Math.cos(agentAnglePos * agentNumber) * 350) + x, (int) (Math.sin(agentAnglePos * agentNumber) * 300) + y);
				nodes.add(node);
				break;
			}
			case "Jester": {
				Jester jester;

				if(isTraitRandom) jester = new Jester();
				else jester = new Jester(playerTrait);

				ac = container.acceptNewAgent(name, jester);
				ac.start();
				DefaultDrawableNode node =
						generateNode(Util.buildNodeLabel(name, "Jester"), Color.WHITE,
								(int) (Math.cos(agentAnglePos * agentNumber) * 350) + x, (int) (Math.sin(agentAnglePos * agentNumber) * 300) + y);
				nodes.add(node);
				break;
			}
			case "Healer": {
				Healer healer;

				if(isTraitRandom) healer = new Healer();
				else healer = new Healer(playerTrait);

				ac = container.acceptNewAgent(name, healer);
				ac.start();
				DefaultDrawableNode node =
						generateNode(Util.buildNodeLabel(name, "Healer"), Color.GREEN,
								(int) (Math.cos(agentAnglePos * agentNumber) * 350) + x, (int) (Math.sin(agentAnglePos * agentNumber) * 300) + y);
				nodes.add(node);
				break;
			}
			case "Detective": {
				Detective detective;

				if(isTraitRandom) detective = new Detective();
				else detective = new Detective(playerTrait);

				ac = container.acceptNewAgent(name, detective);
				ac.start();
				DefaultDrawableNode node =
						generateNode(Util.buildNodeLabel(name, "Detective"), Color.GREEN,
								(int) (Math.cos(agentAnglePos * agentNumber) * 350) + x, (int) (Math.sin(agentAnglePos * agentNumber) * 300) + y);
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
	private int WIDTH = 1000, HEIGHT = 750;
	private OpenSequenceGraph plot;

	private void buildAndScheduleDisplay() {
		// display surface
		if (dsurf != null) dsurf.dispose();
		dsurf = new DisplaySurface(this, "Night interactions");
		registerDisplaySurface("Night interactions", dsurf);
		Network2DDisplay display = new Network2DDisplay(nodes,WIDTH,HEIGHT);
		dsurf.addDisplayableProbeable(display, "Night interactions");
		dsurf.addZoomable(display);
		addSimEventListener(dsurf);
		dsurf.display();

		// graph
		if (plot != null) plot.dispose();
		plot = new OpenSequenceGraph("Day votes", this);
		plot.setAxisTitles("time", "Number of votes");

		for(int i = 0; i < names.size(); i++) {
			int finalI = i;
			plot.addSequence(names.get(i), new Sequence() {
				public double getSValue() {
					return gameMaster.getVotingResults().getOrDefault(names.get(finalI), 0);
				}
			});
		}
		plot.addSequence("Skip", new Sequence() {
			public double getSValue() {
				return gameMaster.getVotingResults().getOrDefault("Skip", 0);
			}
		});

		plot.display();

		getSchedule().scheduleActionAtInterval(1, dsurf, "updateDisplay", Schedule.LAST);
		getSchedule().scheduleActionAtInterval(10, plot, "step", Schedule.LAST);
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
		init.setNumRuns(2); // works only in batch mode
		init.loadModel(new GameLauncher(runMode), null, runMode);
	}

	public static DefaultDrawableNode getNode(String localName) {
		for(DefaultDrawableNode node : nodes) {
			if(node.getNodeLabel().equals(localName)) {
				return node;
			}
		}
		return null;
	}

	public static DefaultDrawableNode getNodeByAgentName(String agentName) {
		for(DefaultDrawableNode node : nodes) {
			String currLabel = node.getNodeLabel();
			if(currLabel.substring(0, currLabel.indexOf(",")).equals(agentName)) {
				return node;
			}
		}
		return null;
	}

	public static void paintNodeBlack(DefaultDrawableNode node) {
		int nodeIndex = nodes.indexOf(node);

		if(nodeIndex != -1) {
			DefaultDrawableNode deadPlayerNode = nodes.get(nodeIndex);
			deadPlayerNode.setColor(Color.BLACK);
			deadPlayerNode.setLabelColor(Color.BLACK);
			deadPlayerNode.setY(1000);
		}
	}

	public static void paintNodeBlackByAgentName(String agentName) {
		paintNodeBlack(getNodeByAgentName(agentName));
	}

	public static void removeAllNodeEdges(DefaultDrawableNode originNode) {
		removeOutEdges(originNode);
		removeInEdges(originNode);
	}

	public static void removeOutEdges(DefaultDrawableNode originNode) {
		List<Edge> outEdges = new ArrayList<>(originNode.getOutEdges());
		for(Edge curr : outEdges) {
			originNode.removeOutEdge(curr);
		}
	}

	public static void removeInEdges(DefaultDrawableNode originNode) {
		for(DefaultDrawableNode curr : nodes) {
			curr.removeEdgesTo(originNode);
		}
	}

	public static void removeAllNodeEdgesByAgentName(String agentName) {
		removeAllNodeEdges(getNodeByAgentName(agentName));
	}
}
