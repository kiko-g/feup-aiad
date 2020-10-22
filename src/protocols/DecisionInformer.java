package protocols;

import agents.PlayerAgent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

import java.util.List;
import java.util.Random;

public class DecisionInformer extends AchieveREResponder {

    PlayerAgent playerAgent;

    public DecisionInformer(PlayerAgent playerAgent, MessageTemplate mt) {
        super(playerAgent, mt);
        this.playerAgent = playerAgent;
    }

    @Override
    protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
        System.out.println(request.getContent());

        ACLMessage agree = request.createReply();
        agree.setPerformative(ACLMessage.AGREE);

        return agree;
    }

    @Override
    protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
//                if (performAction()) {
//                    System.out.println("Agent "+getLocalName()+": Action successfully performed");
//                    ACLMessage inform = request.createReply();
//                    inform.setPerformative(ACLMessage.INFORM);
//                    return inform;
//                }
//                else {
//                    System.out.println("Agent "+getLocalName()+": Action failed");
//                    throw new FailureException("unexpected-error");
//                }
        //Decides the person to kill during night
        List<String> killablePlayers = this.playerAgent.getGameContext().getAlivePlayers();

        Random r = new Random();
        int playerIndex = r.nextInt(killablePlayers.size());

        String playerToKill = killablePlayers.get(playerIndex);

        ACLMessage inform = request.createReply();
        inform.setContent(playerToKill);
        inform.setPerformative(ACLMessage.INFORM);

        return inform;
    }
}
