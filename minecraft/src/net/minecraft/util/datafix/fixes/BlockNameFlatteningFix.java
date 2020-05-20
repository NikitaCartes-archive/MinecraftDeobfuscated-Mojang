package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class BlockNameFlatteningFix extends DataFix {
	public BlockNameFlatteningFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	@Override
	public TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.BLOCK_NAME);
		Type<?> type2 = this.getOutputSchema().getType(References.BLOCK_NAME);
		Type<Pair<String, Either<Integer, String>>> type3 = DSL.named(References.BLOCK_NAME.typeName(), DSL.or(DSL.intType(), NamespacedSchema.namespacedString()));
		Type<Pair<String, String>> type4 = DSL.named(References.BLOCK_NAME.typeName(), NamespacedSchema.namespacedString());
		if (Objects.equals(type, type3) && Objects.equals(type2, type4)) {
			return this.fixTypeEverywhere(
				"BlockNameFlatteningFix",
				type3,
				type4,
				dynamicOps -> pair -> pair.mapSecond(
							either -> either.map(BlockStateData::upgradeBlock, string -> BlockStateData.upgradeBlock(NamespacedSchema.ensureNamespaced(string)))
						)
			);
		} else {
			throw new IllegalStateException("Expected and actual types don't match.");
		}
	}
}
