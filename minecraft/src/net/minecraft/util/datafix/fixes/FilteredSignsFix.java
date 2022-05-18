package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class FilteredSignsFix extends NamedEntityFix {
	public FilteredSignsFix(Schema schema) {
		super(schema, false, "Remove filtered text from signs", References.BLOCK_ENTITY, "minecraft:sign");
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(DSL.remainderFinder(), dynamic -> dynamic.remove("FilteredText1").remove("FilteredText2").remove("FilteredText3").remove("FilteredText4"));
	}
}
