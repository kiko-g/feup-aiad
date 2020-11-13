package utils;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.util.Random;

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

    public static String getFaction(String role) {
        switch (role) {
            case "Villager" :
            case "Healer" :
            case "Detective" : {
                return "Town";
            }

            case "Leader" :
            case "Killing" : {
                return "Mafia";
            }

            default:
                return "Neutral";
        }
    }

    public enum Trait {
        OverTheLine,
        Agressive,
        Mild,
        Peaceful;

        public static Trait getRandomTrait() {
            Random random = new Random();
            return values()[random.nextInt(values().length)];
        }
    }

    public static double getTraitMultiplier(Trait trait) {
        switch (trait) {
            case OverTheLine:
                return 1.5;
            case Agressive:
                return 1.3;
            case Mild:
                return 1.1;
            case Peaceful:
            default:
                return 1;
        }
    }
}
