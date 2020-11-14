package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import utils.ChatLog;
import utils.ChatMessage;
import utils.ChatMessageTemplate;
import utils.GameContext;

import java.util.HashMap;
import java.util.Map;

public abstract class PlayerAgent extends Agent {

    HashMap<String, Double> susRateMap = new HashMap<>();

    private enum TimeOfDay {
        Day,
        Night
    }

    // GameMaster Description
    protected DFAgentDescription game_master_desc;

    // Other player states (Alive / Dead)
    protected GameContext gameContext;

    // Day or Night
    private TimeOfDay currentTime;

    // All chat messages received up until now
    private ChatLog chatLog;

    public PlayerAgent() {
        this.chatLog = new ChatLog();
    }

    @Override
    protected void setup() {
        // Searches for GameMaster and saves to game_master_desc
        if (!this.findGameMaster()) {
            this.takeDown();
        }
    }

    public void logMessage(String msg) {
        String senderID = this.getLocalName();
        String finalMessage = "[" + senderID + "]  \t" + msg;
        System.out.println(finalMessage);
    }

    public boolean findGameMaster() {
        // Searches for GameMaster
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setName("AgentType");
        sd.setType("GameMaster");
        template.addServices(sd);

        // Search results handling
        try {
            DFAgentDescription[] search_results = DFService.search(this, template);
            if (search_results.length != 1) {
                this.logMessage("Error finding the GameMaster!");
                return false;
            }
            else {
                this.game_master_desc = search_results[0];
                return true;
            }
        } catch (FIPAException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String buildPresentationString(String role) {
        return "Hi! I am " + this.getLocalName() + ", the " + role + ".";
    }

    protected ACLMessage buildJoinMessage(String role) {
        // Role Information Message AKA attempt to join
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(this.game_master_desc.getName());
        msg.setProtocol("Join");
        msg.setContent(this.buildPresentationString(role));

        return msg;
    }

    protected void registerAgent(String role) throws FIPAException {
        DFAgentDescription dfad = new DFAgentDescription();
        dfad.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setName("AgentType");
        sd.setType("Player");
        dfad.addServices(sd);

        ServiceDescription sd2 = new ServiceDescription();
        sd2.setName("Role");
        sd2.setType(role);
        dfad.addServices(sd2);

        DFService.register(this, dfad);
    }

    protected void deregisterAgent() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void takeDown() {
        this.deregisterAgent();
//        System.out.println(this.getRole() + " shutdown");
        super.takeDown();
    }

    public abstract String getRole();

    public abstract void setDayTimeBehavior();

    public abstract void setNightTimeBehaviour();

    public final ACLMessage handleVoteRequest(ACLMessage request, ACLMessage response) {
        return (this.currentTime.equals(TimeOfDay.Day)) ?
                handleDayVoteRequest(request, response) : handleNightVoteRequest(request, response);
    }

    public abstract ACLMessage handleDayVoteRequest(ACLMessage request, ACLMessage response);

    public abstract ACLMessage handleNightVoteRequest(ACLMessage request, ACLMessage response);

    public void buryPlayer(String playerName) {
        this.gameContext.playerWasKilled(playerName);
    }

    public GameContext getGameContext() {
        return this.gameContext;
    }

    public void setGameContext(GameContext gc) {
        this.gameContext = gc;
    }

    public void setDay() {
        this.currentTime = TimeOfDay.Day;
    }

    public void setNight() {
        this.currentTime = TimeOfDay.Night;
    }

    public boolean isDay() {
        return this.currentTime == TimeOfDay.Day;
    }

    public void addToChatLog(ChatMessage message) {
        this.chatLog.addMessage(message);
    }

    public AID getGameMasterAID() { return this.game_master_desc.getName(); }

    public void addToSusRateMap(String name, Double value) {
        susRateMap.put(name, value);
    }

    public void setPlayerSusRate(String name, double delta) {
        double oldSusRate = this.susRateMap.get(name);
        this.susRateMap.replace(name, oldSusRate * delta);
    }

    public void handleChatMsg(ChatMessage message) {
        switch (message.getTemplateMessage()) {
            case ChatMessageTemplate.RevealRole: {
                setPlayerSusRate(message.getSenderName(), 0.8);
                break;
            }
            case ChatMessageTemplate.AccusePlayerRole: {
                break;
            }
            case ChatMessageTemplate.SkipAccusation: {
                setPlayerSusRate(message.getSenderName(), 1.1);
                break;
            }
            case ChatMessageTemplate.AccusePlayer: {
                String victim = message.getContent().substring(18);
                setPlayerSusRate(victim, 1.4);
                break;
            }
        }

        String susRates = "";
        for(Map.Entry<String, Double> currentPlayer : this.susRateMap.entrySet())
            susRates += currentPlayer.getKey() + " " + String.format("%.0f", currentPlayer.getValue()*100) + "% ; ";

        this.logMessage(susRates);
    }
}
