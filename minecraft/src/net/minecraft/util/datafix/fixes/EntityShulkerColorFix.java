package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class EntityShulkerColorFix extends NamedEntityFix {
	public EntityShulkerColorFix(Schema schema, boolean bl) {
		super(schema, bl, "EntityShulkerColorFix", References.ENTITY, "minecraft:shulker");
	}

	public Dynamic<?> fixTag(Dynamic<?> dynamic) {
		return !dynamic.get("Color").map(Dynamic::asNumber).isPresent() ? dynamic.set("Color", dynamic.createByte((byte)10)) : dynamic;
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(DSL.remainderFinder(), this::fixTag);
	}
}
