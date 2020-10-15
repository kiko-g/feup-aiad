package agents.mafia;

import agents.PlayerAgent;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import protocols.RoleInformer;

public class Killing extends PlayerAgent {

    @Override
    public String getRole() {
        return "Killing";
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
        this.addBehaviour(new RoleInformer(this, msg));
    }

    @Override
    public void takeDown() {
        this.deregisterAgent();
        System.out.println("Killing shutdown!");
    }
}
