/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;

public abstract class PoiTypeRename
extends DataFix {
    public PoiTypeRename(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<Pair<String, Dynamic<?>>> type = DSL.named(References.POI_CHUNK.typeName(), DSL.remainderType());
        if (!Objects.equals(type, this.getInputSchema().getType(References.POI_CHUNK))) {
            throw new IllegalStateException("Poi type is not what was expected.");
        }
        return this.fixTypeEverywhere("POI rename", type, dynamicOps -> pair -> pair.mapSecond(this::cap));
    }

    private <T> Dynamic<T> cap(Dynamic<T> dynamic2) {
        return dynamic2.update("Sections", dynamic -> dynamic.updateMapValues(pair -> pair.mapSecond(dynamic2 -> dynamic2.update("Records", dynamic -> DataFixUtils.orElse(this.renameRecords((Dynamic)dynamic), dynamic)))));
    }

    private <T> Optional<Dynamic<T>> renameRecords(Dynamic<T> dynamic) {
        return dynamic.asStreamOpt().map(stream -> dynamic.createList(stream.map(dynamic2 -> dynamic2.update("type", dynamic -> DataFixUtils.orElse(dynamic.asString().map(this::rename).map(dynamic::createString), dynamic)))));
    }

    protected abstract String rename(String var1);
}

