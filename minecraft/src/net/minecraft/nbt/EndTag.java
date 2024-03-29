package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class EndTag implements Tag {
	private static final int SELF_SIZE_IN_BYTES = 8;
	public static final TagType<EndTag> TYPE = new TagType<EndTag>() {
		public EndTag load(DataInput dataInput, NbtAccounter nbtAccounter) {
			nbtAccounter.accountBytes(8L);
			return EndTag.INSTANCE;
		}

		@Override
		public StreamTagVisitor.ValueResult parse(DataInput dataInput, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) {
			nbtAccounter.accountBytes(8L);
			return streamTagVisitor.visitEnd();
		}

		@Override
		public void skip(DataInput dataInput, int i, NbtAccounter nbtAccounter) {
		}

		@Override
		public void skip(DataInput dataInput, NbtAccounter nbtAccounter) {
		}

		@Override
		public String getName() {
			return "END";
		}

		@Override
		public String getPrettyName() {
			return "TAG_End";
		}

		@Override
		public boolean isValue() {
			return true;
		}
	};
	public static final EndTag INSTANCE = new EndTag();

	private EndTag() {
	}

	@Override
	public void write(DataOutput dataOutput) throws IOException {
	}

	@Override
	public int sizeInBytes() {
		return 8;
	}

	@Override
	public byte getId() {
		return 0;
	}

	@Override
	public TagType<EndTag> getType() {
		return TYPE;
	}

	@Override
	public String toString() {
		return this.getAsString();
	}

	public EndTag copy() {
		return this;
	}

	@Override
	public void accept(TagVisitor tagVisitor) {
		tagVisitor.visitEnd(this);
	}

	@Override
	public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamTagVisitor) {
		return streamTagVisitor.visitEnd();
	}
}
