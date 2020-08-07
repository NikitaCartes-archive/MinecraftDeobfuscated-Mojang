package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;

public abstract class SimpleEntityRenameFix extends EntityRenameFix {
	public SimpleEntityRenameFix(String string, Schema schema, boolean bl) {
		super(string, schema, bl);
	}

	@Override
	protected Pair<String, Typed<?>> fix(String string, Typed<?> typed) {
		Pair<String, Dynamic<?>> pair = this.getNewNameAndTag(string, typed.getOrCreate(DSL.remainderFinder()));
		return Pair.of(pair.getFirst(), typed.set(DSL.remainderFinder(), pair.getSecond()));
	}

	protected abstract Pair<String, Dynamic<?>> getNewNameAndTag(String string, Dynamic<?> dynamic);
}
