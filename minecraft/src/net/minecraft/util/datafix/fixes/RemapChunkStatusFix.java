package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.UnaryOperator;

public class RemapChunkStatusFix extends DataFix {
	private final String name;
	private final UnaryOperator<String> mapper;

	public RemapChunkStatusFix(Schema schema, String string, UnaryOperator<String> unaryOperator) {
		super(schema, false);
		this.name = string;
		this.mapper = unaryOperator;
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return this.fixTypeEverywhereTyped(
			this.name,
			this.getInputSchema().getType(References.CHUNK),
			typed -> typed.update(
					DSL.remainderFinder(),
					dynamic -> dynamic.update("Status", this::fixStatus).update("below_zero_retrogen", dynamicx -> dynamicx.update("target_status", this::fixStatus))
				)
		);
	}

	private <T> Dynamic<T> fixStatus(Dynamic<T> dynamic) {
		return DataFixUtils.orElse(dynamic.asString().result().map(this.mapper).map(dynamic::createString), dynamic);
	}
}
