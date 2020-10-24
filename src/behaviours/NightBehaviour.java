package behaviours;

import agents.GameMaster;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import protocols.DecisionRequester;
import utils.ProtocolNames;

import static utils.Util.createMessage;

public class NightBehaviour extends SequentialBehaviour {

    GameMaster gameMaster;

    public NightBehaviour(GameMaster gameMaster) {
        this.gameMaster = gameMaster;

        // Informs alive agents about the current time of day
        this.addSubBehaviour(new GameStateInformer(this.gameMaster, ProtocolNames.TimeOfDay));

        // Town Detective -> Mafia -> Town Healer

        // Town Detective
//        ACLMessage msg = createMessage(ACLMessage.REQUEST,
//                gameMaster.getGameLobby().getFirstRole("Detective"),
//                "TargetDetective", "Handle night content Village");
//
//        this.addSubBehaviour(new DecisionRequester(gameMaster, msg));

        // Mafia
        ACLMessage msg2 = createMessage(ACLMessage.REQUEST,
                gameMaster.getGameLobby().getFirstRole("Killing"),
                ProtocolNames.TargetKilling, "Who do you want to kill this night?");

        this.addSubBehaviour(new DecisionRequester(gameMaster, msg2));

        // Town Healer
//        ACLMessage msg3 = createMessage(ACLMessage.REQUEST,
//                gameMaster.getGameLobby().getFirstRole("Healer"),
//                "TargetHealer", "Handle night content Killing");
//
//        this.addSubBehaviour(new DecisionRequester(gameMaster, msg3));

    }

    @Override
    public int onEnd() {
        // Apply requests
        System.out.println("======> Night is over!");
        this.gameMaster.setGameState(GameMaster.GameStates.DAY);
        return 1;
    }
}
