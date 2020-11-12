package agents;

import behaviours.GameLoop;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import protocols.PlayerWaiter;
import utils.GameLobby;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
    private HashMap<String, String> savedPlayers; // Players visited by Healer during night
    private HashMap<String, String> actuallySavedPlayers; // Players attacked and then saved by Healer

    private List<String> nightDeaths; // Players that were attacked and not saved

    private String dayDeath;
    private boolean jesterDayDeath = false;

    public GameMaster(int numberPlayers) {
        this.gameLobby = new GameLobby(numberPlayers);
        this.gameState = GameStates.WAITING_FOR_PLAYERS;

        this.attackedPlayers = new ArrayList<>();
        this.savedPlayers = new HashMap<>();
        this.actuallySavedPlayers = new HashMap<>();
        this.nightDeaths = new ArrayList<>();
        this.dayDeath = "";
    }

    @Override
    protected void setup() {
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

    @Override
    public void takeDown() {
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

    private DFAgentDescription[] findAllPLayerDescriptions() throws FIPAException {
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

    public HashMap<String, String> getSavedPlayers() {
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

    public void setSavedPlayers(HashMap<String, String> savedPlayers) {
        this.savedPlayers = savedPlayers;
    }

    public void setActuallySavedPlayers(HashMap<String, String> actuallySavedPlayers) {
        this.actuallySavedPlayers = actuallySavedPlayers;
    }

    public HashMap<String, String> getActuallySavedPlayers() {
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
}