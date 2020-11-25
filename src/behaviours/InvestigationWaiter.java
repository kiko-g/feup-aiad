package behaviours;

import agents.town.Detective;
import sajas.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.ProtocolNames;

public class InvestigationWaiter extends Behaviour {

    private enum Steps {
        Init,
        WaitingResponse,
        Done
    };

    private final Detective detective;
    private Steps currentStep;

    private ACLMessage targetMessage;
    
    private MessageTemplate requestTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
            MessageTemplate.MatchProtocol(ProtocolNames.Investigate)
    );

    private MessageTemplate resultTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchProtocol(ProtocolNames.Investigate)
    );
    
    public InvestigationWaiter(Detective detective) {
        this.detective = detective;
        this.currentStep = Steps.Init;
    }

    @Override
    public void action() {

        switch (this.currentStep) {
            case Init: {
                ACLMessage requestMessage = this.detective.receive(this.requestTemplate);

                if(requestMessage != null) {
                	targetMessage = this.detective.handleNightVoteRequest(requestMessage, null);
                    this.detective.send(targetMessage);

                    this.currentStep = Steps.WaitingResponse;
                }
                else block();
                
                break;
            }
            case WaitingResponse: {
                ACLMessage investigationResult = this.detective.receive(this.resultTemplate);
                if(investigationResult != null) {
                	boolean isSus = investigationResult.getContent().equals("Kinda sus");
                    this.detective.addVisit(this.targetMessage.getContent(), isSus);

                    if(!isSus)
                        this.detective.setPlayerSusRate(this.targetMessage.getContent(), 0.8);
                    else
                        this.detective.setPlayerSusRate(this.targetMessage.getContent(), 1.2);

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
