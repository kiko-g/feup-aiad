package utils;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class Util {

    public static ACLMessage buildMessage(int messageType, String protocolName, String content) {
        ACLMessage msg = new ACLMessage(messageType);
        msg.setProtocol(protocolName);

        if (content != null)
            msg.setContent(content);

        return msg;
    }

    public static ACLMessage createMessage(int messageType, AID receiver, String protocolName, String content) {
        ACLMessage msg = buildMessage(messageType, protocolName, content);
        msg.addReceiver(receiver);

        return msg;
    }

    public static ACLMessage createMessage(int messageType, AID receiver, String protocolName) {
        return createMessage(messageType, receiver, protocolName, null);
    }
}
