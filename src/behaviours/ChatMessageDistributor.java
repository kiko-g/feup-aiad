package behaviours;

import agents.GameMaster;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import utils.ChatMessage;
import utils.GlobalVars;
import utils.ProtocolNames;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChatMessageDistributor extends Behaviour {

    private GameMaster gameMaster;
    private long discussionBeginTime;

    private final MessageTemplate mt = MessageTemplate.and(
            MessageTemplate.MatchProtocol(ProtocolNames.Chat),
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST)
    );

    public ChatMessageDistributor(GameMaster gameMaster) {
        this.gameMaster = gameMaster;
        this.discussionBeginTime = System.currentTimeMillis();

        System.out.println("\t======> Discussion Begins (" + GlobalVars.DISCUSSION_MAX_TIME_SECONDS + "s)");
    }

    @Override
    public void action() {
        ACLMessage msg = this.gameMaster.receive(mt);
        if(msg != null) {
            // Received Message object content
            ChatMessage cm;
            try {
                cm = (ChatMessage) msg.getContentObject();

                // Timestamps the reception time
                cm.stampReceptionTime();

                // Prints comment
                System.out.println("\t" + cm);

                // Create new message
                ACLMessage retransmission = new ACLMessage(ACLMessage.INFORM);
                retransmission.setProtocol(ProtocolNames.Chat);
                retransmission.setContentObject(cm);

                // Add receivers (every player alive except the sender)
                List<AID> alivePlayers = this.gameMaster.getGameLobby().getAlivePlayersAID();
                alivePlayers.remove(msg.getSender());

                retransmission = this.gameMaster.addReceiversMessage(retransmission, alivePlayers);

                // Send
                this.gameMaster.send(retransmission);
            } catch (UnreadableException | IOException e) {
                System.out.println("Error getting/setting message content object");
                e.printStackTrace();
            }
        }
        else block(500);
    }

    @Override
    public boolean done() {
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - this.discussionBeginTime;

        // Max discussion time is validated here
        return TimeUnit.MILLISECONDS.toSeconds(deltaTime) > GlobalVars.DISCUSSION_MAX_TIME_SECONDS;
    }

    @Override
    public int onEnd() {
        System.out.println("\t======> Discussion End");
        return super.onEnd();
    }
}
