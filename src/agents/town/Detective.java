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
import utils.Util;

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

    public Detective(Util.Trait trait) {
        super(trait);
        this.nightVisits = new ArrayList<>();
    }

    public Detective() {
        this.nightVisits = new ArrayList<>();
    }

    @Override
    public String getRole() {
        return "Detective";
    }

    @Override
    public void setDayTimeBehavior() {
        VisitReport lastReport = getLastNightReport();

        if(lastReport.isSus)
        {
            boolean isLeader = false;
            for(int i = 0; i < nightVisits.size() - 1; i++) {
                if(lastReport.playerName.equals(nightVisits.get(i).playerName) && !nightVisits.get(i).isSus) {
                    isLeader = true;
                    this.setPlayerSusRate(lastReport.playerName, 1000);
                    break;
                }
            }
            if(isLeader)
                this.addBehaviour(new ChatPoster(this, ChatMessageTemplate.DetectiveAcuseLeader,
                        ChatMessageTemplate.detectiveAcuseLeader(lastReport.playerName)));
            else
                this.addBehaviour(new ChatPoster(this, ChatMessageTemplate.DetectiveMessageHasActivity,
                    ChatMessageTemplate.detectiveMessageHasActivity(lastReport.playerName)));
        }
        else
            this.addBehaviour(new ChatPoster(this, ChatMessageTemplate.DetectiveMessageHasNoActivity,
                    ChatMessageTemplate.detectiveMessageHasNoActivity(lastReport.playerName)));

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

        this.setPlayersKilledDuringNight(0);
        this.setPlayersSavedDuringNight(0);

        for(String playerName : this.gameContext.getAlivePlayers()) {
            setPlayerSusRate(playerName, 1.05);
        }
    }

    // Who will be visited / investigated during night
    @Override
    public ACLMessage handleNightVoteRequest(ACLMessage request, ACLMessage response) {
        List<String> alivePlayers = this.getGameContext().getAlivePlayers();
        alivePlayers.remove(this.getLocalName());
        Random r;
        int playerIndex = 0;
        String playerForTrial;

        if(new Random().nextInt(10) < 7) {
            List<String> nonVisitedPlayers = new ArrayList<>(alivePlayers);
            for(String playerVisited : getPlayersVisited()) {
                nonVisitedPlayers.remove(playerVisited);
            }
            if(nonVisitedPlayers.size() == 0) {
                r = new Random();
                playerIndex = r.nextInt(alivePlayers.size());
            }
            else {
                for(int i = getMostSuspectPlayers(0.0).size() - 1; i >= 0; i--) {
                    if(nonVisitedPlayers.contains(getMostSuspectPlayers(0.0).get(i))) {
                        playerForTrial = getMostSuspectPlayers(0.0).get(i);
                        playerIndex = alivePlayers.indexOf(playerForTrial);
                        break;
                    }
                }
            }
        }
        else {
            r = new Random();
            playerIndex = r.nextInt(alivePlayers.size());
        }

        playerForTrial = alivePlayers.get(playerIndex);

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

    private List<String> getPlayersVisited() {
        List<String> players = new ArrayList<>();

        for(int i = 0; i < nightVisits.size(); i++) {
            players.add(nightVisits.get(i).playerName);
        }
        return players;
    }
}
