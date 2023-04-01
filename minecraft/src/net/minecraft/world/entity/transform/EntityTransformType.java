package net.minecraft.world.entity.transform;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public record EntityTransformType(Optional<EntityTransformType.TypeModifier> entity, float scale, Optional<GameProfile> playerSkin) {
	public static final EntityTransformType IDENTITY = new EntityTransformType(Optional.empty(), 1.0F, Optional.empty());
	public static final float MIN_SCALE = 0.1F;
	public static final float MAX_SCALE = 16.0F;
	public static final Codec<EntityTransformType> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					EntityTransformType.TypeModifier.CODEC.optionalFieldOf("entity").forGetter(EntityTransformType::entity),
					ExtraCodecs.floatRange(0.09999F, 16.0F).optionalFieldOf("scale", 1.0F).forGetter(EntityTransformType::scale),
					ExtraCodecs.GAME_PROFILE.optionalFieldOf("player_skin").forGetter(EntityTransformType::playerSkin)
				)
				.apply(instance, EntityTransformType::new)
	);

	public EntityTransformType(Optional<EntityTransformType.TypeModifier> entity, float scale, Optional<GameProfile> playerSkin) {
		scale = Mth.clamp(scale, 0.1F, 16.0F);
		this.entity = entity;
		this.scale = scale;
		this.playerSkin = playerSkin;
	}

	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeWithCodec(NbtOps.INSTANCE, CODEC, this);
	}

	public static EntityTransformType read(FriendlyByteBuf friendlyByteBuf) {
		return friendlyByteBuf.readWithCodec(NbtOps.INSTANCE, CODEC);
	}

	public EntityTransform create(LivingEntity livingEntity) {
		Entity entity = (Entity)this.entity.map(typeModifier -> typeModifier.create(livingEntity)).orElse(null);
		return new EntityTransform(this, entity, (GameProfile)this.playerSkin.orElse(null));
	}

	public EntityTransformType withEntity(EntityType<?> entityType, Optional<CompoundTag> optional) {
		return this.withEntity(Optional.of(new EntityTransformType.TypeModifier(entityType, optional)));
	}

	public EntityTransformType withEntity(Optional<EntityTransformType.TypeModifier> optional) {
		return new EntityTransformType(optional, this.scale, this.playerSkin);
	}

	public EntityTransformType withScale(float f) {
		return new EntityTransformType(this.entity, f, this.playerSkin);
	}

	public EntityTransformType withPlayerSkin(Optional<GameProfile> optional) {
		return new EntityTransformType(this.entity, this.scale, optional);
	}

	public boolean isIdentity() {
		return this.entity.isEmpty() && Math.abs(this.scale() - 1.0F) < 1.0E-5F && this.playerSkin.isEmpty();
	}

	public static record TypeModifier(EntityType<?> type, Optional<CompoundTag> tag) {
		public static final Codec<EntityTransformType.TypeModifier> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter(EntityTransformType.TypeModifier::type),
						CompoundTag.CODEC.optionalFieldOf("tag").forGetter(EntityTransformType.TypeModifier::tag)
					)
					.apply(instance, EntityTransformType.TypeModifier::new)
		);

		@Nullable
		public Entity create(LivingEntity livingEntity) {
			Entity entity = this.type.create(livingEntity.level);
			if (entity == null) {
				return null;
			} else {
				this.tag.ifPresent(entity::load);
				return entity;
			}
		}
	}
}
