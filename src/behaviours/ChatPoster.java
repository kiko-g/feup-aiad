package behaviours;

import agents.PlayerAgent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import utils.ChatMessage;
import utils.ProtocolNames;

import java.io.IOException;

public class ChatPoster extends OneShotBehaviour {

    private PlayerAgent playerAgent;
    private String messageContent;

    public ChatPoster(PlayerAgent playerAgent, String messageContent) {
        this.playerAgent = playerAgent;
        this.messageContent = messageContent;
    }

    @Override
    public void action() {
        // Creates Chat Message to be sent
        ChatMessage cm = new ChatMessage(this.messageContent, this.playerAgent.getLocalName());

        // Creates ACLMessage and puts ChatMessage as objectContent
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.setProtocol(ProtocolNames.Chat);
        try {
            msg.setContentObject(cm);

            // Sets destination (GameMaster)
            msg.addReceiver(this.playerAgent.getGameMasterAID());

            // Sends
            this.playerAgent.send(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
