package behaviours;

import agents.GameMaster;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import protocols.ContextInformer;
import protocols.MafiaInformer;
import utils.ProtocolNames;
import utils.Util;

import java.util.ArrayList;
import java.util.List;

public class ReadyBehaviour extends SequentialBehaviour {

    GameMaster gameMaster;

    private ACLMessage buildContextMessage() {
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

        return msg;
    }

    private ACLMessage buildMafiaMessage() {
        List<String> mafiaKillings = this.gameMaster.getGameLobby().getPlayerNamesRole("Killing");
        List<String> mafiaLeaders = this.gameMaster.getGameLobby().getPlayerNamesRole("Leader");
        StringBuilder messageContent = new StringBuilder();

        // Message format: "name role\n"

        for(String currKilling : mafiaKillings)
            messageContent.append(currKilling).append(" ").append("Killing").append("\n");

        for(String currLeader : mafiaLeaders)
            messageContent.append(currLeader).append(" ").append("Leader").append("\n");


        ACLMessage mafiaMsg = Util.buildMessage(ACLMessage.INFORM,
                ProtocolNames.MafiaPlayers,
                messageContent.toString()
        );

        mafiaMsg = this.gameMaster.addReceiversMessage(mafiaMsg,
                this.gameMaster.getGameLobby().getPlayersAIDFaction("Mafia")
        );

        return mafiaMsg;
    }

    public ReadyBehaviour(GameMaster gameMaster) {
        this.gameMaster = gameMaster;

        ACLMessage msg = buildContextMessage();
        this.addSubBehaviour(new ContextInformer(this.gameMaster, msg));

        ACLMessage mafiaMsg = buildMafiaMessage();
        this.addSubBehaviour(new MafiaInformer(this.gameMaster, mafiaMsg));
    }

    @Override
    public int onEnd() {
        this.gameMaster.setGameState(GameMaster.GameStates.NIGHT);
        return super.onEnd();
    }
}
