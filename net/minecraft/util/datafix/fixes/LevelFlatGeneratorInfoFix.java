/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.util.datafix.fixes.EntityBlockStateFix;
import net.minecraft.util.datafix.fixes.References;
import org.apache.commons.lang3.math.NumberUtils;

public class LevelFlatGeneratorInfoFix
extends DataFix {
    private static final String GENERATOR_OPTIONS = "generatorOptions";
    @VisibleForTesting
    static final String DEFAULT = "minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block;1;village";
    private static final Splitter SPLITTER = Splitter.on(';').limit(5);
    private static final Splitter LAYER_SPLITTER = Splitter.on(',');
    private static final Splitter OLD_AMOUNT_SPLITTER = Splitter.on('x').limit(2);
    private static final Splitter AMOUNT_SPLITTER = Splitter.on('*').limit(2);
    private static final Splitter BLOCK_SPLITTER = Splitter.on(':').limit(3);

    public LevelFlatGeneratorInfoFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("LevelFlatGeneratorInfoFix", this.getInputSchema().getType(References.LEVEL), typed -> typed.update(DSL.remainderFinder(), this::fix));
    }

    private Dynamic<?> fix(Dynamic<?> dynamic2) {
        if (dynamic2.get("generatorName").asString("").equalsIgnoreCase("flat")) {
            return dynamic2.update(GENERATOR_OPTIONS, dynamic -> DataFixUtils.orElse(dynamic.asString().map(this::fixString).map(dynamic::createString).result(), dynamic));
        }
        return dynamic2;
    }

    @VisibleForTesting
    String fixString(String string2) {
        String string3;
        int i;
        if (string2.isEmpty()) {
            return DEFAULT;
        }
        Iterator<String> iterator = SPLITTER.split(string2).iterator();
        String string22 = iterator.next();
        if (iterator.hasNext()) {
            i = NumberUtils.toInt(string22, 0);
            string3 = iterator.next();
        } else {
            i = 0;
            string3 = string22;
        }
        if (i < 0 || i > 3) {
            return DEFAULT;
        }
        StringBuilder stringBuilder = new StringBuilder();
        Splitter splitter = i < 3 ? OLD_AMOUNT_SPLITTER : AMOUNT_SPLITTER;
        stringBuilder.append(StreamSupport.stream(LAYER_SPLITTER.split(string3).spliterator(), false).map(string -> {
            String string2;
            int j;
            List<String> list = splitter.splitToList((CharSequence)string);
            if (list.size() == 2) {
                j = NumberUtils.toInt(list.get(0));
                string2 = list.get(1);
            } else {
                j = 1;
                string2 = list.get(0);
            }
            List<String> list2 = BLOCK_SPLITTER.splitToList(string2);
            int k = list2.get(0).equals("minecraft") ? 1 : 0;
            String string3 = list2.get(k);
            int l = i == 3 ? EntityBlockStateFix.getBlockId("minecraft:" + string3) : NumberUtils.toInt(string3, 0);
            int m = k + 1;
            int n = list2.size() > m ? NumberUtils.toInt(list2.get(m), 0) : 0;
            return (j == 1 ? "" : j + "*") + BlockStateData.getTag(l << 4 | n).get("Name").asString("");
        }).collect(Collectors.joining(",")));
        while (iterator.hasNext()) {
            stringBuilder.append(';').append(iterator.next());
        }
        return stringBuilder.toString();
    }
}

