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
import utils.GlobalVars;
import utils.ProtocolNames;
import utils.Util;

import java.util.ArrayList;
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
    protected void postSetup() {
        super.postSetup();

        MessageTemplate mafiaNamesTemplate = MessageTemplate.and(
                MessageTemplate.MatchProtocol(ProtocolNames.MafiaPlayers),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );

        // Stores the mafia team
        this.addBehaviour(new MafiaWaiter(this, mafiaNamesTemplate));
    }

    @Override
    public void setDayTimeBehavior() {
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
        else this.addBehaviour(new TargetKilling(this));

        this.setPlayersKilledDuringNight(0);
        this.setPlayersSavedDuringNight(0);
    }

    // Last proposal was rejected
    public ACLMessage handleNightVoteRequestRejected(ACLMessage request, List<String> rejectedNames) {
        List<String> killablePlayers = this.getGameContext().getAlivePlayers();
        List<String> mafiaPlayers = this.gameContext.getMafiaPlayerNames(false);
        String playerName;

        for(String mafiaPlayer : mafiaPlayers)
            killablePlayers.remove(mafiaPlayer);

        for(String rejected : rejectedNames)
            killablePlayers.remove(rejected);

        // No possible choice to make ==> Skip
        if(killablePlayers.size() == 0) {
            ACLMessage inform = request.createReply();
            inform.setContent("Skip");
            inform.setPerformative(ACLMessage.INFORM);
            return inform;
        }

        int playerIndex;

        if(new Random().nextInt(10) < 3) {
            do{
                playerName = getLessSuspectPlayers().get(new Random().nextInt(getLessSuspectPlayers().size()));
            } while (!killablePlayers.contains(playerName));
            playerIndex = killablePlayers.indexOf(playerName);
        }
        else {
            Random r = new Random();
            playerIndex = r.nextInt(killablePlayers.size());
        }

        String playerToKill = killablePlayers.get(playerIndex);

        ACLMessage inform = request.createReply();
        inform.setContent(playerToKill);
        inform.setPerformative(ACLMessage.PROPOSE);

        return inform;
    }

    @Override
    public ACLMessage handleDayVoteRequest(ACLMessage request, ACLMessage response) {
        printSusRate();

        List<String> mostSusPlayersWithoutMafia = new ArrayList<>();
        List<String> mafiaPlayers = this.gameContext.getMafiaPlayerNames(true);
        List<String> mostSusPlayers = getMostSuspectPlayers(GlobalVars.VOTE_MIN_SUS_VALUE);
        String content;

        if(mostSusPlayers.size() > 0) {
            for (String playerName : getMostSuspectPlayers(GlobalVars.VOTE_MIN_SUS_VALUE)) {
                if (!mafiaPlayers.contains(playerName)) {
                    mostSusPlayersWithoutMafia.add(playerName);
                }
            }
        }

        if(mostSusPlayersWithoutMafia.size() > 0) {
            Random r = new Random();
            int playerIndex = r.nextInt(mostSusPlayersWithoutMafia.size());
            content = mostSusPlayersWithoutMafia.get(playerIndex);
        }
        else
            content = "Skip";

        ACLMessage inform = request.createReply();
        inform.setContent(content);
        inform.setPerformative(ACLMessage.INFORM);

        return inform;
    }
}
