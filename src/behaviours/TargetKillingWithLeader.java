package behaviours;

import agents.mafia.Killing;
import sajas.core.behaviours.Behaviour;
import sajas.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.ProtocolNames;

public class TargetKillingWithLeader extends Behaviour {

    private enum Steps {
        Presentation,
        Execution,
        Done
    }

    private ACLMessage targetRequest;
    private Killing killingAgent;
    private Steps step;

    MessageTemplate gmRequestTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
            MessageTemplate.MatchProtocol(ProtocolNames.TargetKilling)
    );

    MessageTemplate leaderOrderTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchProtocol(ProtocolNames.LeaderOrder)
    );

    public TargetKillingWithLeader(Killing killingAgent) {
        this.killingAgent = killingAgent;
        this.step = Steps.Presentation;
    }

    @Override
    public void action() {
//        ACLMessage msg = this.killingAgent.receive(
//                MessageTemplate.or(gmRequestTemplate, leaderOrderTemplate));
//
//        if (msg == null)
//            block();
//        else {
//            if(step == Steps.Presentation)
//                handlePresentation(msg);
//            else
//                handleExecution(msg);
//        }

        if(step == Steps.Presentation) {
            ACLMessage msg = this.killingAgent.receive(gmRequestTemplate);
            if(msg != null)
                handlePresentation(msg);
            else
                block();
        } else {
            ACLMessage msg = this.killingAgent.receive(leaderOrderTemplate);
            if(msg != null)
                handleExecution(msg);
            else block();
        }
    }

    private void handlePresentation(ACLMessage gmRequest) {
        // Saves GM Request to reply to it later
        this.targetRequest = gmRequest;

        // Prepares
        ACLMessage presentation = new ACLMessage(ACLMessage.INFORM);
        presentation.setProtocol(ProtocolNames.LeaderOrder);
        presentation.setContent("I am " + killingAgent.getLocalName() + ", your loyal soldier. What do you want me to do Boss?");

        // Finds leader
        DFAgentDescription leader = this.findLeader();

        if(leader != null)
            presentation.addReceiver(leader.getName());

        this.killingAgent.send(presentation);
        this.step = Steps.Execution;
    }

    private void handleExecution(ACLMessage leaderOrder) {
        // msg format: "I want you to unalive "<name>
        String[] contentWords = leaderOrder.getContent().split(" ");
        String target = contentWords[contentWords.length - 1];

        ACLMessage inform = this.targetRequest.createReply();
        inform.setContent(target);
        inform.setPerformative(ACLMessage.PROPOSE);

        this.killingAgent.send(inform);
        this.step = Steps.Done;
    }

    private DFAgentDescription findLeader() {
        // Searches for Leader
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setName("AgentType");
        sd.setType("Player");

        ServiceDescription sd2 = new ServiceDescription();
        sd.setName("Role");
        sd.setType("Leader");

        template.addServices(sd);
        template.addServices(sd2);

        DFAgentDescription[] search_results;
        try {
            search_results = DFService.search(this.killingAgent, template);
            return search_results[0];
        } catch (FIPAException e) {
            System.out.println("Error finding Mafia Leader");
            return null;
        }
    }

    @Override
    public boolean done() {
        return step == Steps.Done;
    }
}
