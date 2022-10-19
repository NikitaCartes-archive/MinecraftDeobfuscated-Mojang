package net.minecraft.network.chat;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.Optional;
import javax.annotation.Nullable;

public class LastSeenMessagesValidator {
	private final int lastSeenCount;
	private final ObjectList<LastSeenTrackedEntry> trackedMessages = new ObjectArrayList<>();
	@Nullable
	private MessageSignature lastPendingMessage;

	public LastSeenMessagesValidator(int i) {
		this.lastSeenCount = i;

		for (int j = 0; j < i; j++) {
			this.trackedMessages.add(null);
		}
	}

	public void addPending(MessageSignature messageSignature) {
		if (!messageSignature.equals(this.lastPendingMessage)) {
			this.trackedMessages.add(new LastSeenTrackedEntry(messageSignature, true));
			this.lastPendingMessage = messageSignature;
		}
	}

	public int trackedMessagesCount() {
		return this.trackedMessages.size();
	}

	public boolean applyOffset(int i) {
		int j = this.trackedMessages.size() - this.lastSeenCount;
		if (i >= 0 && i <= j) {
			this.trackedMessages.removeElements(0, i);
			return true;
		} else {
			return false;
		}
	}

	public Optional<LastSeenMessages> applyUpdate(LastSeenMessages.Update update) {
		if (!this.applyOffset(update.offset())) {
			return Optional.empty();
		} else {
			ObjectList<MessageSignature> objectList = new ObjectArrayList<>(update.acknowledged().cardinality());
			if (update.acknowledged().length() > this.lastSeenCount) {
				return Optional.empty();
			} else {
				for (int i = 0; i < this.lastSeenCount; i++) {
					boolean bl = update.acknowledged().get(i);
					LastSeenTrackedEntry lastSeenTrackedEntry = (LastSeenTrackedEntry)this.trackedMessages.get(i);
					if (bl) {
						if (lastSeenTrackedEntry == null) {
							return Optional.empty();
						}

						this.trackedMessages.set(i, lastSeenTrackedEntry.acknowledge());
						objectList.add(lastSeenTrackedEntry.signature());
					} else {
						if (lastSeenTrackedEntry != null && !lastSeenTrackedEntry.pending()) {
							return Optional.empty();
						}

						this.trackedMessages.set(i, null);
					}
				}

				return Optional.of(new LastSeenMessages(objectList));
			}
		}
	}
}
