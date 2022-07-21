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
import java.util.Set;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemRemoveBlockEntityTagFix extends DataFix {
	private final Set<String> items;

	public ItemRemoveBlockEntityTagFix(Schema schema, boolean bl, Set<String> set) {
		super(schema, bl);
		this.items = set;
	}

	@Override
	public TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
		OpticFinder<Pair<String, String>> opticFinder = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
		OpticFinder<?> opticFinder2 = type.findField("tag");
		OpticFinder<?> opticFinder3 = opticFinder2.type().findField("BlockEntityTag");
		return this.fixTypeEverywhereTyped("ItemRemoveBlockEntityTagFix", type, typed -> {
			Optional<Pair<String, String>> optional = typed.getOptional(opticFinder);
			if (optional.isPresent() && this.items.contains(((Pair)optional.get()).getSecond())) {
				Optional<? extends Typed<?>> optional2 = typed.getOptionalTyped(opticFinder2);
				if (optional2.isPresent()) {
					Typed<?> typed2 = (Typed<?>)optional2.get();
					Optional<? extends Typed<?>> optional3 = typed2.getOptionalTyped(opticFinder3);
					if (optional3.isPresent()) {
						Optional<? extends Dynamic<?>> optional4 = typed2.write().result();
						Dynamic<?> dynamic = optional4.isPresent() ? (Dynamic)optional4.get() : typed2.get(DSL.remainderFinder());
						Dynamic<?> dynamic2 = dynamic.remove("BlockEntityTag");
						Optional<? extends Pair<? extends Typed<?>, ?>> optional5 = opticFinder2.type().readTyped(dynamic2).result();
						if (optional5.isEmpty()) {
							return typed;
						}

						return typed.set(opticFinder2, (Typed)((Pair)optional5.get()).getFirst());
					}
				}
			}

			return typed;
		});
	}
}
