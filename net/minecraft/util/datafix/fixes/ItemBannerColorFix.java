/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemBannerColorFix
extends DataFix {
    public ItemBannerColorFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder<Pair<String, String>> opticFinder = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder<?> opticFinder2 = type.findField("tag");
        OpticFinder<?> opticFinder3 = opticFinder2.type().findField("BlockEntityTag");
        return this.fixTypeEverywhereTyped("ItemBannerColorFix", type, typed -> {
            Optional optional = typed.getOptional(opticFinder);
            if (optional.isPresent() && Objects.equals(((Pair)optional.get()).getSecond(), "minecraft:banner")) {
                Typed<Dynamic<?>> typed2;
                Optional optional3;
                Dynamic dynamic = typed.get(DSL.remainderFinder());
                Optional optional2 = typed.getOptionalTyped(opticFinder2);
                if (optional2.isPresent() && (optional3 = (typed2 = optional2.get()).getOptionalTyped(opticFinder3)).isPresent()) {
                    Typed<Dynamic<?>> typed3 = optional3.get();
                    Dynamic<?> dynamic2 = typed2.get(DSL.remainderFinder());
                    Dynamic<?> dynamic3 = typed3.getOrCreate(DSL.remainderFinder());
                    if (dynamic3.get("Base").asNumber().result().isPresent()) {
                        Dynamic dynamic4;
                        dynamic = dynamic.set("Damage", dynamic.createShort((short)(dynamic3.get("Base").asInt(0) & 0xF)));
                        Optional<Dynamic<?>> optional4 = dynamic2.get("display").result();
                        if (optional4.isPresent() && Objects.equals(dynamic4 = optional4.get(), dynamic4.emptyMap().merge(dynamic4.createString("Lore"), dynamic4.createList(Stream.of(dynamic4.createString("(+NBT")))))) {
                            return typed.set(DSL.remainderFinder(), dynamic);
                        }
                        dynamic3.remove("Base");
                        return typed.set(DSL.remainderFinder(), dynamic).set(opticFinder2, typed2.set(opticFinder3, typed3.set(DSL.remainderFinder(), dynamic3)));
                    }
                }
                return typed.set(DSL.remainderFinder(), dynamic);
            }
            return typed;
        });
    }
}

