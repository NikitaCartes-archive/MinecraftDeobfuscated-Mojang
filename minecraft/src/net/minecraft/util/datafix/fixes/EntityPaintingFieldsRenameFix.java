package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class EntityPaintingFieldsRenameFix extends NamedEntityFix {
	public EntityPaintingFieldsRenameFix(Schema schema) {
		super(schema, false, "EntityPaintingFieldsRenameFix", References.ENTITY, "minecraft:painting");
	}

	public Dynamic<?> fixTag(Dynamic<?> dynamic) {
		return this.renameField(this.renameField(dynamic, "Motive", "variant"), "Facing", "facing");
	}

	private Dynamic<?> renameField(Dynamic<?> dynamic, String string, String string2) {
		Optional<? extends Dynamic<?>> optional = dynamic.get(string).result();
		Optional<? extends Dynamic<?>> optional2 = optional.map(dynamic2 -> dynamic.remove(string).set(string2, dynamic2));
		return DataFixUtils.orElse(optional2, dynamic);
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(DSL.remainderFinder(), this::fixTag);
	}
}
