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
import net.minecraft.util.datafix.schemas.V100;

public class V703
extends Schema {
    public V703(int i, Schema schema) {
        super(i, schema);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);
        map.remove("EntityHorse");
        schema.register(map, "Horse", () -> DSL.optionalFields("ArmorItem", References.ITEM_STACK.in(schema), "SaddleItem", References.ITEM_STACK.in(schema), V100.equipment(schema)));
        schema.register(map, "Donkey", () -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(schema)), "SaddleItem", References.ITEM_STACK.in(schema), V100.equipment(schema)));
        schema.register(map, "Mule", () -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(schema)), "SaddleItem", References.ITEM_STACK.in(schema), V100.equipment(schema)));
        schema.register(map, "ZombieHorse", () -> DSL.optionalFields("SaddleItem", References.ITEM_STACK.in(schema), V100.equipment(schema)));
        schema.register(map, "SkeletonHorse", () -> DSL.optionalFields("SaddleItem", References.ITEM_STACK.in(schema), V100.equipment(schema)));
        return map;
    }
}

