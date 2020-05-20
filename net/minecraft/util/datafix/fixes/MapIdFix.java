/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;

public class MapIdFix
extends DataFix {
    public MapIdFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.SAVED_DATA);
        OpticFinder<?> opticFinder = type.findField("data");
        return this.fixTypeEverywhereTyped("Map id fix", type, typed -> {
            Optional optional = typed.getOptionalTyped(opticFinder);
            if (optional.isPresent()) {
                return typed;
            }
            return typed.update(DSL.remainderFinder(), dynamic -> dynamic.createMap(ImmutableMap.of(dynamic.createString("data"), dynamic)));
        });
    }
}

