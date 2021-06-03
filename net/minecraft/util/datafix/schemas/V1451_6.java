/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.schemas;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.Hook;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class V1451_6
extends NamespacedSchema {
    public static final String SPECIAL_OBJECTIVE_MARKER = "_special";
    protected static final Hook.HookFunction UNPACK_OBJECTIVE_ID = new Hook.HookFunction(){

        @Override
        public <T> T apply(DynamicOps<T> dynamicOps, T object) {
            Dynamic dynamic = new Dynamic(dynamicOps, object);
            return DataFixUtils.orElse(dynamic.get("CriteriaName").asString().get().left().map(string -> {
                int i = string.indexOf(58);
                if (i < 0) {
                    return Pair.of(V1451_6.SPECIAL_OBJECTIVE_MARKER, string);
                }
                try {
                    ResourceLocation resourceLocation = ResourceLocation.of(string.substring(0, i), '.');
                    ResourceLocation resourceLocation2 = ResourceLocation.of(string.substring(i + 1), '.');
                    return Pair.of(resourceLocation.toString(), resourceLocation2.toString());
                } catch (Exception exception) {
                    return Pair.of(V1451_6.SPECIAL_OBJECTIVE_MARKER, string);
                }
            }).map(pair -> dynamic.set("CriteriaType", dynamic.createMap(ImmutableMap.of(dynamic.createString("type"), dynamic.createString((String)pair.getFirst()), dynamic.createString("id"), dynamic.createString((String)pair.getSecond()))))), dynamic).getValue();
        }
    };
    protected static final Hook.HookFunction REPACK_OBJECTIVE_ID = new Hook.HookFunction(){

        private String packWithDot(String string) {
            ResourceLocation resourceLocation = ResourceLocation.tryParse(string);
            return resourceLocation != null ? resourceLocation.getNamespace() + "." + resourceLocation.getPath() : string;
        }

        @Override
        public <T> T apply(DynamicOps<T> dynamicOps, T object) {
            Dynamic dynamic = new Dynamic(dynamicOps, object);
            Optional<Dynamic> optional = dynamic.get("CriteriaType").get().get().left().flatMap(dynamic2 -> {
                Optional<String> optional = dynamic2.get("type").asString().get().left();
                Optional<String> optional2 = dynamic2.get("id").asString().get().left();
                if (optional.isPresent() && optional2.isPresent()) {
                    String string = optional.get();
                    if (string.equals(V1451_6.SPECIAL_OBJECTIVE_MARKER)) {
                        return Optional.of(dynamic.createString(optional2.get()));
                    }
                    return Optional.of(dynamic2.createString(this.packWithDot(string) + ":" + this.packWithDot(optional2.get())));
                }
                return Optional.empty();
            });
            return DataFixUtils.orElse(optional.map(dynamic2 -> dynamic.set("CriteriaName", (Dynamic<?>)dynamic2).remove("CriteriaType")), dynamic).getValue();
        }
    };

    public V1451_6(int i, Schema schema) {
        super(i, schema);
    }

    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
        super.registerTypes(schema, map, map2);
        Supplier<TypeTemplate> supplier = () -> DSL.compoundList(References.ITEM_NAME.in(schema), DSL.constType(DSL.intType()));
        schema.registerType(false, References.STATS, () -> DSL.optionalFields("stats", DSL.optionalFields("minecraft:mined", DSL.compoundList(References.BLOCK_NAME.in(schema), DSL.constType(DSL.intType())), "minecraft:crafted", (TypeTemplate)supplier.get(), "minecraft:used", (TypeTemplate)supplier.get(), "minecraft:broken", (TypeTemplate)supplier.get(), "minecraft:picked_up", (TypeTemplate)supplier.get(), DSL.optionalFields("minecraft:dropped", (TypeTemplate)supplier.get(), "minecraft:killed", DSL.compoundList(References.ENTITY_NAME.in(schema), DSL.constType(DSL.intType())), "minecraft:killed_by", DSL.compoundList(References.ENTITY_NAME.in(schema), DSL.constType(DSL.intType())), "minecraft:custom", DSL.compoundList(DSL.constType(V1451_6.namespacedString()), DSL.constType(DSL.intType()))))));
        Map<String, Supplier<TypeTemplate>> map3 = V1451_6.createCriterionTypes(schema);
        schema.registerType(false, References.OBJECTIVE, () -> DSL.hook(DSL.optionalFields("CriteriaType", DSL.taggedChoiceLazy("type", DSL.string(), map3)), UNPACK_OBJECTIVE_ID, REPACK_OBJECTIVE_ID));
    }

    protected static Map<String, Supplier<TypeTemplate>> createCriterionTypes(Schema schema) {
        Supplier<TypeTemplate> supplier = () -> DSL.optionalFields("id", References.ITEM_NAME.in(schema));
        Supplier<TypeTemplate> supplier2 = () -> DSL.optionalFields("id", References.BLOCK_NAME.in(schema));
        Supplier<TypeTemplate> supplier3 = () -> DSL.optionalFields("id", References.ENTITY_NAME.in(schema));
        HashMap<String, Supplier<TypeTemplate>> map = Maps.newHashMap();
        map.put("minecraft:mined", supplier2);
        map.put("minecraft:crafted", supplier);
        map.put("minecraft:used", supplier);
        map.put("minecraft:broken", supplier);
        map.put("minecraft:picked_up", supplier);
        map.put("minecraft:dropped", supplier);
        map.put("minecraft:killed", supplier3);
        map.put("minecraft:killed_by", supplier3);
        map.put("minecraft:custom", () -> DSL.optionalFields("id", DSL.constType(V1451_6.namespacedString())));
        map.put(SPECIAL_OBJECTIVE_MARKER, () -> DSL.optionalFields("id", DSL.constType(DSL.string())));
        return map;
    }
}

