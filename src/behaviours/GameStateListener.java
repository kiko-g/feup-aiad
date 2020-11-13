package behaviours;

import agents.PlayerAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.ProtocolNames;
import utils.Util;


public class GameStateListener extends CyclicBehaviour {

    private final PlayerAgent playerAgent;

    public GameStateListener(PlayerAgent playerAgent) {
        this.playerAgent = playerAgent;
    }

    // Listening this type of messages only
    MessageTemplate mt = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.or(
                    MessageTemplate.MatchProtocol(ProtocolNames.PlayerDeath),
                    MessageTemplate.or(
                            MessageTemplate.MatchProtocol(ProtocolNames.TimeOfDay),
                            MessageTemplate.MatchProtocol(ProtocolNames.End)
                    )
            )
    );

    MessageTemplate mt2 = MessageTemplate.and(
        MessageTemplate.MatchProtocol(ProtocolNames.PlayerInfo),
        MessageTemplate.MatchPerformative(ACLMessage.REQUEST)
    );

    @Override
    public void action() {
        ACLMessage msg = this.playerAgent.receive(MessageTemplate.or(mt, mt2));
        if (msg != null) {
            switch (msg.getProtocol()) {
                case ProtocolNames.PlayerDeath: {
                    this.handlePlayerDeath(msg.getContent());
                    break;
                }
                case ProtocolNames.TimeOfDay: {
                    this.handleTimeOfDay(msg.getContent());
                    break;
                }
                case ProtocolNames.End: {
                    this.handleEndOfGame(msg.getContent());
                    break;
                }
                case ProtocolNames.PlayerInfo: {
                    this.handleSendPlayerInfo(msg);
                    break;
                }
            }
        }
    }

    private void handleSendPlayerInfo(ACLMessage msg) {
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.INFORM);

        String info = "trait#name:susRate#";
        reply.setContent(info);
        this.playerAgent.send(reply);
    }

    private void handleEndOfGame(String content) {
        String[] message = content.split(" ");
        String winnerFaction = message[0];

        boolean iAmWinner = winnerFaction.equals(
            Util.getFaction(this.playerAgent.getRole())
        );

        if(iAmWinner) {
            this.playerAgent.logMessage("GG IZI");
        } else {
            this.playerAgent.logMessage("Congratulations " + winnerFaction + "!");
        }

        this.playerAgent.takeDown();
    }

    private void handleTimeOfDay(String messageContent) {
        if (messageContent.equals("Day")) {
            this.playerAgent.setDay();
            this.playerAgent.setDayTimeBehavior();
        }
        else {
            this.playerAgent.setNight();
            this.playerAgent.setNightTimeBehaviour();
        }
    }

    private void handlePlayerDeath(String messageContent) {

        String[] names = messageContent.split("\n");

        for(String currentName : names) {
            if(!this.playerAgent.isDay())
                this.playerAgent.logMessage("I was informed that " + currentName + " just died... RIP " + currentName + ", I will always remember you!");

            // Updates GameContext a.k.a. personal player state
            this.playerAgent.buryPlayer(currentName);
        }
    }
}
