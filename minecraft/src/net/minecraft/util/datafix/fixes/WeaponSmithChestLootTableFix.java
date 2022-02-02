package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class WeaponSmithChestLootTableFix extends NamedEntityFix {
	public WeaponSmithChestLootTableFix(Schema schema, boolean bl) {
		super(schema, bl, "WeaponSmithChestLootTableFix", References.BLOCK_ENTITY, "minecraft:chest");
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(
			DSL.remainderFinder(),
			dynamic -> {
				String string = dynamic.get("LootTable").asString("");
				return string.equals("minecraft:chests/village_blacksmith")
					? dynamic.set("LootTable", dynamic.createString("minecraft:chests/village/village_weaponsmith"))
					: dynamic;
			}
		);
	}
}
