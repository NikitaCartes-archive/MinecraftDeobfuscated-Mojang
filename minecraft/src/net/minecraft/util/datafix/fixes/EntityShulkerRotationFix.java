package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.List;

public class EntityShulkerRotationFix extends NamedEntityFix {
	public EntityShulkerRotationFix(Schema schema) {
		super(schema, false, "EntityShulkerRotationFix", References.ENTITY, "minecraft:shulker");
	}

	public Dynamic<?> fixTag(Dynamic<?> dynamic) {
		List<Double> list = dynamic.get("Rotation").asList(dynamicx -> dynamicx.asDouble(180.0));
		if (!list.isEmpty()) {
			list.set(0, (Double)list.get(0) - 180.0);
			return dynamic.set("Rotation", dynamic.createList(list.stream().map(dynamic::createDouble)));
		} else {
			return dynamic;
		}
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(DSL.remainderFinder(), this::fixTag);
	}
}
