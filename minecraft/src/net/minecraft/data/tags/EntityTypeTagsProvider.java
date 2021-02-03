package net.minecraft.data.tags;

import java.nio.file.Path;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;

public class EntityTypeTagsProvider extends TagsProvider<EntityType<?>> {
	public EntityTypeTagsProvider(DataGenerator dataGenerator) {
		super(dataGenerator, Registry.ENTITY_TYPE);
	}

	@Override
	protected void addTags() {
		this.tag(EntityTypeTags.SKELETONS).add(EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON);
		this.tag(EntityTypeTags.RAIDERS)
			.add(EntityType.EVOKER, EntityType.PILLAGER, EntityType.RAVAGER, EntityType.VINDICATOR, EntityType.ILLUSIONER, EntityType.WITCH);
		this.tag(EntityTypeTags.BEEHIVE_INHABITORS).add(EntityType.BEE);
		this.tag(EntityTypeTags.ARROWS).add(EntityType.ARROW, EntityType.SPECTRAL_ARROW);
		this.tag(EntityTypeTags.IMPACT_PROJECTILES)
			.addTag(EntityTypeTags.ARROWS)
			.add(
				EntityType.SNOWBALL,
				EntityType.FIREBALL,
				EntityType.SMALL_FIREBALL,
				EntityType.EGG,
				EntityType.TRIDENT,
				EntityType.DRAGON_FIREBALL,
				EntityType.WITHER_SKULL
			);
		this.tag(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS).add(EntityType.RABBIT, EntityType.ENDERMITE, EntityType.SILVERFISH, EntityType.FOX);
		this.tag(EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES)
			.add(EntityType.TROPICAL_FISH, EntityType.PUFFERFISH, EntityType.SALMON, EntityType.COD, EntityType.SQUID, EntityType.GLOW_SQUID);
		this.tag(EntityTypeTags.AXOLOTL_TEMPTED_HOSTILES).add(EntityType.DROWNED, EntityType.GUARDIAN);
	}

	@Override
	protected Path getPath(ResourceLocation resourceLocation) {
		return this.generator.getOutputFolder().resolve("data/" + resourceLocation.getNamespace() + "/tags/entity_types/" + resourceLocation.getPath() + ".json");
	}

	@Override
	public String getName() {
		return "Entity Type Tags";
	}
}
