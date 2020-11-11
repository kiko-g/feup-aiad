package behaviours;


import agents.GameMaster;
import jade.core.behaviours.OneShotBehaviour;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NightResultsCalculator extends OneShotBehaviour {
    private final GameMaster gameMaster;

    public NightResultsCalculator(GameMaster gameMaster) {
        this.gameMaster = gameMaster;
    }

    @Override
    public void action() {
        // Calculates night outcome

        List<String> possibleDeaths = this.gameMaster.getAttackedPlayers();
        HashMap<String, String> savedPLayers = this.gameMaster.getSavedPlayers();

        // Checks for cases of Healer kills
        for(Map.Entry<String, String> currSaveCase : savedPLayers.entrySet()) {
            String savedPlayer = currSaveCase.getKey();
            String savior = currSaveCase.getValue();

            // Healer was attacked and no one saved it
            if(possibleDeaths.contains(savior) && !savedPLayers.containsKey(savior))
                savedPLayers.remove(savedPlayer);
        }

        for(String currentAttackedPlayer : possibleDeaths) {
            // Attacked players that were not saved, die in the morning
            if(!savedPLayers.containsKey(currentAttackedPlayer)) {
                this.gameMaster.getGameLobby().killPlayer(currentAttackedPlayer);
                this.gameMaster.addNightDeath(currentAttackedPlayer);
            }
        }

        // Clears registers for next night
        this.gameMaster.setAttackedPlayers(new ArrayList<>());
        this.gameMaster.setSavedPlayers(new HashMap<>());
    }
}
