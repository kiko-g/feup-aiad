package protocols;

import agents.PlayerAgent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

public class MafiaWaiter extends AchieveREResponder {

    private PlayerAgent playerAgent;

    public MafiaWaiter(PlayerAgent a, MessageTemplate mt) {
        super(a, mt);
        this.playerAgent = a;
    }

    @Override
    protected ACLMessage handleRequest(ACLMessage request) {
        ACLMessage agree = request.createReply();
        agree.setPerformative(ACLMessage.AGREE);

        return agree;
    }

    @Override
    protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
        // name role\n
        String[] mafiaPlayers = request.getContent().split("\n");

        for(String mafiaPlayer : mafiaPlayers) {
            String[] playerInfo = mafiaPlayer.split(" ");
            this.playerAgent.getGameContext().setPlayerRole(playerInfo[0], playerInfo[1]);
        }

        ACLMessage resp = request.createReply();
        resp.setPerformative(ACLMessage.INFORM);

        return resp;
    }
}
