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

    boolean nightBehaviourAdded = false;
    private boolean dayBehaviourAdded = false;
    private boolean readyBehaviourAdded = false;

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

            List<String> players = this.gameMaster.getGameLobby().getAlivePlayerNames();
            StringBuilder messageContent = new StringBuilder();
            for(String currName : players) {
                messageContent.append(currName).append("\n");
            }

            ACLMessage msg = Util.buildMessage(ACLMessage.INFORM,
                    ProtocolNames.PlayerNames,
                    messageContent.toString()
            );

            // Adds every alive player as receiver
            msg = this.gameMaster.addReceiversMessage(msg, true);

            // Once this behaviour finishes, gameloop state is updated
            this.gameMaster.addBehaviour(new ContextInformer(this.gameMaster, msg));

            System.out.println("======> Just sent Player names");

            this.readyBehaviourAdded = true;
        } catch (FIPAException e) {
            System.out.println("Error finding and updating all players desc");
            this.gameMaster.takeDown();
        }
    }

    private void handleDay() {
        // Informs Alive agents about who died last night
        this.gameMaster.addBehaviour(new GameStateInformer(this.gameMaster, ProtocolNames.PlayerDeath));

        //TODO: Handle voting...

        this.dayBehaviourAdded = true;
    }

    private void handleNight() {
        this.gameMaster.addBehaviour(new NightBehaviour(this.gameMaster));
        this.nightBehaviourAdded = true;
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
