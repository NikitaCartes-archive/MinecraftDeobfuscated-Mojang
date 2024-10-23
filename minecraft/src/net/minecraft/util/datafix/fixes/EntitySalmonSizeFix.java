package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class EntitySalmonSizeFix extends NamedEntityFix {
	public EntitySalmonSizeFix(Schema schema) {
		super(schema, false, "EntitySalmonSizeFix", References.ENTITY, "minecraft:salmon");
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(DSL.remainderFinder(), dynamic -> {
			String string = dynamic.get("type").asString("medium");
			return string.equals("large") ? dynamic : dynamic.set("type", dynamic.createString("medium"));
		});
	}
}
