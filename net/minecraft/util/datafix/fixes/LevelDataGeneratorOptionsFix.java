/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.JsonOps;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.datafix.fixes.References;
import org.jetbrains.annotations.Nullable;

public class LevelDataGeneratorOptionsFix
extends DataFix {
    static final Map<String, String> MAP = Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put("0", "minecraft:ocean");
        hashMap.put("1", "minecraft:plains");
        hashMap.put("2", "minecraft:desert");
        hashMap.put("3", "minecraft:mountains");
        hashMap.put("4", "minecraft:forest");
        hashMap.put("5", "minecraft:taiga");
        hashMap.put("6", "minecraft:swamp");
        hashMap.put("7", "minecraft:river");
        hashMap.put("8", "minecraft:nether");
        hashMap.put("9", "minecraft:the_end");
        hashMap.put("10", "minecraft:frozen_ocean");
        hashMap.put("11", "minecraft:frozen_river");
        hashMap.put("12", "minecraft:snowy_tundra");
        hashMap.put("13", "minecraft:snowy_mountains");
        hashMap.put("14", "minecraft:mushroom_fields");
        hashMap.put("15", "minecraft:mushroom_field_shore");
        hashMap.put("16", "minecraft:beach");
        hashMap.put("17", "minecraft:desert_hills");
        hashMap.put("18", "minecraft:wooded_hills");
        hashMap.put("19", "minecraft:taiga_hills");
        hashMap.put("20", "minecraft:mountain_edge");
        hashMap.put("21", "minecraft:jungle");
        hashMap.put("22", "minecraft:jungle_hills");
        hashMap.put("23", "minecraft:jungle_edge");
        hashMap.put("24", "minecraft:deep_ocean");
        hashMap.put("25", "minecraft:stone_shore");
        hashMap.put("26", "minecraft:snowy_beach");
        hashMap.put("27", "minecraft:birch_forest");
        hashMap.put("28", "minecraft:birch_forest_hills");
        hashMap.put("29", "minecraft:dark_forest");
        hashMap.put("30", "minecraft:snowy_taiga");
        hashMap.put("31", "minecraft:snowy_taiga_hills");
        hashMap.put("32", "minecraft:giant_tree_taiga");
        hashMap.put("33", "minecraft:giant_tree_taiga_hills");
        hashMap.put("34", "minecraft:wooded_mountains");
        hashMap.put("35", "minecraft:savanna");
        hashMap.put("36", "minecraft:savanna_plateau");
        hashMap.put("37", "minecraft:badlands");
        hashMap.put("38", "minecraft:wooded_badlands_plateau");
        hashMap.put("39", "minecraft:badlands_plateau");
        hashMap.put("40", "minecraft:small_end_islands");
        hashMap.put("41", "minecraft:end_midlands");
        hashMap.put("42", "minecraft:end_highlands");
        hashMap.put("43", "minecraft:end_barrens");
        hashMap.put("44", "minecraft:warm_ocean");
        hashMap.put("45", "minecraft:lukewarm_ocean");
        hashMap.put("46", "minecraft:cold_ocean");
        hashMap.put("47", "minecraft:deep_warm_ocean");
        hashMap.put("48", "minecraft:deep_lukewarm_ocean");
        hashMap.put("49", "minecraft:deep_cold_ocean");
        hashMap.put("50", "minecraft:deep_frozen_ocean");
        hashMap.put("127", "minecraft:the_void");
        hashMap.put("129", "minecraft:sunflower_plains");
        hashMap.put("130", "minecraft:desert_lakes");
        hashMap.put("131", "minecraft:gravelly_mountains");
        hashMap.put("132", "minecraft:flower_forest");
        hashMap.put("133", "minecraft:taiga_mountains");
        hashMap.put("134", "minecraft:swamp_hills");
        hashMap.put("140", "minecraft:ice_spikes");
        hashMap.put("149", "minecraft:modified_jungle");
        hashMap.put("151", "minecraft:modified_jungle_edge");
        hashMap.put("155", "minecraft:tall_birch_forest");
        hashMap.put("156", "minecraft:tall_birch_hills");
        hashMap.put("157", "minecraft:dark_forest_hills");
        hashMap.put("158", "minecraft:snowy_taiga_mountains");
        hashMap.put("160", "minecraft:giant_spruce_taiga");
        hashMap.put("161", "minecraft:giant_spruce_taiga_hills");
        hashMap.put("162", "minecraft:modified_gravelly_mountains");
        hashMap.put("163", "minecraft:shattered_savanna");
        hashMap.put("164", "minecraft:shattered_savanna_plateau");
        hashMap.put("165", "minecraft:eroded_badlands");
        hashMap.put("166", "minecraft:modified_wooded_badlands_plateau");
        hashMap.put("167", "minecraft:modified_badlands_plateau");
    });

    public LevelDataGeneratorOptionsFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getOutputSchema().getType(References.LEVEL);
        return this.fixTypeEverywhereTyped("LevelDataGeneratorOptionsFix", this.getInputSchema().getType(References.LEVEL), type, (Typed<?> typed) -> {
            Dynamic dynamic2;
            Dynamic dynamic = typed.write();
            Optional<String> optional = dynamic.get("generatorOptions").asString();
            if ("flat".equalsIgnoreCase(dynamic.get("generatorName").asString(""))) {
                String string = optional.orElse("");
                dynamic2 = dynamic.set("generatorOptions", LevelDataGeneratorOptionsFix.convert(string, dynamic.getOps()));
            } else if ("buffet".equalsIgnoreCase(dynamic.get("generatorName").asString("")) && optional.isPresent()) {
                Dynamic<JsonObject> dynamic3 = new Dynamic<JsonObject>(JsonOps.INSTANCE, GsonHelper.parse(optional.get(), true));
                dynamic2 = dynamic.set("generatorOptions", dynamic3.convert(dynamic.getOps()));
            } else {
                dynamic2 = dynamic;
            }
            return type.readTyped(dynamic2).getSecond().orElseThrow(() -> new IllegalStateException("Could not read new level type."));
        });
    }

    private static <T> Dynamic<T> convert(String string, DynamicOps<T> dynamicOps) {
        List<Object> list;
        Iterator<String> iterator = Splitter.on(';').split(string).iterator();
        String string2 = "minecraft:plains";
        HashMap map = Maps.newHashMap();
        if (!string.isEmpty() && iterator.hasNext()) {
            list = LevelDataGeneratorOptionsFix.getLayersInfoFromString(iterator.next());
            if (!list.isEmpty()) {
                if (iterator.hasNext()) {
                    string2 = MAP.getOrDefault(iterator.next(), "minecraft:plains");
                }
                if (iterator.hasNext()) {
                    String[] strings;
                    for (String string3 : strings = iterator.next().toLowerCase(Locale.ROOT).split(",")) {
                        String[] strings3;
                        String[] strings2 = string3.split("\\(", 2);
                        if (strings2[0].isEmpty()) continue;
                        map.put(strings2[0], Maps.newHashMap());
                        if (strings2.length <= 1 || !strings2[1].endsWith(")") || strings2[1].length() <= 1) continue;
                        for (String string4 : strings3 = strings2[1].substring(0, strings2[1].length() - 1).split(" ")) {
                            String[] strings4 = string4.split("=", 2);
                            if (strings4.length != 2) continue;
                            ((Map)map.get(strings2[0])).put(strings4[0], strings4[1]);
                        }
                    }
                } else {
                    map.put("village", Maps.newHashMap());
                }
            }
        } else {
            list = Lists.newArrayList();
            list.add(Pair.of(1, "minecraft:bedrock"));
            list.add(Pair.of(2, "minecraft:dirt"));
            list.add(Pair.of(1, "minecraft:grass_block"));
            map.put("village", Maps.newHashMap());
        }
        Object object = dynamicOps.createList(list.stream().map(pair -> dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("height"), dynamicOps.createInt((Integer)pair.getFirst()), dynamicOps.createString("block"), dynamicOps.createString((String)pair.getSecond())))));
        Object object2 = dynamicOps.createMap(map.entrySet().stream().map(entry2 -> Pair.of(dynamicOps.createString(((String)entry2.getKey()).toLowerCase(Locale.ROOT)), dynamicOps.createMap(((Map)entry2.getValue()).entrySet().stream().map(entry -> Pair.of(dynamicOps.createString((String)entry.getKey()), dynamicOps.createString((String)entry.getValue()))).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))))).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
        return new Dynamic<Object>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("layers"), object, dynamicOps.createString("biome"), dynamicOps.createString(string2), dynamicOps.createString("structures"), object2)));
    }

    @Nullable
    private static Pair<Integer, String> getLayerInfoFromString(String string) {
        int i;
        String[] strings = string.split("\\*", 2);
        if (strings.length == 2) {
            try {
                i = Integer.parseInt(strings[0]);
            } catch (NumberFormatException numberFormatException) {
                return null;
            }
        } else {
            i = 1;
        }
        String string2 = strings[strings.length - 1];
        return Pair.of(i, string2);
    }

    private static List<Pair<Integer, String>> getLayersInfoFromString(String string) {
        String[] strings;
        ArrayList<Pair<Integer, String>> list = Lists.newArrayList();
        for (String string2 : strings = string.split(",")) {
            Pair<Integer, String> pair = LevelDataGeneratorOptionsFix.getLayerInfoFromString(string2);
            if (pair == null) {
                return Collections.emptyList();
            }
            list.add(pair);
        }
        return list;
    }
}

