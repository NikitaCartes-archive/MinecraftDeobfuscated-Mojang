package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class CatTypeFix extends NamedEntityFix {
	public CatTypeFix(Schema schema, boolean bl) {
		super(schema, bl, "CatTypeFix", References.ENTITY, "minecraft:cat");
	}

	public Dynamic<?> fixTag(Dynamic<?> dynamic) {
		return dynamic.get("CatType").asInt(0) == 9 ? dynamic.set("CatType", dynamic.createInt(10)) : dynamic;
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(DSL.remainderFinder(), this::fixTag);
	}
}
