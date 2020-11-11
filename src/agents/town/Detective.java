package agents.town;

import agents.PlayerAgent;
import behaviours.ChatListener;
import behaviours.ChatPoster;
import behaviours.GameStateListener;
import behaviours.InvestigationWaiter;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import protocols.ContextWaiter;
import protocols.DecisionInformer;
import protocols.PlayerInformer;
import utils.ChatMessage;
import utils.ChatMessageTemplate;
import utils.ProtocolNames;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Detective extends PlayerAgent {

    // Each element contains the report of the <index> night
    private List<VisitReport> nightVisits;

    // Contains the player visited and whether or not he was suspicious
    static class VisitReport {
        private final String playerName;
        private final boolean isSus;

        public VisitReport(String playerVisited, boolean isSus) {
            this.playerName = playerVisited;
            this.isSus = isSus;
        }

        public String getPlayerName() {
            return playerName;
        }

        public boolean isSus() {
            return isSus;
        }
    }

    public Detective() {
        this.nightVisits = new ArrayList<>();
    }

    @Override
    public String getRole() {
        return "Detective";
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

        this.addBehaviour(new ChatPoster(this, ChatMessageTemplate.RevealRole, ChatMessageTemplate.revealRole("Detective")));

        MessageTemplate tmp = MessageTemplate.and(
                MessageTemplate.MatchProtocol(ProtocolNames.VoteTarget),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        // Handles ability target requests
        this.addBehaviour(new DecisionInformer(this, tmp));
    }

    @Override
    public void setNightTimeBehaviour() {
        // Handles ability target requests
        this.addBehaviour(new InvestigationWaiter(this));
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

    // Who will be visited / investigated during night
    @Override
    public ACLMessage handleNightVoteRequest(ACLMessage request, ACLMessage response) {
        List<String> alivePlayers = this.getGameContext().getAlivePlayers();

        Random r = new Random();
        int playerIndex = r.nextInt(alivePlayers.size());

        String playerForTrial = alivePlayers.get(playerIndex);

        ACLMessage inform = request.createReply();
        inform.setContent(playerForTrial);
        inform.setPerformative(ACLMessage.INFORM);

        return inform;
    }

    private VisitReport getLastNightReport() {
        return this.nightVisits.get(this.nightVisits.size() - 1);
    }

    public String lastNightVisitName() {
        return getLastNightReport().getPlayerName();
    }

    public boolean lastNightVisitIsSus() {
        return getLastNightReport().isSus();
    }

    public void addVisit(String visitedPlayer, boolean isSus) {
        this.nightVisits.add(new VisitReport(visitedPlayer, isSus));
    }
}
