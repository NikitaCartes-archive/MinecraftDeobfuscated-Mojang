package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public abstract class BlockRenameFix extends DataFix {
	private final String name;

	public BlockRenameFix(Schema schema, String string) {
		super(schema, false);
		this.name = string;
	}

	@Override
	public TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.BLOCK_NAME);
		Type<Pair<String, String>> type2 = DSL.named(References.BLOCK_NAME.typeName(), NamespacedSchema.namespacedString());
		if (!Objects.equals(type, type2)) {
			throw new IllegalStateException("block type is not what was expected.");
		} else {
			TypeRewriteRule typeRewriteRule = this.fixTypeEverywhere(this.name + " for block", type2, dynamicOps -> pair -> pair.mapSecond(this::renameBlock));
			TypeRewriteRule typeRewriteRule2 = this.fixTypeEverywhereTyped(
				this.name + " for block_state", this.getInputSchema().getType(References.BLOCK_STATE), typed -> typed.update(DSL.remainderFinder(), this::fixBlockState)
			);
			TypeRewriteRule typeRewriteRule3 = this.fixTypeEverywhereTyped(
				this.name + " for flat_block_state",
				this.getInputSchema().getType(References.FLAT_BLOCK_STATE),
				typed -> typed.update(
						DSL.remainderFinder(), dynamic -> DataFixUtils.orElse(dynamic.asString().result().map(this::fixFlatBlockState).map(dynamic::createString), dynamic)
					)
			);
			return TypeRewriteRule.seq(typeRewriteRule, typeRewriteRule2, typeRewriteRule3);
		}
	}

	private Dynamic<?> fixBlockState(Dynamic<?> dynamic) {
		Optional<String> optional = dynamic.get("Name").asString().result();
		return optional.isPresent() ? dynamic.set("Name", dynamic.createString(this.renameBlock((String)optional.get()))) : dynamic;
	}

	private String fixFlatBlockState(String string) {
		int i = string.indexOf(91);
		int j = string.indexOf(123);
		int k = string.length();
		if (i > 0) {
			k = i;
		}

		if (j > 0) {
			k = Math.min(k, j);
		}

		String string2 = string.substring(0, k);
		String string3 = this.renameBlock(string2);
		return string3 + string.substring(k);
	}

	protected abstract String renameBlock(String string);

	public static DataFix create(Schema schema, String string, Function<String, String> function) {
		return new BlockRenameFix(schema, string) {
			@Override
			protected String renameBlock(String string) {
				return (String)function.apply(string);
			}
		};
	}
}
