package net.minecraft.nbt.visitors;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.TagType;

public record FieldTree(int depth, Map<String, TagType<?>> selectedFields, Map<String, FieldTree> fieldsToRecurse) {
	private FieldTree(int i) {
		this(i, new HashMap(), new HashMap());
	}

	public static FieldTree createRoot() {
		return new FieldTree(1);
	}

	public void addEntry(FieldSelector fieldSelector) {
		if (this.depth <= fieldSelector.path().size()) {
			((FieldTree)this.fieldsToRecurse.computeIfAbsent((String)fieldSelector.path().get(this.depth - 1), string -> new FieldTree(this.depth + 1)))
				.addEntry(fieldSelector);
		} else {
			this.selectedFields.put(fieldSelector.name(), fieldSelector.type());
		}
	}

	public boolean isSelected(TagType<?> tagType, String string) {
		return tagType.equals(this.selectedFields().get(string));
	}
}
