package agents;

import behaviours.TestBehaviour;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class TestAgent extends Agent {

    private int state = 20;

    @Override
    protected void setup() {

        // DF
        DFAgentDescription dfad = new DFAgentDescription();
        dfad.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName("Role");
        sd.setType("Villager");
        dfad.addServices(sd);

        try{
            DFService.register(this, dfad);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        this.addBehaviour(new TestBehaviour(this));
        System.out.println("Hello world from Test agent");
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
