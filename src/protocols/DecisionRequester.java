package protocols;

import agents.GameMaster;
import jade.lang.acl.ACLMessage;
import launcher.GameLauncher;
import sajas.proto.AchieveREInitiator;
import uchicago.src.sim.network.DefaultDrawableNode;
import utils.Edge;
import utils.ProtocolNames;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class DecisionRequester extends AchieveREInitiator {
    GameMaster gameMaster;
    String protocolName;

    // Day time voting register
    HashMap<String, Integer> votingResults;

    public DecisionRequester(GameMaster gameMaster, ACLMessage msg) {
        super(gameMaster, msg);
        this.gameMaster = gameMaster;
        this.protocolName = msg.getProtocol();

        if(msg.getProtocol().equals(ProtocolNames.VoteTarget)) {
            this.votingResults = new HashMap<>();
        }
    }

    @Override
    protected void handleInform(ACLMessage inform) {

        switch (this.protocolName) {
            case ProtocolNames.TargetKilling: {
                System.out.println(inform.getSender().getLocalName() + " has decided to attack " + inform.getContent());
                this.gameMaster.addAttackedPlayer(inform.getContent());
                break;
            }
            case ProtocolNames.VoteTarget: {
                System.out.println(inform.getSender().getLocalName() + " has voted in " + inform.getContent());
                String playerName = inform.getContent();

                // If exists, increments
                if (this.votingResults.containsKey(playerName))
                    this.votingResults.replace(playerName, this.votingResults.get(playerName) + 1);
                else
                    this.votingResults.put(playerName, 1); // If not, adds; Skips go here too!

                break;
            }
            case ProtocolNames.TargetHealing: {

                DefaultDrawableNode healerNode = GameLauncher.getNodeByAgentName(inform.getSender().getLocalName());
                DefaultDrawableNode targetNode = GameLauncher.getNodeByAgentName(inform.getContent());

                if(healerNode != null) {
                    GameLauncher.removeOutEdges(healerNode);

                    Edge edgeHealer = new Edge(healerNode, targetNode, "Heal");
                    edgeHealer.setColor(Color.GREEN);
                    healerNode.addOutEdge(edgeHealer);
                }

                System.out.println(inform.getSender().getLocalName()+" has decided to visit " + inform.getContent());

                // Stores the saved player and its savior
                this.gameMaster.addSavedPlayer(inform.getContent() , inform.getSender().getLocalName());
                break;
            }
        }
    }

    @Override
    public int onEnd() {

        // Decides whether or not there's a majority of votes to kill a player
        if(this.protocolName.equals(ProtocolNames.VoteTarget)) {
            boolean duplicateFound = false;
            String playerName = "";
            int max = 0;
            for(Map.Entry<String, Integer> currPlayer : this.votingResults.entrySet()) {
                if(currPlayer.getValue() > max) {
                    max = currPlayer.getValue();
                    playerName = currPlayer.getKey();
                    duplicateFound = false;
                }
                else if(currPlayer.getValue() == max) {
                    duplicateFound = true;
                }
            }

            // Majority achieved in a player
            if(!duplicateFound && !playerName.equals("Skip")) {
                this.gameMaster.setDayDeath(playerName);
                this.gameMaster.getGameLobby().killPlayer(playerName);
                System.out.println("The town has chosen " + playerName + " for trial!");

                // Jester win
                if(this.gameMaster.getGameLobby().getPlayerRole(playerName).equals("Jester"))
                    this.gameMaster.jesterDiedDuringDay();
            }
            else
                System.out.println("No one was chosen for trial!");
        }

        return super.onEnd();
    }
}
