/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import net.minecraft.util.datafix.schemas.V100;

public class V1470
extends NamespacedSchema {
    public V1470(int i, Schema schema) {
        super(i, schema);
    }

    protected static void registerMob(Schema schema, Map<String, Supplier<TypeTemplate>> map, String string) {
        schema.register(map, string, () -> V100.equipment(schema));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);
        V1470.registerMob(schema, map, "minecraft:turtle");
        V1470.registerMob(schema, map, "minecraft:cod_mob");
        V1470.registerMob(schema, map, "minecraft:tropical_fish");
        V1470.registerMob(schema, map, "minecraft:salmon_mob");
        V1470.registerMob(schema, map, "minecraft:puffer_fish");
        V1470.registerMob(schema, map, "minecraft:phantom");
        V1470.registerMob(schema, map, "minecraft:dolphin");
        V1470.registerMob(schema, map, "minecraft:drowned");
        schema.register(map, "minecraft:trident", (String string) -> DSL.optionalFields("inBlockState", References.BLOCK_STATE.in(schema)));
        return map;
    }
}

