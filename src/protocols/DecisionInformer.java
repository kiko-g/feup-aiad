package protocols;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

public class DecisionInformer extends AchieveREResponder {

    public DecisionInformer(Agent a, MessageTemplate mt) {
        super(a, mt);
    }

    @Override
    protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
        System.out.println(request.getContent());

        ACLMessage agree = request.createReply();
        agree.setPerformative(ACLMessage.AGREE);

        return agree;
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
        inform.setContent("Ola");
        inform.setPerformative(ACLMessage.INFORM);

        return inform;
    }
}
