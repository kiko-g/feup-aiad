package protocols;

import agents.PlayerAgent;
import jade.lang.acl.ACLMessage;
import sajas.proto.AchieveREInitiator;

public class PlayerInformer extends AchieveREInitiator {

    private final PlayerAgent agent;

    public PlayerInformer(PlayerAgent pa, ACLMessage msg) {
        super(pa, msg);
        this.agent = pa;
    }

    @Override
    protected void handleRefuse(ACLMessage refuse) {
        this.agent.logMessage("Agent "+refuse.getSender().getName()+" refused to perform the requested action");
    }

    @Override
    protected void handleAgree(ACLMessage agree) {
//        this.agent.logMessage("Agent "+agree.getSender().getName()+" has accepted me in his GameLobby");
    }

    @Override
    protected void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            // FAILURE notification from the JADE runtime: the receiver
            // does not exist
            this.agent.logMessage("Responder does not exist");
        }
        else {
            this.agent.logMessage("Agent "+failure.getSender().getName()+" failed to perform the requested action");
        }
    }

    @Override
    protected void handleInform(ACLMessage inform) {
        // At this point, GameMaster has already all the information needed about this agent
//        this.agent.logMessage("Im in to the game!");
    }
}
