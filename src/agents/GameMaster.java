package agents;

import behaviours.GameLoop;
import jade.core.AID;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import sajas.core.Agent;
import sajas.core.Runtime;
import sajas.core.behaviours.Behaviour;
import sajas.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import protocols.PlayerWaiter;
import utils.ChatMessage;
import utils.GameLobby;
import utils.ProtocolNames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GameMaster extends Agent {

    public enum GameStates {
        WAITING_FOR_PLAYERS,
        READY,
        DAY,
        NIGHT,
        END
    }

    private GameStates gameState;
    private GameLobby gameLobby;

    private List<String> attackedPlayers; // Players attacked by Mafia

    // key: player saved
    // value: who saved it
    private ConcurrentHashMap<String, String> savedPlayers; // Players visited by Healer during night
    private ConcurrentHashMap<String, String> actuallySavedPlayers; // Players attacked and then saved by Healer

    private List<String> nightDeaths; // Players that were attacked and not saved

    private List<ChatMessage> chatLog;

    // Day time voting register
    private ConcurrentHashMap<String, Integer> votingResults;

    private String dayDeath;
    private boolean jesterDayDeath = false;

    private List<Behaviour> allBehaviours;

    public GameMaster(int numberPlayers) {
        this.gameLobby = new GameLobby(numberPlayers);
        this.gameState = GameStates.WAITING_FOR_PLAYERS;

        this.attackedPlayers = new ArrayList<>();
        this.savedPlayers = new ConcurrentHashMap<>();
        this.actuallySavedPlayers = new ConcurrentHashMap<>();
        this.nightDeaths = new ArrayList<>();
        this.dayDeath = "";
        this.votingResults = new ConcurrentHashMap<>();

        this.chatLog = new ArrayList<>();

        this.allBehaviours = new ArrayList<>();
    }

    @Override
    protected void setup() {
    	super.setup();
    	
        // DF
        DFAgentDescription dfad = new DFAgentDescription();
        dfad.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName("AgentType");
        sd.setType("GameMaster");
        dfad.addServices(sd);

        try {
            DFService.register(this, dfad);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol("Join"),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST) );

        this.addBehaviour(new PlayerWaiter(this, template));
        this.addBehaviour(new GameLoop(this));
    }

    protected void deregisterAgent() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    public GameLobby getGameLobby() {
        return gameLobby;
    }

    public GameStates getGameState() {
        return gameState;
    }

    public void setGameState(GameStates gameState) {
        this.gameState = gameState;
    }

    public ConcurrentHashMap<String, Integer> getVotingResults() {
        return votingResults;
    }

    public void setVotingResults(ConcurrentHashMap<String, Integer> votingResults) {
        this.votingResults = votingResults;
    }

    public DFAgentDescription[] findAllPLayerDescriptions() throws FIPAException {
        // Searches for registered players
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setName("AgentType");
        sd.setType("Player");
        template.addServices(sd);

        // Search results handling
        return DFService.search(this, template);
    }

    public void updateAgentInfo() throws FIPAException {
        DFAgentDescription[] allDesc = this.findAllPLayerDescriptions();
        this.gameLobby.setDescriptions(allDesc);
    }

    public void multicastMessage(ACLMessage message, AID[] receivers) {
        for(AID cur : receivers) {
            message.addReceiver(cur);
        }
        send(message);
    }

    public void sendMessageAlivePlayers(ACLMessage message) {
        send(addReceiversMessage(message, true));
    }

    public void sendMessageAllPlayers(ACLMessage msg) {
        send(
                addReceiversMessage(addReceiversMessage(msg, true), false)
        );
    }

    public ACLMessage addReceiversMessage(ACLMessage message, boolean alive) {
        List<AID> players = (alive) ? this.gameLobby.getAlivePlayersAID() : this.gameLobby.getDeadPlayersAID();

        for(AID curr : players) {
            message.addReceiver(curr);
        }

        return message;
    }

    public ACLMessage addReceiversMessage(ACLMessage message, List<AID> receivers) {

        for(AID agent : receivers)
            message.addReceiver(agent);

        return message;
    }

    public String getDayDeath() {
        return dayDeath;
    }

    public void setDayDeath(String dayDeath) {
        this.dayDeath = dayDeath;
    }

    public List<String> getNightDeaths() {
        return nightDeaths;
    }

    public void setNightDeaths(List<String> nightDeaths) {
        this.nightDeaths = nightDeaths;
    }

    public void addNightDeath(String name) {
        this.nightDeaths.add(name);
    }

    public String getWinnerFaction() {

        if(isJesterDayDeath())
            return "Jester";

        // Parsing
        int[] nPlayers = this.gameLobby.getNumberPlayersPerFactions();
        int nTown = nPlayers[0];
        int nMafia = nPlayers[1];

        if (nTown == 0)
            return "Mafia";
        else if(nMafia == 0)
            return "Town";
        else
            return null;
    }

    public boolean isJesterDayDeath() {
        return jesterDayDeath;
    }

    public void jesterDiedDuringDay() {
        this.jesterDayDeath = true;
    }

    public ConcurrentHashMap<String, String> getSavedPlayers() {
        return savedPlayers;
    }

    public String getPlayerSavior(String savedPlayerName) {
        return this.savedPlayers.get(savedPlayerName);
    }

    public void addSavedPlayer(String savedPlayer, String saviour) {
        this.savedPlayers.put(savedPlayer, saviour);
    }

    public void addActuallySavedPlayer(String savedPlayer, String saviour) {
        this.actuallySavedPlayers.put(savedPlayer, saviour);
    }

    public void setSavedPlayers(ConcurrentHashMap<String, String> savedPlayers) {
        this.savedPlayers = savedPlayers;
    }

    public void setActuallySavedPlayers(ConcurrentHashMap<String, String> actuallySavedPlayers) {
        this.actuallySavedPlayers = actuallySavedPlayers;
    }

    public ConcurrentHashMap<String, String> getActuallySavedPlayers() {
        return this.actuallySavedPlayers;
    }

    public List<String> getAttackedPlayers() {
        return attackedPlayers;
    }

    public void setAttackedPlayers(List<String> attackedPlayers) {
        this.attackedPlayers = attackedPlayers;
    }

    public void addAttackedPlayer(String attackedPlayer) {
        this.attackedPlayers.add(attackedPlayer);
    }
    
    public void addToLog(ChatMessage cm) {
        this.chatLog.add(cm);
    }

    @Override
    public void takeDown() {
        deregisterAgent();
        super.takeDown();

        // Container kill
        try {
            this.getContainerController().kill();
            this.getContainerController().getPlatformController().kill();
        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addBehaviour(Behaviour b) {
        super.addBehaviour(b);
        this.allBehaviours.add(b);
    }

    public void removeAllBehaviours() {
        for(Behaviour b : this.allBehaviours)
            if(!b.done())
                this.removeBehaviour(b);
    }

    // Format: <Who won the game>,#Vilagers,#Healers,#Detectives,#Jesters,#Killings,#Leaders\n
    public String getGameStateExport() {
        StringBuilder result = new StringBuilder();

        // Format: #Vilagers,#Healers,#Detectives,#Jesters,#Killings,#Leaders
        String nPlayerRole = this.gameLobby.getNumberAlivePerRole();
        String winner = this.getWinnerFaction();

        // String building
        result.append(winner).append(",");
        result.append(nPlayerRole);

        return result.toString();
    }
}