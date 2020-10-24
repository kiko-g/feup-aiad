package protocols;

import agents.PlayerAgent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

public class DecisionInformer extends AchieveREResponder {

    PlayerAgent playerAgent;

    public DecisionInformer(PlayerAgent playerAgent, MessageTemplate mt) {
        super(playerAgent, mt);
        this.playerAgent = playerAgent;
    }

    @Override
    protected ACLMessage handleRequest(ACLMessage request) {
        ACLMessage agree = request.createReply();
        agree.setPerformative(ACLMessage.AGREE);
        return agree;
    }

    @Override
    protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
        return this.playerAgent.handleVoteRequest(request, response);
    }
}
