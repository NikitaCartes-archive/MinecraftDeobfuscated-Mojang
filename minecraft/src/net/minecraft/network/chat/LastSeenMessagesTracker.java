package net.minecraft.network.chat;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.BitSet;
import java.util.Objects;
import javax.annotation.Nullable;

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
		} else {
			this.lastTrackedMessage = messageSignature;
			this.addEntry(bl ? new LastSeenTrackedEntry(messageSignature, true) : null);
			return true;
		}
	}

	private void addEntry(@Nullable LastSeenTrackedEntry lastSeenTrackedEntry) {
		int i = this.tail;
		this.tail = (i + 1) % this.trackedMessages.length;
		this.offset++;
		this.trackedMessages[i] = lastSeenTrackedEntry;
	}

	public void ignorePending(MessageSignature messageSignature) {
		for (int i = 0; i < this.trackedMessages.length; i++) {
			LastSeenTrackedEntry lastSeenTrackedEntry = this.trackedMessages[i];
			if (lastSeenTrackedEntry != null && lastSeenTrackedEntry.pending() && messageSignature.equals(lastSeenTrackedEntry.signature())) {
				this.trackedMessages[i] = null;
				break;
			}
		}
	}

	public int getAndClearOffset() {
		int i = this.offset;
		this.offset = 0;
		return i;
	}

	public LastSeenMessagesTracker.Update generateAndApplyUpdate() {
		int i = this.getAndClearOffset();
		BitSet bitSet = new BitSet(this.trackedMessages.length);
		ObjectList<MessageSignature> objectList = new ObjectArrayList<>(this.trackedMessages.length);

		for (int j = 0; j < this.trackedMessages.length; j++) {
			int k = (this.tail + j) % this.trackedMessages.length;
			LastSeenTrackedEntry lastSeenTrackedEntry = this.trackedMessages[k];
			if (lastSeenTrackedEntry != null) {
				bitSet.set(j, true);
				objectList.add(lastSeenTrackedEntry.signature());
				this.trackedMessages[k] = lastSeenTrackedEntry.acknowledge();
			}
		}

		LastSeenMessages lastSeenMessages = new LastSeenMessages(objectList);
		LastSeenMessages.Update update = new LastSeenMessages.Update(i, bitSet);
		return new LastSeenMessagesTracker.Update(lastSeenMessages, update);
	}

	public int offset() {
		return this.offset;
	}

	public static record Update(LastSeenMessages lastSeen, LastSeenMessages.Update update) {
	}
}
