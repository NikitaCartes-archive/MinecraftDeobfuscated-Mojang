package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.util.datafix.ExtraDataFixUtils;

public class FixProjectileStoredItem extends DataFix {
	private static final String EMPTY_POTION = "minecraft:empty";

	public FixProjectileStoredItem(Schema schema) {
		super(schema, true);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.ENTITY);
		Type<?> type2 = this.getOutputSchema().getType(References.ENTITY);
		return this.fixTypeEverywhereTyped(
			"Fix AbstractArrow item type",
			type,
			type2,
			ExtraDataFixUtils.chainAllFilters(
				this.fixChoice("minecraft:trident", FixProjectileStoredItem::castUnchecked),
				this.fixChoice("minecraft:arrow", FixProjectileStoredItem::fixArrow),
				this.fixChoice("minecraft:spectral_arrow", FixProjectileStoredItem::fixSpectralArrow)
			)
		);
	}

	private Function<Typed<?>, Typed<?>> fixChoice(String string, FixProjectileStoredItem.SubFixer<?> subFixer) {
		Type<?> type = this.getInputSchema().getChoiceType(References.ENTITY, string);
		Type<?> type2 = this.getOutputSchema().getChoiceType(References.ENTITY, string);
		return fixChoiceCap(string, subFixer, type, type2);
	}

	private static <T> Function<Typed<?>, Typed<?>> fixChoiceCap(String string, FixProjectileStoredItem.SubFixer<?> subFixer, Type<?> type, Type<T> type2) {
		OpticFinder<?> opticFinder = DSL.namedChoice(string, type);
		return typed -> typed.updateTyped(opticFinder, type2, typedx -> subFixer.fix(typedx, type2));
	}

	private static <T> Typed<T> fixArrow(Typed<?> typed, Type<T> type) {
		return Util.writeAndReadTypedOrThrow(typed, type, dynamic -> dynamic.set("item", createItemStack(dynamic, getArrowType(dynamic))));
	}

	private static String getArrowType(Dynamic<?> dynamic) {
		return dynamic.get("Potion").asString("minecraft:empty").equals("minecraft:empty") ? "minecraft:arrow" : "minecraft:tipped_arrow";
	}

	private static <T> Typed<T> fixSpectralArrow(Typed<?> typed, Type<T> type) {
		return Util.writeAndReadTypedOrThrow(typed, type, dynamic -> dynamic.set("item", createItemStack(dynamic, "minecraft:spectral_arrow")));
	}

	private static Dynamic<?> createItemStack(Dynamic<?> dynamic, String string) {
		return dynamic.createMap(ImmutableMap.of(dynamic.createString("id"), dynamic.createString(string), dynamic.createString("Count"), dynamic.createInt(1)));
	}

	private static <T> Typed<T> castUnchecked(Typed<?> typed, Type<T> type) {
		return new Typed<>(type, typed.getOps(), (T)typed.getValue());
	}

	interface SubFixer<F> {
		Typed<F> fix(Typed<?> typed, Type<F> type);
	}
}
