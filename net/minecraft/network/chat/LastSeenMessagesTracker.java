/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.BitSet;
import java.util.Objects;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.LastSeenTrackedEntry;
import net.minecraft.network.chat.MessageSignature;
import org.jetbrains.annotations.Nullable;

public class LastSeenMessagesTracker {
    private final LastSeenTrackedEntry[] trackedMessages;
    private int tail;
    private int offset;
    @Nullable
    private MessageSignature lastTrackedMessage;

    public LastSeenMessagesTracker(int i) {
        this.trackedMessages = new LastSeenTrackedEntry[i];
    }

    public boolean addPending(MessageSignature messageSignature, boolean bl) {
        if (Objects.equals(messageSignature, this.lastTrackedMessage)) {
            return false;
        }
        this.lastTrackedMessage = messageSignature;
        this.addEntry(bl ? new LastSeenTrackedEntry(messageSignature, true) : null);
        return true;
    }

    private void addEntry(@Nullable LastSeenTrackedEntry lastSeenTrackedEntry) {
        int i = this.tail;
        this.tail = (i + 1) % this.trackedMessages.length;
        ++this.offset;
        this.trackedMessages[i] = lastSeenTrackedEntry;
    }

    public void ignorePending(MessageSignature messageSignature) {
        for (int i = 0; i < this.trackedMessages.length; ++i) {
            LastSeenTrackedEntry lastSeenTrackedEntry = this.trackedMessages[i];
            if (lastSeenTrackedEntry == null || !lastSeenTrackedEntry.pending() || !messageSignature.equals(lastSeenTrackedEntry.signature())) continue;
            this.trackedMessages[i] = null;
            break;
        }
    }

    public int getAndClearOffset() {
        int i = this.offset;
        this.offset = 0;
        return i;
    }

    public Update generateAndApplyUpdate() {
        int i = this.getAndClearOffset();
        BitSet bitSet = new BitSet(this.trackedMessages.length);
        ObjectArrayList<MessageSignature> objectList = new ObjectArrayList<MessageSignature>(this.trackedMessages.length);
        for (int j = 0; j < this.trackedMessages.length; ++j) {
            int k = (this.tail + j) % this.trackedMessages.length;
            LastSeenTrackedEntry lastSeenTrackedEntry = this.trackedMessages[k];
            if (lastSeenTrackedEntry == null) continue;
            bitSet.set(j, true);
            objectList.add(lastSeenTrackedEntry.signature());
            this.trackedMessages[k] = lastSeenTrackedEntry.acknowledge();
        }
        LastSeenMessages lastSeenMessages = new LastSeenMessages(objectList);
        LastSeenMessages.Update update = new LastSeenMessages.Update(i, bitSet);
        return new Update(lastSeenMessages, update);
    }

    public int offset() {
        return this.offset;
    }

    public record Update(LastSeenMessages lastSeen, LastSeenMessages.Update update) {
    }
}

