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

public class V2100
extends NamespacedSchema {
    public V2100(int i, Schema schema) {
        super(i, schema);
    }

    protected static void registerMob(Schema schema, Map<String, Supplier<TypeTemplate>> map, String string) {
        schema.register(map, string, () -> V100.equipment(schema));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);
        V2100.registerMob(schema, map, "minecraft:bee");
        V2100.registerMob(schema, map, "minecraft:bee_stinger");
        return map;
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);
        schema.register(map, "minecraft:beehive", () -> DSL.optionalFields("Bees", DSL.list(DSL.optionalFields("EntityData", References.ENTITY_TREE.in(schema)))));
        return map;
    }
}

