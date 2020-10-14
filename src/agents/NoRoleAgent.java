package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

public class NoRoleAgent extends Agent {
    DFAgentDescription game_master_desc;

    public void logMessage(String msg) {
        String senderID = this.getName();
        String finalMessage = "[" + senderID + "] " + msg;
        System.out.println(finalMessage);
    }

    @Override
    protected void setup() {

        // Searches for GameMaster
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setName("AgentType");
        sd.setType("GameMaster");
        template.addServices(sd);

        // Search results handling
        try {
            DFAgentDescription[] search_results = DFService.search(this, template);
            if (search_results.length != 1) {
                this.logMessage("Error finding the GameMaster!");
                this.takeDown();
                return;
            }
            else this.game_master_desc = search_results[0];
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        // Role Request Message
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(this.game_master_desc.getName());
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        msg.setContent("I need a role and a name!");

        this.logMessage("Setting handlers");

        setHandlers(msg);
    }

    private void setHandlers(ACLMessage msg) {
        addBehaviour(new AchieveREInitiator(this, msg) {

            @Override
            protected void handleRefuse(ACLMessage refuse) {
                System.out.println("Agent "+refuse.getSender().getName()+" refused to perform the requested action");
            }

            @Override
            protected void handleAgree(ACLMessage agree) {
                System.out.println("Agent "+agree.getSender().getName()+" agreed to perform the requested action");
            }

            @Override
            protected void handleFailure(ACLMessage failure) {
                if (failure.getSender().equals(myAgent.getAMS())) {
                    // FAILURE notification from the JADE runtime: the receiver
                    // does not exist
                    System.out.println("Responder does not exist");
                }
                else {
                    System.out.println("Agent "+failure.getSender().getName()+" failed to perform the requested action");
                }
            }

            @Override
            protected void handleInform(ACLMessage inform) {
                System.out.println("Agent "+inform.getSender().getName()+" successfully performed the requested action");
            }

        });
    }
}
