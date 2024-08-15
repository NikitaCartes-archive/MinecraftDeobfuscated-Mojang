package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.function.UnaryOperator;

public class AttributesRenameLegacy extends DataFix {
	private final String name;
	private final UnaryOperator<String> renames;

	public AttributesRenameLegacy(Schema schema, String string, UnaryOperator<String> unaryOperator) {
		super(schema, false);
		this.name = string;
		this.renames = unaryOperator;
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
		OpticFinder<?> opticFinder = type.findField("tag");
		return TypeRewriteRule.seq(
			this.fixTypeEverywhereTyped(this.name + " (ItemStack)", type, typed -> typed.updateTyped(opticFinder, this::fixItemStackTag)),
			this.fixTypeEverywhereTyped(this.name + " (Entity)", this.getInputSchema().getType(References.ENTITY), this::fixEntity),
			this.fixTypeEverywhereTyped(this.name + " (Player)", this.getInputSchema().getType(References.PLAYER), this::fixEntity)
		);
	}

	private Dynamic<?> fixName(Dynamic<?> dynamic) {
		return DataFixUtils.orElse(dynamic.asString().result().map(this.renames).map(dynamic::createString), dynamic);
	}

	private Typed<?> fixItemStackTag(Typed<?> typed) {
		return typed.update(
			DSL.remainderFinder(),
			dynamic -> dynamic.update(
					"AttributeModifiers",
					dynamicx -> DataFixUtils.orElse(
							dynamicx.asStreamOpt().result().map(stream -> stream.map(dynamicxx -> dynamicxx.update("AttributeName", this::fixName))).map(dynamicx::createList),
							dynamicx
						)
				)
		);
	}

	private Typed<?> fixEntity(Typed<?> typed) {
		return typed.update(
			DSL.remainderFinder(),
			dynamic -> dynamic.update(
					"Attributes",
					dynamicx -> DataFixUtils.orElse(
							dynamicx.asStreamOpt().result().map(stream -> stream.map(dynamicxx -> dynamicxx.update("Name", this::fixName))).map(dynamicx::createList), dynamicx
						)
				)
		);
	}
}
