package behaviours;

import agents.PlayerAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.ProtocolNames;


public class GameStateListener extends CyclicBehaviour {

    private final PlayerAgent playerAgent;

    public GameStateListener(PlayerAgent playerAgent) {
        this.playerAgent = playerAgent;
    }

    // Listening this type of messages only
    MessageTemplate mt = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchProtocol(ProtocolNames.PlayerDeath)
    );

    @Override
    public void action() {
        ACLMessage msg = this.playerAgent.receive(mt);
        if (msg != null) {
            if (msg.getProtocol().equals(ProtocolNames.PlayerDeath)) {
                this.handlePlayerDeaths(msg.getContent());
            }
        }
    }

    private void handlePlayerDeaths(String messageContent) {

        String[] deadPlayerNames = messageContent.split("\n");

        for(String currName : deadPlayerNames) {
            this.playerAgent.logMessage("I was informed that " + currName + " just died... RIP " + currName + ", I will always remember you!");

            // Updates GameContext a.k.a. personal player state
            this.playerAgent.buryPlayer(currName);
        }
    }
}
