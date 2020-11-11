package protocols;

import agents.GameMaster;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

public class PlayerWaiter extends AchieveREResponder {

    GameMaster gameMaster;

    public PlayerWaiter(GameMaster gameMaster, MessageTemplate mt) {
        super(gameMaster, mt);
        this.gameMaster = gameMaster;
    }

    @Override
    protected ACLMessage handleRequest(ACLMessage request) throws RefuseException {
        if (request.getContent() == null)
            throw new RefuseException("Request not valid!");

        System.out.println(request.getContent().substring(9));

        // Parsing request
        // Message format: Hi! I am <NAME>, the <ROLE>.
        String [] splitMessage = request.getContent().split(" ");
        String role = splitMessage[5].substring(0, splitMessage[5].length() - 1);

        // Sender Info
        String playerGameName = request.getSender().getLocalName();

        if (this.gameMaster.getGameState() != GameMaster.GameStates.WAITING_FOR_PLAYERS)
            throw new RefuseException("Lobby already full!");

        this.gameMaster.getGameLobby().addPlayer(playerGameName, role, null);

        if (this.gameMaster.getGameLobby().isFull())
            this.gameMaster.setGameState(GameMaster.GameStates.READY);

        ACLMessage agree = request.createReply();
        agree.setPerformative(ACLMessage.AGREE);

        return agree;
    }

    @Override
    protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {

        ACLMessage inform = request.createReply();
        inform.setPerformative(ACLMessage.INFORM);
        return inform;
    }
}
