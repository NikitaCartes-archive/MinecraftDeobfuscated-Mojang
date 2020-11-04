package net.minecraft.nbt;

public interface TagVisitor {
	void visitString(StringTag stringTag);

	void visitByte(ByteTag byteTag);

	void visitShort(ShortTag shortTag);

	void visitInt(IntTag intTag);

	void visitLong(LongTag longTag);

	void visitFloat(FloatTag floatTag);

	void visitDouble(DoubleTag doubleTag);

	void visitByteArray(ByteArrayTag byteArrayTag);

	void visitIntArray(IntArrayTag intArrayTag);

	void visitLongArray(LongArrayTag longArrayTag);

	void visitList(ListTag listTag);

	void visitCompound(CompoundTag compoundTag);

	void visitEnd(EndTag endTag);
}
