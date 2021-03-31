/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.effect;

import net.minecraft.ChatFormatting;

public enum MobEffectCategory {
    BENEFICIAL(ChatFormatting.BLUE),
    HARMFUL(ChatFormatting.RED),
    NEUTRAL(ChatFormatting.BLUE);

    private final ChatFormatting tooltipFormatting;

    private MobEffectCategory(ChatFormatting chatFormatting) {
        this.tooltipFormatting = chatFormatting;
    }

    public ChatFormatting getTooltipFormatting() {
        return this.tooltipFormatting;
    }
}

