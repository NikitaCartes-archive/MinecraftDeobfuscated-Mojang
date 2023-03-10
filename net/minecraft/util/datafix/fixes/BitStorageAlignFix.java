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
import com.mojang.datafixers.types.templates.List;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.stream.LongStream;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.fixes.References;

public class BitStorageAlignFix
extends DataFix {
    private static final int BIT_TO_LONG_SHIFT = 6;
    private static final int SECTION_WIDTH = 16;
    private static final int SECTION_HEIGHT = 16;
    private static final int SECTION_SIZE = 4096;
    private static final int HEIGHTMAP_BITS = 9;
    private static final int HEIGHTMAP_SIZE = 256;

    public BitStorageAlignFix(Schema schema) {
        super(schema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.CHUNK);
        Type<?> type2 = type.findFieldType("Level");
        OpticFinder<?> opticFinder = DSL.fieldFinder("Level", type2);
        OpticFinder<?> opticFinder2 = opticFinder.type().findField("Sections");
        Type type3 = ((List.ListType)opticFinder2.type()).getElement();
        OpticFinder opticFinder3 = DSL.typeFinder(type3);
        Type<Pair<String, Dynamic<?>>> type4 = DSL.named(References.BLOCK_STATE.typeName(), DSL.remainderType());
        OpticFinder<Pair<String, Dynamic<?>>> opticFinder4 = DSL.fieldFinder("Palette", DSL.list(type4));
        return this.fixTypeEverywhereTyped("BitStorageAlignFix", type, this.getOutputSchema().getType(References.CHUNK), (Typed<?> typed2) -> typed2.updateTyped(opticFinder, typed -> this.updateHeightmaps(BitStorageAlignFix.updateSections(opticFinder2, opticFinder3, opticFinder4, typed))));
    }

    private Typed<?> updateHeightmaps(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), dynamic -> dynamic.update("Heightmaps", dynamic2 -> dynamic2.updateMapValues(pair -> pair.mapSecond(dynamic2 -> BitStorageAlignFix.updateBitStorage(dynamic, dynamic2, 256, 9)))));
    }

    private static Typed<?> updateSections(OpticFinder<?> opticFinder, OpticFinder<?> opticFinder2, OpticFinder<List<Pair<String, Dynamic<?>>>> opticFinder3, Typed<?> typed) {
        return typed.updateTyped(opticFinder, typed2 -> typed2.updateTyped(opticFinder2, typed -> {
            int i = typed.getOptional(opticFinder3).map(list -> Math.max(4, DataFixUtils.ceillog2(list.size()))).orElse(0);
            if (i == 0 || Mth.isPowerOfTwo(i)) {
                return typed;
            }
            return typed.update(DSL.remainderFinder(), dynamic -> dynamic.update("BlockStates", dynamic2 -> BitStorageAlignFix.updateBitStorage(dynamic, dynamic2, 4096, i)));
        }));
    }

    private static Dynamic<?> updateBitStorage(Dynamic<?> dynamic, Dynamic<?> dynamic2, int i, int j) {
        long[] ls = dynamic2.asLongStream().toArray();
        long[] ms = BitStorageAlignFix.addPadding(i, j, ls);
        return dynamic.createLongList(LongStream.of(ms));
    }

    public static long[] addPadding(int i, int j, long[] ls) {
        int k = ls.length;
        if (k == 0) {
            return ls;
        }
        long l = (1L << j) - 1L;
        int m = 64 / j;
        int n = (i + m - 1) / m;
        long[] ms = new long[n];
        int o = 0;
        int p = 0;
        long q = 0L;
        int r = 0;
        long s = ls[0];
        long t = k > 1 ? ls[1] : 0L;
        for (int u = 0; u < i; ++u) {
            int aa;
            long z;
            int v = u * j;
            int w = v >> 6;
            int x = (u + 1) * j - 1 >> 6;
            int y = v ^ w << 6;
            if (w != r) {
                s = t;
                t = w + 1 < k ? ls[w + 1] : 0L;
                r = w;
            }
            if (w == x) {
                z = s >>> y & l;
            } else {
                aa = 64 - y;
                z = (s >>> y | t << aa) & l;
            }
            aa = p + j;
            if (aa >= 64) {
                ms[o++] = q;
                q = z;
                p = j;
                continue;
            }
            q |= z << p;
            p = aa;
        }
        if (q != 0L) {
            ms[o] = q;
        }
        return ms;
    }
}

