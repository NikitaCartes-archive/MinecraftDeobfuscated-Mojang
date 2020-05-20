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
import java.util.List;
import java.util.Optional;

public class FurnaceRecipeFix extends DataFix {
	public FurnaceRecipeFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return this.cap(this.getOutputSchema().getTypeRaw(References.RECIPE));
	}

	private <R> TypeRewriteRule cap(Type<R> type) {
		Type<Pair<Either<Pair<List<Pair<R, Integer>>, Dynamic<?>>, Unit>, Dynamic<?>>> type2 = DSL.and(
			DSL.optional(DSL.field("RecipesUsed", DSL.and(DSL.compoundList(type, DSL.intType()), DSL.remainderType()))), DSL.remainderType()
		);
		OpticFinder<?> opticFinder = DSL.namedChoice("minecraft:furnace", this.getInputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:furnace"));
		OpticFinder<?> opticFinder2 = DSL.namedChoice(
			"minecraft:blast_furnace", this.getInputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:blast_furnace")
		);
		OpticFinder<?> opticFinder3 = DSL.namedChoice("minecraft:smoker", this.getInputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:smoker"));
		Type<?> type3 = this.getOutputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:furnace");
		Type<?> type4 = this.getOutputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:blast_furnace");
		Type<?> type5 = this.getOutputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:smoker");
		Type<?> type6 = this.getInputSchema().getType(References.BLOCK_ENTITY);
		Type<?> type7 = this.getOutputSchema().getType(References.BLOCK_ENTITY);
		return this.fixTypeEverywhereTyped(
			"FurnaceRecipesFix",
			type6,
			type7,
			typed -> typed.updateTyped(opticFinder, type3, typedx -> this.updateFurnaceContents(type, type2, typedx))
					.updateTyped(opticFinder2, type4, typedx -> this.updateFurnaceContents(type, type2, typedx))
					.updateTyped(opticFinder3, type5, typedx -> this.updateFurnaceContents(type, type2, typedx))
		);
	}

	private <R> Typed<?> updateFurnaceContents(Type<R> type, Type<Pair<Either<Pair<List<Pair<R, Integer>>, Dynamic<?>>, Unit>, Dynamic<?>>> type2, Typed<?> typed) {
		Dynamic<?> dynamic = typed.getOrCreate(DSL.remainderFinder());
		int i = dynamic.get("RecipesUsedSize").asInt(0);
		dynamic = dynamic.remove("RecipesUsedSize");
		List<Pair<R, Integer>> list = Lists.<Pair<R, Integer>>newArrayList();

		for (int j = 0; j < i; j++) {
			String string = "RecipeLocation" + j;
			String string2 = "RecipeAmount" + j;
			Optional<? extends Dynamic<?>> optional = dynamic.get(string).result();
			int k = dynamic.get(string2).asInt(0);
			if (k > 0) {
				optional.ifPresent(dynamicx -> {
					Optional<? extends Pair<R, ? extends Dynamic<?>>> optionalx = type.read(dynamicx).result();
					optionalx.ifPresent(pair -> list.add(Pair.of(pair.getFirst(), k)));
				});
			}

			dynamic = dynamic.remove(string).remove(string2);
		}

		return typed.set(DSL.remainderFinder(), type2, Pair.of(Either.left(Pair.of(list, dynamic.emptyMap())), dynamic));
	}
}
