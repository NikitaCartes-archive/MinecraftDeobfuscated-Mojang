/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Map;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class StatsRenameFix
extends DataFix {
    private final String name;
    private final Map<String, String> renames;

    public StatsRenameFix(Schema schema, String string, Map<String, String> map) {
        super(schema, false);
        this.name = string;
        this.renames = map;
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getOutputSchema().getType(References.STATS);
        Type<?> type2 = this.getInputSchema().getType(References.STATS);
        OpticFinder<?> opticFinder = type2.findField("stats");
        OpticFinder<?> opticFinder2 = opticFinder.type().findField("minecraft:custom");
        OpticFinder<String> opticFinder3 = NamespacedSchema.namespacedString().finder();
        return this.fixTypeEverywhereTyped(this.name, type2, type, (Typed<?> typed) -> typed.updateTyped(opticFinder, typed2 -> typed2.updateTyped(opticFinder2, typed -> typed.update(opticFinder3, string -> {
            for (Map.Entry<String, String> entry : this.renames.entrySet()) {
                if (!string.equals(entry.getKey())) continue;
                return entry.getValue();
            }
            return string;
        }))));
    }
}

