package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public abstract class ItemRenameFix extends DataFix {
	private final String name;

	public ItemRenameFix(Schema schema, String string) {
		super(schema, false);
		this.name = string;
	}

	@Override
	public TypeRewriteRule makeRule() {
		Type<Pair<String, String>> type = DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString());
		if (!Objects.equals(this.getInputSchema().getType(References.ITEM_NAME), type)) {
			throw new IllegalStateException("item name type is not what was expected.");
		} else {
			return this.fixTypeEverywhere(this.name, type, dynamicOps -> pair -> pair.mapSecond(this::fixItem));
		}
	}

	protected abstract String fixItem(String string);

	public static DataFix create(Schema schema, String string, Function<String, String> function) {
		return new ItemRenameFix(schema, string) {
			@Override
			protected String fixItem(String string) {
				return (String)function.apply(string);
			}
		};
	}
}
