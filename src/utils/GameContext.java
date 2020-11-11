package utils;

import agents.PlayerAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// This class contains the perceived state of other players by a PlayerAgent (aka Villager, Mafia, Detective...)
public class GameContext {

    // key: Player name
    // value: isAlive
    private HashMap<String, GameFacts> gameContext;

    private PlayerAgent playerAgent;

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

    public GameContext(PlayerAgent playerAgent, List<String> playerNames) {
        this.playerAgent = playerAgent;
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

    // Returns the names of the players that the agent knows, for a fact, are Mafia
    public List<String> getMafiaPlayerNames(boolean excludeMyself) {
        List<String> mafiaPlayers = new ArrayList<>();
        for(Map.Entry<String, GameFacts> currentPlayer : this.gameContext.entrySet()) {
            if(currentPlayer.getValue().isAlive()) {
                String role = currentPlayer.getValue().getRole();
                if(Util.getFaction(role).equals("Mafia")) {
                    if(!excludeMyself || !this.playerAgent.getLocalName().equals(currentPlayer.getKey()))
                        mafiaPlayers.add(currentPlayer.getKey());
                }
            }
        }
        return mafiaPlayers;
    }

    // Sets player's role
    public void setPlayerRole(String name, String role) {
        gameContext.get(name).setRole(role);
    }

    public List<String> getPlayerNamesByRole(String role, boolean isAlive) {
        List<String> names = new ArrayList<>();

        for(Map.Entry<String, GameFacts> currPlayer : this.gameContext.entrySet()) {
            if(currPlayer.getValue().isAlive() == isAlive)
                if(currPlayer.getValue().getRole().equals(role))
                    names.add(currPlayer.getKey());
        }

        return names;
    }

    public List<String> getPlayerNamesByRole(String role) {
        List<String> names = new ArrayList<>();

        for(Map.Entry<String, GameFacts> currPlayer : this.gameContext.entrySet()) {
            if(currPlayer.getValue().getRole().equals(role))
               names.add(currPlayer.getKey());
        }

        return names;
    }
}
