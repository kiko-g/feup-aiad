package behaviours;

import agents.GameMaster;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import utils.ProtocolNames;

import java.util.List;

public class GameStateInformer extends OneShotBehaviour {

    GameMaster gameMaster;

    // Kind of message to send
    String typeInfo;

    // The protocol name decides what message is sent!!
    public GameStateInformer(GameMaster gameMaster, String protocolName) {
        this.gameMaster = gameMaster;
        this.typeInfo = protocolName;
    }

    @Override
    public void action() {
        if (this.typeInfo.equals(ProtocolNames.PlayerDeath)) {
            this.sendDeadPlayersList();
        }
    }

    private void sendDeadPlayersList() {
        List<String> deadPlayerNames = this.gameMaster.getGameLobby().getDeadPlayerNames();

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setProtocol(ProtocolNames.PlayerDeath);

        StringBuilder messageContent = new StringBuilder();
        for(String currName : deadPlayerNames) {
            messageContent.append(currName).append("\n");
        }

        msg.setContent(messageContent.toString());

        this.gameMaster.sendMessageAlivePlayers(msg);
    }
}
