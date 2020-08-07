package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Sets;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.Set;

public class EntityHealthFix extends DataFix {
	private static final Set<String> ENTITIES = Sets.<String>newHashSet(
		"ArmorStand",
		"Bat",
		"Blaze",
		"CaveSpider",
		"Chicken",
		"Cow",
		"Creeper",
		"EnderDragon",
		"Enderman",
		"Endermite",
		"EntityHorse",
		"Ghast",
		"Giant",
		"Guardian",
		"LavaSlime",
		"MushroomCow",
		"Ozelot",
		"Pig",
		"PigZombie",
		"Rabbit",
		"Sheep",
		"Shulker",
		"Silverfish",
		"Skeleton",
		"Slime",
		"SnowMan",
		"Spider",
		"Squid",
		"Villager",
		"VillagerGolem",
		"Witch",
		"WitherBoss",
		"Wolf",
		"Zombie"
	);

	public EntityHealthFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	public Dynamic<?> fixTag(Dynamic<?> dynamic) {
		Optional<Number> optional = dynamic.get("HealF").asNumber().result();
		Optional<Number> optional2 = dynamic.get("Health").asNumber().result();
		float f;
		if (optional.isPresent()) {
			f = ((Number)optional.get()).floatValue();
			dynamic = dynamic.remove("HealF");
		} else {
			if (!optional2.isPresent()) {
				return dynamic;
			}

			f = ((Number)optional2.get()).floatValue();
		}

		return dynamic.set("Health", dynamic.createFloat(f));
	}

	@Override
	public TypeRewriteRule makeRule() {
		return this.fixTypeEverywhereTyped(
			"EntityHealthFix", this.getInputSchema().getType(References.ENTITY), typed -> typed.update(DSL.remainderFinder(), this::fixTag)
		);
	}
}
