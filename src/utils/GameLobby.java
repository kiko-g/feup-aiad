package utils;


import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GameLobby {
    private static class AgentInfo {
        private final String role;
        private boolean isAlive;
        private String information;
        private DFAgentDescription agentDesc;

        public AgentInfo(String role, DFAgentDescription agentDesc) {
            this.role = role;
            this.information = "";
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

        public AID getAID() {
            return this.agentDesc.getName();
        }
    }


    // key: Player name
    // value: AgentInfo
    private final int capacity;
    private final HashMap<String, AgentInfo> lobby;

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


    public List<AID> getAllPlayers() {
        List<AID> players = new ArrayList<>();
        for(Map.Entry<String, AgentInfo> currentPlayer : lobby.entrySet())
            players.add(currentPlayer.getValue().getAID());

        return players;
    }

    private List<AgentInfo> getPlayersByStatus(boolean isAlive) {
        List<AgentInfo> players = new ArrayList<>();
        for(Map.Entry<String, AgentInfo> currentPlayer : lobby.entrySet()) {
            if(currentPlayer.getValue().isAlive() == isAlive)
                players.add(currentPlayer.getValue());
        }
        return players;
    }

    // Returns a list with all dead players
    public List<DFAgentDescription> getDeadPlayers() {
        List<AgentInfo> temp = this.getPlayersByStatus(false);
        return temp.stream().map(AgentInfo::getAgentDesc).collect(Collectors.toList());
    }

    // Returns a list with all dead players names (AID)
    public List<AID> getDeadPlayersAID() {
        List<AgentInfo> temp = this.getPlayersByStatus(false);
        return temp.stream().map(AgentInfo::getAID).collect(Collectors.toList());
    }

    // Returns a list with all players names (AID) that are still in game
    public List<AID> getAlivePlayersAID() {
        List<AgentInfo> temp = this.getPlayersByStatus(true);
        return temp.stream().map(AgentInfo::getAID).collect(Collectors.toList());
    }

    public boolean isAlive(String playerName) {
        return lobby.get(playerName).isAlive();
    }

    private List<AgentInfo> getPlayersByRole(String role, boolean isAlive) {
        List<AgentInfo> players = new ArrayList<>();

        for(Map.Entry<String, AgentInfo> currentPlayer : lobby.entrySet()) {
            if(currentPlayer.getValue().isAlive() == isAlive)
                if(currentPlayer.getValue().getRole().equals(role))
                    players.add(currentPlayer.getValue());
        }

        return players;
    }

    private List<AgentInfo> getPlayersByRole(String role) {
        List<AgentInfo> players = new ArrayList<>();

        for(Map.Entry<String, AgentInfo> currentPlayer : lobby.entrySet()) {
            if(currentPlayer.getValue().getRole().equals(role))
                players.add(currentPlayer.getValue());
        }

        return players;
    }

    private List<AgentInfo> getPlayersByFaction(String faction, boolean isAlive) {
        List<AgentInfo> players = new ArrayList<>();

        for(Map.Entry<String, AgentInfo> currentPlayer : lobby.entrySet()) {
            if(currentPlayer.getValue().isAlive() == isAlive)
                if(Util.getFaction(currentPlayer.getValue().getRole()).equals(faction))
                    players.add(currentPlayer.getValue());
        }

        return players;
    }

    private List<AgentInfo> getPlayersByFaction(String faction) {
        List<AgentInfo> players = new ArrayList<>();

        for(Map.Entry<String, AgentInfo> currentPlayer : lobby.entrySet()) {
            if(Util.getFaction(currentPlayer.getValue().getRole()).equals(faction))
                players.add(currentPlayer.getValue());
        }

        return players;
    }

    public List<AID> getPlayersAIDRole(String role, boolean isAlive) {
        List<AgentInfo> temp = getPlayersByRole(role, isAlive);
        return temp.stream().map(AgentInfo::getAID).collect(Collectors.toList());
    }

    public List<AID> getPlayersAIDRole(String role) {
        List<AgentInfo> temp = getPlayersByRole(role);
        return temp.stream().map(AgentInfo::getAID).collect(Collectors.toList());
    }

    public List<AID> getPlayersAIDFaction(String faction, boolean isAlive) {
        List<AgentInfo> temp = getPlayersByFaction(faction, isAlive);
        return temp.stream().map(AgentInfo::getAID).collect(Collectors.toList());
    }

    public List<AID> getPlayersAIDFaction(String faction) {
        List<AgentInfo> temp = getPlayersByFaction(faction);
        return temp.stream().map(AgentInfo::getAID).collect(Collectors.toList());
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

    public int[] getNumberPlayersPerFactions() {
        // index 0: Town
        // index 1: Mafia
        // index 2: Neutral
        int[] faction = {0, 0, 0};

        for(Map.Entry<String, AgentInfo> currentPlayer : lobby.entrySet()) {
            AgentInfo currentPlayerInfo = currentPlayer.getValue();
            if(currentPlayerInfo.isAlive())
                switch(Util.getFaction(currentPlayerInfo.getRole())) {
                    case "Town" -> faction[0]++;
                    case "Mafia" -> faction[1]++;
                    case "Neutral" -> faction[2]++;
                }
        }

        return faction;
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


    public List<String> getPlayerNamesRole(String role, boolean isAlive) {
        List<String> playerNames = new ArrayList<>();

        for(Map.Entry<String, AgentInfo> currentPlayer : lobby.entrySet()) {
            AgentInfo currentPlayerInfo = currentPlayer.getValue();
            if(currentPlayerInfo.isAlive() == isAlive) {
                if(currentPlayerInfo.getRole().equals(role)) {
                    playerNames.add(currentPlayer.getKey());
                }
            }
        }
        return playerNames;
    }

    public List<String> getPlayerNamesRole(String role) {
        List<String> playerNames = new ArrayList<>();

        for(Map.Entry<String, AgentInfo> currentPlayer : lobby.entrySet()) {
            AgentInfo currentPlayerInfo = currentPlayer.getValue();
            if(currentPlayerInfo.getRole().equals(role)) {
                playerNames.add(currentPlayer.getKey());
            }
        }
        return playerNames;
    }

    public List<String> getAllNames() {
        List<String> playerNames = new ArrayList<>();

        for(Map.Entry<String, AgentInfo> currentPlayer : lobby.entrySet()) {
            playerNames.add(currentPlayer.getKey());
        }
        return playerNames;
    }

    public List<String> getPlayerNamesFaction(String faction) {
        List<String> playerNames = new ArrayList<>();

        for(Map.Entry<String, AgentInfo> currentPlayer : lobby.entrySet()) {
            AgentInfo currentPlayerInfo = currentPlayer.getValue();
            if(Util.getFaction(currentPlayerInfo.getRole()).equals(faction)) {
                playerNames.add(currentPlayer.getKey());
            }
        }
        return playerNames;
    }

    public List<String> getPlayerNamesFaction(String faction, boolean isAlive) {
        List<String> playerNames = new ArrayList<>();

        for(Map.Entry<String, AgentInfo> currentPlayer : lobby.entrySet()) {
            AgentInfo currentPlayerInfo = currentPlayer.getValue();
            if(currentPlayerInfo.isAlive() == isAlive) {
                if(Util.getFaction(currentPlayerInfo.getRole()).equals(faction)) {
                    playerNames.add(currentPlayer.getKey());
                }
            }
        }
        return playerNames;
    }

    public boolean didAllKillingsDie() {
        return this.getPlayersByRole("Killing", true).size() == 0;
    }

    public String getPlayerRole(String playerName) {
        return lobby.get(playerName).getRole();
    }

    public AID getAIDByName(String playerName) {
        return ! this.lobby.containsKey(playerName) ? null : this.lobby.get(playerName).getAID();
    }

    public String getRoleByName(String playerName) {
        return ! this.lobby.containsKey(playerName) ? null : this.lobby.get(playerName).getRole();
    }

    public String[] getAllPlayerNames() {
        List<String> players = new ArrayList<>(lobby.keySet());
        return players.toArray(new String[0]);
    }
}
