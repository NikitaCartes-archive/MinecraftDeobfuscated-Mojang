package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.ExtraDataFixUtils;

public abstract class ItemStackComponentRemainderFix extends DataFix {
	private final String name;
	private final String componentId;
	private final String newComponentId;

	public ItemStackComponentRemainderFix(Schema schema, String string, String string2) {
		this(schema, string, string2, string2);
	}

	public ItemStackComponentRemainderFix(Schema schema, String string, String string2, String string3) {
		super(schema, false);
		this.name = string;
		this.componentId = string2;
		this.newComponentId = string3;
	}

	@Override
	public final TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
		OpticFinder<?> opticFinder = type.findField("components");
		return this.fixTypeEverywhereTyped(
			this.name,
			type,
			typed -> typed.updateTyped(
					opticFinder,
					typedx -> typedx.update(
							DSL.remainderFinder(), dynamic -> ExtraDataFixUtils.renameAndFixField(dynamic, this.componentId, this.newComponentId, this::fixComponent)
						)
				)
		);
	}

	protected abstract <T> Dynamic<T> fixComponent(Dynamic<T> dynamic);
}
