package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// This class contains the perceived state of other players by a PlayerAgent (aka Villager, Mafia, Detective...)
public class GameContext {

    // key: Player name
    // value: isAlive
    private HashMap<String, GameFacts> gameContext;

    private class GameFacts {

        private boolean isAlive;
        private String role = "";

        public GameFacts() {
            isAlive = true;

        }

        public boolean isAlive() {
            return isAlive;
        }

        public void setAlive(boolean alive) {
            isAlive = alive;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    public GameContext(List<String> playerNames) {
        this.gameContext = new HashMap<>();

        for(String currentName : playerNames) {
            this.gameContext.put(currentName, new GameFacts());
        }
    }

    public List<String> getAlivePlayers() {
        List<String> alivePlayers = new ArrayList<>();
        for(Map.Entry<String, GameFacts> currentPlayer : this.gameContext.entrySet()) {
            //Player is alive
            if(currentPlayer.getValue().isAlive()) {
                alivePlayers.add(currentPlayer.getKey());
            }
        }

        return alivePlayers;
    }

    public boolean isPlayerAlive(String name) {
        return this.gameContext.get(name).isAlive();
    }

    // Sets isAlive value to false
    public void playerWasKilled(String name) {
        this.gameContext.get(name).setAlive(false);
    }

    public List<String> getMafiaPlayers() {
        List<String> mafiaPlayers = new ArrayList<>();
        for(Map.Entry<String, GameFacts> currentPlayer : this.gameContext.entrySet()) {
            if(currentPlayer.getValue().isAlive()) {
                String role = currentPlayer.getValue().getRole();
                if(Util.getFaction(role).equals("Mafia")) {
                    mafiaPlayers.add(currentPlayer.getKey());
                }
            }
        }
        return mafiaPlayers;
    }

    public void setPlayerRole(String name, String role) {
        gameContext.get(name).setRole(role);
    }
}
