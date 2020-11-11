package utils;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class ChatMessage implements Serializable {

    private long timeReception; // Time at which the message was received by the GM
    private final String content;
    private final String templateMessage;
    private final String senderName;

    public ChatMessage(String content, String templateMessage, String senderName) {
        this.content = content;
        this.senderName = senderName;
        this.templateMessage = templateMessage;
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

    public String getTemplateMessage() {
        return templateMessage;
    }

    @Override
    public String toString() {
        String time = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(timeReception) % 24,
                TimeUnit.MILLISECONDS.toMinutes(timeReception) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(timeReception) % TimeUnit.MINUTES.toSeconds(1));

        return "[" + time + "] " +
                senderName + ": " + content;
    }
}
