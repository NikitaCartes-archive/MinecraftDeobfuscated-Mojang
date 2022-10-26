/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@Environment(value=EnvType.CLIENT)
public class Button
extends AbstractButton {
    public static final OnTooltip NO_TOOLTIP = (button, poseStack, i, j) -> {};
    public static final int SMALL_WIDTH = 120;
    public static final int DEFAULT_WIDTH = 150;
    public static final int DEFAULT_HEIGHT = 20;
    protected static final CreateNarration DEFAULT_NARRATION = supplier -> (MutableComponent)supplier.get();
    protected final OnPress onPress;
    protected final OnTooltip onTooltip;
    protected final CreateNarration createNarration;

    public static Builder builder(Component component, OnPress onPress) {
        return new Builder(component, onPress);
    }

    protected Button(int i, int j, int k, int l, Component component, OnPress onPress, OnTooltip onTooltip, CreateNarration createNarration) {
        super(i, j, k, l, component);
        this.onPress = onPress;
        this.onTooltip = onTooltip;
        this.createNarration = createNarration;
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        return this.createNarration.createNarrationMessage(() -> super.createNarrationMessage());
    }

    @Override
    public void renderButton(PoseStack poseStack, int i, int j, float f) {
        super.renderButton(poseStack, i, j, f);
        if (this.isHoveredOrFocused()) {
            this.renderToolTip(poseStack, i, j);
        }
    }

    @Override
    public void renderToolTip(PoseStack poseStack, int i, int j) {
        this.onTooltip.onTooltip(this, poseStack, i, j);
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
        this.onTooltip.narrateTooltip(component -> narrationElementOutput.add(NarratedElementType.HINT, (Component)component));
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private final Component message;
        private final OnPress onPress;
        private OnTooltip onTooltip = NO_TOOLTIP;
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;
        private CreateNarration createNarration = DEFAULT_NARRATION;

        public Builder(Component component, OnPress onPress) {
            this.message = component;
            this.onPress = onPress;
        }

        public Builder pos(int i, int j) {
            this.x = i;
            this.y = j;
            return this;
        }

        public Builder width(int i) {
            this.width = i;
            return this;
        }

        public Builder size(int i, int j) {
            this.width = i;
            this.height = j;
            return this;
        }

        public Builder bounds(int i, int j, int k, int l) {
            return this.pos(i, j).size(k, l);
        }

        public Builder tooltip(OnTooltip onTooltip) {
            this.onTooltip = onTooltip;
            return this;
        }

        public Builder createNarration(CreateNarration createNarration) {
            this.createNarration = createNarration;
            return this;
        }

        public Button build() {
            return new Button(this.x, this.y, this.width, this.height, this.message, this.onPress, this.onTooltip, this.createNarration);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static interface OnPress {
        public void onPress(Button var1);
    }

    @Environment(value=EnvType.CLIENT)
    public static interface OnTooltip {
        public void onTooltip(Button var1, PoseStack var2, int var3, int var4);

        default public void narrateTooltip(Consumer<Component> consumer) {
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static interface CreateNarration {
        public MutableComponent createNarrationMessage(Supplier<MutableComponent> var1);
    }
}

