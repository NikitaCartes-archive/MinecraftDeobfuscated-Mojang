package net.minecraft.nbt.visitors;

import java.util.List;
import net.minecraft.nbt.TagType;

public record FieldSelector(List<String> path, TagType<?> type, String name) {
	public FieldSelector(TagType<?> tagType, String string) {
		this(List.of(), tagType, string);
	}

	public FieldSelector(String string, TagType<?> tagType, String string2) {
		this(List.of(string), tagType, string2);
	}

	public FieldSelector(String string, String string2, TagType<?> tagType, String string3) {
		this(List.of(string, string2), tagType, string3);
	}
}
