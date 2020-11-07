package utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChatLog {
    private List<ChatMessage> messagesReceived;

    public ChatLog() {
        this.messagesReceived = new ArrayList<>();
    }

    public List<ChatMessage> getMessagesReceived() {
        return messagesReceived;
    }

    public void addMessage(ChatMessage chatMessage) {
        this.messagesReceived.add(chatMessage);

        // Sorts by time
        messagesReceived.sort(Comparator.comparingLong(ChatMessage::getTimeReception));
    }

    // All messages stores from agent with name == agentName, ordered by timestamp
    public List<ChatMessage> getMessagesFrom(String agentName) {
        List<ChatMessage> results = new ArrayList<>();

        for (ChatMessage chatMessage : this.messagesReceived)
            if (chatMessage.getSenderName().equals(agentName))
                results.add(chatMessage);

        return results;
    }
}
