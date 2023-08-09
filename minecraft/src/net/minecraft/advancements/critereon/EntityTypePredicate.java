package net.minecraft.advancements.critereon;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Optional;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public record EntityTypePredicate(HolderSet<EntityType<?>> types) {
	public static final Codec<EntityTypePredicate> CODEC = Codec.either(
			TagKey.hashedCodec(Registries.ENTITY_TYPE), BuiltInRegistries.ENTITY_TYPE.holderByNameCodec()
		)
		.flatComapMap(
			either -> either.map(
					tagKey -> new EntityTypePredicate(BuiltInRegistries.ENTITY_TYPE.getOrCreateTag(tagKey)), holder -> new EntityTypePredicate(HolderSet.direct(holder))
				),
			entityTypePredicate -> {
				HolderSet<EntityType<?>> holderSet = entityTypePredicate.types();
				Optional<TagKey<EntityType<?>>> optional = holderSet.unwrapKey();
				if (optional.isPresent()) {
					return DataResult.success(Either.left((TagKey)optional.get()));
				} else {
					return holderSet.size() == 1
						? DataResult.success(Either.right(holderSet.get(0)))
						: DataResult.error(() -> "Entity type set must have a single element, but got " + holderSet.size());
				}
			}
		);

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
