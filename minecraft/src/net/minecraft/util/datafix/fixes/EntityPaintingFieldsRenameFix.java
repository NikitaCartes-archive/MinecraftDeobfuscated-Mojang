package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.ExtraDataFixUtils;

public class EntityPaintingFieldsRenameFix extends NamedEntityFix {
	public EntityPaintingFieldsRenameFix(Schema schema) {
		super(schema, false, "EntityPaintingFieldsRenameFix", References.ENTITY, "minecraft:painting");
	}

	public Dynamic<?> fixTag(Dynamic<?> dynamic) {
		return ExtraDataFixUtils.renameField(ExtraDataFixUtils.renameField(dynamic, "Motive", "variant"), "Facing", "facing");
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(DSL.remainderFinder(), this::fixTag);
	}
}
