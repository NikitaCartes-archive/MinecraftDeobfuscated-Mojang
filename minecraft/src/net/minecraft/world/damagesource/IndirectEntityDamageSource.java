package net.minecraft.world.damagesource;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class IndirectEntityDamageSource extends EntityDamageSource {
	@Nullable
	private final Entity cause;

	public IndirectEntityDamageSource(String string, Entity entity, @Nullable Entity entity2) {
		super(string, entity);
		this.cause = entity2;
	}

	@Nullable
	@Override
	public Entity getDirectEntity() {
		return this.entity;
	}

	@Nullable
	@Override
	public Entity getEntity() {
		return this.cause;
	}

	@Override
	public Component getLocalizedDeathMessage(LivingEntity livingEntity) {
		Component component = this.cause == null ? this.entity.getDisplayName() : this.cause.getDisplayName();
		ItemStack itemStack = this.cause instanceof LivingEntity livingEntity2 ? livingEntity2.getMainHandItem() : ItemStack.EMPTY;
		String string = "death.attack." + this.msgId;
		if (!itemStack.isEmpty() && itemStack.hasCustomHoverName()) {
			String string2 = string + ".item";
			return Component.translatable(string2, livingEntity.getDisplayName(), component, itemStack.getDisplayName());
		} else {
			return Component.translatable(string, livingEntity.getDisplayName(), component);
		}
	}
}
