package agents.mafia;

import agents.PlayerAgent;
import behaviours.*;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import protocols.ContextWaiter;
import protocols.DecisionInformer;
import protocols.MafiaWaiter;
import protocols.PlayerInformer;
import utils.ProtocolNames;
import utils.Util;

import java.util.List;
import java.util.Random;

public class Killing extends PlayerAgent {

    public Killing(Util.Trait trait) {
        super(trait);
    }

    public Killing() {
        super();
    }

    @Override
    public String getRole() {
        return "Killing";
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

        // Reports role to gameMaster
        this.addBehaviour(new PlayerInformer(this, msg));


        MessageTemplate playerNamesTemplate = MessageTemplate.and(
                MessageTemplate.MatchProtocol(ProtocolNames.PlayerNames),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );

        // Builds context
        this.addBehaviour(new ContextWaiter(this, playerNamesTemplate));

        MessageTemplate mafiaNamesTemplate = MessageTemplate.and(
                MessageTemplate.MatchProtocol(ProtocolNames.MafiaPlayers),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );

        // Stores the mafia team
        this.addBehaviour(new MafiaWaiter(this, mafiaNamesTemplate));

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

        List<String> leaders = this.getGameContext().getPlayerNamesByRole("Leader", true);
        if(leaders.size() > 0) {
            // If the leader is alive, this agent waits for
            // the gm to request a target, and presents itself to the leader
            this.addBehaviour(new TargetKillingWithLeader(this));
        }
        else {
//            MessageTemplate tmp = MessageTemplate.and(
//                    MessageTemplate.MatchProtocol(ProtocolNames.TargetKilling),
//                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
//
//            // Handles ability target requests
//            this.addBehaviour(new DecisionInformer(this, tmp));
            this.addBehaviour(new TargetKilling(this));
        }
    }

    @Override
    // Only happens if/when there are no Mafia Leaders alive
    public ACLMessage handleNightVoteRequest(ACLMessage request, ACLMessage response) {
        List<String> killablePlayers = this.getGameContext().getAlivePlayers();

        Random r = new Random();
        int playerIndex = r.nextInt(killablePlayers.size());

        String playerToKill = killablePlayers.get(playerIndex);

        ACLMessage inform = request.createReply();
        inform.setContent(playerToKill);
        inform.setPerformative(ACLMessage.INFORM);

        return inform;
    }

    // Last proposal was rejected
    public ACLMessage handleNightVoteRequestRejected(ACLMessage request, List<String> rejectedNames) {
        ACLMessage inform = request.createReply();

        List<String> killablePlayers = this.getGameContext().getAlivePlayers();

        for(String currRejectedPlayerName : rejectedNames)
            killablePlayers.remove(currRejectedPlayerName);

        if(killablePlayers.size() == 0) {
            inform.setContent("Skip");
            inform.setPerformative(ACLMessage.PROPOSE);
            return inform;
        }

        Random r = new Random();
        int playerIndex = r.nextInt(killablePlayers.size());

        String playerToKill = killablePlayers.get(playerIndex);


        inform.setContent(playerToKill);
        inform.setPerformative(ACLMessage.PROPOSE);

        return inform;
    }

    @Override
    public ACLMessage handleDayVoteRequest(ACLMessage request, ACLMessage response) {
        // Person to kill during night
        List<String> killablePlayers = this.getGameContext().getAlivePlayers();

        Random r = new Random();
        int playerIndex = r.nextInt(killablePlayers.size());

        String playerToKill = killablePlayers.get(playerIndex);

        ACLMessage inform = request.createReply();
        inform.setContent(playerToKill);
        inform.setPerformative(ACLMessage.INFORM);

        return inform;
    }
}
