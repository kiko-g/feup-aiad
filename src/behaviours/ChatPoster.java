package behaviours;

import agents.PlayerAgent;
import sajas.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import utils.ChatMessage;
import utils.ProtocolNames;

import java.io.IOException;

public class ChatPoster extends OneShotBehaviour {

    private PlayerAgent playerAgent;
    private String templateMessage;
    private String messageContent;

    public ChatPoster(PlayerAgent playerAgent, String templateMessage, String messageContent) {
        this.playerAgent = playerAgent;
        this.templateMessage = templateMessage;
        this.messageContent = messageContent;
    }

    @Override
    public void action() {
        // Creates Chat Message to be sent
        ChatMessage cm = new ChatMessage(this.messageContent, this.templateMessage, this.playerAgent.getLocalName());

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
