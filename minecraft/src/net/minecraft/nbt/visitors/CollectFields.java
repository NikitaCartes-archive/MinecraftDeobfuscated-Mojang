package net.minecraft.nbt.visitors;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.TagType;

public class CollectFields extends CollectToTag {
	private int fieldsToGetCount;
	private final Set<TagType<?>> wantedTypes;
	private final Deque<FieldTree> stack = new ArrayDeque();

	public CollectFields(FieldSelector... fieldSelectors) {
		this.fieldsToGetCount = fieldSelectors.length;
		Builder<TagType<?>> builder = ImmutableSet.builder();
		FieldTree fieldTree = FieldTree.createRoot();

		for (FieldSelector fieldSelector : fieldSelectors) {
			fieldTree.addEntry(fieldSelector);
			builder.add(fieldSelector.type());
		}

		this.stack.push(fieldTree);
		builder.add(CompoundTag.TYPE);
		this.wantedTypes = builder.build();
	}

	@Override
	public StreamTagVisitor.ValueResult visitRootEntry(TagType<?> tagType) {
		return tagType != CompoundTag.TYPE ? StreamTagVisitor.ValueResult.HALT : super.visitRootEntry(tagType);
	}

	@Override
	public StreamTagVisitor.EntryResult visitEntry(TagType<?> tagType) {
		FieldTree fieldTree = (FieldTree)this.stack.element();
		if (this.depth() > fieldTree.depth()) {
			return super.visitEntry(tagType);
		} else if (this.fieldsToGetCount <= 0) {
			return StreamTagVisitor.EntryResult.HALT;
		} else {
			return !this.wantedTypes.contains(tagType) ? StreamTagVisitor.EntryResult.SKIP : super.visitEntry(tagType);
		}
	}

	@Override
	public StreamTagVisitor.EntryResult visitEntry(TagType<?> tagType, String string) {
		FieldTree fieldTree = (FieldTree)this.stack.element();
		if (this.depth() > fieldTree.depth()) {
			return super.visitEntry(tagType, string);
		} else if (fieldTree.selectedFields().remove(string, tagType)) {
			this.fieldsToGetCount--;
			return super.visitEntry(tagType, string);
		} else {
			if (tagType == CompoundTag.TYPE) {
				FieldTree fieldTree2 = (FieldTree)fieldTree.fieldsToRecurse().get(string);
				if (fieldTree2 != null) {
					this.stack.push(fieldTree2);
					return super.visitEntry(tagType, string);
				}
			}

			return StreamTagVisitor.EntryResult.SKIP;
		}
	}

	@Override
	public StreamTagVisitor.ValueResult visitContainerEnd() {
		if (this.depth() == ((FieldTree)this.stack.element()).depth()) {
			this.stack.pop();
		}

		return super.visitContainerEnd();
	}

	public int getMissingFieldCount() {
		return this.fieldsToGetCount;
	}
}
