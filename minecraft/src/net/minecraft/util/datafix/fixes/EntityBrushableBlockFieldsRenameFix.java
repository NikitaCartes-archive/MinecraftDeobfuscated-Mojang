package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class EntityBrushableBlockFieldsRenameFix extends NamedEntityFix {
	public EntityBrushableBlockFieldsRenameFix(Schema schema) {
		super(schema, false, "EntityBrushableBlockFieldsRenameFix", References.BLOCK_ENTITY, "minecraft:brushable_block");
	}

	public Dynamic<?> fixTag(Dynamic<?> dynamic) {
		return this.renameField(this.renameField(dynamic, "loot_table", "LootTable"), "loot_table_seed", "LootTableSeed");
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
