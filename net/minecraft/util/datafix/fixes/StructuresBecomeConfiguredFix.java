/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.util.datafix.fixes.References;

public class StructuresBecomeConfiguredFix
extends DataFix {
    private static final Map<String, Conversion> CONVERSION_MAP = ImmutableMap.builder().put("mineshaft", Conversion.biomeMapped(Map.of(List.of("minecraft:badlands", "minecraft:eroded_badlands", "minecraft:wooded_badlands"), "minecraft:mineshaft_mesa"), "minecraft:mineshaft")).put("shipwreck", Conversion.biomeMapped(Map.of(List.of("minecraft:beach", "minecraft:snowy_beach"), "minecraft:shipwreck_beached"), "minecraft:shipwreck")).put("ocean_ruin", Conversion.biomeMapped(Map.of(List.of("minecraft:warm_ocean", "minecraft:lukewarm_ocean", "minecraft:deep_lukewarm_ocean"), "minecraft:ocean_ruin_warm"), "minecraft:ocean_ruin_cold")).put("village", Conversion.biomeMapped(Map.of(List.of("minecraft:desert"), "minecraft:village_desert", List.of("minecraft:savanna"), "minecraft:village_savanna", List.of("minecraft:snowy_plains"), "minecraft:village_snowy", List.of("minecraft:taiga"), "minecraft:village_taiga"), "minecraft:village_plains")).put("ruined_portal", Conversion.biomeMapped(Map.of(List.of("minecraft:desert"), "minecraft:ruined_portal_desert", List.of("minecraft:badlands", "minecraft:eroded_badlands", "minecraft:wooded_badlands", "minecraft:windswept_hills", "minecraft:windswept_forest", "minecraft:windswept_gravelly_hills", "minecraft:savanna_plateau", "minecraft:windswept_savanna", "minecraft:stony_shore", "minecraft:meadow", "minecraft:frozen_peaks", "minecraft:jagged_peaks", "minecraft:stony_peaks", "minecraft:snowy_slopes"), "minecraft:ruined_portal_mountain", List.of("minecraft:bamboo_jungle", "minecraft:jungle", "minecraft:sparse_jungle"), "minecraft:ruined_portal_jungle", List.of("minecraft:deep_frozen_ocean", "minecraft:deep_cold_ocean", "minecraft:deep_ocean", "minecraft:deep_lukewarm_ocean", "minecraft:frozen_ocean", "minecraft:ocean", "minecraft:cold_ocean", "minecraft:lukewarm_ocean", "minecraft:warm_ocean"), "minecraft:ruined_portal_ocean"), "minecraft:ruined_portal")).put("pillager_outpost", Conversion.trivial("minecraft:pillager_outpost")).put("mansion", Conversion.trivial("minecraft:mansion")).put("jungle_pyramid", Conversion.trivial("minecraft:jungle_pyramid")).put("desert_pyramid", Conversion.trivial("minecraft:desert_pyramid")).put("igloo", Conversion.trivial("minecraft:igloo")).put("swamp_hut", Conversion.trivial("minecraft:swamp_hut")).put("stronghold", Conversion.trivial("minecraft:stronghold")).put("monument", Conversion.trivial("minecraft:monument")).put("fortress", Conversion.trivial("minecraft:fortress")).put("endcity", Conversion.trivial("minecraft:end_city")).put("buried_treasure", Conversion.trivial("minecraft:buried_treasure")).put("nether_fossil", Conversion.trivial("minecraft:nether_fossil")).put("bastion_remnant", Conversion.trivial("minecraft:bastion_remnant")).build();

    public StructuresBecomeConfiguredFix(Schema schema) {
        super(schema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.CHUNK);
        Type<?> type2 = this.getInputSchema().getType(References.CHUNK);
        return this.writeFixAndRead("StucturesToConfiguredStructures", type, type2, this::fix);
    }

    private Dynamic<?> fix(Dynamic<?> dynamic) {
        return dynamic.update("structures", dynamic22 -> dynamic22.update("starts", dynamic2 -> this.updateStarts((Dynamic<?>)dynamic2, dynamic)).update("References", dynamic2 -> this.updateReferences((Dynamic<?>)dynamic2, dynamic)));
    }

    private Dynamic<?> updateStarts(Dynamic<?> dynamic3, Dynamic<?> dynamic22) {
        Map<Dynamic<?>, Dynamic<?>> map = dynamic3.getMapValues().result().get();
        ArrayList list = new ArrayList();
        map.forEach((dynamic, dynamic2) -> {
            if (dynamic2.get("id").asString("INVALID").equals("INVALID")) {
                list.add(dynamic);
            }
        });
        for (Dynamic dynamic32 : list) {
            dynamic3 = dynamic3.remove(dynamic32.asString(""));
        }
        return dynamic3.updateMapValues(pair -> this.updateStart((Pair<Dynamic<?>, Dynamic<?>>)pair, dynamic22));
    }

    private Pair<Dynamic<?>, Dynamic<?>> updateStart(Pair<Dynamic<?>, Dynamic<?>> pair, Dynamic<?> dynamic) {
        Dynamic<?> dynamic2 = this.findUpdatedStructureType(pair, dynamic);
        return new Pair(dynamic2, pair.getSecond().set("id", dynamic2));
    }

    private Dynamic<?> updateReferences(Dynamic<?> dynamic3, Dynamic<?> dynamic22) {
        Map<Dynamic<?>, Dynamic<?>> map = dynamic3.getMapValues().result().get();
        ArrayList list = new ArrayList();
        map.forEach((dynamic, dynamic2) -> {
            if (dynamic2.asLongStream().count() == 0L) {
                list.add(dynamic);
            }
        });
        for (Dynamic dynamic32 : list) {
            dynamic3 = dynamic3.remove(dynamic32.asString(""));
        }
        return dynamic3.updateMapValues(pair -> this.updateReference((Pair<Dynamic<?>, Dynamic<?>>)pair, dynamic22));
    }

    private Pair<Dynamic<?>, Dynamic<?>> updateReference(Pair<Dynamic<?>, Dynamic<?>> pair, Dynamic<?> dynamic) {
        return pair.mapFirst(dynamic2 -> this.findUpdatedStructureType(pair, dynamic));
    }

    private Dynamic<?> findUpdatedStructureType(Pair<Dynamic<?>, Dynamic<?>> pair, Dynamic<?> dynamic) {
        Optional<String> optional;
        String string = pair.getFirst().asString("UNKNOWN").toLowerCase(Locale.ROOT);
        Conversion conversion = CONVERSION_MAP.get(string);
        if (conversion == null) {
            throw new IllegalStateException("Found unknown structure: " + string);
        }
        Dynamic<?> dynamic2 = pair.getSecond();
        String string2 = conversion.fallback;
        if (!conversion.biomeMapping().isEmpty() && (optional = this.guessConfiguration(dynamic, conversion)).isPresent()) {
            string2 = optional.get();
        }
        Dynamic dynamic3 = dynamic2.createString(string2);
        return dynamic3;
    }

    private Optional<String> guessConfiguration(Dynamic<?> dynamic, Conversion conversion) {
        Object2IntArrayMap object2IntArrayMap = new Object2IntArrayMap();
        dynamic.get("sections").asList(Function.identity()).forEach(dynamic2 -> dynamic2.get("biomes").get("palette").asList(Function.identity()).forEach(dynamic -> {
            String string = conversion.biomeMapping().get(dynamic.asString(""));
            if (string != null) {
                object2IntArrayMap.mergeInt(string, 1, Integer::sum);
            }
        }));
        return object2IntArrayMap.object2IntEntrySet().stream().max(Comparator.comparingInt(Object2IntMap.Entry::getIntValue)).map(Map.Entry::getKey);
    }

    record Conversion(Map<String, String> biomeMapping, String fallback) {
        public static Conversion trivial(String string) {
            return new Conversion(Map.of(), string);
        }

        public static Conversion biomeMapped(Map<List<String>, String> map, String string) {
            return new Conversion(Conversion.unpack(map), string);
        }

        private static Map<String, String> unpack(Map<List<String>, String> map) {
            ImmutableMap.Builder builder = ImmutableMap.builder();
            for (Map.Entry<List<String>, String> entry : map.entrySet()) {
                entry.getKey().forEach(string -> builder.put(string, (String)entry.getValue()));
            }
            return builder.build();
        }
    }
}

