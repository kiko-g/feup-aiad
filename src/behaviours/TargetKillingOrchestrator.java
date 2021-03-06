package behaviours;

import agents.GameMaster;
import jade.core.AID;
import launcher.GameLauncher;
import sajas.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import uchicago.src.sim.network.DefaultDrawableNode;
import utils.Edge;
import utils.ProtocolNames;

import java.awt.*;
import java.util.List;

public class TargetKillingOrchestrator extends Behaviour {

    enum Steps {
        Init,
        Waiting
    }

    private Steps currentStep;

    private final GameMaster gameMaster;
    private int nRequestsSent;
    private int nRequestsResolved;

    private MessageTemplate proposalTemplate = MessageTemplate.and(
        MessageTemplate.MatchProtocol(ProtocolNames.TargetKilling),
            MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM)
            )
    );

    public TargetKillingOrchestrator(GameMaster gameMaster) {
        this.gameMaster = gameMaster;
        this.nRequestsResolved = 0;
        this.currentStep = Steps.Init;
    }

    @Override
    public void action() {
        if (this.currentStep == Steps.Init) {
            ACLMessage initialRequest = new ACLMessage(ACLMessage.REQUEST);
            initialRequest.setProtocol(ProtocolNames.TargetKilling);
            initialRequest.setContent("Who do you want to unalive tonight?");

            List<AID> messageReceivers = (this.gameMaster.getGameLobby().didAllKillingsDie()) ?
                    this.gameMaster.getGameLobby().getPlayersAIDRole("Leader", true) :
                    this.gameMaster.getGameLobby().getPlayersAIDRole("Killing", true);


            initialRequest = this.gameMaster.addReceiversMessage(initialRequest, messageReceivers);

            this.nRequestsSent = messageReceivers.size();
            this.gameMaster.send(initialRequest);
            this.currentStep = Steps.Waiting;
        }
        else {
            ACLMessage proposal = this.gameMaster.receive(this.proposalTemplate);
            if(proposal != null) {
                ACLMessage response = proposal.createReply();

                // Target evaluation only occurs if the Leader is dead
                if(this.gameMaster.getGameLobby().getPlayersAIDRole("Leader", true).size() == 0) {
                    if (this.gameMaster.getAttackedPlayers().contains(proposal.getContent())) {
                        response.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        response.setProtocol(ProtocolNames.TargetKilling);
                        response.setContent("Target already selected!");
                    } else {
                        // Updates Network graph
                        DefaultDrawableNode originNode = GameLauncher.getNodeByAgentName(proposal.getSender().getLocalName());
                        DefaultDrawableNode destinationNode = GameLauncher.getNodeByAgentName(proposal.getContent());
                        if(originNode != null) {
                            GameLauncher.removeAllNodeEdges(originNode);

                            Edge edge = new Edge(originNode, destinationNode, "Attack");
                            edge.setColor(Color.RED);
                            originNode.addOutEdge(edge);
                        }

                        response.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        response.setProtocol(ProtocolNames.TargetKilling);
                        response.setContent("OK");

                        System.out.println(proposal.getSender().getLocalName() + " decided to attack " + proposal.getContent());
                        this.gameMaster.addAttackedPlayer(proposal.getContent());
                        this.nRequestsResolved++;
                    }

                    this.gameMaster.send(response);
                } else { // #Leaders == 1
                    // Updates Network graph
                    DefaultDrawableNode killingNode = GameLauncher.getNodeByAgentName(proposal.getSender().getLocalName());
                    DefaultDrawableNode targetNode = GameLauncher.getNodeByAgentName(proposal.getContent());

                    List<AID> leaderAID = this.gameMaster.getGameLobby().getPlayersAIDRole("Leader", true);
                    DefaultDrawableNode leaderNode = GameLauncher.getNodeByAgentName(leaderAID.get(0).getLocalName());

                    if(killingNode != null) {
                        GameLauncher.removeAllNodeEdges(killingNode);

                        Edge edgeLeader = new Edge(leaderNode, killingNode, "Order");
                        edgeLeader.setColor(Color.YELLOW);
                        leaderNode.addOutEdge(edgeLeader);

                        Edge edgeKilling = new Edge(killingNode, targetNode, "Attack");
                        edgeKilling.setColor(Color.RED);
                        killingNode.addOutEdge(edgeKilling);
                    }

                    System.out.println(proposal.getSender().getLocalName() + " decided to attack " + proposal.getContent());
                    this.gameMaster.addAttackedPlayer(proposal.getContent());
                    this.nRequestsResolved++;
                }
            } else block();
        }
    }

    @Override
    public boolean done() {
        return this.nRequestsResolved == this.nRequestsSent;
    }
}
