package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import protocols.PlayerWaiter;
import utils.GameLobby;

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
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST) );

        this.addBehaviour(new PlayerWaiter(this, template));
    }

    @Override
    protected void takeDown() {
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
}
