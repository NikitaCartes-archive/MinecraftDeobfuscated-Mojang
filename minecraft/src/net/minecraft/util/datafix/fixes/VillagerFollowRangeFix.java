package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class VillagerFollowRangeFix extends NamedEntityFix {
	public VillagerFollowRangeFix(Schema schema) {
		super(schema, false, "Villager Follow Range Fix", References.ENTITY, "minecraft:villager");
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(DSL.remainderFinder(), VillagerFollowRangeFix::fixValue);
	}

	private static Dynamic<?> fixValue(Dynamic<?> dynamic) {
		return dynamic.update(
			"Attributes",
			dynamic2 -> dynamic.createList(
					dynamic2.asStream()
						.map(
							dynamicxx -> dynamicxx.get("Name").asString("").equals("generic.follow_range") && dynamicxx.get("Base").asDouble(0.0) == 16.0
									? dynamicxx.set("Base", dynamicxx.createDouble(48.0))
									: dynamicxx
						)
				)
		);
	}
}
