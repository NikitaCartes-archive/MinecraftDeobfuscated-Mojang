package net.minecraft.nbt.visitors;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.TagType;

public class CollectFields extends CollectToTag {
	private int fieldsToGetCount;
	private final Set<TagType<?>> wantedTypes;
	private final Deque<CollectFields.StackFrame> stack = new ArrayDeque();

	public CollectFields(CollectFields.WantedField... wantedFields) {
		this.fieldsToGetCount = wantedFields.length;
		Builder<TagType<?>> builder = ImmutableSet.builder();
		CollectFields.StackFrame stackFrame = new CollectFields.StackFrame(1);

		for (CollectFields.WantedField wantedField : wantedFields) {
			stackFrame.addEntry(wantedField);
			builder.add(wantedField.type);
		}

		this.stack.push(stackFrame);
		builder.add(CompoundTag.TYPE);
		this.wantedTypes = builder.build();
	}

	@Override
	public StreamTagVisitor.ValueResult visitRootEntry(TagType<?> tagType) {
		return tagType != CompoundTag.TYPE ? StreamTagVisitor.ValueResult.HALT : super.visitRootEntry(tagType);
	}

	@Override
	public StreamTagVisitor.EntryResult visitEntry(TagType<?> tagType) {
		CollectFields.StackFrame stackFrame = (CollectFields.StackFrame)this.stack.element();
		if (this.depth() > stackFrame.depth()) {
			return super.visitEntry(tagType);
		} else if (this.fieldsToGetCount <= 0) {
			return StreamTagVisitor.EntryResult.HALT;
		} else {
			return !this.wantedTypes.contains(tagType) ? StreamTagVisitor.EntryResult.SKIP : super.visitEntry(tagType);
		}
	}

	@Override
	public StreamTagVisitor.EntryResult visitEntry(TagType<?> tagType, String string) {
		CollectFields.StackFrame stackFrame = (CollectFields.StackFrame)this.stack.element();
		if (this.depth() > stackFrame.depth()) {
			return super.visitEntry(tagType, string);
		} else if (stackFrame.fieldsToGet.remove(string, tagType)) {
			this.fieldsToGetCount--;
			return super.visitEntry(tagType, string);
		} else {
			if (tagType == CompoundTag.TYPE) {
				CollectFields.StackFrame stackFrame2 = (CollectFields.StackFrame)stackFrame.fieldsToRecurse.get(string);
				if (stackFrame2 != null) {
					this.stack.push(stackFrame2);
					return super.visitEntry(tagType, string);
				}
			}

			return StreamTagVisitor.EntryResult.SKIP;
		}
	}

	@Override
	public StreamTagVisitor.ValueResult visitContainerEnd() {
		if (this.depth() == ((CollectFields.StackFrame)this.stack.element()).depth()) {
			this.stack.pop();
		}

		return super.visitContainerEnd();
	}

	public int getMissingFieldCount() {
		return this.fieldsToGetCount;
	}

	static record StackFrame() {
		private final int depth;
		final Map<String, TagType<?>> fieldsToGet;
		final Map<String, CollectFields.StackFrame> fieldsToRecurse;

		public StackFrame(int i) {
			this(i, new HashMap(), new HashMap());
		}

		private StackFrame(int i, Map<String, TagType<?>> map, Map<String, CollectFields.StackFrame> map2) {
			this.depth = i;
			this.fieldsToGet = map;
			this.fieldsToRecurse = map2;
		}

		public void addEntry(CollectFields.WantedField wantedField) {
			if (this.depth <= wantedField.path.size()) {
				((CollectFields.StackFrame)this.fieldsToRecurse
						.computeIfAbsent((String)wantedField.path.get(this.depth - 1), string -> new CollectFields.StackFrame(this.depth + 1)))
					.addEntry(wantedField);
			} else {
				this.fieldsToGet.put(wantedField.name, wantedField.type);
			}
		}
	}

	public static record WantedField() {
		final List<String> path;
		final TagType<?> type;
		final String name;

		public WantedField(TagType<?> tagType, String string) {
			this(List.of(), tagType, string);
		}

		public WantedField(String string, TagType<?> tagType, String string2) {
			this(List.of(string), tagType, string2);
		}

		public WantedField(String string, String string2, TagType<?> tagType, String string3) {
			this(List.of(string, string2), tagType, string3);
		}

		public WantedField(List<String> list, TagType<?> tagType, String string) {
			this.path = list;
			this.type = tagType;
			this.name = string;
		}
	}
}
