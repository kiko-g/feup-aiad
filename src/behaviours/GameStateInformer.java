package behaviours;

import agents.GameMaster;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import utils.ProtocolNames;
import utils.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GameStateInformer extends OneShotBehaviour {

    GameMaster gameMaster;

    // Kind of message to send
    String typeInfo;

    //Used if its a player death
    private boolean isDay;

    // The protocol name decides what message is sent!!
    public GameStateInformer(GameMaster gameMaster, String protocolName) {
        this.gameMaster = gameMaster;
        this.typeInfo = protocolName;
    }

    public GameStateInformer(GameMaster gameMaster, boolean dayTime) {
        this.gameMaster = gameMaster;
        this.typeInfo = ProtocolNames.PlayerDeath;
        this.isDay = dayTime;
    }

    @Override
    public void action() {

        switch(this.typeInfo) {
            case ProtocolNames.PlayerDeath -> {
                if(this.isDay) sendDeadPlayerNameDay();
                else sendDeadPlayerNamesNight();
            }
            case ProtocolNames.PlayerSaved -> {
                sendSavedPlayerName();
            }
            case ProtocolNames.TimeOfDay -> {
                this.sendTimeOfDay();
            }
            case ProtocolNames.End -> {
                this.sendEndGameMessage();
            }
        }
    }

    private void sendEndGameMessage() {
        String content = this.gameMaster.getWinnerFaction() + " won the game!";

        ACLMessage msg = Util.buildMessage(ACLMessage.INFORM,
                ProtocolNames.End, content);

        this.gameMaster.sendMessageAllPlayers(msg);
    }

    private void sendTimeOfDay() {
        String content = (this.gameMaster.getGameState().equals(GameMaster.GameStates.DAY)) ? "Day" : "Night";

        ACLMessage msg = Util.buildMessage(ACLMessage.INFORM,
                ProtocolNames.TimeOfDay, content);

        this.gameMaster.sendMessageAlivePlayers(msg);
    }

    private void sendDeadPlayerNamesNight() {
        // Informs Alive agents about who died last night if anyone died
        List<String> deadPlayerNames = this.gameMaster.getNightDeaths();

        if(deadPlayerNames.size() > 0) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setProtocol(ProtocolNames.PlayerDeath);

            StringBuilder messageContent = new StringBuilder();
            for(String currName : deadPlayerNames) {
                messageContent.append(currName).append("\n");
                System.out.println("\t" + currName + " was attacked last night, and died in the morning...");
            }

            msg.setContent(messageContent.toString());
            this.gameMaster.sendMessageAlivePlayers(msg);
            this.gameMaster.setNightDeaths(new ArrayList<>());
        }
    }

    private void sendDeadPlayerNameDay() {
        if(!this.gameMaster.getDayDeath().equals("")) {

            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setProtocol(ProtocolNames.PlayerDeath);
            msg.setContent(this.gameMaster.getDayDeath() + "\n");
            this.gameMaster.sendMessageAlivePlayers(msg);

            this.gameMaster.setDayDeath("");
        }
    }

    private void sendSavedPlayerName() {
        ConcurrentHashMap<String, String> savedPlayers = this.gameMaster.getActuallySavedPlayers();

        List<AID> healers = this.gameMaster.getGameLobby().getPlayersAIDRole("Healer", true);
        for(HashMap.Entry<String, String> currentPlayer : savedPlayers.entrySet()) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setProtocol(ProtocolNames.PlayerSaved);
            msg.setContent("Last night you saved " + currentPlayer.getKey());

            for(AID currentHealer : healers) {
                if(currentHealer.getLocalName().equals(currentPlayer.getValue())) {
                    msg.addReceiver(currentHealer);
                    break;
                }
            }
            this.gameMaster.send(msg);
        }

        this.gameMaster.setActuallySavedPlayers(new ConcurrentHashMap<>());
    }
}
