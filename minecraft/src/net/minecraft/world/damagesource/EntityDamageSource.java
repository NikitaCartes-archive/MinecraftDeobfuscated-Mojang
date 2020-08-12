package net.minecraft.world.damagesource;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class EntityDamageSource extends DamageSource {
	@Nullable
	protected final Entity entity;
	private boolean isThorns;
	private boolean isCritSpecial;

	public EntityDamageSource(String string, @Nullable Entity entity) {
		super(string);
		this.entity = entity;
	}

	public EntityDamageSource setThorns() {
		this.isThorns = true;
		return this;
	}

	public EntityDamageSource setCritSpecial(boolean bl) {
		this.isCritSpecial = bl;
		return this;
	}

	public boolean isThorns() {
		return this.isThorns;
	}

	@Nullable
	@Override
	public Entity getEntity() {
		return this.entity;
	}

	@Override
	public Component getLocalizedDeathMessage(LivingEntity livingEntity) {
		ItemStack itemStack = this.entity instanceof LivingEntity ? ((LivingEntity)this.entity).getMainHandItem() : ItemStack.EMPTY;
		String string = "death.attack." + this.msgId;
		return !itemStack.isEmpty() && itemStack.hasCustomHoverName()
			? new TranslatableComponent(string + ".item", livingEntity.getDisplayName(), this.entity.getDisplayName(), itemStack.getDisplayName())
			: new TranslatableComponent(string, livingEntity.getDisplayName(), this.entity.getDisplayName());
	}

	@Override
	public boolean scalesWithDifficulty() {
		return this.entity != null && this.entity instanceof LivingEntity && !(this.entity instanceof Player);
	}

	@Nullable
	@Override
	public Vec3 getSourcePosition() {
		return this.entity != null ? this.entity.position() : null;
	}

	@Override
	public String toString() {
		return "EntityDamageSource (" + this.entity + ")";
	}
}
