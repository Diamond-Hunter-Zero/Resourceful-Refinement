package com.resourceful_refinement.content.glare;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelemetryService {
    public static final int MAX_MESSAGES = 16;

    private final Map<GlareAddress, Deque<GlareMessage>> inboxes = new HashMap<>();

    public void send(GlareMessage message) {
        Deque<GlareMessage> inbox = inboxes.computeIfAbsent(message.to(), ignored -> new ArrayDeque<>());
        inbox.addLast(message);
        while (inbox.size() > MAX_MESSAGES) {
            inbox.removeFirst();
        }
    }

    public List<GlareMessage> read(GlareAddress address) {
        Deque<GlareMessage> inbox = inboxes.get(address);
        return inbox == null ? List.of() : List.copyOf(inbox);
    }

    public void discard(GlareAddress address, int index) {
        Deque<GlareMessage> inbox = inboxes.get(address);
        if (inbox == null || index < 0 || index >= inbox.size()) {
            return;
        }
        GlareMessage[] messages = inbox.toArray(GlareMessage[]::new);
        inbox.clear();
        for (int i = 0; i < messages.length; i++) {
            if (i != index) {
                inbox.addLast(messages[i]);
            }
        }
    }
}
