package behaviours;

import agents.PlayerAgent;
import sajas.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import utils.ChatMessage;
import utils.ProtocolNames;

public class ChatListener extends CyclicBehaviour {

    private PlayerAgent playerAgent;

    private MessageTemplate mt = MessageTemplate.and(
        MessageTemplate.MatchProtocol(ProtocolNames.Chat),
        MessageTemplate.MatchPerformative(ACLMessage.INFORM)
    );

    public ChatListener(PlayerAgent playerAgent) {
        this.playerAgent = playerAgent;
    }

    @Override
    public void action() {
        ACLMessage msg = this.playerAgent.receive(mt);
        if (msg != null) {
            try {
                ChatMessage cm = (ChatMessage) msg.getContentObject();

                // Stores it
                this.playerAgent.addToChatLog(cm);

                this.playerAgent.handleChatMsg(cm);

            } catch (UnreadableException e) {
                e.printStackTrace();
            }
        } else block(1000);
    }
}
