package behaviours;

import agents.GameMaster;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import protocols.DecisionRequester;
import utils.ProtocolNames;

import static utils.Util.buildMessage;

public class NightBehaviour extends SequentialBehaviour {

    GameMaster gameMaster;

    public NightBehaviour(GameMaster gameMaster) {
        this.gameMaster = gameMaster;

        System.out.println("\n---------- Night is begins ----------");

        // Informs alive agents about the current time of day
        this.addSubBehaviour(new GameStateInformer(this.gameMaster, ProtocolNames.TimeOfDay));

        // Town Detective -> Mafia -> Town Healer

        // Town Detective
        if(this.gameMaster.getGameLobby().getPlayersAIDRole("Detective", true).size() > 0) //There are Detectives alive
            this.addSubBehaviour(new InvestigationInitiator(this.gameMaster));

        // Mafia

        // If there is (are) Killing(s) alive, the request is sent to it (them)
        // If not, the leader is the one that receives the request
        this.addSubBehaviour(new TargetKillingOrchestrator(this.gameMaster));

        // Town Healer
        ACLMessage msgHealer = buildMessage(ACLMessage.REQUEST,
                ProtocolNames.TargetHealing,
                "Who will you visit tonight?"
        );

        msgHealer = this.gameMaster.addReceiversMessage(
                msgHealer,
                this.gameMaster.getGameLobby().getPlayersAIDRole("Healer", true)
        );

        this.addSubBehaviour(new DecisionRequester(gameMaster, msgHealer));


        this.addSubBehaviour(new NightResultsCalculator(this.gameMaster));
    }

    @Override
    public int onEnd() {

        String winner = this.gameMaster.getWinnerFaction();

        if (winner == null) {
            System.out.println("---------- Night is over! ----------");
            this.gameMaster.setGameState(GameMaster.GameStates.DAY);
        }
        else {
            this.gameMaster.setGameState(GameMaster.GameStates.END);
            System.out.println("========== Game is over! ==========");
            System.out.println(winner + " won the game!\n");
        }

        return super.onEnd();
    }
}
