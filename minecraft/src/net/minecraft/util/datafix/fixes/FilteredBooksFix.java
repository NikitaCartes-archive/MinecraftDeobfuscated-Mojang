package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class FilteredBooksFix extends ItemStackTagFix {
	public FilteredBooksFix(Schema schema) {
		super(schema, "Remove filtered text from books", string -> string.equals("minecraft:writable_book") || string.equals("minecraft:written_book"));
	}

	@Override
	protected <T> Dynamic<T> fixItemStackTag(Dynamic<T> dynamic) {
		return dynamic.remove("filtered_title").remove("filtered_pages");
	}
}
