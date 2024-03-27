package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.phys.Vec3;

public record DamageSourcePredicate(List<TagPredicate<DamageType>> tags, Optional<EntityPredicate> directEntity, Optional<EntityPredicate> sourceEntity) {
	public static final Codec<DamageSourcePredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					TagPredicate.codec(Registries.DAMAGE_TYPE).listOf().optionalFieldOf("tags", List.of()).forGetter(DamageSourcePredicate::tags),
					EntityPredicate.CODEC.optionalFieldOf("direct_entity").forGetter(DamageSourcePredicate::directEntity),
					EntityPredicate.CODEC.optionalFieldOf("source_entity").forGetter(DamageSourcePredicate::sourceEntity)
				)
				.apply(instance, DamageSourcePredicate::new)
	);

	public boolean matches(ServerPlayer serverPlayer, DamageSource damageSource) {
		return this.matches(serverPlayer.serverLevel(), serverPlayer.position(), damageSource);
	}

	public boolean matches(ServerLevel serverLevel, Vec3 vec3, DamageSource damageSource) {
		for (TagPredicate<DamageType> tagPredicate : this.tags) {
			if (!tagPredicate.matches(damageSource.typeHolder())) {
				return false;
			}
		}

		return this.directEntity.isPresent() && !((EntityPredicate)this.directEntity.get()).matches(serverLevel, vec3, damageSource.getDirectEntity())
			? false
			: !this.sourceEntity.isPresent() || ((EntityPredicate)this.sourceEntity.get()).matches(serverLevel, vec3, damageSource.getEntity());
	}

	public static class Builder {
		private final ImmutableList.Builder<TagPredicate<DamageType>> tags = ImmutableList.builder();
		private Optional<EntityPredicate> directEntity = Optional.empty();
		private Optional<EntityPredicate> sourceEntity = Optional.empty();

		public static DamageSourcePredicate.Builder damageType() {
			return new DamageSourcePredicate.Builder();
		}

		public DamageSourcePredicate.Builder tag(TagPredicate<DamageType> tagPredicate) {
			this.tags.add(tagPredicate);
			return this;
		}

		public DamageSourcePredicate.Builder direct(EntityPredicate.Builder builder) {
			this.directEntity = Optional.of(builder.build());
			return this;
		}

		public DamageSourcePredicate.Builder source(EntityPredicate.Builder builder) {
			this.sourceEntity = Optional.of(builder.build());
			return this;
		}

		public DamageSourcePredicate build() {
			return new DamageSourcePredicate(this.tags.build(), this.directEntity, this.sourceEntity);
		}
	}
}
