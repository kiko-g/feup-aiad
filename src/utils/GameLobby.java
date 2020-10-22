package utils;


import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameLobby {

    private class AgentInfo {
        private final String role;
        private boolean isAlive;
        private DFAgentDescription agentDesc;

        public AgentInfo(String role, DFAgentDescription agentDesc) {
            this.role = role;
            this.agentDesc = agentDesc;
            this.isAlive = true;
        }

        public String getRole() {
            return role;
        }

        public DFAgentDescription getAgentDesc() {
            return agentDesc;
        }

        public void setAgentDesc(DFAgentDescription agentDesc) {
            this.agentDesc = agentDesc;
        }

        public boolean isAlive() {
            return isAlive;
        }

        public void setAlive(boolean alive) {
            isAlive = alive;
        }
    }

    private final int capacity;

    // key: Player name
    // value: AgentInfo
    private HashMap<String, AgentInfo> lobby;

    public GameLobby(int capacity) {
        this.capacity = capacity;
        this.lobby = new HashMap<>();
    }

    public void addPlayer(String name, String role, DFAgentDescription agentDesc) {
        AgentInfo aInf = new AgentInfo(role, agentDesc);
        this.lobby.put(name, aInf);
    }

    public boolean isFull() {
        return this.lobby.size() == this.capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public AgentInfo getAgentInfo(String name) {
        return this.lobby.get(name);
    }

    public void setDescriptions(DFAgentDescription[] descriptions) {
        for (DFAgentDescription desc: descriptions) {
           this.getAgentInfo(desc.getName().getLocalName()).setAgentDesc(desc);
        }
    }

    private List<String> getPlayerNames(boolean isAlive) {
        List<String> playerNames = new ArrayList<>();

        for(Map.Entry<String, AgentInfo> currentPlayer : lobby.entrySet()) {
            if(currentPlayer.getValue().isAlive() == isAlive)
                playerNames.add(currentPlayer.getKey());
        }

        return playerNames;
    }

    // Returns a list with all dead players
    public List<String> getDeadPlayerNames() {
        return this.getPlayerNames(false);
    }

    // Returns a list with all players still in game
    public List<String> getAlivePlayerNames() {
        return this.getPlayerNames(true);
    }

    private List<DFAgentDescription> getPlayers(boolean isAlive) {
        List<DFAgentDescription> players = new ArrayList<>();

        for(Map.Entry<String, AgentInfo> currentPlayer : lobby.entrySet()) {
            if(currentPlayer.getValue().isAlive() == isAlive)
                players.add(currentPlayer.getValue().getAgentDesc());
        }

        return players;
    }

    // Returns a list with all dead players
    public List<DFAgentDescription> getDeadPlayers() {
        return this.getPlayers(false);
    }

    // Returns a list with all players still in game
    public List<DFAgentDescription> getAlivePlayers() {
        return this.getPlayers(true);
    }

    public AID getFirstRole(String role) {
        for(Map.Entry<String, AgentInfo> currentPlayer : lobby.entrySet()) {
            if(currentPlayer.getValue().getRole().equals(role))
                return currentPlayer.getValue().getAgentDesc().getName();
        }

        return null;
    }

    public void killPlayer(String name) {
        // Unalives a player
        this.lobby.get(name).setAlive(false);
    }
}
