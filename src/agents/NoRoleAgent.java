package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import protocols.RoleRequester;

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

        this.addBehaviour(new RoleRequester(this, msg));
    }
}
