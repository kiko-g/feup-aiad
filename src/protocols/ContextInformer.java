package protocols;

import agents.GameMaster;
import jade.lang.acl.ACLMessage;
import sajas.proto.AchieveREInitiator;
public class ContextInformer extends AchieveREInitiator {

    GameMaster gameMaster;

    public ContextInformer(GameMaster gameMaster, ACLMessage msg) {
        super(gameMaster, msg);
        this.gameMaster = gameMaster;
    }

    @Override
    protected void handleAgree(ACLMessage agree) {
//        System.out.println(agree.getSender().getName() + " agreed to set context");
    }

    @Override
    protected void handleInform(ACLMessage inform) {
//        System.out.println(inform.getSender().getName() + " just finished setting context");
    }
}
