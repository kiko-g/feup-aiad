package agents.neutral;

import agents.PlayerAgent;
import behaviours.ChatListener;
import behaviours.GameStateListener;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import protocols.ContextWaiter;
import protocols.DecisionInformer;
import protocols.PlayerInformer;
import utils.ProtocolNames;

import java.util.List;
import java.util.Random;

public class Jester extends PlayerAgent
{
    @Override
    public String getRole() {
        return "Jester";
    }

    @Override
    protected void setup() {
        super.setup();

        // Agent Registration
        try {
            this.registerAgent(this.getRole());
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        // Agent tries to join the game's lobby
        ACLMessage msg = this.buildJoinMessage(this.getRole());

        // Handlers here
        this.addBehaviour(new PlayerInformer(this, msg));

        MessageTemplate playerNamesTemplate = MessageTemplate.and(
                MessageTemplate.MatchProtocol(ProtocolNames.PlayerNames),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );

        // Builds context
        this.addBehaviour(new ContextWaiter(this, playerNamesTemplate));

        // Reads and handles game state updates (Day/Night, PlayerDeaths...)
        this.addBehaviour(new GameStateListener(this));

        // Reads and stores messages posted by other agents
        this.addBehaviour(new ChatListener(this));
    }

    @Override
    public void setDayTimeBehavior() {
        // TODO: Post beliefs in chat

        MessageTemplate tmp = MessageTemplate.and(
                MessageTemplate.MatchProtocol(ProtocolNames.VoteTarget),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        // Handles ability target requests
        this.addBehaviour(new DecisionInformer(this, tmp));
    }

    @Override
    public void setNightTimeBehaviour() {
        // Nothing at all
    }

    @Override
    public ACLMessage handleDayVoteRequest(ACLMessage request, ACLMessage response) {
        List<String> alivePlayers = this.getGameContext().getAlivePlayers();

        Random r = new Random();
        int playerIndex = r.nextInt(alivePlayers.size());

        String playerForTrial = alivePlayers.get(playerIndex);

        ACLMessage inform = request.createReply();
        inform.setContent(playerForTrial);
        inform.setPerformative(ACLMessage.INFORM);

        return inform;
    }

    @Override
    public ACLMessage handleNightVoteRequest(ACLMessage request, ACLMessage response) {
        // Should not happen
        return null;
    }
}
