package net.minecraft.data.tags;

import java.nio.file.Path;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagCollection;
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
	}

	@Override
	protected Path getPath(ResourceLocation resourceLocation) {
		return this.generator.getOutputFolder().resolve("data/" + resourceLocation.getNamespace() + "/tags/entity_types/" + resourceLocation.getPath() + ".json");
	}

	@Override
	public String getName() {
		return "Entity Type Tags";
	}

	@Override
	protected void useTags(TagCollection<EntityType<?>> tagCollection) {
		EntityTypeTags.reset(tagCollection);
	}
}
