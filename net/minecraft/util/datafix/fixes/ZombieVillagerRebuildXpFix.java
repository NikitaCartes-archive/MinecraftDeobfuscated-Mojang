/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.fixes.VillagerRebuildLevelAndXpFix;

public class ZombieVillagerRebuildXpFix
extends NamedEntityFix {
    public ZombieVillagerRebuildXpFix(Schema schema, boolean bl) {
        super(schema, bl, "Zombie Villager XP rebuild", References.ENTITY, "minecraft:zombie_villager");
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), dynamic -> {
            Optional<Number> optional = dynamic.get("Xp").asNumber().result();
            if (!optional.isPresent()) {
                int i = dynamic.get("VillagerData").get("level").asInt(1);
                return dynamic.set("Xp", dynamic.createInt(VillagerRebuildLevelAndXpFix.getMinXpPerLevel(i)));
            }
            return dynamic;
        });
    }
}

