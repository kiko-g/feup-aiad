package protocols;

import agents.GameMaster;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

public class RoleDistributor extends AchieveREResponder {

    GameMaster gm;

    public RoleDistributor(GameMaster a, MessageTemplate mt) {
        super(a, mt);
        this.gm = a;
    }

    @Override
    protected ACLMessage handleRequest(ACLMessage request) throws RefuseException {
        if (request.getContent().equals("I need a role and a name!")) {
            ACLMessage agree = request.createReply();
            agree.setPerformative(ACLMessage.AGREE);
            agree.setContent(gm.getRemainingNames().poll() + ", the " + gm.getRemainingRoles().poll());

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
}
