package behaviours;

import agents.GameMaster;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import protocols.DecisionRequester;
import utils.ProtocolNames;
import utils.Util;

import java.util.ArrayList;
import java.util.List;

public class DayBehaviour extends SequentialBehaviour {

    GameMaster gameMaster;

    public DayBehaviour(GameMaster gameMaster) {
        this.gameMaster = gameMaster;

        // Informs alive agents about the current time of day
        this.addSubBehaviour(new GameStateInformer(this.gameMaster, ProtocolNames.TimeOfDay));

        // Informs Alive agents about who died last night if anyone died
//        if(this.gameMaster.getNightDeaths().size() > 0) {
//            List<String> lastNightDeaths = this.gameMaster.getNightDeaths();
//            for (String currentName : lastNightDeaths) {
//                this.addSubBehaviour(new GameStateInformer(this.gameMaster, true, currentName));
//            }
//            this.gameMaster.setNightDeaths(new ArrayList<>());
//        }

        this.addSubBehaviour(new GameStateInformer(this.gameMaster, false));

        ACLMessage msg = Util.buildMessage(ACLMessage.REQUEST,
                ProtocolNames.VoteTarget, "Who do you want to send to trial?");

        // Asks for votes from everyone alive
        this.addSubBehaviour(new DecisionRequester(
                this.gameMaster,
                this.gameMaster.addReceiversMessage(msg, true)
        ));

        // Informs who died in trial
//        if(!this.gameMaster.getDayDeath().equals("")) {
//            System.out.println("================ SENT VOTING RESULTS ================");
//
//            this.addSubBehaviour(new GameStateInformer(this.gameMaster, true, this.gameMaster.getDayDeath()));
//            this.gameMaster.setDayDeath("");
//        }

        this.addSubBehaviour(new GameStateInformer(this.gameMaster, true));
    }

    @Override
    public int onEnd() {

        String winner = this.gameMaster.getWinnerFaction();

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
