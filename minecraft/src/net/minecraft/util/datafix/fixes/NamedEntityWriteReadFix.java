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
import net.minecraft.util.datafix.ExtraDataFixUtils;

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
		Type<?> type5 = ExtraDataFixUtils.patchSubType(type2, type, type3);
		return this.fix(type, type3, opticFinder, type4, type5);
	}

	private <S, T, A, B> TypeRewriteRule fix(Type<S> type, Type<T> type2, OpticFinder<A> opticFinder, Type<B> type3, Type<?> type4) {
		return this.fixTypeEverywhere(this.name, type, type2, dynamicOps -> object -> {
				Typed<S> typed = new Typed<>(type, dynamicOps, (S)object);
				return typed.update(opticFinder, type3, objectx -> {
					Typed<A> typedx = new Typed<>((Type<A>)type4, dynamicOps, (A)objectx);
					return Util.<A, B>writeAndReadTypedOrThrow(typedx, type3, this::fix).getValue();
				}).getValue();
			});
	}

	protected abstract <T> Dynamic<T> fix(Dynamic<T> dynamic);
}
