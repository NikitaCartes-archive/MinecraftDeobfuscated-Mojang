package net.minecraft.nbt;

import java.io.DataInput;
import java.io.IOException;

public interface TagType<T extends Tag> {
	T load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException;

	default boolean isValue() {
		return false;
	}

	String getName();

	String getPrettyName();

	static TagType<EndTag> createInvalid(int i) {
		return new TagType<EndTag>() {
			public EndTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
				throw new IllegalArgumentException("Invalid tag id: " + i);
			}

			@Override
			public String getName() {
				return "INVALID[" + i + "]";
			}

			@Override
			public String getPrettyName() {
				return "UNKNOWN_" + i;
			}
		};
	}
}
