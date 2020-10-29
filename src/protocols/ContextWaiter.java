package protocols;

import agents.PlayerAgent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import utils.GameContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContextWaiter extends AchieveREResponder {

    PlayerAgent playerAgent;

    public ContextWaiter(PlayerAgent playerAgent, MessageTemplate mt) {
        super(playerAgent, mt);
        this.playerAgent = playerAgent;
    }

    @Override
    protected ACLMessage handleRequest(ACLMessage request) {
        return new ACLMessage(ACLMessage.AGREE);
    }

    @Override
    protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {

        // Sets context
        String[] playerNames = request.getContent().split("\n");
        List<String> temp = new ArrayList<>(Arrays.asList(playerNames));
        this.playerAgent.setGameContext(new GameContext(this.playerAgent, temp));

        // Informs success
        ACLMessage inform = request.createReply();
        inform.setPerformative(ACLMessage.INFORM);
        return inform;
    }
}
