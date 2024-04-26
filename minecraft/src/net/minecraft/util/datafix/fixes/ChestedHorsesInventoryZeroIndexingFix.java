package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;

public class ChestedHorsesInventoryZeroIndexingFix extends DataFix {
	public ChestedHorsesInventoryZeroIndexingFix(Schema schema) {
		super(schema, false);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		OpticFinder<Pair<String, Pair<Either<Pair<String, String>, Unit>, Pair<Either<?, Unit>, Dynamic<?>>>>> opticFinder = DSL.typeFinder(
			(Type<Pair<String, Pair<Either<Pair<String, String>, Unit>, Pair<Either<?, Unit>, Dynamic<?>>>>>)this.getInputSchema().getType(References.ITEM_STACK)
		);
		Type<?> type = this.getInputSchema().getType(References.ENTITY);
		return TypeRewriteRule.seq(
			this.horseLikeInventoryIndexingFixer(opticFinder, type, "minecraft:llama"),
			this.horseLikeInventoryIndexingFixer(opticFinder, type, "minecraft:trader_llama"),
			this.horseLikeInventoryIndexingFixer(opticFinder, type, "minecraft:mule"),
			this.horseLikeInventoryIndexingFixer(opticFinder, type, "minecraft:donkey")
		);
	}

	private TypeRewriteRule horseLikeInventoryIndexingFixer(
		OpticFinder<Pair<String, Pair<Either<Pair<String, String>, Unit>, Pair<Either<?, Unit>, Dynamic<?>>>>> opticFinder, Type<?> type, String string
	) {
		Type<?> type2 = this.getInputSchema().getChoiceType(References.ENTITY, string);
		OpticFinder<?> opticFinder2 = DSL.namedChoice(string, type2);
		OpticFinder<?> opticFinder3 = type2.findField("Items");
		return this.fixTypeEverywhereTyped(
			"Fix non-zero indexing in chest horse type " + string,
			type,
			typed -> typed.updateTyped(
					opticFinder2,
					typedx -> typedx.updateTyped(
							opticFinder3,
							typedxx -> typedxx.update(
									opticFinder,
									pair -> pair.mapSecond(
											pairx -> pairx.mapSecond(
													pairxx -> pairxx.mapSecond(dynamic -> dynamic.update("Slot", dynamicx -> dynamicx.createByte((byte)(dynamicx.asInt(2) - 2))))
												)
										)
								)
						)
				)
		);
	}
}
