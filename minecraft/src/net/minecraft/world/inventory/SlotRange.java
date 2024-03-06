package net.minecraft.world.inventory;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.StringRepresentable;

public interface SlotRange extends StringRepresentable {
	IntList slots();

	default int size() {
		return this.slots().size();
	}

	static SlotRange of(String string, IntList intList) {
		return new SlotRange() {
			@Override
			public IntList slots() {
				return intList;
			}

			@Override
			public String getSerializedName() {
				return string;
			}

			public String toString() {
				return string;
			}
		};
	}
}
