package net.minecraft.nbt;

import java.io.DataInput;
import java.io.IOException;

public interface TagType<T extends Tag> {
	T load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException;

	StreamTagVisitor.ValueResult parse(DataInput dataInput, StreamTagVisitor streamTagVisitor) throws IOException;

	default void parseRoot(DataInput dataInput, StreamTagVisitor streamTagVisitor) throws IOException {
		switch (streamTagVisitor.visitRootEntry(this)) {
			case CONTINUE:
				this.parse(dataInput, streamTagVisitor);
			case HALT:
			default:
				break;
			case BREAK:
				this.skip(dataInput);
		}
	}

	void skip(DataInput dataInput, int i) throws IOException;

	void skip(DataInput dataInput) throws IOException;

	default boolean isValue() {
		return false;
	}

	String getName();

	String getPrettyName();

	static TagType<EndTag> createInvalid(int i) {
		return new TagType<EndTag>() {
			private IOException createException() {
				return new IOException("Invalid tag id: " + i);
			}

			public EndTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
				throw this.createException();
			}

			@Override
			public StreamTagVisitor.ValueResult parse(DataInput dataInput, StreamTagVisitor streamTagVisitor) throws IOException {
				throw this.createException();
			}

			@Override
			public void skip(DataInput dataInput, int i) throws IOException {
				throw this.createException();
			}

			@Override
			public void skip(DataInput dataInput) throws IOException {
				throw this.createException();
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

	public interface StaticSize<T extends Tag> extends TagType<T> {
		@Override
		default void skip(DataInput dataInput) throws IOException {
			dataInput.skipBytes(this.size());
		}

		@Override
		default void skip(DataInput dataInput, int i) throws IOException {
			dataInput.skipBytes(this.size() * i);
		}

		int size();
	}

	public interface VariableSize<T extends Tag> extends TagType<T> {
		@Override
		default void skip(DataInput dataInput, int i) throws IOException {
			for (int j = 0; j < i; j++) {
				this.skip(dataInput);
			}
		}
	}
}
