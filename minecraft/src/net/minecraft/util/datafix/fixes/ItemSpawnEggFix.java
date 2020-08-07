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

public class ItemSpawnEggFix extends DataFix {
	private static final String[] ID_TO_ENTITY = DataFixUtils.make(new String[256], strings -> {
		strings[1] = "Item";
		strings[2] = "XPOrb";
		strings[7] = "ThrownEgg";
		strings[8] = "LeashKnot";
		strings[9] = "Painting";
		strings[10] = "Arrow";
		strings[11] = "Snowball";
		strings[12] = "Fireball";
		strings[13] = "SmallFireball";
		strings[14] = "ThrownEnderpearl";
		strings[15] = "EyeOfEnderSignal";
		strings[16] = "ThrownPotion";
		strings[17] = "ThrownExpBottle";
		strings[18] = "ItemFrame";
		strings[19] = "WitherSkull";
		strings[20] = "PrimedTnt";
		strings[21] = "FallingSand";
		strings[22] = "FireworksRocketEntity";
		strings[23] = "TippedArrow";
		strings[24] = "SpectralArrow";
		strings[25] = "ShulkerBullet";
		strings[26] = "DragonFireball";
		strings[30] = "ArmorStand";
		strings[41] = "Boat";
		strings[42] = "MinecartRideable";
		strings[43] = "MinecartChest";
		strings[44] = "MinecartFurnace";
		strings[45] = "MinecartTNT";
		strings[46] = "MinecartHopper";
		strings[47] = "MinecartSpawner";
		strings[40] = "MinecartCommandBlock";
		strings[48] = "Mob";
		strings[49] = "Monster";
		strings[50] = "Creeper";
		strings[51] = "Skeleton";
		strings[52] = "Spider";
		strings[53] = "Giant";
		strings[54] = "Zombie";
		strings[55] = "Slime";
		strings[56] = "Ghast";
		strings[57] = "PigZombie";
		strings[58] = "Enderman";
		strings[59] = "CaveSpider";
		strings[60] = "Silverfish";
		strings[61] = "Blaze";
		strings[62] = "LavaSlime";
		strings[63] = "EnderDragon";
		strings[64] = "WitherBoss";
		strings[65] = "Bat";
		strings[66] = "Witch";
		strings[67] = "Endermite";
		strings[68] = "Guardian";
		strings[69] = "Shulker";
		strings[90] = "Pig";
		strings[91] = "Sheep";
		strings[92] = "Cow";
		strings[93] = "Chicken";
		strings[94] = "Squid";
		strings[95] = "Wolf";
		strings[96] = "MushroomCow";
		strings[97] = "SnowMan";
		strings[98] = "Ozelot";
		strings[99] = "VillagerGolem";
		strings[100] = "EntityHorse";
		strings[101] = "Rabbit";
		strings[120] = "Villager";
		strings[200] = "EnderCrystal";
	});

	public ItemSpawnEggFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	@Override
	public TypeRewriteRule makeRule() {
		Schema schema = this.getInputSchema();
		Type<?> type = schema.getType(References.ITEM_STACK);
		OpticFinder<Pair<String, String>> opticFinder = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
		OpticFinder<String> opticFinder2 = DSL.fieldFinder("id", DSL.string());
		OpticFinder<?> opticFinder3 = type.findField("tag");
		OpticFinder<?> opticFinder4 = opticFinder3.type().findField("EntityTag");
		OpticFinder<?> opticFinder5 = DSL.typeFinder(schema.getTypeRaw(References.ENTITY));
		Type<?> type2 = this.getOutputSchema().getTypeRaw(References.ENTITY);
		return this.fixTypeEverywhereTyped(
			"ItemSpawnEggFix",
			type,
			typed -> {
				Optional<Pair<String, String>> optional = typed.getOptional(opticFinder);
				if (optional.isPresent() && Objects.equals(((Pair)optional.get()).getSecond(), "minecraft:spawn_egg")) {
					Dynamic<?> dynamic = typed.get(DSL.remainderFinder());
					short s = dynamic.get("Damage").asShort((short)0);
					Optional<? extends Typed<?>> optional2 = typed.getOptionalTyped(opticFinder3);
					Optional<? extends Typed<?>> optional3 = optional2.flatMap(typedx -> typedx.getOptionalTyped(opticFinder4));
					Optional<? extends Typed<?>> optional4 = optional3.flatMap(typedx -> typedx.getOptionalTyped(opticFinder5));
					Optional<String> optional5 = optional4.flatMap(typedx -> typedx.getOptional(opticFinder2));
					Typed<?> typed2 = typed;
					String string = ID_TO_ENTITY[s & 255];
					if (string != null && (!optional5.isPresent() || !Objects.equals(optional5.get(), string))) {
						Typed<?> typed3 = typed.getOrCreateTyped(opticFinder3);
						Typed<?> typed4 = typed3.getOrCreateTyped(opticFinder4);
						Typed<?> typed5 = typed4.getOrCreateTyped(opticFinder5);
						Typed<?> typed6 = (Typed<?>)((Pair)typed5.write()
								.flatMap(dynamic2 -> type2.readTyped(dynamic2.set("id", dynamic.createString(string))))
								.result()
								.orElseThrow(() -> new IllegalStateException("Could not parse new entity")))
							.getFirst();
						typed2 = typed.set(opticFinder3, typed3.set(opticFinder4, typed4.set(opticFinder5, typed6)));
					}

					if (s != 0) {
						dynamic = dynamic.set("Damage", dynamic.createShort((short)0));
						typed2 = typed2.set(DSL.remainderFinder(), dynamic);
					}

					return typed2;
				} else {
					return typed;
				}
			}
		);
	}
}
