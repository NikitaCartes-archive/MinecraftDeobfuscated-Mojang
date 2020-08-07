package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class StriderGravityFix extends NamedEntityFix {
	public StriderGravityFix(Schema schema, boolean bl) {
		super(schema, bl, "StriderGravityFix", References.ENTITY, "minecraft:strider");
	}

	public Dynamic<?> fixTag(Dynamic<?> dynamic) {
		return dynamic.get("NoGravity").asBoolean(false) ? dynamic.set("NoGravity", dynamic.createBoolean(false)) : dynamic;
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(DSL.remainderFinder(), this::fixTag);
	}
}
