package net.minecraft.world.damagesource;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class IndirectEntityDamageSource extends EntityDamageSource {
	@Nullable
	private final Entity owner;

	public IndirectEntityDamageSource(String string, Entity entity, @Nullable Entity entity2) {
		super(string, entity);
		this.owner = entity2;
	}

	@Nullable
	@Override
	public Entity getDirectEntity() {
		return this.entity;
	}

	@Nullable
	@Override
	public Entity getEntity() {
		return this.owner;
	}

	@Override
	public Component getLocalizedDeathMessage(LivingEntity livingEntity) {
		Component component = this.owner == null ? this.entity.getDisplayName() : this.owner.getDisplayName();
		ItemStack itemStack = this.owner instanceof LivingEntity ? ((LivingEntity)this.owner).getMainHandItem() : ItemStack.EMPTY;
		String string = "death.attack." + this.msgId;
		String string2 = string + ".item";
		return !itemStack.isEmpty() && itemStack.hasCustomHoverName()
			? new TranslatableComponent(string2, livingEntity.getDisplayName(), component, itemStack.getDisplayName())
			: new TranslatableComponent(string, livingEntity.getDisplayName(), component);
	}
}
