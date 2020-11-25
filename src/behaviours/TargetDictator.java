package behaviours;

import agents.mafia.Leader;
import sajas.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.ProtocolNames;

public class TargetDictator extends OneShotBehaviour {

    private final Leader leaderAgent;

    private MessageTemplate killingPresentationTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchProtocol(ProtocolNames.LeaderOrder)
    );

    public TargetDictator(Leader leaderAgent) {
        this.leaderAgent = leaderAgent;
    }

    @Override
    public void action() {
        ACLMessage msg = this.leaderAgent.receive(this.killingPresentationTemplate);

        if (msg != null) {
            // msg.content => "I am <name>, your loyal soldier. What do you want me to do Boss?"
            ACLMessage response = this.leaderAgent.handleKillOrder(msg);
            this.leaderAgent.send(response);
        } else block();
    }
}
