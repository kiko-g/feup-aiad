package utils;

public class ChatMessageTemplate {

    public static final String SkipAccusation = "SkipAccusation";
    public static final String AccusePlayer = "AccusePlayer";
    public static final String ClearPlayer = "ClearPlayer";
    public static final String HealerMessage = "HealerMessage";
    public static final String DetectiveMessageHasActivity = "DetectiveMessageHasActivity";
    public static final String DetectiveMessageHasNoActivity = "DetectiveMessageHasNoActivity";
    public static final String DetectiveAcuseLeader = "DetectiveAcuseLeader";


    public static String skipAccusation() { return "I'm not suspicious of anyone"; }

    public static String accusePlayerX(String name) { return "I'm suspicious of " + name; }

    public static String clearPlayer(String name) { return "I think " + name + "is innocent"; }

    public static String healerMessage(String name) { return "Tonight I saved " + name; }

    public static String detectiveMessageHasActivity(String name) { return name + " has night activity"; }

    public static String detectiveMessageHasNoActivity(String name) { return name + " doesn't have night activity"; }

    public static String detectiveAcuseLeader(String name) { return name + " is the leader of Mafia!!!!"; }
}
