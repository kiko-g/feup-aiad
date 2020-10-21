package behaviours;

import agents.GameMaster;
import jade.core.AID;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import protocols.DecisionRequester;

public class NightBehaviour extends SequentialBehaviour {

    GameMaster gameMaster;

    public NightBehaviour(GameMaster gameMaster) {
        this.gameMaster = gameMaster;

        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(gameMaster.getGameLobby().getFirstRole("Villager"));
        msg.setProtocol("Target");
        msg.setContent("Handle night content Village");

        this.addSubBehaviour(new DecisionRequester(gameMaster, msg));

        ACLMessage msg2 = new ACLMessage(ACLMessage.REQUEST);

        msg2.addReceiver(gameMaster.getGameLobby().getFirstRole("Killing"));
        msg2.setProtocol("TargetKilling");
        msg2.setContent("Handle night content Killing");

        this.addSubBehaviour(new DecisionRequester(gameMaster, msg2));
    }

    @Override
    public int onEnd() {
        // Apply requests
        System.out.println("Night is over!");
        this.gameMaster.setGameState(GameMaster.GameStates.DAY);
        return 1;
    }
}
