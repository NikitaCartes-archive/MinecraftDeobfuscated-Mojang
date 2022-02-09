/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;

public class StructureSettingsFlattenFix
extends DataFix {
    public StructureSettingsFlattenFix(Schema schema) {
        super(schema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.WORLD_GEN_SETTINGS);
        OpticFinder<?> opticFinder = type.findField("dimensions");
        return this.fixTypeEverywhereTyped("StructureSettingsFlatten", type, typed2 -> typed2.updateTyped(opticFinder, typed -> {
            Dynamic<?> dynamic = typed.write().result().orElseThrow();
            Dynamic<?> dynamic2 = dynamic.updateMapValues(StructureSettingsFlattenFix::fixDimension);
            return opticFinder.type().readTyped(dynamic2).result().orElseThrow().getFirst();
        }));
    }

    private static Pair<Dynamic<?>, Dynamic<?>> fixDimension(Pair<Dynamic<?>, Dynamic<?>> pair) {
        Dynamic<?> dynamic = pair.getSecond();
        return Pair.of(pair.getFirst(), dynamic.update("generator", dynamic2 -> dynamic2.update("settings", dynamic -> dynamic.update("structures", StructureSettingsFlattenFix::fixStructures))));
    }

    private static Dynamic<?> fixStructures(Dynamic<?> dynamic) {
        Dynamic<?> dynamic2 = dynamic.get("structures").result().get().updateMapValues(pair -> pair.mapSecond(dynamic2 -> dynamic2.set("type", dynamic.createString("minecraft:random_spread"))));
        Dynamic<?> dynamic3 = dynamic.get("stronghold").result().get().set("type", dynamic.createString("minecraft:concentric_rings"));
        return dynamic2.set("minecraft:stronghold", dynamic3);
    }
}

