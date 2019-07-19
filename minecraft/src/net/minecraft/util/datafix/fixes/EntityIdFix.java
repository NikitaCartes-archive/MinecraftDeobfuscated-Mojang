package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import java.util.Map;

public class EntityIdFix extends DataFix {
	private static final Map<String, String> ID_MAP = DataFixUtils.make(Maps.<String, String>newHashMap(), hashMap -> {
		hashMap.put("AreaEffectCloud", "minecraft:area_effect_cloud");
		hashMap.put("ArmorStand", "minecraft:armor_stand");
		hashMap.put("Arrow", "minecraft:arrow");
		hashMap.put("Bat", "minecraft:bat");
		hashMap.put("Blaze", "minecraft:blaze");
		hashMap.put("Boat", "minecraft:boat");
		hashMap.put("CaveSpider", "minecraft:cave_spider");
		hashMap.put("Chicken", "minecraft:chicken");
		hashMap.put("Cow", "minecraft:cow");
		hashMap.put("Creeper", "minecraft:creeper");
		hashMap.put("Donkey", "minecraft:donkey");
		hashMap.put("DragonFireball", "minecraft:dragon_fireball");
		hashMap.put("ElderGuardian", "minecraft:elder_guardian");
		hashMap.put("EnderCrystal", "minecraft:ender_crystal");
		hashMap.put("EnderDragon", "minecraft:ender_dragon");
		hashMap.put("Enderman", "minecraft:enderman");
		hashMap.put("Endermite", "minecraft:endermite");
		hashMap.put("EyeOfEnderSignal", "minecraft:eye_of_ender_signal");
		hashMap.put("FallingSand", "minecraft:falling_block");
		hashMap.put("Fireball", "minecraft:fireball");
		hashMap.put("FireworksRocketEntity", "minecraft:fireworks_rocket");
		hashMap.put("Ghast", "minecraft:ghast");
		hashMap.put("Giant", "minecraft:giant");
		hashMap.put("Guardian", "minecraft:guardian");
		hashMap.put("Horse", "minecraft:horse");
		hashMap.put("Husk", "minecraft:husk");
		hashMap.put("Item", "minecraft:item");
		hashMap.put("ItemFrame", "minecraft:item_frame");
		hashMap.put("LavaSlime", "minecraft:magma_cube");
		hashMap.put("LeashKnot", "minecraft:leash_knot");
		hashMap.put("MinecartChest", "minecraft:chest_minecart");
		hashMap.put("MinecartCommandBlock", "minecraft:commandblock_minecart");
		hashMap.put("MinecartFurnace", "minecraft:furnace_minecart");
		hashMap.put("MinecartHopper", "minecraft:hopper_minecart");
		hashMap.put("MinecartRideable", "minecraft:minecart");
		hashMap.put("MinecartSpawner", "minecraft:spawner_minecart");
		hashMap.put("MinecartTNT", "minecraft:tnt_minecart");
		hashMap.put("Mule", "minecraft:mule");
		hashMap.put("MushroomCow", "minecraft:mooshroom");
		hashMap.put("Ozelot", "minecraft:ocelot");
		hashMap.put("Painting", "minecraft:painting");
		hashMap.put("Pig", "minecraft:pig");
		hashMap.put("PigZombie", "minecraft:zombie_pigman");
		hashMap.put("PolarBear", "minecraft:polar_bear");
		hashMap.put("PrimedTnt", "minecraft:tnt");
		hashMap.put("Rabbit", "minecraft:rabbit");
		hashMap.put("Sheep", "minecraft:sheep");
		hashMap.put("Shulker", "minecraft:shulker");
		hashMap.put("ShulkerBullet", "minecraft:shulker_bullet");
		hashMap.put("Silverfish", "minecraft:silverfish");
		hashMap.put("Skeleton", "minecraft:skeleton");
		hashMap.put("SkeletonHorse", "minecraft:skeleton_horse");
		hashMap.put("Slime", "minecraft:slime");
		hashMap.put("SmallFireball", "minecraft:small_fireball");
		hashMap.put("SnowMan", "minecraft:snowman");
		hashMap.put("Snowball", "minecraft:snowball");
		hashMap.put("SpectralArrow", "minecraft:spectral_arrow");
		hashMap.put("Spider", "minecraft:spider");
		hashMap.put("Squid", "minecraft:squid");
		hashMap.put("Stray", "minecraft:stray");
		hashMap.put("ThrownEgg", "minecraft:egg");
		hashMap.put("ThrownEnderpearl", "minecraft:ender_pearl");
		hashMap.put("ThrownExpBottle", "minecraft:xp_bottle");
		hashMap.put("ThrownPotion", "minecraft:potion");
		hashMap.put("Villager", "minecraft:villager");
		hashMap.put("VillagerGolem", "minecraft:villager_golem");
		hashMap.put("Witch", "minecraft:witch");
		hashMap.put("WitherBoss", "minecraft:wither");
		hashMap.put("WitherSkeleton", "minecraft:wither_skeleton");
		hashMap.put("WitherSkull", "minecraft:wither_skull");
		hashMap.put("Wolf", "minecraft:wolf");
		hashMap.put("XPOrb", "minecraft:xp_orb");
		hashMap.put("Zombie", "minecraft:zombie");
		hashMap.put("ZombieHorse", "minecraft:zombie_horse");
		hashMap.put("ZombieVillager", "minecraft:zombie_villager");
	});

	public EntityIdFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	@Override
	public TypeRewriteRule makeRule() {
		TaggedChoiceType<String> taggedChoiceType = (TaggedChoiceType<String>)this.getInputSchema().findChoiceType(References.ENTITY);
		TaggedChoiceType<String> taggedChoiceType2 = (TaggedChoiceType<String>)this.getOutputSchema().findChoiceType(References.ENTITY);
		Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
		Type<?> type2 = this.getOutputSchema().getType(References.ITEM_STACK);
		return TypeRewriteRule.seq(
			this.convertUnchecked("item stack entity name hook converter", type, type2),
			this.fixTypeEverywhere(
				"EntityIdFix", taggedChoiceType, taggedChoiceType2, dynamicOps -> pair -> pair.mapFirst(string -> (String)ID_MAP.getOrDefault(string, string))
			)
		);
	}
}
