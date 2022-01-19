package net.minecraft.nbt.visitors;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.TagType;

public class SkipFields extends CollectToTag {
	private final Deque<FieldTree> stack = new ArrayDeque();

	public SkipFields(FieldSelector... fieldSelectors) {
		FieldTree fieldTree = FieldTree.createRoot();

		for (FieldSelector fieldSelector : fieldSelectors) {
			fieldTree.addEntry(fieldSelector);
		}

		this.stack.push(fieldTree);
	}

	@Override
	public StreamTagVisitor.EntryResult visitEntry(TagType<?> tagType, String string) {
		FieldTree fieldTree = (FieldTree)this.stack.element();
		if (fieldTree.isSelected(tagType, string)) {
			return StreamTagVisitor.EntryResult.SKIP;
		} else {
			if (tagType == CompoundTag.TYPE) {
				FieldTree fieldTree2 = (FieldTree)fieldTree.fieldsToRecurse().get(string);
				if (fieldTree2 != null) {
					this.stack.push(fieldTree2);
				}
			}

			return super.visitEntry(tagType, string);
		}
	}

	@Override
	public StreamTagVisitor.ValueResult visitContainerEnd() {
		if (this.depth() == ((FieldTree)this.stack.element()).depth()) {
			this.stack.pop();
		}

		return super.visitContainerEnd();
	}
}
