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

        System.out.println("\n---------- Day begins ----------");

        // Informs alive agents about the current time of day
        this.addSubBehaviour(new GameStateInformer(this.gameMaster, ProtocolNames.TimeOfDay));

        // Who couldn't make it till morning
        this.addSubBehaviour(new GameStateInformer(this.gameMaster, false));

        // Informs healers if they saved someone
        this.addSubBehaviour(new GameStateInformer(this.gameMaster, ProtocolNames.PlayerSaved));

        // Discussion time
        this.addSubBehaviour(new ChatMessageDistributor(this.gameMaster));

        ACLMessage msg = Util.buildMessage(ACLMessage.REQUEST,
                ProtocolNames.VoteTarget, "Who do you want to send to trial?");

        // Asks for votes from everyone alive
        this.addSubBehaviour(new DecisionRequester(
                this.gameMaster,
                this.gameMaster.addReceiversMessage(msg, true)
        ));

        // Who died in trial
        this.addSubBehaviour(new GameStateInformer(this.gameMaster, true));
    }

    @Override
    public int onEnd() {

        String winner = this.gameMaster.getWinnerFaction();

        if (winner == null) {
            System.out.println("---------- Day is over! ----------");
            this.gameMaster.setGameState(GameMaster.GameStates.NIGHT);
        }
        else {
            this.gameMaster.setGameState(GameMaster.GameStates.END);
            System.out.println("========== Game is over! ==========");
            System.out.println(winner + " won the game!");
        }

        return super.onEnd();
    }
}
