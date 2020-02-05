/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;

public class FurnaceRecipeFix
extends DataFix {
    public FurnaceRecipeFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.cap(this.getOutputSchema().getTypeRaw(References.RECIPE));
    }

    private <R> TypeRewriteRule cap(Type<R> type) {
        Type type2 = DSL.and(DSL.optional(DSL.field("RecipesUsed", DSL.and(DSL.compoundList(type, DSL.intType()), DSL.remainderType()))), DSL.remainderType());
        OpticFinder<?> opticFinder = DSL.namedChoice("minecraft:furnace", this.getInputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:furnace"));
        OpticFinder<?> opticFinder2 = DSL.namedChoice("minecraft:blast_furnace", this.getInputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:blast_furnace"));
        OpticFinder<?> opticFinder3 = DSL.namedChoice("minecraft:smoker", this.getInputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:smoker"));
        Type<?> type3 = this.getOutputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:furnace");
        Type<?> type4 = this.getOutputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:blast_furnace");
        Type<?> type5 = this.getOutputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:smoker");
        Type<?> type6 = this.getInputSchema().getType(References.BLOCK_ENTITY);
        Type<?> type7 = this.getOutputSchema().getType(References.BLOCK_ENTITY);
        return this.fixTypeEverywhereTyped("FurnaceRecipesFix", type6, type7, (Typed<?> typed2) -> typed2.updateTyped(opticFinder, type3, typed -> this.updateFurnaceContents(type, type2, (Typed<?>)typed)).updateTyped(opticFinder2, type4, typed -> this.updateFurnaceContents(type, type2, (Typed<?>)typed)).updateTyped(opticFinder3, type5, typed -> this.updateFurnaceContents(type, type2, (Typed<?>)typed)));
    }

    private <R> Typed<?> updateFurnaceContents(Type<R> type, Type<Pair<Either<Pair<List<Pair<R, Integer>>, Dynamic<?>>, Unit>, Dynamic<?>>> type2, Typed<?> typed) {
        Dynamic<?> dynamic2 = typed.getOrCreate(DSL.remainderFinder());
        int i = dynamic2.get("RecipesUsedSize").asNumber().orElse(0).intValue();
        dynamic2 = dynamic2.remove("RecipesUsedSize");
        ArrayList list = Lists.newArrayList();
        for (int j = 0; j < i; ++j) {
            String string = "RecipeLocation" + j;
            String string2 = "RecipeAmount" + j;
            Optional<Dynamic<?>> optional = dynamic2.get(string).get();
            int k = dynamic2.get(string2).asNumber().orElse(0).intValue();
            if (k > 0) {
                optional.ifPresent(dynamic -> {
                    Pair pair = type.read(dynamic);
                    pair.getSecond().ifPresent(object -> list.add(Pair.of(object, k)));
                });
            }
            dynamic2 = dynamic2.remove(string).remove(string2);
        }
        return typed.set(DSL.remainderFinder(), type2, Pair.of(Either.left(Pair.of(list, dynamic2.emptyMap())), dynamic2));
    }
}

