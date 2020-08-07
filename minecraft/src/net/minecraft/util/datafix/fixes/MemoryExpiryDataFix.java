package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;

public class MemoryExpiryDataFix extends NamedEntityFix {
	public MemoryExpiryDataFix(Schema schema, String string) {
		super(schema, false, "Memory expiry data fix (" + string + ")", References.ENTITY, string);
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(DSL.remainderFinder(), this::fixTag);
	}

	public Dynamic<?> fixTag(Dynamic<?> dynamic) {
		return dynamic.update("Brain", this::updateBrain);
	}

	private Dynamic<?> updateBrain(Dynamic<?> dynamic) {
		return dynamic.update("memories", this::updateMemories);
	}

	private Dynamic<?> updateMemories(Dynamic<?> dynamic) {
		return dynamic.updateMapValues(this::updateMemoryEntry);
	}

	private Pair<Dynamic<?>, Dynamic<?>> updateMemoryEntry(Pair<Dynamic<?>, Dynamic<?>> pair) {
		return pair.mapSecond(this::wrapMemoryValue);
	}

	private Dynamic<?> wrapMemoryValue(Dynamic<?> dynamic) {
		return dynamic.createMap(ImmutableMap.of(dynamic.createString("value"), dynamic));
	}
}
