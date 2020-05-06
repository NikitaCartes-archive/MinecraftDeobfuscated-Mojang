package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

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
							dynamicxx -> ((String)dynamicxx.get("Name").asString().orElse("")).equals("generic.follow_range")
										&& ((Number)dynamicxx.get("Base").asNumber().orElse(0)).doubleValue() == 16.0
									? dynamicxx.set("Base", dynamicxx.createDouble(48.0))
									: dynamicxx
						)
				)
		);
	}
}
