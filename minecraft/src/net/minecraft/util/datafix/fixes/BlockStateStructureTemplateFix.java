package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class BlockStateStructureTemplateFix extends DataFix {
	public BlockStateStructureTemplateFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	@Override
	public TypeRewriteRule makeRule() {
		return this.fixTypeEverywhereTyped(
			"BlockStateStructureTemplateFix",
			this.getInputSchema().getType(References.BLOCK_STATE),
			typed -> typed.update(DSL.remainderFinder(), BlockStateData::upgradeBlockStateTag)
		);
	}
}
