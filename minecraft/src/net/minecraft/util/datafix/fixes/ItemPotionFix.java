package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemPotionFix extends DataFix {
	private static final int SPLASH = 16384;
	private static final String[] POTIONS = DataFixUtils.make(new String[128], strings -> {
		strings[0] = "minecraft:water";
		strings[1] = "minecraft:regeneration";
		strings[2] = "minecraft:swiftness";
		strings[3] = "minecraft:fire_resistance";
		strings[4] = "minecraft:poison";
		strings[5] = "minecraft:healing";
		strings[6] = "minecraft:night_vision";
		strings[7] = null;
		strings[8] = "minecraft:weakness";
		strings[9] = "minecraft:strength";
		strings[10] = "minecraft:slowness";
		strings[11] = "minecraft:leaping";
		strings[12] = "minecraft:harming";
		strings[13] = "minecraft:water_breathing";
		strings[14] = "minecraft:invisibility";
		strings[15] = null;
		strings[16] = "minecraft:awkward";
		strings[17] = "minecraft:regeneration";
		strings[18] = "minecraft:swiftness";
		strings[19] = "minecraft:fire_resistance";
		strings[20] = "minecraft:poison";
		strings[21] = "minecraft:healing";
		strings[22] = "minecraft:night_vision";
		strings[23] = null;
		strings[24] = "minecraft:weakness";
		strings[25] = "minecraft:strength";
		strings[26] = "minecraft:slowness";
		strings[27] = "minecraft:leaping";
		strings[28] = "minecraft:harming";
		strings[29] = "minecraft:water_breathing";
		strings[30] = "minecraft:invisibility";
		strings[31] = null;
		strings[32] = "minecraft:thick";
		strings[33] = "minecraft:strong_regeneration";
		strings[34] = "minecraft:strong_swiftness";
		strings[35] = "minecraft:fire_resistance";
		strings[36] = "minecraft:strong_poison";
		strings[37] = "minecraft:strong_healing";
		strings[38] = "minecraft:night_vision";
		strings[39] = null;
		strings[40] = "minecraft:weakness";
		strings[41] = "minecraft:strong_strength";
		strings[42] = "minecraft:slowness";
		strings[43] = "minecraft:strong_leaping";
		strings[44] = "minecraft:strong_harming";
		strings[45] = "minecraft:water_breathing";
		strings[46] = "minecraft:invisibility";
		strings[47] = null;
		strings[48] = null;
		strings[49] = "minecraft:strong_regeneration";
		strings[50] = "minecraft:strong_swiftness";
		strings[51] = "minecraft:fire_resistance";
		strings[52] = "minecraft:strong_poison";
		strings[53] = "minecraft:strong_healing";
		strings[54] = "minecraft:night_vision";
		strings[55] = null;
		strings[56] = "minecraft:weakness";
		strings[57] = "minecraft:strong_strength";
		strings[58] = "minecraft:slowness";
		strings[59] = "minecraft:strong_leaping";
		strings[60] = "minecraft:strong_harming";
		strings[61] = "minecraft:water_breathing";
		strings[62] = "minecraft:invisibility";
		strings[63] = null;
		strings[64] = "minecraft:mundane";
		strings[65] = "minecraft:long_regeneration";
		strings[66] = "minecraft:long_swiftness";
		strings[67] = "minecraft:long_fire_resistance";
		strings[68] = "minecraft:long_poison";
		strings[69] = "minecraft:healing";
		strings[70] = "minecraft:long_night_vision";
		strings[71] = null;
		strings[72] = "minecraft:long_weakness";
		strings[73] = "minecraft:long_strength";
		strings[74] = "minecraft:long_slowness";
		strings[75] = "minecraft:long_leaping";
		strings[76] = "minecraft:harming";
		strings[77] = "minecraft:long_water_breathing";
		strings[78] = "minecraft:long_invisibility";
		strings[79] = null;
		strings[80] = "minecraft:awkward";
		strings[81] = "minecraft:long_regeneration";
		strings[82] = "minecraft:long_swiftness";
		strings[83] = "minecraft:long_fire_resistance";
		strings[84] = "minecraft:long_poison";
		strings[85] = "minecraft:healing";
		strings[86] = "minecraft:long_night_vision";
		strings[87] = null;
		strings[88] = "minecraft:long_weakness";
		strings[89] = "minecraft:long_strength";
		strings[90] = "minecraft:long_slowness";
		strings[91] = "minecraft:long_leaping";
		strings[92] = "minecraft:harming";
		strings[93] = "minecraft:long_water_breathing";
		strings[94] = "minecraft:long_invisibility";
		strings[95] = null;
		strings[96] = "minecraft:thick";
		strings[97] = "minecraft:regeneration";
		strings[98] = "minecraft:swiftness";
		strings[99] = "minecraft:long_fire_resistance";
		strings[100] = "minecraft:poison";
		strings[101] = "minecraft:strong_healing";
		strings[102] = "minecraft:long_night_vision";
		strings[103] = null;
		strings[104] = "minecraft:long_weakness";
		strings[105] = "minecraft:strength";
		strings[106] = "minecraft:long_slowness";
		strings[107] = "minecraft:leaping";
		strings[108] = "minecraft:strong_harming";
		strings[109] = "minecraft:long_water_breathing";
		strings[110] = "minecraft:long_invisibility";
		strings[111] = null;
		strings[112] = null;
		strings[113] = "minecraft:regeneration";
		strings[114] = "minecraft:swiftness";
		strings[115] = "minecraft:long_fire_resistance";
		strings[116] = "minecraft:poison";
		strings[117] = "minecraft:strong_healing";
		strings[118] = "minecraft:long_night_vision";
		strings[119] = null;
		strings[120] = "minecraft:long_weakness";
		strings[121] = "minecraft:strength";
		strings[122] = "minecraft:long_slowness";
		strings[123] = "minecraft:leaping";
		strings[124] = "minecraft:strong_harming";
		strings[125] = "minecraft:long_water_breathing";
		strings[126] = "minecraft:long_invisibility";
		strings[127] = null;
	});
	public static final String DEFAULT = "minecraft:water";

	public ItemPotionFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	@Override
	public TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
		OpticFinder<Pair<String, String>> opticFinder = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
		OpticFinder<?> opticFinder2 = type.findField("tag");
		return this.fixTypeEverywhereTyped(
			"ItemPotionFix",
			type,
			typed -> {
				Optional<Pair<String, String>> optional = typed.getOptional(opticFinder);
				if (optional.isPresent() && Objects.equals(((Pair)optional.get()).getSecond(), "minecraft:potion")) {
					Dynamic<?> dynamic = typed.get(DSL.remainderFinder());
					Optional<? extends Typed<?>> optional2 = typed.getOptionalTyped(opticFinder2);
					short s = dynamic.get("Damage").asShort((short)0);
					if (optional2.isPresent()) {
						Typed<?> typed2 = typed;
						Dynamic<?> dynamic2 = ((Typed)optional2.get()).get(DSL.remainderFinder());
						Optional<String> optional3 = dynamic2.get("Potion").asString().result();
						if (optional3.isEmpty()) {
							String string = POTIONS[s & 127];
							Typed<?> typed3 = ((Typed)optional2.get())
								.set(DSL.remainderFinder(), dynamic2.set("Potion", dynamic2.createString(string == null ? "minecraft:water" : string)));
							typed2 = typed.set(opticFinder2, typed3);
							if ((s & 16384) == 16384) {
								typed2 = typed2.set(opticFinder, Pair.of(References.ITEM_NAME.typeName(), "minecraft:splash_potion"));
							}
						}

						if (s != 0) {
							dynamic = dynamic.set("Damage", dynamic.createShort((short)0));
						}

						return typed2.set(DSL.remainderFinder(), dynamic);
					}
				}

				return typed;
			}
		);
	}
}
