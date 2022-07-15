package net.minecraft.network.chat;

import java.util.Arrays;

public class LastSeenMessagesTracker {
	private final LastSeenMessages.Entry[] status;
	private int size;
	private LastSeenMessages result = LastSeenMessages.EMPTY;

	public LastSeenMessagesTracker(int i) {
		this.status = new LastSeenMessages.Entry[i];
	}

	public void push(LastSeenMessages.Entry entry) {
		LastSeenMessages.Entry entry2 = entry;

		for (int i = 0; i < this.size; i++) {
			LastSeenMessages.Entry entry3 = this.status[i];
			this.status[i] = entry2;
			entry2 = entry3;
			if (entry3.profileId().equals(entry.profileId())) {
				entry2 = null;
				break;
			}
		}

		if (entry2 != null && this.size < this.status.length) {
			this.status[this.size++] = entry2;
		}

		this.result = new LastSeenMessages(Arrays.asList((LastSeenMessages.Entry[])Arrays.copyOf(this.status, this.size)));
	}

	public LastSeenMessages get() {
		return this.result;
	}
}
