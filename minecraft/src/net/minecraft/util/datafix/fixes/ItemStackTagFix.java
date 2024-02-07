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
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public abstract class ItemStackTagFix extends DataFix {
	private final String name;
	private final Predicate<String> idFilter;

	public ItemStackTagFix(Schema schema, String string, Predicate<String> predicate) {
		super(schema, false);
		this.name = string;
		this.idFilter = predicate;
	}

	@Override
	public final TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
		return this.fixTypeEverywhereTyped(this.name, type, createFixer(type, this.idFilter, this::fixItemStackTag));
	}

	public static UnaryOperator<Typed<?>> createFixer(Type<?> type, Predicate<String> predicate, UnaryOperator<Dynamic<?>> unaryOperator) {
		OpticFinder<Pair<String, String>> opticFinder = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
		OpticFinder<?> opticFinder2 = type.findField("tag");
		return typed -> {
			Optional<Pair<String, String>> optional = typed.getOptional(opticFinder);
			return optional.isPresent() && predicate.test((String)((Pair)optional.get()).getSecond())
				? typed.updateTyped(opticFinder2, typedx -> typedx.update(DSL.remainderFinder(), unaryOperator))
				: typed;
		};
	}

	protected abstract <T> Dynamic<T> fixItemStackTag(Dynamic<T> dynamic);
}
