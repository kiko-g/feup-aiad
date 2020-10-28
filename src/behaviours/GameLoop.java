package behaviours;

import agents.GameMaster;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import protocols.ContextInformer;
import utils.ProtocolNames;
import utils.Util;

import java.util.List;

public class GameLoop extends Behaviour {
    GameMaster gameMaster;
    boolean endLoop = false;

    private boolean readyBehaviourAdded = false;
    private boolean nightBehaviourAdded = false;
    private boolean dayBehaviourAdded = false;

    public GameLoop(GameMaster gameMaster) {
        this.gameMaster = gameMaster;
    }

    @Override
    public void action() {
        switch (this.gameMaster.getGameState()) {

            case WAITING_FOR_PLAYERS: {
                return;
            }
            case READY: {
                if(!readyBehaviourAdded)
                    handleReady();
                break;
            }
            case DAY: {
                if(!dayBehaviourAdded)
                    handleDay();
                break;
            }
            case NIGHT: {
                if(!nightBehaviourAdded)
                    handleNight();
                break;
            }
            case END: {
                handleEnd();
                break;
            }
        }
    }

    private void handleReady() {
        // Finishes to complete the GameLobby info
        try {
            this.gameMaster.updateAgentInfo();
            System.out.println("Information successfully updated!");

            this.readyBehaviourAdded = true;
        } catch (FIPAException e) {
            System.out.println("Error finding and updating all players desc");
            this.gameMaster.takeDown();
        }
    }

    private void handleDay() {
        this.gameMaster.addBehaviour(new DayBehaviour(this.gameMaster));
        this.dayBehaviourAdded = true;
        this.nightBehaviourAdded = false;

        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleNight() {
        this.gameMaster.addBehaviour(new NightBehaviour(this.gameMaster));
        this.nightBehaviourAdded = true;
        this.dayBehaviourAdded = false;

        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleEnd() {
        // Something
        this.gameMaster.addBehaviour(new GameStateInformer(this.gameMaster, ProtocolNames.End));
        this.endLoop = true;
    }

    @Override
    public boolean done() {
        return this.endLoop;
    }

    @Override
    public int onEnd() {
        System.out.println("GameMaster shutting down!");
        return super.onEnd();
    }
}
