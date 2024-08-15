package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.UnaryOperator;
import net.minecraft.util.datafix.ExtraDataFixUtils;

public class AttributesRenameFix extends DataFix {
	private final String name;
	private final UnaryOperator<String> renames;

	public AttributesRenameFix(Schema schema, String string, UnaryOperator<String> unaryOperator) {
		super(schema, false);
		this.name = string;
		this.renames = unaryOperator;
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return TypeRewriteRule.seq(
			this.fixTypeEverywhereTyped(this.name + " (Components)", this.getInputSchema().getType(References.DATA_COMPONENTS), this::fixDataComponents),
			this.fixTypeEverywhereTyped(this.name + " (Entity)", this.getInputSchema().getType(References.ENTITY), this::fixEntity),
			this.fixTypeEverywhereTyped(this.name + " (Player)", this.getInputSchema().getType(References.PLAYER), this::fixEntity)
		);
	}

	private Typed<?> fixDataComponents(Typed<?> typed) {
		return typed.update(
			DSL.remainderFinder(),
			dynamic -> dynamic.update(
					"minecraft:attribute_modifiers",
					dynamicx -> dynamicx.update(
							"modifiers",
							dynamicxx -> DataFixUtils.orElse(dynamicxx.asStreamOpt().result().map(stream -> stream.map(this::fixTypeField)).map(dynamicxx::createList), dynamicxx)
						)
				)
		);
	}

	private Typed<?> fixEntity(Typed<?> typed) {
		return typed.update(
			DSL.remainderFinder(),
			dynamic -> dynamic.update(
					"attributes",
					dynamicx -> DataFixUtils.orElse(dynamicx.asStreamOpt().result().map(stream -> stream.map(this::fixIdField)).map(dynamicx::createList), dynamicx)
				)
		);
	}

	private Dynamic<?> fixIdField(Dynamic<?> dynamic) {
		return ExtraDataFixUtils.fixStringField(dynamic, "id", this.renames);
	}

	private Dynamic<?> fixTypeField(Dynamic<?> dynamic) {
		return ExtraDataFixUtils.fixStringField(dynamic, "type", this.renames);
	}
}
