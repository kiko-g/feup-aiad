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

import java.util.List;

public class GameMaster extends Agent {

    public enum GameStates {
        WAITING_FOR_PLAYERS,
        READY,
        DAY,
        NIGHT,
        END
    }
    //waker behavior
    private GameStates gameState;
    private GameLobby gameLobby;

    public GameMaster(int numberPlayers) {
        this.gameLobby = new GameLobby(numberPlayers);
        this.gameState = GameStates.WAITING_FOR_PLAYERS;
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

    public ACLMessage addReceiversMessage(ACLMessage message, boolean alive) {
        List<DFAgentDescription> players = (alive) ? this.gameLobby.getAlivePlayers() : this.gameLobby.getDeadPlayers();

        for(DFAgentDescription curr : players) {
            message.addReceiver(curr.getName());
        }

        return message;
    }

    public ACLMessage addReceiversMessageRole(ACLMessage message, String role) {
        List<DFAgentDescription> players = this.gameLobby.getPlayersRole(role);

        for(DFAgentDescription curr : players) {
            message.addReceiver(curr.getName());
        }

        return message;
    }
}