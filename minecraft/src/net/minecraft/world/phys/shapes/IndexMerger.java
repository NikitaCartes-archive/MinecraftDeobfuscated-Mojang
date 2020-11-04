package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;

interface IndexMerger {
	DoubleList getList();

	boolean forMergedIndexes(IndexMerger.IndexConsumer indexConsumer);

	int size();

	public interface IndexConsumer {
		boolean merge(int i, int j, int k);
	}
}
