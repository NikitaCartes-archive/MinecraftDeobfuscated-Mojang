package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.Objects;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class SimpleRenameFix extends DataFix {
	private final String fixerName;
	private final Map<String, String> nameMapping;
	private final TypeReference typeReference;

	public SimpleRenameFix(Schema schema, TypeReference typeReference, Map<String, String> map) {
		this(schema, typeReference, typeReference.typeName() + "-renames at version: " + schema.getVersionKey(), map);
	}

	public SimpleRenameFix(Schema schema, TypeReference typeReference, String string, Map<String, String> map) {
		super(schema, false);
		this.nameMapping = map;
		this.fixerName = string;
		this.typeReference = typeReference;
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<Pair<String, String>> type = DSL.named(this.typeReference.typeName(), NamespacedSchema.namespacedString());
		if (!Objects.equals(type, this.getInputSchema().getType(this.typeReference))) {
			throw new IllegalStateException("\"" + this.typeReference.typeName() + "\" type is not what was expected.");
		} else {
			return this.fixTypeEverywhere(this.fixerName, type, dynamicOps -> pair -> pair.mapSecond(string -> (String)this.nameMapping.getOrDefault(string, string)));
		}
	}
}
