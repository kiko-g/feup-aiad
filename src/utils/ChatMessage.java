package utils;

import java.io.Serializable;

public class ChatMessage implements Serializable {

    private long timeReception; // Time at which the message was received by the GM
    private final String content;
    private final String senderName;

//    public ChatMessage(String chatPostMessageContent) {
//        // Message Format: "<sentAt>#<senderName>#<content>"
//        String[] messageFields = chatPostMessageContent.split("#");
//
//        this.sentAt = Long.parseLong(messageFields[0]);
//        this.content = messageFields[2];
//        this.senderName = messageFields[1];
//    }

    public ChatMessage(String content, String senderName) {
        this.content = content;
        this.senderName = senderName;
    }

    // To be called by GM before retransmitting
    public void stampReceptionTime() {
        this.timeReception = System.currentTimeMillis();
    }

    public long getTimeReception() {
        return timeReception;
    }

    public String getContent() {
        return content;
    }

    public String getSenderName() {
        return senderName;
    }
}
