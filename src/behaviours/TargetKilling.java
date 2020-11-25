package behaviours;

import agents.mafia.Killing;
import sajas.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.ProtocolNames;

import java.util.ArrayList;
import java.util.List;

public class TargetKilling extends Behaviour {

    enum Steps {
        Init,
        Negotiation,
        End
    }

    private List<String> rejectedTargets;
    private final Killing killing;
    private Steps currStep;

    private MessageTemplate requestTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
            MessageTemplate.MatchProtocol(ProtocolNames.TargetKilling)
    );

    private MessageTemplate responseTemplate = MessageTemplate.and(
            MessageTemplate.MatchProtocol(ProtocolNames.TargetKilling),
            MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
                    MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL)
            )
    );

    private ACLMessage requestReceived;
    private ACLMessage lastProposal;

    public TargetKilling(Killing killing) {
        this.killing = killing;
        this.currStep = Steps.Init;
        this.rejectedTargets = new ArrayList<>();
    }

    @Override
    public void action() {
        switch (currStep) {
            case Init: {
                requestReceived = this.killing.receive(requestTemplate);

                if(requestReceived != null) {
                    lastProposal = this.killing.handleNightVoteRequestRejected(this.requestReceived, this.rejectedTargets);
                    lastProposal.setPerformative(ACLMessage.PROPOSE);
                    this.killing.send(lastProposal);

                    this.currStep = Steps.Negotiation;
                }
                break;
            }
            case Negotiation: {
                ACLMessage proposalResponse = this.killing.receive(responseTemplate);

                if(proposalResponse != null) {
                    // Proposal wasn't accepted; Need to make a new one
                    if(proposalResponse.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
                        this.rejectedTargets.add(lastProposal.getContent());

                        ACLMessage newProposal = this.killing.handleNightVoteRequestRejected(this.requestReceived, this.rejectedTargets);
                        this.killing.send(newProposal);
                    }
                    else this.currStep = Steps.End;
                }

                break;
            }
        }
    }

    @Override
    public boolean done() {
        return this.currStep == Steps.End;
    }
}
