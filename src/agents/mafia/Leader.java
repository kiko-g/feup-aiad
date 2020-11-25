package agents.mafia;

import agents.PlayerAgent;
import behaviours.ChatListener;
import behaviours.GameStateListener;
import behaviours.TargetDictator;
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

public class Leader extends PlayerAgent {

    private List<String> killOrdersGiven = new ArrayList<>();

    public Leader(Util.Trait trait) {
        super(trait);
    }

    public Leader() {
        super();
    }

    @Override
    public String getRole() {
        return "Leader";
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
        // Resets killOrders backlog
        this.killOrdersGiven = new ArrayList<>();

        MessageTemplate tmp = MessageTemplate.and(
                MessageTemplate.MatchProtocol(ProtocolNames.VoteTarget),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        // Handles ability target requests
        this.addBehaviour(new DecisionInformer(this, tmp));
    }

    @Override
    public void setNightTimeBehaviour() {
        List<String> killingsAlive = this.getGameContext().getPlayerNamesByRole("Killing", true);
        if(killingsAlive.size() > 0) {
            // If there are killings, this agent waits for them to present themselves and then orders them
            for(int i = 0; i < killingsAlive.size(); i++)
                this.addBehaviour(new TargetDictator(this));
        }
        else {
            // If there are no more killings, this agent has the same behaviour as one
            MessageTemplate tmp = MessageTemplate.and(
                    MessageTemplate.MatchProtocol(ProtocolNames.TargetKilling),
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

            // Handles ability target requests
            this.addBehaviour(new DecisionInformer(this, tmp));
        }

        this.setPlayersKilledDuringNight(0);
        this.setPlayersSavedDuringNight(0);
    }

    @Override
    public ACLMessage handleNightVoteRequest(ACLMessage request, ACLMessage response) {
        // Only happens if/when there are no Mafia Killings alive
        List<String> killablePlayers = this.getGameContext().getAlivePlayers();
        List<String> mafiaPlayers = this.gameContext.getMafiaPlayerNames(false);
        int playerIndex;
        String playerName;

        if(new Random().nextInt(10) < 6) {
            do {
                playerName = getLessSuspectPlayers().get(new Random().nextInt(getLessSuspectPlayers().size()));
            } while (mafiaPlayers.contains(playerName) || !killablePlayers.contains(playerName));
            playerIndex = killablePlayers.indexOf(playerName);
        }
        else {
            do {
                Random r = new Random();
                playerIndex = r.nextInt(killablePlayers.size());
            } while (mafiaPlayers.contains(killablePlayers.get(playerIndex)));
        }

        String playerToKill = killablePlayers.get(playerIndex);

        ACLMessage inform = request.createReply();
        inform.setContent(playerToKill);
        inform.setPerformative(ACLMessage.INFORM);

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

    public ACLMessage handleKillOrder(ACLMessage request) {
        // Only happens if/when there are no Mafia Leaders alive
        List<String> killablePlayers = this.getGameContext().getAlivePlayers();
        List<String> mafiaPlayers = this.gameContext.getMafiaPlayerNames(false);

        String playerToKill = "";

        // Tries to get a target, if it has already been chosen, tries to get another. Max 3 times
        for(int i = 0; i < 3; i++) {
            int r1 = new Random().nextInt(10);
            if(r1 < 3) {
                do {
                    List<String> l = getLessSuspectPlayers();
                    playerToKill = getLessSuspectPlayers().get(new Random().nextInt(3));
                } while(mafiaPlayers.contains(playerToKill) || !killablePlayers.contains(playerToKill));
            }
            else {
                int playerIndex;
                do {
                    Random r = new Random();
                    playerIndex = r.nextInt(killablePlayers.size());
                    playerToKill = killablePlayers.get(playerIndex);
                } while (mafiaPlayers.contains(playerToKill));
            }

            if(!this.killOrdersGiven.contains(playerToKill))
                break;
        }

        ACLMessage inform = request.createReply();

        System.out.println("[Leader Decision] " + playerToKill);
        this.killOrdersGiven.add(playerToKill);

        // String msgContent = "I want you to unalive "+ ;
        inform.setContent("I want you to unalive " + playerToKill);
        inform.setPerformative(ACLMessage.INFORM);

        return inform;
    }
}
