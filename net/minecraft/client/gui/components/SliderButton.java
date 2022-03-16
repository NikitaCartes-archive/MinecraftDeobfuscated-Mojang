/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.ProgressOption;
import net.minecraft.client.gui.components.AbstractOptionSliderButton;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.util.FormattedCharSequence;

@Environment(value=EnvType.CLIENT)
public class SliderButton
extends AbstractOptionSliderButton
implements TooltipAccessor {
    private final ProgressOption option;
    private final Option.TooltipSupplier<Double> tooltip;

    public SliderButton(Options options, int i, int j, int k, int l, ProgressOption progressOption, Option.TooltipSupplier<Double> tooltipSupplier) {
        super(options, i, j, k, l, (double)((float)progressOption.toPct(progressOption.get(options))));
        this.option = progressOption;
        this.tooltip = tooltipSupplier;
        this.updateMessage();
    }

    @Override
    protected void applyValue() {
        this.option.set(this.options, this.option.toValue(this.value));
        this.options.save();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(this.option.getMessage(this.options));
    }

    @Override
    public List<FormattedCharSequence> getTooltip() {
        return (List)this.tooltip.apply(this.option.toValue(this.value));
    }
}

