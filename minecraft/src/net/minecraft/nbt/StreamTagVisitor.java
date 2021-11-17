package net.minecraft.nbt;

public interface StreamTagVisitor {
	StreamTagVisitor.ValueResult visitEnd();

	StreamTagVisitor.ValueResult visit(String string);

	StreamTagVisitor.ValueResult visit(byte b);

	StreamTagVisitor.ValueResult visit(short s);

	StreamTagVisitor.ValueResult visit(int i);

	StreamTagVisitor.ValueResult visit(long l);

	StreamTagVisitor.ValueResult visit(float f);

	StreamTagVisitor.ValueResult visit(double d);

	StreamTagVisitor.ValueResult visit(byte[] bs);

	StreamTagVisitor.ValueResult visit(int[] is);

	StreamTagVisitor.ValueResult visit(long[] ls);

	StreamTagVisitor.ValueResult visitList(TagType<?> tagType, int i);

	StreamTagVisitor.EntryResult visitEntry(TagType<?> tagType);

	StreamTagVisitor.EntryResult visitEntry(TagType<?> tagType, String string);

	StreamTagVisitor.EntryResult visitElement(TagType<?> tagType, int i);

	StreamTagVisitor.ValueResult visitContainerEnd();

	StreamTagVisitor.ValueResult visitRootEntry(TagType<?> tagType);

	public static enum EntryResult {
		ENTER,
		SKIP,
		BREAK,
		HALT;
	}

	public static enum ValueResult {
		CONTINUE,
		BREAK,
		HALT;
	}
}
