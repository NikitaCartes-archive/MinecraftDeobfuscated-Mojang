/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.damagesource;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class IndirectEntityDamageSource
extends EntityDamageSource {
    private final Entity owner;

    public IndirectEntityDamageSource(String string, Entity entity, @Nullable Entity entity2) {
        super(string, entity);
        this.owner = entity2;
    }

    @Override
    @Nullable
    public Entity getDirectEntity() {
        return this.entity;
    }

    @Override
    @Nullable
    public Entity getEntity() {
        return this.owner;
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity livingEntity) {
        Component component = this.owner == null ? this.entity.getDisplayName() : this.owner.getDisplayName();
        ItemStack itemStack = this.owner instanceof LivingEntity ? ((LivingEntity)this.owner).getMainHandItem() : ItemStack.EMPTY;
        String string = "death.attack." + this.msgId;
        String string2 = string + ".item";
        if (!itemStack.isEmpty() && itemStack.hasCustomHoverName()) {
            return new TranslatableComponent(string2, livingEntity.getDisplayName(), component, itemStack.getDisplayName());
        }
        return new TranslatableComponent(string, livingEntity.getDisplayName(), component);
    }
}

