package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class EntityPaintingFieldsRenameFix extends NamedEntityFix {
	public EntityPaintingFieldsRenameFix(Schema schema) {
		super(schema, false, "EntityPaintingFieldsRenameFix", References.ENTITY, "minecraft:painting");
	}

	public Dynamic<?> fixTag(Dynamic<?> dynamic) {
		return dynamic.renameField("Motive", "variant").renameField("Facing", "facing");
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(DSL.remainderFinder(), this::fixTag);
	}
}
