package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import java.util.function.UnaryOperator;

public class BlockEntityRenameFix extends DataFix {
	private final String name;
	private final UnaryOperator<String> nameChangeLookup;

	private BlockEntityRenameFix(Schema schema, String string, UnaryOperator<String> unaryOperator) {
		super(schema, true);
		this.name = string;
		this.nameChangeLookup = unaryOperator;
	}

	@Override
	public TypeRewriteRule makeRule() {
		TaggedChoiceType<String> taggedChoiceType = (TaggedChoiceType<String>)this.getInputSchema().findChoiceType(References.BLOCK_ENTITY);
		TaggedChoiceType<String> taggedChoiceType2 = (TaggedChoiceType<String>)this.getOutputSchema().findChoiceType(References.BLOCK_ENTITY);
		return this.fixTypeEverywhere(this.name, taggedChoiceType, taggedChoiceType2, dynamicOps -> pair -> pair.mapFirst(this.nameChangeLookup));
	}

	public static DataFix create(Schema schema, String string, UnaryOperator<String> unaryOperator) {
		return new BlockEntityRenameFix(schema, string, unaryOperator);
	}
}
