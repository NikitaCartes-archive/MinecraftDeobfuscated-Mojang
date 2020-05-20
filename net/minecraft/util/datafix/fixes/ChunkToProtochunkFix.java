/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.util.datafix.fixes.References;

public class ChunkToProtochunkFix
extends DataFix {
    public ChunkToProtochunkFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.CHUNK);
        Type<?> type2 = this.getOutputSchema().getType(References.CHUNK);
        Type<?> type3 = type.findFieldType("Level");
        Type<?> type4 = type2.findFieldType("Level");
        Type<?> type5 = type3.findFieldType("TileTicks");
        OpticFinder<?> opticFinder = DSL.fieldFinder("Level", type3);
        OpticFinder<?> opticFinder2 = DSL.fieldFinder("TileTicks", type5);
        return TypeRewriteRule.seq(this.fixTypeEverywhereTyped("ChunkToProtoChunkFix", type, this.getOutputSchema().getType(References.CHUNK), (Typed<?> typed) -> typed.updateTyped(opticFinder, type4, typed2 -> {
            Dynamic<Object> dynamic3;
            Optional optional = typed2.getOptionalTyped(opticFinder2).flatMap(typed -> typed.write().result()).flatMap(dynamic -> dynamic.asStreamOpt().result());
            Dynamic<Object> dynamic2 = typed2.get(DSL.remainderFinder());
            boolean bl = dynamic2.get("TerrainPopulated").asBoolean(false) && (!dynamic2.get("LightPopulated").asNumber().result().isPresent() || dynamic2.get("LightPopulated").asBoolean(false));
            dynamic2 = dynamic2.set("Status", dynamic2.createString(bl ? "mobs_spawned" : "empty"));
            dynamic2 = dynamic2.set("hasLegacyStructureData", dynamic2.createBoolean(true));
            if (bl) {
                Optional<ByteBuffer> optional2 = dynamic2.get("Biomes").asByteBufferOpt().result();
                if (optional2.isPresent()) {
                    ByteBuffer byteBuffer = optional2.get();
                    int[] is = new int[256];
                    for (int i2 = 0; i2 < is.length; ++i2) {
                        if (i2 >= byteBuffer.capacity()) continue;
                        is[i2] = byteBuffer.get(i2) & 0xFF;
                    }
                    dynamic2 = dynamic2.set("Biomes", dynamic2.createIntList(Arrays.stream(is)));
                }
                Dynamic<Object> dynamic22 = dynamic2;
                List list = IntStream.range(0, 16).mapToObj(i -> new ShortArrayList()).collect(Collectors.toList());
                if (optional.isPresent()) {
                    ((Stream)optional.get()).forEach(dynamic -> {
                        int i = dynamic.get("x").asInt(0);
                        int j = dynamic.get("y").asInt(0);
                        int k = dynamic.get("z").asInt(0);
                        short s = ChunkToProtochunkFix.packOffsetCoordinates(i, j, k);
                        ((ShortList)list.get(j >> 4)).add(s);
                    });
                    dynamic2 = dynamic2.set("ToBeTicked", dynamic2.createList(list.stream().map(shortList -> dynamic22.createList(shortList.stream().map(dynamic22::createShort)))));
                }
                dynamic3 = DataFixUtils.orElse(typed2.set(DSL.remainderFinder(), dynamic2).write().result(), dynamic2);
            } else {
                dynamic3 = dynamic2;
            }
            return type4.readTyped(dynamic3).result().orElseThrow(() -> new IllegalStateException("Could not read the new chunk")).getFirst();
        })), this.writeAndRead("Structure biome inject", this.getInputSchema().getType(References.STRUCTURE_FEATURE), this.getOutputSchema().getType(References.STRUCTURE_FEATURE)));
    }

    private static short packOffsetCoordinates(int i, int j, int k) {
        return (short)(i & 0xF | (j & 0xF) << 4 | (k & 0xF) << 8);
    }
}

