package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import utils.*;

import java.util.*;

public abstract class PlayerAgent extends Agent {

    private HashMap<String, Double> susRateMap = new HashMap<>();

    private final Util.Trait playerTrait;

    private int playersKilledDuringNight;

    private int playersSavedDuringNight;

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

    public PlayerAgent(Util.Trait playerTrait){
        this.chatLog = new ChatLog();
        this.playerTrait = playerTrait;
    }

    public PlayerAgent() {
        this.chatLog = new ChatLog();
        this.playerTrait = Util.Trait.getRandomTrait();
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
        super.takeDown();
        this.doDelete();
    }

    public abstract String getRole();

    public abstract void setDayTimeBehavior();

    public abstract void setNightTimeBehaviour();

    public final ACLMessage handleVoteRequest(ACLMessage request, ACLMessage response) {
        return (this.currentTime.equals(TimeOfDay.Day)) ?
                handleDayVoteRequest(request, response) : handleNightVoteRequest(request, response);
    }

    public ACLMessage handleDayVoteRequest(ACLMessage request, ACLMessage response) {
        printSusRate();
        // -------------
        List<String> alivePlayers = this.gameContext.getAlivePlayers();
        List<String> mostSusPlayers = this.getMostSuspectPlayers(GlobalVars.VOTE_MIN_SUS_VALUE);
        String content;
        int minMafiaPlayersAlive = getPlayersKilledDuringNight() + getPlayersSavedDuringNight();

        if(mostSusPlayers.size() > 0) {
            Random r = new Random();
            int playerIndex = r.nextInt(mostSusPlayers.size());
            //content = mostSusPlayers.get(playerIndex);
            content = getTheMostSuspect();
        }
        else if(alivePlayers.size() <= minMafiaPlayersAlive * 2 + 2) {
            List<String> mostSusFinalPlayers = this.getMostSuspectPlayers(0.5);
            Random r = new Random();
            int playerIndex = r.nextInt(mostSusFinalPlayers.size());
            content = mostSusFinalPlayers.get(playerIndex);
        }
        else
            content = "Skip";

        ACLMessage inform = request.createReply();
        inform.setContent(content);
        inform.setPerformative(ACLMessage.INFORM);

        return inform;
    }

    public ACLMessage handleNightVoteRequest(ACLMessage request, ACLMessage response) { return null; }

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
        double multiplier = Util.getTraitMultiplier(this.playerTrait);
        double oldSusRate = this.susRateMap.get(name);
        double newSus;

        if(delta > 1 || delta == 0)
            newSus = oldSusRate * delta * multiplier;
        else
            newSus = oldSusRate * delta / multiplier;

        this.susRateMap.replace(name, Math.min(1.0, newSus));
    }

    public void handleChatMsg(ChatMessage message) {
        switch(message.getTemplateMessage()) {
            case ChatMessageTemplate.SkipAccusation : {
                setPlayerSusRate(message.getSenderName(), 1.1);
                break;
            }
            case ChatMessageTemplate.AccusePlayer : {
                String[] messageWords = message.getContent().split(" ");
                String victim = messageWords[messageWords.length - 1];

                setPlayerSusRate(victim, 1.4);
                break;
            }
            case ChatMessageTemplate.HealerMessage : {
                String[] messageWords = message.getContent().split(" ");
                String victim = messageWords[messageWords.length - 1];

                setPlayerSusRate(message.getSenderName(), 0.1);
                setPlayerSusRate(victim, 0.1);

                playersSavedDuringNight += 1;
                break;
            }
            case ChatMessageTemplate.DetectiveMessageHasActivity : {
                String[] messageWords = message.getContent().split(" ");
                String victim = messageWords[0];

                setPlayerSusRate(message.getSenderName(), 0.65);
                setPlayerSusRate(victim, 1.5);
                break;
            }
            case ChatMessageTemplate.DetectiveMessageHasNoActivity : {
                String[] messageWords = message.getContent().split(" ");
                String victim = messageWords[0];

                setPlayerSusRate(message.getSenderName(), 0.65);
                setPlayerSusRate(victim, 0.75);
                break;
            }
            case ChatMessageTemplate.DetectiveAcuseLeader : {
                String[] messageWords = message.getContent().split(" ");
                String leader = messageWords[0];

                setPlayerSusRate(message.getSenderName(), 0.3);
                setPlayerSusRate(leader, 10);
                break;
            }
        }
    }

    public String getTheMostSuspect() {
        List<String> players = getPlayerBySusOrder();

        for(int i = players.size() - 1; i >= 0; i--) {
            if(this.gameContext.getAlivePlayers().contains(players.get(i)))
                return players.get(i);
        }
        return null;
    }

    public List<String> getMostSuspectPlayers(double minSus) {
        List<String> mostSusPlayers = new ArrayList<>();

        for(String playerName : getPlayerBySusOrder()) {
            if(susRateMap.get(playerName) >= minSus && this.gameContext.isPlayerAlive(playerName)) {
                if(!playerName.equals(this.getLocalName()))
                    mostSusPlayers.add(playerName);
            }
        }
        return mostSusPlayers;
    }

    public List<String> getLessSuspectPlayers() {
        List<String> lessSuspectPlayers = new ArrayList<>(3);
        int i = 0;
        for(String playerName : getPlayerBySusOrder()) {
            if(this.gameContext.isPlayerAlive(playerName)) {
                if(!playerName.equals(this.getLocalName())) {
                    lessSuspectPlayers.add(playerName);
                    i++;
                    if(i == 3)
                        return lessSuspectPlayers;
                }
            }
        }
        return lessSuspectPlayers;
    }

    private List<String> getPlayerBySusOrder() {
        List<String> playersList = new ArrayList<>(susRateMap.keySet());
        List<Double> playersSusRate = new ArrayList<>(susRateMap.values());
        playersSusRate.sort(Double::compareTo);
        List<String> s = Arrays.asList(new String[playersList.size()]);

        for(String playerName : playersList) {
            Double value = susRateMap.get(playerName);
            int index = playersSusRate.indexOf(value);
            playersSusRate.set(index, -1.0);
            s.set(index, playerName);
        }

        return s;
    }

    public void printSusRate() {
        if(!GlobalVars.VERBOSE) return;

        StringBuilder susRates = new StringBuilder();
        for(Map.Entry<String, Double> currentPlayer : this.susRateMap.entrySet())
            if(this.getGameContext().getAlivePlayers().contains(currentPlayer.getKey()))
                susRates.append(currentPlayer.getKey()).append(" ").append(String.format("%.0f", currentPlayer.getValue()*100)).append("% ; ");
        this.logMessage(susRates.toString());
    }

    public Util.Trait getPlayerTrait() {
        return playerTrait;
    }

    public HashMap<String, Double> getSusRateMap() {
        return susRateMap;
    }
    
    public int getPlayersKilledDuringNight() {
        return playersKilledDuringNight;
    }

    public void setPlayersKilledDuringNight(int playersKilledDuringNight) {
        this.playersKilledDuringNight = playersKilledDuringNight;
    }

    public int getPlayersSavedDuringNight() {
        return playersSavedDuringNight;
    }

    public void setPlayersSavedDuringNight(int playersSavedDuringNight) {
        this.playersSavedDuringNight = playersSavedDuringNight;
    }
}
