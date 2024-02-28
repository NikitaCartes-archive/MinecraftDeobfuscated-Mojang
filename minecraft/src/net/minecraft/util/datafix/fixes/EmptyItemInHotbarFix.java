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
import java.util.Optional;

public class EmptyItemInHotbarFix extends DataFix {
	public EmptyItemInHotbarFix(Schema schema) {
		super(schema, false);
	}

	@Override
	public TypeRewriteRule makeRule() {
		OpticFinder<Pair<String, Pair<Either<Pair<String, String>, Unit>, Pair<Either<?, Unit>, Dynamic<?>>>>> opticFinder = DSL.typeFinder(
			(Type<Pair<String, Pair<Either<Pair<String, String>, Unit>, Pair<Either<?, Unit>, Dynamic<?>>>>>)this.getInputSchema().getType(References.ITEM_STACK)
		);
		return this.fixTypeEverywhereTyped(
			"EmptyItemInHotbarFix", this.getInputSchema().getType(References.HOTBAR), typed -> typed.update(opticFinder, pair -> pair.mapSecond(pairx -> {
						Optional<String> optional = ((Either)pairx.getFirst()).left().map(Pair::getSecond);
						Dynamic<?> dynamic = (Dynamic<?>)((Pair)pairx.getSecond()).getSecond();
						boolean bl = optional.isEmpty() || ((String)optional.get()).equals("minecraft:air");
						boolean bl2 = dynamic.get("Count").asInt(0) <= 0;
						return !bl && !bl2 ? pairx : Pair.of(Either.right(Unit.INSTANCE), Pair.of(Either.right(Unit.INSTANCE), dynamic.emptyMap()));
					}))
		);
	}
}
