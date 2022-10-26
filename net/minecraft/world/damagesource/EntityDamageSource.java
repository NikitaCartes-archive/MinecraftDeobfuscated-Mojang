/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.damagesource;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class EntityDamageSource
extends DamageSource {
    protected final Entity entity;
    private boolean isThorns;

    public EntityDamageSource(String string, Entity entity) {
        super(string);
        this.entity = entity;
    }

    public EntityDamageSource setThorns() {
        this.isThorns = true;
        return this;
    }

    public boolean isThorns() {
        return this.isThorns;
    }

    @Override
    public Entity getEntity() {
        return this.entity;
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity livingEntity) {
        ItemStack itemStack;
        Entity entity = this.entity;
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity2 = (LivingEntity)entity;
            itemStack = livingEntity2.getMainHandItem();
        } else {
            itemStack = ItemStack.EMPTY;
        }
        ItemStack itemStack2 = itemStack;
        String string = "death.attack." + this.msgId;
        if (!itemStack2.isEmpty() && itemStack2.hasCustomHoverName()) {
            return Component.translatable(string + ".item", livingEntity.getDisplayName(), this.entity.getDisplayName(), itemStack2.getDisplayName());
        }
        return Component.translatable(string, livingEntity.getDisplayName(), this.entity.getDisplayName());
    }

    @Override
    public boolean scalesWithDifficulty() {
        return this.entity instanceof LivingEntity && !(this.entity instanceof Player);
    }

    @Override
    @Nullable
    public Vec3 getSourcePosition() {
        return this.entity.position();
    }

    @Override
    public String toString() {
        return "EntityDamageSource (" + this.entity + ")";
    }
}

