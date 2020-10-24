package behaviours;

import agents.GameMaster;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import utils.ProtocolNames;
import utils.Util;

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

        switch (this.typeInfo) {
            case ProtocolNames.PlayerDeath: {
                this.sendDeadPlayersList();
                break;
            }
            case ProtocolNames.TimeOfDay: {
                this.sendTimeOfDay();
                break;
            }
        }
    }

    private void sendTimeOfDay() {
        String content = (this.gameMaster.getGameState().equals(GameMaster.GameStates.DAY)) ? "Day" : "Night";

        ACLMessage msg = Util.buildMessage(ACLMessage.INFORM,
                ProtocolNames.TimeOfDay, content);

        this.gameMaster.sendMessageAlivePlayers(msg);
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
