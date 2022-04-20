/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.damagesource;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class BadRespawnPointDamage
extends DamageSource {
    protected BadRespawnPointDamage() {
        super("badRespawnPoint");
        this.setScalesWithDifficulty();
        this.setExplosion();
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity livingEntity) {
        MutableComponent component = ComponentUtils.wrapInSquareBrackets(Component.translatable("death.attack.badRespawnPoint.link")).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://bugs.mojang.com/browse/MCPE-28723")).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("MCPE-28723"))));
        return Component.translatable("death.attack.badRespawnPoint.message", livingEntity.getDisplayName(), component);
    }
}

