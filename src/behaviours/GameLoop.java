package behaviours;

import agents.GameMaster;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import protocols.DecisionRequester;

public class GameLoop extends Behaviour {
    GameMaster gameMaster;
    boolean endLoop = false;

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
                handleReady();
                break;
            }
            case DAY: {
                handleDay();
                break;
            }
            case NIGHT: {
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

            this.gameMaster.setGameState(GameMaster.GameStates.NIGHT);
        } catch (FIPAException e) {
            System.out.println("Error finding and updating all players desc");
            this.gameMaster.takeDown();
        }
    }

    private void handleDay() {
        // Something
    }

    private void handleNight() {
        // Town Detective -> Mafia -> Town Healer
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(this.gameMaster.getGameLobby().getFirstRole("Villager"));
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        msg.setContent("Handle night content");

        this.gameMaster.addBehaviour(new DecisionRequester(this.gameMaster, msg));

    }

    private void handleEnd() {
        // Something

        this.endLoop = true;
    }

    @Override
    public boolean done() {
        return this.endLoop;
    }
}
