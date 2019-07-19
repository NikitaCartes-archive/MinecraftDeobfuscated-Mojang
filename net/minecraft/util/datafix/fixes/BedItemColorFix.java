/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;

public class BedItemColorFix
extends DataFix {
    public BedItemColorFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    public TypeRewriteRule makeRule() {
        OpticFinder<Pair<String, String>> opticFinder = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), DSL.namespacedString()));
        return this.fixTypeEverywhereTyped("BedItemColorFix", this.getInputSchema().getType(References.ITEM_STACK), typed -> {
            Dynamic dynamic;
            Optional optional = typed.getOptional(opticFinder);
            if (optional.isPresent() && Objects.equals(((Pair)optional.get()).getSecond(), "minecraft:bed") && (dynamic = typed.get(DSL.remainderFinder())).get("Damage").asInt(0) == 0) {
                return typed.set(DSL.remainderFinder(), dynamic.set("Damage", dynamic.createShort((short)14)));
            }
            return typed;
        });
    }
}

