/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.damagesource;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class IndirectEntityDamageSource
extends EntityDamageSource {
    @Nullable
    private final Entity cause;

    public IndirectEntityDamageSource(String string, Entity entity, @Nullable Entity entity2) {
        super(string, entity);
        this.cause = entity2;
    }

    @Override
    @Nullable
    public Entity getDirectEntity() {
        return this.entity;
    }

    @Override
    @Nullable
    public Entity getEntity() {
        return this.cause;
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity livingEntity) {
        ItemStack itemStack;
        Component component = this.cause == null ? this.entity.getDisplayName() : this.cause.getDisplayName();
        Entity entity = this.cause;
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity2 = (LivingEntity)entity;
            itemStack = livingEntity2.getMainHandItem();
        } else {
            itemStack = ItemStack.EMPTY;
        }
        ItemStack itemStack2 = itemStack;
        String string = "death.attack." + this.msgId;
        if (!itemStack2.isEmpty() && itemStack2.hasCustomHoverName()) {
            String string2 = string + ".item";
            return Component.translatable(string2, livingEntity.getDisplayName(), component, itemStack2.getDisplayName());
        }
        return Component.translatable(string, livingEntity.getDisplayName(), component);
    }
}

