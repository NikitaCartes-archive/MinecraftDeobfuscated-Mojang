package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;

public class NbtAccounter {
	public static final NbtAccounter UNLIMITED = new NbtAccounter(0L) {
		@Override
		public void accountBits(long l) {
		}
	};
	private final long quota;
	private long usage;

	public NbtAccounter(long l) {
		this.quota = l;
	}

	public void accountBits(long l) {
		this.usage += l / 8L;
		if (this.usage > this.quota) {
			throw new RuntimeException("Tried to read NBT tag that was too big; tried to allocate: " + this.usage + "bytes where max allowed: " + this.quota);
		}
	}

	@VisibleForTesting
	public long getUsage() {
		return this.usage;
	}
}
