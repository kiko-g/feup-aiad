package protocols;

import agents.GameMaster;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

public class DecisionRequester extends AchieveREInitiator {

    GameMaster gameMaster;

    public DecisionRequester(GameMaster gameMaster, ACLMessage msg) {
        super(gameMaster, msg);
        this.gameMaster = gameMaster;
    }

    @Override
    protected void handleAgree(ACLMessage agree) {
        System.out.println("Agent "+agree.getSender().getName()+" has accepted me in his GameLobby");
    }

    @Override
    protected void handleInform(ACLMessage inform) {
        System.out.println(inform.getContent());
    }
}