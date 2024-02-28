package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public record EntityTypePredicate(HolderSet<EntityType<?>> types) {
	public static final Codec<EntityTypePredicate> CODEC = RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE)
		.xmap(EntityTypePredicate::new, EntityTypePredicate::types);

	public static EntityTypePredicate of(EntityType<?> entityType) {
		return new EntityTypePredicate(HolderSet.direct(entityType.builtInRegistryHolder()));
	}

	public static EntityTypePredicate of(TagKey<EntityType<?>> tagKey) {
		return new EntityTypePredicate(BuiltInRegistries.ENTITY_TYPE.getOrCreateTag(tagKey));
	}

	public boolean matches(EntityType<?> entityType) {
		return entityType.is(this.types);
	}
}
