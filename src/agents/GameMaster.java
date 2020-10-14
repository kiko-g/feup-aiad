package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

import java.util.*;

public class GameMaster extends Agent {
    int MAX_AGENTS;

    private Queue<String> remainingRoles;
    private Queue<String> remainingNames;

    public GameMaster(List<String> roles, List<String> names) {
        MAX_AGENTS = roles.size();

        // Role handling
        List<String> tempRoles = new ArrayList<>(roles);
        Collections.shuffle(tempRoles);
        this.remainingRoles = new LinkedList<>(tempRoles);

        // Name handling
        List<String> tempNames = new ArrayList<>(names);
        Collections.shuffle(tempNames);
        this.remainingNames = new LinkedList<>(tempNames);
    }

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

        setHandlers(template, this);
    }

    private void setHandlers(MessageTemplate template, GameMaster gm) {
        this.addBehaviour(new AchieveREResponder(this, template) {
            @Override
            protected ACLMessage handleRequest(ACLMessage request) throws RefuseException {
                if (request.getContent().equals("I need a role and a name!")) {
                    ACLMessage agree = request.createReply();
                    agree.setPerformative(ACLMessage.AGREE);
                    agree.setContent(gm.remainingNames.poll() + ", the " + gm.remainingRoles.poll());

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
