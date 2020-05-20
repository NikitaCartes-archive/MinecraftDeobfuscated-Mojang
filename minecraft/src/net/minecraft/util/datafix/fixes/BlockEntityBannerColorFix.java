package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class BlockEntityBannerColorFix extends NamedEntityFix {
	public BlockEntityBannerColorFix(Schema schema, boolean bl) {
		super(schema, bl, "BlockEntityBannerColorFix", References.BLOCK_ENTITY, "minecraft:banner");
	}

	public Dynamic<?> fixTag(Dynamic<?> dynamic) {
		dynamic = dynamic.update("Base", dynamicx -> dynamicx.createInt(15 - dynamicx.asInt(0)));
		return dynamic.update(
			"Patterns",
			dynamicx -> DataFixUtils.orElse(
					dynamicx.asStreamOpt()
						.map(stream -> stream.map(dynamicxx -> dynamicxx.update("Color", dynamicxxx -> dynamicxxx.createInt(15 - dynamicxxx.asInt(0)))))
						.map(dynamicx::createList)
						.result(),
					dynamicx
				)
		);
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(DSL.remainderFinder(), this::fixTag);
	}
}
