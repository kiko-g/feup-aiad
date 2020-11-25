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
import utils.Util;

import java.util.List;
import java.util.Random;

public class Jester extends PlayerAgent
{
    public Jester(Util.Trait trait) {
        super(trait);
    }

    public Jester() {
        super();
    }

    @Override
    public String getRole() {
        return "Jester";
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
        this.setPlayersKilledDuringNight(0);
        this.setPlayersSavedDuringNight(0);

        for(String playerName : this.gameContext.getAlivePlayers()) {
            setPlayerSusRate(playerName, 1.05);
        }
    }
}
