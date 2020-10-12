package behaviours;

import agents.TestAgent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class TestBehaviour extends Behaviour {

    private TestAgent agent;

    public TestBehaviour(TestAgent agent) {
        this.agent = agent;
    }

    @Override
    public void action() {
        this.agent.setState(this.agent.getState() - 1);

        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setName("Role");
        sd.setType("Villager");
        template.addServices(sd);

        //Search for service 'Role'
        try {
            DFAgentDescription[] searchResults = DFService.search(this.agent, template);

            System.out.println("I found "+ searchResults.length + " Role services!");

        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean done() {
        System.out.println("Im done working!");
        return this.agent.getState() == -1;
    }

}
