package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;

public class NbtAccounter {
	private static final int MAX_STACK_DEPTH = 512;
	private final long quota;
	private long usage;
	private final int maxDepth;
	private int depth;

	public NbtAccounter(long l, int i) {
		this.quota = l;
		this.maxDepth = i;
	}

	public static NbtAccounter create(long l) {
		return new NbtAccounter(l, 512);
	}

	public static NbtAccounter unlimitedHeap() {
		return new NbtAccounter(Long.MAX_VALUE, 512);
	}

	public void accountBytes(long l) {
		this.usage += l;
		if (this.usage > this.quota) {
			throw new NbtAccounterException("Tried to read NBT tag that was too big; tried to allocate: " + this.usage + " bytes where max allowed: " + this.quota);
		}
	}

	public void pushDepth() {
		this.depth++;
		if (this.depth > this.maxDepth) {
			throw new NbtAccounterException("Tried to read NBT tag with too high complexity, depth > " + this.maxDepth);
		}
	}

	public void popDepth() {
		this.depth--;
		if (this.depth < 0) {
			throw new NbtAccounterException("NBT-Accounter tried to pop stack-depth at top-level");
		}
	}

	@VisibleForTesting
	public long getUsage() {
		return this.usage;
	}

	@VisibleForTesting
	public int getDepth() {
		return this.depth;
	}
}
