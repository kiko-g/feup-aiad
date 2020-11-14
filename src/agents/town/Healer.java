package agents.town;

import agents.PlayerAgent;
import behaviours.ChatListener;
import behaviours.ChatPoster;
import behaviours.GameStateListener;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import protocols.ContextWaiter;
import protocols.DecisionInformer;
import protocols.PlayerInformer;
import utils.ChatMessageTemplate;
import utils.GlobalVars;
import utils.ProtocolNames;
import utils.Util;

import java.util.List;
import java.util.Random;

public class Healer extends PlayerAgent {

    private String playerSavedLastNight;

    public Healer(Util.Trait trait) {
        super(trait);
        this.playerSavedLastNight = "";
    }

    public Healer() {
        super();
        this.playerSavedLastNight = "";
    }

    @Override
    public String getRole() {
        return "Healer";
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
        this.playerSavedLastNight = "";

        MessageTemplate tmp = MessageTemplate.and(
                MessageTemplate.MatchProtocol(ProtocolNames.TargetHealing),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        // Handles ability target requests
        this.addBehaviour(new DecisionInformer(this, tmp));

        this.setPlayersKilledDuringNight(0);
        this.setPlayersSavedDuringNight(0);

        for(String playerName : this.gameContext.getAlivePlayers()) {
            setPlayerSusRate(playerName, 1.05);
        }
    }

    @Override
    public ACLMessage handleDayVoteRequest(ACLMessage request, ACLMessage response) {
        if(!playerSavedLastNight.equals(""))
            setPlayerSusRate(playerSavedLastNight, 0);

        return super.handleDayVoteRequest(request, response);
    }

    @Override
    public ACLMessage handleNightVoteRequest(ACLMessage request, ACLMessage response) {
        List<String> alivePlayers = this.getGameContext().getAlivePlayers();
        String playerToSave;

        do {
            playerToSave = getLessSuspectPlayers().get(new Random().nextInt(2));
        } while (!alivePlayers.contains(playerToSave));

        ACLMessage inform = request.createReply();
        inform.setContent(playerToSave);
        inform.setPerformative(ACLMessage.INFORM);

        return inform;
    }

    public String getPlayerSavedLastNight() {
        return playerSavedLastNight;
    }

    public void setPlayerSavedLastNight(String playerSavedLastNight) {
        this.playerSavedLastNight = playerSavedLastNight;
    }
}
