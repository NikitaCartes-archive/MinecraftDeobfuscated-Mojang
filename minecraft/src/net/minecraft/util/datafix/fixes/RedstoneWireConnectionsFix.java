package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class RedstoneWireConnectionsFix extends DataFix {
	public RedstoneWireConnectionsFix(Schema schema) {
		super(schema, false);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Schema schema = this.getInputSchema();
		return this.fixTypeEverywhereTyped(
			"RedstoneConnectionsFix", schema.getType(References.BLOCK_STATE), typed -> typed.update(DSL.remainderFinder(), this::updateRedstoneConnections)
		);
	}

	private <T> Dynamic<T> updateRedstoneConnections(Dynamic<T> dynamic) {
		boolean bl = dynamic.get("Name").asString().result().filter("minecraft:redstone_wire"::equals).isPresent();
		return !bl
			? dynamic
			: dynamic.update(
				"Properties",
				dynamicx -> {
					String string = dynamicx.get("east").asString("none");
					String string2 = dynamicx.get("west").asString("none");
					String string3 = dynamicx.get("north").asString("none");
					String string4 = dynamicx.get("south").asString("none");
					boolean blx = isConnected(string) || isConnected(string2);
					boolean bl2 = isConnected(string3) || isConnected(string4);
					String string5 = !isConnected(string) && !bl2 ? "side" : string;
					String string6 = !isConnected(string2) && !bl2 ? "side" : string2;
					String string7 = !isConnected(string3) && !blx ? "side" : string3;
					String string8 = !isConnected(string4) && !blx ? "side" : string4;
					return dynamicx.update("east", dynamicxx -> dynamicxx.createString(string5))
						.update("west", dynamicxx -> dynamicxx.createString(string6))
						.update("north", dynamicxx -> dynamicxx.createString(string7))
						.update("south", dynamicxx -> dynamicxx.createString(string8));
				}
			);
	}

	private static boolean isConnected(String string) {
		return !"none".equals(string);
	}
}
