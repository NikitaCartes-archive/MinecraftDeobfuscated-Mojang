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
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.GameRenderer;
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
    protected static final int BUTTON_TEXTURE_Y_OFFSET = 46;
    protected int width;
    protected int height;
    private int x;
    private int y;
    private Component message;
    protected boolean isHovered;
    public boolean active = true;
    public boolean visible = true;
    protected float alpha = 1.0f;
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

    protected ResourceLocation getTextureLocation() {
        return WIDGETS_LOCATION;
    }

    protected int getTextureY() {
        int i = 1;
        if (!this.active) {
            i = 0;
        } else if (this.isHoveredOrFocused()) {
            i = 2;
        }
        return 46 + i * 20;
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        if (!this.visible) {
            return;
        }
        this.isHovered = i >= this.getX() && j >= this.getY() && i < this.getX() + this.width && j < this.getY() + this.height;
        this.renderButton(poseStack, i, j, f);
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
        return DefaultTooltipPositioner.INSTANCE;
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

    public void renderButton(PoseStack poseStack, int i, int j, float f) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.getTextureLocation());
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        int k = this.width / 2;
        int l = this.width - k;
        int m = this.getTextureY();
        this.blit(poseStack, this.getX(), this.getY(), 0, m, k, this.height);
        this.blit(poseStack, this.getX() + k, this.getY(), 200 - l, m, l, this.height);
        this.renderBg(poseStack, minecraft, i, j);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        int n = this.active ? 0xFFFFFF : 0xA0A0A0;
        AbstractWidget.drawCenteredString(poseStack, font, this.getMessage(), this.getX() + k, this.getY() + (this.height - 8) / 2, n | Mth.ceil(this.alpha * 255.0f) << 24);
    }

    protected void renderBg(PoseStack poseStack, Minecraft minecraft, int i, int j) {
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

    public boolean isHoveredOrFocused() {
        return this.isHovered || this.isFocused();
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
        return new ScreenRectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }
}

