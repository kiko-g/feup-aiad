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
                playerName = getLessSuspectPlayers().get(new Random().nextInt(3));
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
