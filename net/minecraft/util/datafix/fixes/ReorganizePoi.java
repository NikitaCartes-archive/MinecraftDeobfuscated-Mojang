/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;

public class ReorganizePoi
extends DataFix {
    public ReorganizePoi(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<Pair<String, Dynamic<?>>> type = DSL.named(References.POI_CHUNK.typeName(), DSL.remainderType());
        if (!Objects.equals(type, this.getInputSchema().getType(References.POI_CHUNK))) {
            throw new IllegalStateException("Poi type is not what was expected.");
        }
        return this.fixTypeEverywhere("POI reorganization", type, dynamicOps -> pair -> pair.mapSecond(ReorganizePoi::cap));
    }

    private static <T> Dynamic<T> cap(Dynamic<T> dynamic) {
        HashMap map = Maps.newHashMap();
        for (int i = 0; i < 16; ++i) {
            String string = String.valueOf(i);
            Optional<Dynamic<T>> optional = dynamic.get(string).result();
            if (!optional.isPresent()) continue;
            Dynamic<T> dynamic2 = optional.get();
            Dynamic dynamic3 = dynamic.createMap(ImmutableMap.of(dynamic.createString("Records"), dynamic2));
            map.put(dynamic.createInt(i), dynamic3);
            dynamic = dynamic.remove(string);
        }
        return dynamic.set("Sections", dynamic.createMap(map));
    }
}

