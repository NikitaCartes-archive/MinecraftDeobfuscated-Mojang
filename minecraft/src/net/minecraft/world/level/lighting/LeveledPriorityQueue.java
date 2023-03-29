package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;

public class LeveledPriorityQueue {
	private final int levelCount;
	private final LongLinkedOpenHashSet[] queues;
	private int firstQueuedLevel;

	public LeveledPriorityQueue(int i, int j) {
		this.levelCount = i;
		this.queues = new LongLinkedOpenHashSet[i];

		for (int k = 0; k < i; k++) {
			this.queues[k] = new LongLinkedOpenHashSet(j, 0.5F) {
				@Override
				protected void rehash(int i) {
					if (i > j) {
						super.rehash(i);
					}
				}
			};
		}

		this.firstQueuedLevel = i;
	}

	public long removeFirstLong() {
		LongLinkedOpenHashSet longLinkedOpenHashSet = this.queues[this.firstQueuedLevel];
		long l = longLinkedOpenHashSet.removeFirstLong();
		if (longLinkedOpenHashSet.isEmpty()) {
			this.checkFirstQueuedLevel(this.levelCount);
		}

		return l;
	}

	public boolean isEmpty() {
		return this.firstQueuedLevel >= this.levelCount;
	}

	public void dequeue(long l, int i, int j) {
		LongLinkedOpenHashSet longLinkedOpenHashSet = this.queues[i];
		longLinkedOpenHashSet.remove(l);
		if (longLinkedOpenHashSet.isEmpty() && this.firstQueuedLevel == i) {
			this.checkFirstQueuedLevel(j);
		}
	}

	public void enqueue(long l, int i) {
		this.queues[i].add(l);
		if (this.firstQueuedLevel > i) {
			this.firstQueuedLevel = i;
		}
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
}
