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

public class V1906
extends NamespacedSchema {
    public V1906(int i, Schema schema) {
        super(i, schema);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);
        V1906.registerInventory(schema, map, "minecraft:barrel");
        V1906.registerInventory(schema, map, "minecraft:smoker");
        V1906.registerInventory(schema, map, "minecraft:blast_furnace");
        schema.register(map, "minecraft:lectern", (String string) -> DSL.optionalFields("Book", References.ITEM_STACK.in(schema)));
        schema.registerSimple(map, "minecraft:bell");
        return map;
    }

    protected static void registerInventory(Schema schema, Map<String, Supplier<TypeTemplate>> map, String string) {
        schema.register(map, string, () -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(schema))));
    }
}

