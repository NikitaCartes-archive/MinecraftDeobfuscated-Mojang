package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ReorganizePoi extends DataFix {
	public ReorganizePoi(Schema schema, boolean bl) {
		super(schema, bl);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<Pair<String, Dynamic<?>>> type = DSL.named(References.POI_CHUNK.typeName(), DSL.remainderType());
		if (!Objects.equals(type, this.getInputSchema().getType(References.POI_CHUNK))) {
			throw new IllegalStateException("Poi type is not what was expected.");
		} else {
			return this.fixTypeEverywhere("POI reorganization", type, dynamicOps -> pair -> pair.mapSecond(ReorganizePoi::cap));
		}
	}

	private static <T> Dynamic<T> cap(Dynamic<T> dynamic) {
		Map<Dynamic<T>, Dynamic<T>> map = Maps.<Dynamic<T>, Dynamic<T>>newHashMap();

		for (int i = 0; i < 16; i++) {
			String string = String.valueOf(i);
			Optional<Dynamic<T>> optional = dynamic.get(string).result();
			if (optional.isPresent()) {
				Dynamic<T> dynamic2 = (Dynamic<T>)optional.get();
				Dynamic<T> dynamic3 = dynamic.createMap(ImmutableMap.of(dynamic.createString("Records"), dynamic2));
				map.put(dynamic.createInt(i), dynamic3);
				dynamic = dynamic.remove(string);
			}
		}

		return dynamic.set("Sections", dynamic.createMap(map));
	}
}
