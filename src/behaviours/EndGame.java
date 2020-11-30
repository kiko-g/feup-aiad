package behaviours;

import agents.GameMaster;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import sajas.core.behaviours.Behaviour;

public class EndGame extends Behaviour {

    private boolean finished;
    private GameMaster gameMaster;

    public EndGame(GameMaster gameMaster) {
        this.gameMaster = gameMaster;
        this.finished = false;
    }

    @Override
    public void action() {
        try {
            DFAgentDescription [] agentsUp = this.gameMaster.findAllPLayerDescriptions();
            if(agentsUp.length == 0) {
                this.finished = true;
                this.gameMaster.doDelete();
            }
            else
                block(500);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean done() {
        return finished;
    }
}
