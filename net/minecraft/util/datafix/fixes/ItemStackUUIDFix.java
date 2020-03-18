/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.datafix.fixes.AbstractUUIDFix;
import net.minecraft.util.datafix.fixes.References;

public class ItemStackUUIDFix
extends AbstractUUIDFix {
    public ItemStackUUIDFix(Schema schema) {
        super(schema, References.ITEM_STACK);
    }

    @Override
    public TypeRewriteRule makeRule() {
        OpticFinder<Pair<String, String>> opticFinder = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), DSL.namespacedString()));
        return this.fixTypeEverywhereTyped("ItemStackUUIDFix", this.getInputSchema().getType(this.typeReference), typed2 -> {
            if (typed2.getOptional(opticFinder).map(pair -> "minecraft:player_head".equals(pair.getSecond())).orElse(false).booleanValue()) {
                OpticFinder<?> opticFinder2 = typed2.getType().findField("tag");
                return typed2.updateTyped(opticFinder2, typed -> typed.update(DSL.remainderFinder(), dynamic2 -> dynamic2.update("SkullOwner", dynamic -> ItemStackUUIDFix.replaceUUIDString(dynamic, "Id", "Id").orElse((Dynamic<?>)dynamic))));
            }
            return typed2;
        });
    }
}

