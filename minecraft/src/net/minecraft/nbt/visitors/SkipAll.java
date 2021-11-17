package net.minecraft.nbt.visitors;

import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.TagType;

public interface SkipAll extends StreamTagVisitor {
	SkipAll INSTANCE = new SkipAll() {
	};

	@Override
	default StreamTagVisitor.ValueResult visitEnd() {
		return StreamTagVisitor.ValueResult.CONTINUE;
	}

	@Override
	default StreamTagVisitor.ValueResult visit(String string) {
		return StreamTagVisitor.ValueResult.CONTINUE;
	}

	@Override
	default StreamTagVisitor.ValueResult visit(byte b) {
		return StreamTagVisitor.ValueResult.CONTINUE;
	}

	@Override
	default StreamTagVisitor.ValueResult visit(short s) {
		return StreamTagVisitor.ValueResult.CONTINUE;
	}

	@Override
	default StreamTagVisitor.ValueResult visit(int i) {
		return StreamTagVisitor.ValueResult.CONTINUE;
	}

	@Override
	default StreamTagVisitor.ValueResult visit(long l) {
		return StreamTagVisitor.ValueResult.CONTINUE;
	}

	@Override
	default StreamTagVisitor.ValueResult visit(float f) {
		return StreamTagVisitor.ValueResult.CONTINUE;
	}

	@Override
	default StreamTagVisitor.ValueResult visit(double d) {
		return StreamTagVisitor.ValueResult.CONTINUE;
	}

	@Override
	default StreamTagVisitor.ValueResult visit(byte[] bs) {
		return StreamTagVisitor.ValueResult.CONTINUE;
	}

	@Override
	default StreamTagVisitor.ValueResult visit(int[] is) {
		return StreamTagVisitor.ValueResult.CONTINUE;
	}

	@Override
	default StreamTagVisitor.ValueResult visit(long[] ls) {
		return StreamTagVisitor.ValueResult.CONTINUE;
	}

	@Override
	default StreamTagVisitor.ValueResult visitList(TagType<?> tagType, int i) {
		return StreamTagVisitor.ValueResult.CONTINUE;
	}

	@Override
	default StreamTagVisitor.EntryResult visitElement(TagType<?> tagType, int i) {
		return StreamTagVisitor.EntryResult.SKIP;
	}

	@Override
	default StreamTagVisitor.EntryResult visitEntry(TagType<?> tagType) {
		return StreamTagVisitor.EntryResult.SKIP;
	}

	@Override
	default StreamTagVisitor.EntryResult visitEntry(TagType<?> tagType, String string) {
		return StreamTagVisitor.EntryResult.SKIP;
	}

	@Override
	default StreamTagVisitor.ValueResult visitContainerEnd() {
		return StreamTagVisitor.ValueResult.CONTINUE;
	}

	@Override
	default StreamTagVisitor.ValueResult visitRootEntry(TagType<?> tagType) {
		return StreamTagVisitor.ValueResult.CONTINUE;
	}
}
