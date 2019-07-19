/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Map;
import java.util.Objects;
import net.minecraft.util.datafix.fixes.References;

public class ChunkStatusFix2
extends DataFix {
    private static final Map<String, String> RENAMES_AND_DOWNGRADES = ImmutableMap.builder().put("structure_references", "empty").put("biomes", "empty").put("base", "surface").put("carved", "carvers").put("liquid_carved", "liquid_carvers").put("decorated", "features").put("lighted", "light").put("mobs_spawned", "spawn").put("finalized", "heightmaps").put("fullchunk", "full").build();

    public ChunkStatusFix2(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.CHUNK);
        Type<?> type2 = type.findFieldType("Level");
        OpticFinder<?> opticFinder = DSL.fieldFinder("Level", type2);
        return this.fixTypeEverywhereTyped("ChunkStatusFix2", type, this.getOutputSchema().getType(References.CHUNK), (Typed<?> typed2) -> typed2.updateTyped(opticFinder, typed -> {
            String string2;
            Dynamic dynamic = typed.get(DSL.remainderFinder());
            String string = dynamic.get("Status").asString("empty");
            if (Objects.equals(string, string2 = RENAMES_AND_DOWNGRADES.getOrDefault(string, "empty"))) {
                return typed;
            }
            return typed.set(DSL.remainderFinder(), dynamic.set("Status", dynamic.createString(string2)));
        }));
    }
}

