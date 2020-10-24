package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import utils.GameContext;

public abstract class PlayerAgent extends Agent {

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

    @Override
    protected void setup() {
        // Searches for GameMaster and saves to game_master_desc
        if (!this.findGameMaster()) {
            this.takeDown();
        }
    }

    public void logMessage(String msg) {
        String senderID = this.getLocalName();
        String finalMessage = "[" + senderID + "] " + msg;
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
    public abstract void takeDown();

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
}
