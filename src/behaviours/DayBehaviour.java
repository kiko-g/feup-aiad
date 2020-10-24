package behaviours;

import agents.GameMaster;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import protocols.DecisionRequester;
import utils.ProtocolNames;
import utils.Util;

public class DayBehaviour extends SequentialBehaviour {

    GameMaster gameMaster;

    public DayBehaviour(GameMaster gameMaster) {
        this.gameMaster = gameMaster;

        // Informs alive agents about the current time of day
        this.addSubBehaviour(new GameStateInformer(this.gameMaster, ProtocolNames.TimeOfDay));

        // Informs Alive agents about who died last night
        this.addSubBehaviour(new GameStateInformer(this.gameMaster, ProtocolNames.PlayerDeath));


        ACLMessage msg = Util.buildMessage(ACLMessage.REQUEST,
                ProtocolNames.VoteTarget, "Who do you want to send to trial?");

        // Asks for votes from everyone alive
        this.addSubBehaviour(new DecisionRequester(
                this.gameMaster,
                this.gameMaster.addReceiversMessage(msg, true)
        ));
    }

    @Override
    public int onEnd() {

        String winner = this.gameMaster.getWinnerFaction();

        System.out.println("[DAY] WINNER");
        System.out.println(winner);

        if (winner == null) {
            System.out.println("======> Day is over!");
            this.gameMaster.setGameState(GameMaster.GameStates.NIGHT);
        }
        else {
            this.gameMaster.setGameState(GameMaster.GameStates.END);
            System.out.println(winner + " won the game!");
        }

        return super.onEnd();
    }
}
