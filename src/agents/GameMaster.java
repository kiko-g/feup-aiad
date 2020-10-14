package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

public class GameMaster  extends Agent {

    @Override
    protected void setup() {
        // DF
        DFAgentDescription dfad = new DFAgentDescription();
        dfad.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName("AgentType");
        sd.setType("GameMaster");
        dfad.addServices(sd);

        try {
            DFService.register(this, dfad);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST) );
        setHandlers(template);
    }

    private void setHandlers(MessageTemplate template) {
        this.addBehaviour(new AchieveREResponder(this, template){
            @Override
            protected ACLMessage handleRequest(ACLMessage request) throws RefuseException {
                if (request.getContent().equals("I need a role and a name!")) {
                    ACLMessage agree = request.createReply();
                    agree.setPerformative(ACLMessage.AGREE);

                    System.out.println("Request received!");

                    return agree;
                } else {
                    throw new RefuseException("Request not valid!");
                }
            }

            @Override
            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
//                if (performAction()) {
//                    System.out.println("Agent "+getLocalName()+": Action successfully performed");
//                    ACLMessage inform = request.createReply();
//                    inform.setPerformative(ACLMessage.INFORM);
//                    return inform;
//                }
//                else {
//                    System.out.println("Agent "+getLocalName()+": Action failed");
//                    throw new FailureException("unexpected-error");
//                }
                ACLMessage inform = request.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                return inform;
            }
        });
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
}
