/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractScrollWidget
extends AbstractWidget
implements Renderable,
GuiEventListener {
    private static final int BORDER_COLOR_FOCUSED = -1;
    private static final int BORDER_COLOR = -6250336;
    private static final int BACKGROUND_COLOR = -16777216;
    private static final int INNER_PADDING = 4;
    private double scrollAmount;
    private boolean scrolling;

    public AbstractScrollWidget(int i, int j, int k, int l, Component component) {
        super(i, j, k, l, component);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        boolean bl2;
        if (!this.visible) {
            return false;
        }
        boolean bl = this.withinContentAreaPoint(d, e);
        boolean bl3 = bl2 = this.scrollbarVisible() && d >= (double)(this.getX() + this.width) && d <= (double)(this.getX() + this.width + 8) && e >= (double)this.getY() && e < (double)(this.getY() + this.height);
        if (bl2 && i == 0) {
            this.scrolling = true;
            return true;
        }
        return bl || bl2;
    }

    @Override
    public boolean mouseReleased(double d, double e, int i) {
        if (i == 0) {
            this.scrolling = false;
        }
        return super.mouseReleased(d, e, i);
    }

    @Override
    public boolean mouseDragged(double d, double e, int i, double f, double g) {
        if (!(this.visible && this.isFocused() && this.scrolling)) {
            return false;
        }
        if (e < (double)this.getY()) {
            this.setScrollAmount(0.0);
        } else if (e > (double)(this.getY() + this.height)) {
            this.setScrollAmount(this.getMaxScrollAmount());
        } else {
            int j = this.getScrollBarHeight();
            double h = Math.max(1, this.getMaxScrollAmount() / (this.height - j));
            this.setScrollAmount(this.scrollAmount + g * h);
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f) {
        if (!this.visible) {
            return false;
        }
        this.setScrollAmount(this.scrollAmount - f * this.scrollRate());
        return true;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        boolean bl2;
        boolean bl = i == 265;
        boolean bl3 = bl2 = i == 264;
        if (bl || bl2) {
            double d = this.scrollAmount;
            this.setScrollAmount(this.scrollAmount + (double)(bl ? -1 : 1) * this.scrollRate());
            if (d != this.scrollAmount) {
                return true;
            }
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public void renderWidget(PoseStack poseStack, int i, int j, float f) {
        if (!this.visible) {
            return;
        }
        this.renderBackground(poseStack);
        AbstractScrollWidget.enableScissor(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1);
        poseStack.pushPose();
        poseStack.translate(0.0, -this.scrollAmount, 0.0);
        this.renderContents(poseStack, i, j, f);
        poseStack.popPose();
        AbstractScrollWidget.disableScissor();
        this.renderDecorations(poseStack);
    }

    private int getScrollBarHeight() {
        return Mth.clamp((int)((float)(this.height * this.height) / (float)this.getContentHeight()), 32, this.height);
    }

    protected void renderDecorations(PoseStack poseStack) {
        if (this.scrollbarVisible()) {
            this.renderScrollBar(poseStack);
        }
    }

    protected int innerPadding() {
        return 4;
    }

    protected int totalInnerPadding() {
        return this.innerPadding() * 2;
    }

    protected double scrollAmount() {
        return this.scrollAmount;
    }

    protected void setScrollAmount(double d) {
        this.scrollAmount = Mth.clamp(d, 0.0, (double)this.getMaxScrollAmount());
    }

    protected int getMaxScrollAmount() {
        return Math.max(0, this.getContentHeight() - (this.height - 4));
    }

    private int getContentHeight() {
        return this.getInnerHeight() + 4;
    }

    private void renderBackground(PoseStack poseStack) {
        int i = this.isFocused() ? -1 : -6250336;
        AbstractScrollWidget.fill(poseStack, this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, i);
        AbstractScrollWidget.fill(poseStack, this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, -16777216);
    }

    private void renderScrollBar(PoseStack poseStack) {
        int i = this.getScrollBarHeight();
        int j = this.getX() + this.width;
        int k = this.getX() + this.width + 8;
        int l = Math.max(this.getY(), (int)this.scrollAmount * (this.height - i) / this.getMaxScrollAmount() + this.getY());
        int m = l + i;
        AbstractScrollWidget.fill(poseStack, j, l, k, m, -8355712);
        AbstractScrollWidget.fill(poseStack, j, l, k - 1, m - 1, -4144960);
    }

    protected boolean withinContentAreaTopBottom(int i, int j) {
        return (double)j - this.scrollAmount >= (double)this.getY() && (double)i - this.scrollAmount <= (double)(this.getY() + this.height);
    }

    protected boolean withinContentAreaPoint(double d, double e) {
        return d >= (double)this.getX() && d < (double)(this.getX() + this.width) && e >= (double)this.getY() && e < (double)(this.getY() + this.height);
    }

    protected abstract int getInnerHeight();

    protected abstract boolean scrollbarVisible();

    protected abstract double scrollRate();

    protected abstract void renderContents(PoseStack var1, int var2, int var3, float var4);
}

