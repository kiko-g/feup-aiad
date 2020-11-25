package protocols;

import agents.GameMaster;
import sajas.core.Agent;
import jade.lang.acl.ACLMessage;
import sajas.proto.AchieveREInitiator;

public class MafiaInformer extends AchieveREInitiator {

    public MafiaInformer(GameMaster gameMaster, ACLMessage msg) {
        super(gameMaster, msg);
    }

    @Override
    protected void handleInform(ACLMessage inform) {
        super.handleInform(inform);
    }
}
