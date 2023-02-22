/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.MenuTooltipPositioner;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractWidget
extends GuiComponent
implements Renderable,
GuiEventListener,
LayoutElement,
NarratableEntry {
    public static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    public static final ResourceLocation ACCESSIBILITY_TEXTURE = new ResourceLocation("textures/gui/accessibility.png");
    private static final double PERIOD_PER_SCROLLED_PIXEL = 0.5;
    private static final double MIN_SCROLL_PERIOD = 3.0;
    protected int width;
    protected int height;
    private int x;
    private int y;
    private Component message;
    protected boolean isHovered;
    public boolean active = true;
    public boolean visible = true;
    protected float alpha = 1.0f;
    private int tabOrderGroup;
    private boolean focused;
    @Nullable
    private Tooltip tooltip;
    private int tooltipMsDelay;
    private long hoverOrFocusedStartTime;
    private boolean wasHoveredOrFocused;

    public AbstractWidget(int i, int j, int k, int l, Component component) {
        this.x = i;
        this.y = j;
        this.width = k;
        this.height = l;
        this.message = component;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        if (!this.visible) {
            return;
        }
        this.isHovered = i >= this.getX() && j >= this.getY() && i < this.getX() + this.width && j < this.getY() + this.height;
        this.renderWidget(poseStack, i, j, f);
        this.updateTooltip();
    }

    private void updateTooltip() {
        Screen screen;
        boolean bl;
        if (this.tooltip == null) {
            return;
        }
        boolean bl2 = bl = this.isHovered || this.isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard();
        if (bl != this.wasHoveredOrFocused) {
            if (bl) {
                this.hoverOrFocusedStartTime = Util.getMillis();
            }
            this.wasHoveredOrFocused = bl;
        }
        if (bl && Util.getMillis() - this.hoverOrFocusedStartTime > (long)this.tooltipMsDelay && (screen = Minecraft.getInstance().screen) != null) {
            screen.setTooltipForNextRenderPass(this.tooltip, this.createTooltipPositioner(), this.isFocused());
        }
    }

    protected ClientTooltipPositioner createTooltipPositioner() {
        if (!this.isHovered && this.isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard()) {
            return new BelowOrAboveWidgetTooltipPositioner(this);
        }
        return new MenuTooltipPositioner(this);
    }

    public void setTooltip(@Nullable Tooltip tooltip) {
        this.tooltip = tooltip;
    }

    public void setTooltipDelay(int i) {
        this.tooltipMsDelay = i;
    }

    protected MutableComponent createNarrationMessage() {
        return AbstractWidget.wrapDefaultNarrationMessage(this.getMessage());
    }

    public static MutableComponent wrapDefaultNarrationMessage(Component component) {
        return Component.translatable("gui.narrate.button", component);
    }

    public abstract void renderWidget(PoseStack var1, int var2, int var3, float var4);

    protected static void renderScrollingString(PoseStack poseStack, Font font, Component component, int i, int j, int k, int l, int m) {
        int n = font.width(component);
        int o = (j + l - font.lineHeight) / 2 + 1;
        int p = k - i;
        if (n > p) {
            int q = n - p;
            double d = (double)Util.getMillis() / 1000.0;
            double e = Math.max((double)q * 0.5, 3.0);
            double f = Math.sin(1.5707963267948966 * Math.cos(Math.PI * 2 * d / e)) / 2.0 + 0.5;
            double g = Mth.lerp(f, 0.0, (double)q);
            AbstractWidget.enableScissor(i, j, k, l);
            AbstractWidget.drawString(poseStack, font, component, i - (int)g, o, m);
            AbstractWidget.disableScissor();
        } else {
            AbstractWidget.drawCenteredString(poseStack, font, component, (i + k) / 2, o, m);
        }
    }

    protected void renderScrollingString(PoseStack poseStack, Font font, int i, int j) {
        int k = this.getX() + i;
        int l = this.getX() + this.getWidth() - i;
        AbstractWidget.renderScrollingString(poseStack, font, this.getMessage(), k, this.getY(), l, this.getY() + this.getHeight(), j);
    }

    public void renderTexture(PoseStack poseStack, ResourceLocation resourceLocation, int i, int j, int k, int l, int m, int n, int o, int p, int q) {
        RenderSystem.setShaderTexture(0, resourceLocation);
        int r = l;
        if (!this.isActive()) {
            r += m * 2;
        } else if (this.isHoveredOrFocused()) {
            r += m;
        }
        RenderSystem.enableDepthTest();
        AbstractWidget.blit(poseStack, i, j, k, r, n, o, p, q);
    }

    public void onClick(double d, double e) {
    }

    public void onRelease(double d, double e) {
    }

    protected void onDrag(double d, double e, double f, double g) {
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        boolean bl;
        if (!this.active || !this.visible) {
            return false;
        }
        if (this.isValidClickButton(i) && (bl = this.clicked(d, e))) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onClick(d, e);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double d, double e, int i) {
        if (this.isValidClickButton(i)) {
            this.onRelease(d, e);
            return true;
        }
        return false;
    }

    protected boolean isValidClickButton(int i) {
        return i == 0;
    }

    @Override
    public boolean mouseDragged(double d, double e, int i, double f, double g) {
        if (this.isValidClickButton(i)) {
            this.onDrag(d, e, f, g);
            return true;
        }
        return false;
    }

    protected boolean clicked(double d, double e) {
        return this.active && this.visible && d >= (double)this.getX() && e >= (double)this.getY() && d < (double)(this.getX() + this.width) && e < (double)(this.getY() + this.height);
    }

    @Override
    @Nullable
    public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        if (!this.active || !this.visible) {
            return null;
        }
        if (!this.isFocused()) {
            return ComponentPath.leaf(this);
        }
        return null;
    }

    @Override
    public boolean isMouseOver(double d, double e) {
        return this.active && this.visible && d >= (double)this.getX() && e >= (double)this.getY() && d < (double)(this.getX() + this.width) && e < (double)(this.getY() + this.height);
    }

    public void playDownSound(SoundManager soundManager) {
        soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    public void setWidth(int i) {
        this.width = i;
    }

    public void setAlpha(float f) {
        this.alpha = f;
    }

    public void setMessage(Component component) {
        this.message = component;
    }

    public Component getMessage() {
        return this.message;
    }

    @Override
    public boolean isFocused() {
        return this.focused;
    }

    public boolean isHovered() {
        return this.isHovered;
    }

    public boolean isHoveredOrFocused() {
        return this.isHovered() || this.isFocused();
    }

    @Override
    public boolean isActive() {
        return this.visible && this.active;
    }

    @Override
    public void setFocused(boolean bl) {
        this.focused = bl;
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        if (this.isFocused()) {
            return NarratableEntry.NarrationPriority.FOCUSED;
        }
        if (this.isHovered) {
            return NarratableEntry.NarrationPriority.HOVERED;
        }
        return NarratableEntry.NarrationPriority.NONE;
    }

    @Override
    public final void updateNarration(NarrationElementOutput narrationElementOutput) {
        this.updateWidgetNarration(narrationElementOutput);
        if (this.tooltip != null) {
            this.tooltip.updateNarration(narrationElementOutput);
        }
    }

    protected abstract void updateWidgetNarration(NarrationElementOutput var1);

    protected void defaultButtonNarrationText(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, (Component)this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.button.usage.focused"));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.button.usage.hovered"));
            }
        }
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public void setX(int i) {
        this.x = i;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public void setY(int i) {
        this.y = i;
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> consumer) {
        consumer.accept(this);
    }

    @Override
    public ScreenRectangle getRectangle() {
        return LayoutElement.super.getRectangle();
    }

    @Override
    public int getTabOrderGroup() {
        return this.tabOrderGroup;
    }

    public void setTabOrderGroup(int i) {
        this.tabOrderGroup = i;
    }
}

