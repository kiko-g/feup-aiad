package agents.town;

import agents.PlayerAgent;
import behaviours.ChatListener;
import behaviours.GameStateListener;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import protocols.ContextWaiter;
import protocols.DecisionInformer;
import protocols.PlayerInformer;
import utils.GlobalVars;
import utils.ProtocolNames;
import utils.Util;

import java.util.List;
import java.util.Random;

public class Villager extends PlayerAgent {

    public Villager(Util.Trait trait) {
        super(trait);
    }

    public Villager() {
        super();
    }

    @Override
    public String getRole() {
        return "Villager";
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
