package utils;


import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;

import java.util.HashMap;
import java.util.Map;

public class GameLobby {

    private class AgentInfo {
        private final String role;
        private DFAgentDescription agentDesc;

        public AgentInfo(String role, DFAgentDescription agentDesc) {
            this.role = role;
            this.agentDesc = agentDesc;
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
    }

    private final int capacity;
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

    public AID getFirstRole(String role) {
        for(Map.Entry<String, AgentInfo> currentPlayer : lobby.entrySet()) {
            if(currentPlayer.getValue().getRole().equals(role))
                return currentPlayer.getValue().getAgentDesc().getName();
        }

        return null;
    }
}
