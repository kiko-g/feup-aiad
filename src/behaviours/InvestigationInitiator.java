package behaviours;

import agents.GameMaster;
import sajas.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.ProtocolNames;

public class InvestigationInitiator extends Behaviour {

    private enum Steps {
        Init,
        WaitingTarget,
        Done
    };

    private Steps currentStep;
    private GameMaster gameMaster;

    private ACLMessage requestMessageSent;

    public InvestigationInitiator(GameMaster gameMaster) {
        this.gameMaster = gameMaster;
        this.currentStep = Steps.Init;
    }
    
    private MessageTemplate responseMessageTemplate = MessageTemplate.and(
            MessageTemplate.MatchProtocol(ProtocolNames.Investigate),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM)
    );

    private ACLMessage buildRequestMessage() {
        ACLMessage messageRequest = new ACLMessage(ACLMessage.REQUEST);
        messageRequest.setContent("Who will you visit tonight?");
        messageRequest.setProtocol(ProtocolNames.Investigate);

        // Max detectives on a game should be 1;
        messageRequest = this.gameMaster.addReceiversMessage(
                messageRequest,
                this.gameMaster.getGameLobby().getPlayersAIDRole("Detective", true)
        );

        this.requestMessageSent = messageRequest;
        return messageRequest;
    }

    private boolean handleMessage(ACLMessage msg) {
        // Only alive players should get here
        String playerName = msg.getContent();
        String playerRole = this.gameMaster.getGameLobby().getPlayerRole(playerName);
        boolean isSus = false;

        if(playerRole.equals("Leader") && this.gameMaster.getGameLobby().didAllKillingsDie())
            isSus = true;

        if(playerRole.equals("Healer") || playerRole.equals("Jester") || playerRole.equals("Killing"))
            isSus = true;

        return isSus;
    }

    @Override
    public void action() {
        switch (this.currentStep) {
            case Init: {
                ACLMessage request = this.buildRequestMessage();
                this.gameMaster.send(request);

                this.currentStep = Steps.WaitingTarget;
                break;
            }
            case WaitingTarget: {
                ACLMessage msg = this.gameMaster.receive(this.responseMessageTemplate);
                if(msg != null) {
                	boolean isSus = handleMessage(msg);

                    ACLMessage response = msg.createReply();
                    if (isSus) {
                        response.setContent("Kinda sus");
                        System.out.println(msg.getSender().getLocalName() + " chose to investigate " + msg.getContent() + ": Kinda sus");
                    }
                    else {
                        response.setContent("Not sus");
                        System.out.println(msg.getSender().getLocalName() + " chose to investigate " + msg.getContent() + ": Not sus!");
                    }

                    this.gameMaster.send(response);
                    this.currentStep = Steps.Done;
                } else block();
                
                break;
            }
        }
    }

    @Override
    public boolean done() {
        return this.currentStep == Steps.Done;
    }
}
