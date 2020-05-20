/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.DynamicOps;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;

public class EntityRidingToPassengersFix
extends DataFix {
    public EntityRidingToPassengersFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Schema schema = this.getInputSchema();
        Schema schema2 = this.getOutputSchema();
        Type<?> type = schema.getTypeRaw(References.ENTITY_TREE);
        Type<?> type2 = schema2.getTypeRaw(References.ENTITY_TREE);
        Type<?> type3 = schema.getTypeRaw(References.ENTITY);
        return this.cap(schema, schema2, type, type2, type3);
    }

    private <OldEntityTree, NewEntityTree, Entity> TypeRewriteRule cap(Schema schema, Schema schema2, Type<OldEntityTree> type, Type<NewEntityTree> type2, Type<Entity> type3) {
        Type<Pair<String, Pair<Either<OldEntityTree, Unit>, Entity>>> type4 = DSL.named(References.ENTITY_TREE.typeName(), DSL.and(DSL.optional(DSL.field("Riding", type)), type3));
        Type<Pair<String, Pair<Either<NewEntityTree, Unit>, Entity>>> type5 = DSL.named(References.ENTITY_TREE.typeName(), DSL.and(DSL.optional(DSL.field("Passengers", DSL.list(type2))), type3));
        Type<?> type6 = schema.getType(References.ENTITY_TREE);
        Type<?> type7 = schema2.getType(References.ENTITY_TREE);
        if (!Objects.equals(type6, type4)) {
            throw new IllegalStateException("Old entity type is not what was expected.");
        }
        if (!type7.equals(type5, true, true)) {
            throw new IllegalStateException("New entity type is not what was expected.");
        }
        OpticFinder opticFinder = DSL.typeFinder(type4);
        OpticFinder opticFinder2 = DSL.typeFinder(type5);
        OpticFinder opticFinder3 = DSL.typeFinder(type2);
        Type<?> type8 = schema.getType(References.PLAYER);
        Type<?> type9 = schema2.getType(References.PLAYER);
        return TypeRewriteRule.seq(this.fixTypeEverywhere("EntityRidingToPassengerFix", type4, type5, dynamicOps -> pair2 -> {
            Optional<Object> optional = Optional.empty();
            Pair pair22 = pair2;
            while (true) {
                Either either = DataFixUtils.orElse(optional.map(pair -> {
                    Typed typed = type2.pointTyped((DynamicOps<?>)dynamicOps).orElseThrow(() -> new IllegalStateException("Could not create new entity tree"));
                    Object object = typed.set(opticFinder2, pair).getOptional(opticFinder3).orElseThrow(() -> new IllegalStateException("Should always have an entity tree here"));
                    return Either.left(ImmutableList.of(object));
                }), Either.right(DSL.unit()));
                optional = Optional.of(Pair.of(References.ENTITY_TREE.typeName(), Pair.of(either, ((Pair)pair22.getSecond()).getSecond())));
                Optional optional2 = ((Either)((Pair)pair22.getSecond()).getFirst()).left();
                if (!optional2.isPresent()) break;
                pair22 = (Pair)new Typed(type, (DynamicOps<?>)dynamicOps, optional2.get()).getOptional(opticFinder).orElseThrow(() -> new IllegalStateException("Should always have an entity here"));
            }
            return (Pair)optional.orElseThrow(() -> new IllegalStateException("Should always have an entity tree here"));
        }), this.writeAndRead("player RootVehicle injecter", type8, type9));
    }
}

