package net.minecraft.world.damagesource;

import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class DamageSource {
	private final Holder<DamageType> type;
	@Nullable
	private final Entity causingEntity;
	@Nullable
	private final Entity directEntity;
	@Nullable
	private final Vec3 damageSourcePosition;

	public String toString() {
		return "DamageSource (" + this.type().msgId() + ")";
	}

	public float getFoodExhaustion() {
		return this.type().exhaustion();
	}

	public boolean isDirect() {
		return this.causingEntity == this.directEntity;
	}

	private DamageSource(Holder<DamageType> holder, @Nullable Entity entity, @Nullable Entity entity2, @Nullable Vec3 vec3) {
		this.type = holder;
		this.causingEntity = entity2;
		this.directEntity = entity;
		this.damageSourcePosition = vec3;
	}

	public DamageSource(Holder<DamageType> holder, @Nullable Entity entity, @Nullable Entity entity2) {
		this(holder, entity, entity2, null);
	}

	public DamageSource(Holder<DamageType> holder, Vec3 vec3) {
		this(holder, null, null, vec3);
	}

	public DamageSource(Holder<DamageType> holder, @Nullable Entity entity) {
		this(holder, entity, entity);
	}

	public DamageSource(Holder<DamageType> holder) {
		this(holder, null, null, null);
	}

	@Nullable
	public Entity getDirectEntity() {
		return this.directEntity;
	}

	@Nullable
	public Entity getEntity() {
		return this.causingEntity;
	}

	public Component getLocalizedDeathMessage(LivingEntity livingEntity) {
		String string = "death.attack." + this.type().msgId();
		if (this.causingEntity == null && this.directEntity == null) {
			LivingEntity livingEntity3 = livingEntity.getKillCredit();
			String string2 = string + ".player";
			return livingEntity3 != null
				? Component.translatable(string2, livingEntity.getDisplayName(), livingEntity3.getDisplayName())
				: Component.translatable(string, livingEntity.getDisplayName());
		} else {
			Component component = this.causingEntity == null ? this.directEntity.getDisplayName() : this.causingEntity.getDisplayName();
			ItemStack itemStack = this.causingEntity instanceof LivingEntity livingEntity2 ? livingEntity2.getMainHandItem() : ItemStack.EMPTY;
			return !itemStack.isEmpty() && itemStack.has(DataComponents.CUSTOM_NAME)
				? Component.translatable(string + ".item", livingEntity.getDisplayName(), component, itemStack.getDisplayName())
				: Component.translatable(string, livingEntity.getDisplayName(), component);
		}
	}

	public String getMsgId() {
		return this.type().msgId();
	}

	public boolean scalesWithDifficulty() {
		return switch (this.type().scaling()) {
			case NEVER -> false;
			case WHEN_CAUSED_BY_LIVING_NON_PLAYER -> this.causingEntity instanceof LivingEntity && !(this.causingEntity instanceof Player);
			case ALWAYS -> true;
		};
	}

	public boolean isCreativePlayer() {
		if (this.getEntity() instanceof Player player && player.getAbilities().instabuild) {
			return true;
		}

		return false;
	}

	@Nullable
	public Vec3 getSourcePosition() {
		if (this.damageSourcePosition != null) {
			return this.damageSourcePosition;
		} else {
			return this.directEntity != null ? this.directEntity.position() : null;
		}
	}

	@Nullable
	public Vec3 sourcePositionRaw() {
		return this.damageSourcePosition;
	}

	public boolean is(TagKey<DamageType> tagKey) {
		return this.type.is(tagKey);
	}

	public boolean is(ResourceKey<DamageType> resourceKey) {
		return this.type.is(resourceKey);
	}

	public DamageType type() {
		return this.type.value();
	}

	public Holder<DamageType> typeHolder() {
		return this.type;
	}
}
