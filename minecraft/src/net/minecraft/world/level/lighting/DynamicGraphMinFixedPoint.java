package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongList;
import java.util.function.LongPredicate;
import net.minecraft.util.Mth;

public abstract class DynamicGraphMinFixedPoint {
	private static final int NO_COMPUTED_LEVEL = 255;
	private final int levelCount;
	private final LongLinkedOpenHashSet[] queues;
	private final Long2ByteMap computedLevels;
	private int firstQueuedLevel;
	private volatile boolean hasWork;

	protected DynamicGraphMinFixedPoint(int i, int j, int k) {
		if (i >= 254) {
			throw new IllegalArgumentException("Level count must be < 254.");
		} else {
			this.levelCount = i;
			this.queues = new LongLinkedOpenHashSet[i];

			for (int l = 0; l < i; l++) {
				this.queues[l] = new LongLinkedOpenHashSet(j, 0.5F) {
					@Override
					protected void rehash(int i) {
						if (i > j) {
							super.rehash(i);
						}
					}
				};
			}

			this.computedLevels = new Long2ByteOpenHashMap(k, 0.5F) {
				@Override
				protected void rehash(int i) {
					if (i > k) {
						super.rehash(i);
					}
				}
			};
			this.computedLevels.defaultReturnValue((byte)-1);
			this.firstQueuedLevel = i;
		}
	}

	private int getKey(int i, int j) {
		int k = i;
		if (i > j) {
			k = j;
		}

		if (k > this.levelCount - 1) {
			k = this.levelCount - 1;
		}

		return k;
	}

	private void checkFirstQueuedLevel(int i) {
		int j = this.firstQueuedLevel;
		this.firstQueuedLevel = i;

		for (int k = j + 1; k < i; k++) {
			if (!this.queues[k].isEmpty()) {
				this.firstQueuedLevel = k;
				break;
			}
		}
	}

	protected void removeFromQueue(long l) {
		int i = this.computedLevels.get(l) & 255;
		if (i != 255) {
			int j = this.getLevel(l);
			int k = this.getKey(j, i);
			this.dequeue(l, k, this.levelCount, true);
			this.hasWork = this.firstQueuedLevel < this.levelCount;
		}
	}

	public void removeIf(LongPredicate longPredicate) {
		LongList longList = new LongArrayList();
		this.computedLevels.keySet().forEach(l -> {
			if (longPredicate.test(l)) {
				longList.add(l);
			}
		});
		longList.forEach(this::removeFromQueue);
	}

	private void dequeue(long l, int i, int j, boolean bl) {
		if (bl) {
			this.computedLevels.remove(l);
		}

		this.queues[i].remove(l);
		if (this.queues[i].isEmpty() && this.firstQueuedLevel == i) {
			this.checkFirstQueuedLevel(j);
		}
	}

	private void enqueue(long l, int i, int j) {
		this.computedLevels.put(l, (byte)i);
		this.queues[j].add(l);
		if (this.firstQueuedLevel > j) {
			this.firstQueuedLevel = j;
		}
	}

	protected void checkNode(long l) {
		this.checkEdge(l, l, this.levelCount - 1, false);
	}

	protected void checkEdge(long l, long m, int i, boolean bl) {
		this.checkEdge(l, m, i, this.getLevel(m), this.computedLevels.get(m) & 255, bl);
		this.hasWork = this.firstQueuedLevel < this.levelCount;
	}

	private void checkEdge(long l, long m, int i, int j, int k, boolean bl) {
		if (!this.isSource(m)) {
			i = Mth.clamp(i, 0, this.levelCount - 1);
			j = Mth.clamp(j, 0, this.levelCount - 1);
			boolean bl2;
			if (k == 255) {
				bl2 = true;
				k = j;
			} else {
				bl2 = false;
			}

			int n;
			if (bl) {
				n = Math.min(k, i);
			} else {
				n = Mth.clamp(this.getComputedLevel(m, l, i), 0, this.levelCount - 1);
			}

			int o = this.getKey(j, k);
			if (j != n) {
				int p = this.getKey(j, n);
				if (o != p && !bl2) {
					this.dequeue(m, o, p, false);
				}

				this.enqueue(m, n, p);
			} else if (!bl2) {
				this.dequeue(m, o, this.levelCount, true);
			}
		}
	}

	protected final void checkNeighbor(long l, long m, int i, boolean bl) {
		int j = this.computedLevels.get(m) & 255;
		int k = Mth.clamp(this.computeLevelFromNeighbor(l, m, i), 0, this.levelCount - 1);
		if (bl) {
			this.checkEdge(l, m, k, this.getLevel(m), j, true);
		} else {
			int n;
			boolean bl2;
			if (j == 255) {
				bl2 = true;
				n = Mth.clamp(this.getLevel(m), 0, this.levelCount - 1);
			} else {
				n = j;
				bl2 = false;
			}

			if (k == n) {
				this.checkEdge(l, m, this.levelCount - 1, bl2 ? n : this.getLevel(m), j, false);
			}
		}
	}

	protected final boolean hasWork() {
		return this.hasWork;
	}

	protected final int runUpdates(int i) {
		if (this.firstQueuedLevel >= this.levelCount) {
			return i;
		} else {
			while (this.firstQueuedLevel < this.levelCount && i > 0) {
				i--;
				LongLinkedOpenHashSet longLinkedOpenHashSet = this.queues[this.firstQueuedLevel];
				long l = longLinkedOpenHashSet.removeFirstLong();
				int j = Mth.clamp(this.getLevel(l), 0, this.levelCount - 1);
				if (longLinkedOpenHashSet.isEmpty()) {
					this.checkFirstQueuedLevel(this.levelCount);
				}

				int k = this.computedLevels.remove(l) & 255;
				if (k < j) {
					this.setLevel(l, k);
					this.checkNeighborsAfterUpdate(l, k, true);
				} else if (k > j) {
					this.enqueue(l, k, this.getKey(this.levelCount - 1, k));
					this.setLevel(l, this.levelCount - 1);
					this.checkNeighborsAfterUpdate(l, j, false);
				}
			}

			this.hasWork = this.firstQueuedLevel < this.levelCount;
			return i;
		}
	}

	public int getQueueSize() {
		return this.computedLevels.size();
	}

	protected abstract boolean isSource(long l);

	protected abstract int getComputedLevel(long l, long m, int i);

	protected abstract void checkNeighborsAfterUpdate(long l, int i, boolean bl);

	protected abstract int getLevel(long l);

	protected abstract void setLevel(long l, int i);

	protected abstract int computeLevelFromNeighbor(long l, long m, int i);
}
