package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;

public abstract class PoiTypeRename extends DataFix {
	public PoiTypeRename(Schema schema, boolean bl) {
		super(schema, bl);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<Pair<String, Dynamic<?>>> type = DSL.named(References.POI_CHUNK.typeName(), DSL.remainderType());
		if (!Objects.equals(type, this.getInputSchema().getType(References.POI_CHUNK))) {
			throw new IllegalStateException("Poi type is not what was expected.");
		} else {
			return this.fixTypeEverywhere("POI rename", type, dynamicOps -> pair -> pair.mapSecond(this::cap));
		}
	}

	private <T> Dynamic<T> cap(Dynamic<T> dynamic) {
		return dynamic.update(
			"Sections",
			dynamicx -> dynamicx.updateMapValues(
					pair -> pair.mapSecond(dynamicxx -> dynamicxx.update("Records", dynamicxxx -> DataFixUtils.orElse(this.renameRecords(dynamicxxx), dynamicxxx)))
				)
		);
	}

	private <T> Optional<Dynamic<T>> renameRecords(Dynamic<T> dynamic) {
		return dynamic.asStreamOpt()
			.<Dynamic<T>>map(
				stream -> dynamic.createList(
						stream.map(
							dynamicxx -> dynamicxx.update(
									"type", dynamicxxx -> DataFixUtils.orElse(dynamicxxx.asString().map(this::rename).map(dynamicxxx::createString).result(), dynamicxxx)
								)
						)
					)
			)
			.result();
	}

	protected abstract String rename(String string);
}
