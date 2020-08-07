package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class EntityEquipmentToArmorAndHandFix extends DataFix {
	public EntityEquipmentToArmorAndHandFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	@Override
	public TypeRewriteRule makeRule() {
		return this.cap(this.getInputSchema().getTypeRaw(References.ITEM_STACK));
	}

	private <IS> TypeRewriteRule cap(Type<IS> type) {
		Type<Pair<Either<List<IS>, Unit>, Dynamic<?>>> type2 = DSL.and(DSL.optional(DSL.field("Equipment", DSL.list(type))), DSL.remainderType());
		Type<Pair<Either<List<IS>, Unit>, Pair<Either<List<IS>, Unit>, Dynamic<?>>>> type3 = DSL.and(
			DSL.optional(DSL.field("ArmorItems", DSL.list(type))), DSL.optional(DSL.field("HandItems", DSL.list(type))), DSL.remainderType()
		);
		OpticFinder<Pair<Either<List<IS>, Unit>, Dynamic<?>>> opticFinder = DSL.typeFinder(type2);
		OpticFinder<List<IS>> opticFinder2 = DSL.fieldFinder("Equipment", DSL.list(type));
		return this.fixTypeEverywhereTyped(
			"EntityEquipmentToArmorAndHandFix",
			this.getInputSchema().getType(References.ENTITY),
			this.getOutputSchema().getType(References.ENTITY),
			typed -> {
				Either<List<IS>, Unit> either = Either.right(DSL.unit());
				Either<List<IS>, Unit> either2 = Either.right(DSL.unit());
				Dynamic<?> dynamic = typed.getOrCreate(DSL.remainderFinder());
				Optional<List<IS>> optional = typed.getOptional(opticFinder2);
				if (optional.isPresent()) {
					List<IS> list = (List<IS>)optional.get();
					IS object = (IS)((Pair)type.read(dynamic.emptyMap())
							.result()
							.orElseThrow(() -> new IllegalStateException("Could not parse newly created empty itemstack.")))
						.getFirst();
					if (!list.isEmpty()) {
						either = Either.left(Lists.<IS>newArrayList((IS[])(new Object[]{list.get(0), object})));
					}

					if (list.size() > 1) {
						List<IS> list2 = Lists.<IS>newArrayList(object, object, object, object);

						for (int i = 1; i < Math.min(list.size(), 5); i++) {
							list2.set(i - 1, list.get(i));
						}

						either2 = Either.left(list2);
					}
				}

				Dynamic<?> dynamic2 = dynamic;
				Optional<? extends Stream<? extends Dynamic<?>>> optional2 = dynamic.get("DropChances").asStreamOpt().result();
				if (optional2.isPresent()) {
					Iterator<? extends Dynamic<?>> iterator = Stream.concat((Stream)optional2.get(), Stream.generate(() -> dynamic2.createInt(0))).iterator();
					float f = ((Dynamic)iterator.next()).asFloat(0.0F);
					if (!dynamic.get("HandDropChances").result().isPresent()) {
						Dynamic<?> dynamic3 = dynamic.createList(Stream.of(f, 0.0F).map(dynamic::createFloat));
						dynamic = dynamic.set("HandDropChances", dynamic3);
					}

					if (!dynamic.get("ArmorDropChances").result().isPresent()) {
						Dynamic<?> dynamic3 = dynamic.createList(
							Stream.of(
									((Dynamic)iterator.next()).asFloat(0.0F),
									((Dynamic)iterator.next()).asFloat(0.0F),
									((Dynamic)iterator.next()).asFloat(0.0F),
									((Dynamic)iterator.next()).asFloat(0.0F)
								)
								.map(dynamic::createFloat)
						);
						dynamic = dynamic.set("ArmorDropChances", dynamic3);
					}

					dynamic = dynamic.remove("DropChances");
				}

				return typed.set(opticFinder, type3, Pair.of(either, Pair.of(either2, dynamic)));
			}
		);
	}
}
