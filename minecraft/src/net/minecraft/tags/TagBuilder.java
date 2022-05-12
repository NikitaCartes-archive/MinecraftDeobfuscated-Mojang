package net.minecraft.tags;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public class TagBuilder {
	private final List<TagEntry> entries = new ArrayList();

	public static TagBuilder create() {
		return new TagBuilder();
	}

	public List<TagEntry> build() {
		return List.copyOf(this.entries);
	}

	public TagBuilder add(TagEntry tagEntry) {
		this.entries.add(tagEntry);
		return this;
	}

	public TagBuilder addElement(ResourceLocation resourceLocation) {
		return this.add(TagEntry.element(resourceLocation));
	}

	public TagBuilder addOptionalElement(ResourceLocation resourceLocation) {
		return this.add(TagEntry.optionalElement(resourceLocation));
	}

	public TagBuilder addTag(ResourceLocation resourceLocation) {
		return this.add(TagEntry.tag(resourceLocation));
	}

	public TagBuilder addOptionalTag(ResourceLocation resourceLocation) {
		return this.add(TagEntry.optionalTag(resourceLocation));
	}
}
