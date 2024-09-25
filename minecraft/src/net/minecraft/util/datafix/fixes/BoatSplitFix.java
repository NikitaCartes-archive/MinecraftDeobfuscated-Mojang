package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class BoatSplitFix extends DataFix {
	public BoatSplitFix(Schema schema) {
		super(schema, true);
	}

	private static boolean isNormalBoat(String string) {
		return string.equals("minecraft:boat");
	}

	private static boolean isChestBoat(String string) {
		return string.equals("minecraft:chest_boat");
	}

	private static boolean isAnyBoat(String string) {
		return isNormalBoat(string) || isChestBoat(string);
	}

	private static String mapVariantToNormalBoat(String string) {
		return switch (string) {
			case "spruce" -> "minecraft:spruce_boat";
			case "birch" -> "minecraft:birch_boat";
			case "jungle" -> "minecraft:jungle_boat";
			case "acacia" -> "minecraft:acacia_boat";
			case "cherry" -> "minecraft:cherry_boat";
			case "dark_oak" -> "minecraft:dark_oak_boat";
			case "mangrove" -> "minecraft:mangrove_boat";
			case "bamboo" -> "minecraft:bamboo_raft";
			default -> "minecraft:oak_boat";
		};
	}

	private static String mapVariantToChestBoat(String string) {
		return switch (string) {
			case "spruce" -> "minecraft:spruce_chest_boat";
			case "birch" -> "minecraft:birch_chest_boat";
			case "jungle" -> "minecraft:jungle_chest_boat";
			case "acacia" -> "minecraft:acacia_chest_boat";
			case "cherry" -> "minecraft:cherry_chest_boat";
			case "dark_oak" -> "minecraft:dark_oak_chest_boat";
			case "mangrove" -> "minecraft:mangrove_chest_boat";
			case "bamboo" -> "minecraft:bamboo_chest_raft";
			default -> "minecraft:oak_chest_boat";
		};
	}

	@Override
	public TypeRewriteRule makeRule() {
		OpticFinder<String> opticFinder = DSL.fieldFinder("id", NamespacedSchema.namespacedString());
		Type<?> type = this.getInputSchema().getType(References.ENTITY);
		Type<?> type2 = this.getOutputSchema().getType(References.ENTITY);
		return this.fixTypeEverywhereTyped("BoatSplitFix", type, type2, typed -> {
			Optional<String> optional = typed.getOptional(opticFinder);
			if (optional.isPresent() && isAnyBoat((String)optional.get())) {
				Dynamic<?> dynamic = typed.getOrCreate(DSL.remainderFinder());
				Optional<String> optional2 = dynamic.get("Type").asString().result();
				String string;
				if (isChestBoat((String)optional.get())) {
					string = (String)optional2.map(BoatSplitFix::mapVariantToChestBoat).orElse("minecraft:oak_chest_boat");
				} else {
					string = (String)optional2.map(BoatSplitFix::mapVariantToNormalBoat).orElse("minecraft:oak_boat");
				}

				return ExtraDataFixUtils.cast(type2, typed).update(DSL.remainderFinder(), dynamicx -> dynamicx.remove("Type")).set(opticFinder, string);
			} else {
				return ExtraDataFixUtils.cast(type2, typed);
			}
		});
	}
}
