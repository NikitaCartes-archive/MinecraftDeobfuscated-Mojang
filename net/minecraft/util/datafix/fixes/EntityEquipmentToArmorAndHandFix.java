/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.datafix.fixes.References;

public class EntityEquipmentToArmorAndHandFix
extends DataFix {
    public EntityEquipmentToArmorAndHandFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.cap(this.getInputSchema().getTypeRaw(References.ITEM_STACK));
    }

    private <IS> TypeRewriteRule cap(Type<IS> type) {
        Type<Pair<Either<IS, Unit>, Dynamic<?>>> type2 = DSL.and(DSL.optional(DSL.field("Equipment", DSL.list(type))), DSL.remainderType());
        Type type3 = DSL.and(DSL.optional(DSL.field("ArmorItems", DSL.list(type))), DSL.optional(DSL.field("HandItems", DSL.list(type))), DSL.remainderType());
        OpticFinder opticFinder = DSL.typeFinder(type2);
        OpticFinder opticFinder2 = DSL.fieldFinder("Equipment", DSL.list(type));
        return this.fixTypeEverywhereTyped("EntityEquipmentToArmorAndHandFix", this.getInputSchema().getType(References.ENTITY), this.getOutputSchema().getType(References.ENTITY), (Typed<?> typed) -> {
            Either<Object, Unit> either = Either.right(DSL.unit());
            Either<Object, Unit> either2 = Either.right(DSL.unit());
            Dynamic dynamic = typed.getOrCreate(DSL.remainderFinder());
            Optional optional = typed.getOptional(opticFinder2);
            if (optional.isPresent()) {
                List list = (List)optional.get();
                Object object = type.read(dynamic.emptyMap()).result().orElseThrow(() -> new IllegalStateException("Could not parse newly created empty itemstack.")).getFirst();
                if (!list.isEmpty()) {
                    either = Either.left(Lists.newArrayList(list.get(0), object));
                }
                if (list.size() > 1) {
                    ArrayList<Object> list2 = Lists.newArrayList(object, object, object, object);
                    for (int i = 1; i < Math.min(list.size(), 5); ++i) {
                        list2.set(i - 1, list.get(i));
                    }
                    either2 = Either.left(list2);
                }
            }
            Dynamic dynamic2 = dynamic;
            Optional<Stream<Dynamic<?>>> optional2 = dynamic.get("DropChances").asStreamOpt().result();
            if (optional2.isPresent()) {
                Dynamic dynamic3;
                Iterator iterator = Stream.concat(optional2.get(), Stream.generate(() -> dynamic2.createInt(0))).iterator();
                float f = ((Dynamic)iterator.next()).asFloat(0.0f);
                if (!dynamic.get("HandDropChances").result().isPresent()) {
                    dynamic3 = dynamic.createList(Stream.of(Float.valueOf(f), Float.valueOf(0.0f)).map(dynamic::createFloat));
                    dynamic = dynamic.set("HandDropChances", dynamic3);
                }
                if (!dynamic.get("ArmorDropChances").result().isPresent()) {
                    dynamic3 = dynamic.createList(Stream.of(Float.valueOf(((Dynamic)iterator.next()).asFloat(0.0f)), Float.valueOf(((Dynamic)iterator.next()).asFloat(0.0f)), Float.valueOf(((Dynamic)iterator.next()).asFloat(0.0f)), Float.valueOf(((Dynamic)iterator.next()).asFloat(0.0f))).map(dynamic::createFloat));
                    dynamic = dynamic.set("ArmorDropChances", dynamic3);
                }
                dynamic = dynamic.remove("DropChances");
            }
            return typed.set(opticFinder, type3, Pair.of(either, Pair.of(either2, dynamic)));
        });
    }
}

