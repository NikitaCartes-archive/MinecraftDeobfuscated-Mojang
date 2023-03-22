package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.function.UnaryOperator;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class NamespacedTypeRenameFix extends DataFix {
	private final String name;
	private final TypeReference type;
	private final UnaryOperator<String> renamer;

	public NamespacedTypeRenameFix(Schema schema, String string, TypeReference typeReference, UnaryOperator<String> unaryOperator) {
		super(schema, false);
		this.name = string;
		this.type = typeReference;
		this.renamer = unaryOperator;
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<Pair<String, String>> type = DSL.named(this.type.typeName(), NamespacedSchema.namespacedString());
		if (!Objects.equals(type, this.getInputSchema().getType(this.type))) {
			throw new IllegalStateException("\"" + this.type.typeName() + "\" is not what was expected.");
		} else {
			return this.fixTypeEverywhere(this.name, type, dynamicOps -> pair -> pair.mapSecond(this.renamer));
		}
	}
}
