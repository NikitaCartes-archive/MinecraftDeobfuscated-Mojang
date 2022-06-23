package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public abstract class SimplestEntityRenameFix extends DataFix {
	private final String name;

	public SimplestEntityRenameFix(String string, Schema schema, boolean bl) {
		super(schema, bl);
		this.name = string;
	}

	@Override
	public TypeRewriteRule makeRule() {
		TaggedChoiceType<String> taggedChoiceType = (TaggedChoiceType<String>)this.getInputSchema().findChoiceType(References.ENTITY);
		TaggedChoiceType<String> taggedChoiceType2 = (TaggedChoiceType<String>)this.getOutputSchema().findChoiceType(References.ENTITY);
		Type<Pair<String, String>> type = DSL.named(References.ENTITY_NAME.typeName(), NamespacedSchema.namespacedString());
		if (!Objects.equals(this.getOutputSchema().getType(References.ENTITY_NAME), type)) {
			throw new IllegalStateException("Entity name type is not what was expected.");
		} else {
			return TypeRewriteRule.seq(this.fixTypeEverywhere(this.name, taggedChoiceType, taggedChoiceType2, dynamicOps -> pair -> pair.mapFirst(string -> {
						String string2 = this.rename(string);
						Type<?> typex = (Type<?>)taggedChoiceType.types().get(string);
						Type<?> type2 = (Type<?>)taggedChoiceType2.types().get(string2);
						if (!type2.equals(typex, true, true)) {
							throw new IllegalStateException(String.format("Dynamic type check failed: %s not equal to %s", type2, typex));
						} else {
							return string2;
						}
					})), this.fixTypeEverywhere(this.name + " for entity name", type, dynamicOps -> pair -> pair.mapSecond(this::rename)));
		}
	}

	protected abstract String rename(String string);
}
