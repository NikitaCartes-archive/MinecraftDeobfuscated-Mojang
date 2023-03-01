/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.damagesource;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

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

    public boolean isIndirect() {
        return this.causingEntity != this.directEntity;
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
        if (this.causingEntity != null || this.directEntity != null) {
            ItemStack itemStack;
            Component component = this.causingEntity == null ? this.directEntity.getDisplayName() : this.causingEntity.getDisplayName();
            Entity entity = this.causingEntity;
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity2 = (LivingEntity)entity;
                v0 = livingEntity2.getMainHandItem();
            } else {
                v0 = itemStack = ItemStack.EMPTY;
            }
            if (!itemStack.isEmpty() && itemStack.hasCustomHoverName()) {
                return Component.translatable(string + ".item", livingEntity.getDisplayName(), component, itemStack.getDisplayName());
            }
            return Component.translatable(string, livingEntity.getDisplayName(), component);
        }
        LivingEntity livingEntity3 = livingEntity.getKillCredit();
        String string2 = string + ".player";
        if (livingEntity3 != null) {
            return Component.translatable(string2, livingEntity.getDisplayName(), livingEntity3.getDisplayName());
        }
        return Component.translatable(string, livingEntity.getDisplayName());
    }

    public String getMsgId() {
        return this.type().msgId();
    }

    public boolean scalesWithDifficulty() {
        return switch (this.type().scaling()) {
            default -> throw new IncompatibleClassChangeError();
            case DamageScaling.NEVER -> false;
            case DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER -> {
                if (this.causingEntity instanceof LivingEntity && !(this.causingEntity instanceof Player)) {
                    yield true;
                }
                yield false;
            }
            case DamageScaling.ALWAYS -> true;
        };
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean isCreativePlayer() {
        Entity entity = this.getEntity();
        if (!(entity instanceof Player)) return false;
        Player player = (Player)entity;
        if (!player.getAbilities().instabuild) return false;
        return true;
    }

    @Nullable
    public Vec3 getSourcePosition() {
        if (this.damageSourcePosition != null) {
            return this.damageSourcePosition;
        }
        if (this.causingEntity != null) {
            return this.causingEntity.position();
        }
        return null;
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

