package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class EntityArmorStandSilentFix extends NamedEntityFix {
	public EntityArmorStandSilentFix(Schema schema, boolean bl) {
		super(schema, bl, "EntityArmorStandSilentFix", References.ENTITY, "ArmorStand");
	}

	public Dynamic<?> fixTag(Dynamic<?> dynamic) {
		return dynamic.get("Silent").asBoolean(false) && !dynamic.get("Marker").asBoolean(false) ? dynamic.remove("Silent") : dynamic;
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(DSL.remainderFinder(), this::fixTag);
	}
}
