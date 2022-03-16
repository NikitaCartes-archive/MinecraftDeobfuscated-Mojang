/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.util.datafix.fixes.References;

public class ChunkDeleteIgnoredLightDataFix
extends DataFix {
    public ChunkDeleteIgnoredLightDataFix(Schema schema) {
        super(schema, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.CHUNK);
        OpticFinder<?> opticFinder = type.findField("sections");
        return this.fixTypeEverywhereTyped("ChunkDeleteIgnoredLightDataFix", type, typed2 -> {
            boolean bl = typed2.get(DSL.remainderFinder()).get("isLightOn").asBoolean(false);
            if (!bl) {
                return typed2.updateTyped(opticFinder, typed -> typed.update(DSL.remainderFinder(), dynamic -> dynamic.remove("BlockLight").remove("SkyLight")));
            }
            return typed2;
        });
    }
}

