package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// This class contains the perceived state of other players by a PlayerAgent (aka Villager, Mafia, Detective...)
public class GameContext {

    // key: Player name
    // value: isAlive
    private HashMap<String, Boolean> gameContext;

    public GameContext(List<String> playerNames) {
        this.gameContext = new HashMap<>();

        for(String currentName : playerNames) {
            this.gameContext.put(currentName, true);
        }
    }

    public List<String> getAlivePlayers() {
        List<String> alivePlayers = new ArrayList<>();
        for(Map.Entry<String, Boolean> currentPlayer : this.gameContext.entrySet()) {
            //Player is alive
            if(currentPlayer.getValue()) {
                alivePlayers.add(currentPlayer.getKey());
            }
        }

        return alivePlayers;
    }

    public boolean isPlayerAlive(String name) {
        return this.gameContext.get(name);
    }

    // Sets isAlive value to false
    public void playerWasKilled(String name) {
        if(this.isPlayerAlive(name))
            this.gameContext.computeIfPresent(name, (k, v) -> !v);
    }
}
