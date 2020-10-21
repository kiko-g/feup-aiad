package agents.town;

import agents.PlayerAgent;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import protocols.DecisionInformer;
import protocols.PlayerInformer;

public class Villager extends PlayerAgent {

    @Override
    public String getRole() {
        return "Villager";
    }

    @Override
    protected void setup() {
        super.setup();

        // Agent Registration
        try {
            this.registerAgent(this.getRole());
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        // Agent tries to join the game's lobby
        ACLMessage msg = this.buildJoinMessage(this.getRole());

        // Handlers here
        this.addBehaviour(new PlayerInformer(this, msg));

        MessageTemplate tmp = MessageTemplate.and(
            MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        this.addBehaviour(new DecisionInformer(this, tmp));
    }

    @Override
    public void takeDown() {
        this.deregisterAgent();
        System.out.println("Villager shutdown!");
    }
}
