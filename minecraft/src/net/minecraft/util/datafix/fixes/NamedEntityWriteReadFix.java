package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.Util;

public abstract class NamedEntityWriteReadFix extends DataFix {
	private final String name;
	private final String entityName;
	private final TypeReference type;

	public NamedEntityWriteReadFix(Schema schema, boolean bl, String string, TypeReference typeReference, String string2) {
		super(schema, bl);
		this.name = string;
		this.type = typeReference;
		this.entityName = string2;
	}

	@Override
	public TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(this.type);
		Type<?> type2 = this.getInputSchema().getChoiceType(this.type, this.entityName);
		Type<?> type3 = this.getOutputSchema().getType(this.type);
		Type<?> type4 = this.getOutputSchema().getChoiceType(this.type, this.entityName);
		OpticFinder<?> opticFinder = DSL.namedChoice(this.entityName, type2);
		return this.fixTypeEverywhereTyped(
			this.name,
			type,
			type3,
			typed -> typed.updateTyped(
					opticFinder,
					type4,
					typedx -> (Typed)Util.getOrThrow(
								typedx.write().map(this::fix).flatMap(type4::readTyped), string -> new IllegalStateException("Could not parse the value " + string)
							)
							.getFirst()
				)
		);
	}

	protected abstract <T> Dynamic<T> fix(Dynamic<T> dynamic);
}
