package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class EntityBrushableBlockFieldsRenameFix extends NamedEntityFix {
	public EntityBrushableBlockFieldsRenameFix(Schema schema) {
		super(schema, false, "EntityBrushableBlockFieldsRenameFix", References.BLOCK_ENTITY, "minecraft:brushable_block");
	}

	public Dynamic<?> fixTag(Dynamic<?> dynamic) {
		return dynamic.renameField("loot_table", "LootTable").renameField("loot_table_seed", "LootTableSeed");
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(DSL.remainderFinder(), this::fixTag);
	}
}
