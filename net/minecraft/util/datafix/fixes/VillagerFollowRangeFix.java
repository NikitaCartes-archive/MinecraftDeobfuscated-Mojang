/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class VillagerFollowRangeFix
extends NamedEntityFix {
    private static final double ORIGINAL_VALUE = 16.0;
    private static final double NEW_BASE_VALUE = 48.0;

    public VillagerFollowRangeFix(Schema schema) {
        super(schema, false, "Villager Follow Range Fix", References.ENTITY, "minecraft:villager");
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), VillagerFollowRangeFix::fixValue);
    }

    private static Dynamic<?> fixValue(Dynamic<?> dynamic) {
        return dynamic.update("Attributes", dynamic22 -> dynamic.createList(dynamic22.asStream().map(dynamic -> {
            if (!dynamic.get("Name").asString("").equals("generic.follow_range") || dynamic.get("Base").asDouble(0.0) != 16.0) {
                return dynamic;
            }
            return dynamic.set("Base", dynamic.createDouble(48.0));
        })));
    }
}

