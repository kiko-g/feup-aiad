package protocols;

import agents.GameMaster;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

public class DecisionRequester extends AchieveREInitiator {
    GameMaster gameMaster;
    boolean answerReceived = false;

    public DecisionRequester(GameMaster gameMaster, ACLMessage msg) {
        super(gameMaster, msg);
        this.gameMaster = gameMaster;
    }

    @Override
    protected void handleAgree(ACLMessage agree) {
        System.out.println("Agent "+agree.getSender().getName()+" is now thinking about it's answer");
    }

    @Override
    protected void handleInform(ACLMessage inform) {
        System.out.println("Agent "+inform.getSender().getName()+" has decided to kill " + inform.getContent());
        this.gameMaster.getGameLobby().killPlayer(inform.getContent());
    }
}
