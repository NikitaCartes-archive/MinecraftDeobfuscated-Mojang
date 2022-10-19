package net.minecraft.network.chat;

public record LastSeenTrackedEntry(MessageSignature signature, boolean pending) {
	public LastSeenTrackedEntry acknowledge() {
		return this.pending ? new LastSeenTrackedEntry(this.signature, false) : this;
	}
}
