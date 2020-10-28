package behaviours;

import agents.GameMaster;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import protocols.ContextInformer;
import protocols.MafiaInformer;
import utils.ProtocolNames;
import utils.Util;

import java.util.List;

public class ReadyBehaviour extends SequentialBehaviour {

    GameMaster gameMaster;

    public ReadyBehaviour(GameMaster gameMaster) {
        this.gameMaster = gameMaster;

        List<String> players = this.gameMaster.getGameLobby().getAlivePlayerNames();
        StringBuilder messageContent = new StringBuilder();
        for(String currName : players) {
            messageContent.append(currName).append("\n");
        }

        ACLMessage msg = Util.buildMessage(ACLMessage.INFORM,
                ProtocolNames.PlayerNames,
                messageContent.toString()
        );

        // Adds every alive player as receiver
        msg = this.gameMaster.addReceiversMessage(msg, true);

        // Once this behaviour finishes, game loop state is updated
        this.addSubBehaviour(new ContextInformer(this.gameMaster, msg));

        // TODO - create messageContent

        ACLMessage mafiaMsg = Util.buildMessage(ACLMessage.INFORM,
                ProtocolNames.MafiaPlayers,
                );
        this.addSubBehaviour(new MafiaInformer(this.gameMaster, ));
    }


    @Override
    public int onEnd() {
        System.out.println("======> Night begins");
        this.gameMaster.setGameState(GameMaster.GameStates.NIGHT);

        return super.onEnd();
    }
}
