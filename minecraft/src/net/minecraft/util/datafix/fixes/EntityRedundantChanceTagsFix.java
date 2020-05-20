package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class EntityRedundantChanceTagsFix extends DataFix {
	public EntityRedundantChanceTagsFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	@Override
	public TypeRewriteRule makeRule() {
		return this.fixTypeEverywhereTyped(
			"EntityRedundantChanceTagsFix", this.getInputSchema().getType(References.ENTITY), typed -> typed.update(DSL.remainderFinder(), dynamic -> {
					Dynamic<?> dynamic2 = dynamic;
					if (Objects.equals(dynamic.get("HandDropChances"), Optional.of(dynamic.createList(Stream.generate(() -> dynamic2.createFloat(0.0F)).limit(2L))))) {
						dynamic = dynamic.remove("HandDropChances");
					}

					if (Objects.equals(dynamic.get("ArmorDropChances"), Optional.of(dynamic.createList(Stream.generate(() -> dynamic2.createFloat(0.0F)).limit(4L))))) {
						dynamic = dynamic.remove("ArmorDropChances");
					}

					return dynamic;
				})
		);
	}
}
