/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class WeaponSmithChestLootTableFix
extends NamedEntityFix {
    public WeaponSmithChestLootTableFix(Schema schema, boolean bl) {
        super(schema, bl, "WeaponSmithChestLootTableFix", References.BLOCK_ENTITY, "minecraft:chest");
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), dynamic -> {
            String string = dynamic.get("LootTable").asString("");
            return string.equals("minecraft:chests/village_blacksmith") ? dynamic.set("LootTable", dynamic.createString("minecraft:chests/village/village_weaponsmith")) : dynamic;
        });
    }
}

