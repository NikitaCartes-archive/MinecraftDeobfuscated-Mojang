package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public record EntityTypePredicate(HolderSet<EntityType<?>> types) {
	public static final Codec<EntityTypePredicate> CODEC = RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE)
		.xmap(EntityTypePredicate::new, EntityTypePredicate::types);

	public static EntityTypePredicate of(HolderGetter<EntityType<?>> holderGetter, EntityType<?> entityType) {
		return new EntityTypePredicate(HolderSet.direct(entityType.builtInRegistryHolder()));
	}

	public static EntityTypePredicate of(HolderGetter<EntityType<?>> holderGetter, TagKey<EntityType<?>> tagKey) {
		return new EntityTypePredicate(holderGetter.getOrThrow(tagKey));
	}

	public boolean matches(EntityType<?> entityType) {
		return entityType.is(this.types);
	}
}
