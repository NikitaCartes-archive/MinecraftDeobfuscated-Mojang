/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.FormattedCharSequence;

@Environment(value=EnvType.CLIENT)
public interface TooltipAccessor {
    public Optional<List<FormattedCharSequence>> getTooltip();
}

