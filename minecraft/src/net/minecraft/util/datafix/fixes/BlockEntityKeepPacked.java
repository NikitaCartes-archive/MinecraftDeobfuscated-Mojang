package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class BlockEntityKeepPacked extends NamedEntityFix {
	public BlockEntityKeepPacked(Schema schema, boolean bl) {
		super(schema, bl, "BlockEntityKeepPacked", References.BLOCK_ENTITY, "DUMMY");
	}

	private static Dynamic<?> fixTag(Dynamic<?> dynamic) {
		return dynamic.set("keepPacked", dynamic.createBoolean(true));
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(DSL.remainderFinder(), BlockEntityKeepPacked::fixTag);
	}
}
