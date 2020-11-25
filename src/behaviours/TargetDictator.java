package behaviours;

import agents.mafia.Leader;
import sajas.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.ProtocolNames;

public class TargetDictator extends Behaviour {

    private final Leader leaderAgent;
    private boolean isDone;

    private MessageTemplate killingPresentationTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchProtocol(ProtocolNames.LeaderOrder)
    );

    public TargetDictator(Leader leaderAgent) {
        this.leaderAgent = leaderAgent;
        this.isDone = false;
    }

    @Override
    public void action() {
        ACLMessage msg = this.leaderAgent.receive(this.killingPresentationTemplate);

        if (msg != null) {
            // msg.content => "I am <name>, your loyal soldier. What do you want me to do Boss?"
            ACLMessage response = this.leaderAgent.handleKillOrder(msg);
            this.leaderAgent.send(response);
            this.isDone = true;
        } else block();
    }

    @Override
    public boolean done() {
        return this.isDone;
    }
}
