package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
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
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public abstract class AbstractWidget extends GuiComponent implements Renderable, GuiEventListener, LayoutElement, NarratableEntry {
	public static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
	public static final ResourceLocation ACCESSIBILITY_TEXTURE = new ResourceLocation("textures/gui/accessibility.png");
	protected static final int BUTTON_TEXTURE_Y_OFFSET = 46;
	protected static final int BUTTON_TEXTURE_WIDTH = 200;
	protected static final int BUTTON_TEXTURE_HEIGHT = 20;
	protected static final int BUTTON_TEXTURE_BORDER = 4;
	private static final int BUTTON_TEXT_MARGIN = 2;
	protected int width;
	protected int height;
	private int x;
	private int y;
	private Component message;
	protected boolean isHovered;
	public boolean active = true;
	public boolean visible = true;
	protected float alpha = 1.0F;
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
		if (this.visible) {
			this.isHovered = i >= this.getX() && j >= this.getY() && i < this.getX() + this.width && j < this.getY() + this.height;
			this.renderWidget(poseStack, i, j, f);
			this.updateTooltip();
		}
	}

	private void updateTooltip() {
		if (this.tooltip != null) {
			boolean bl = this.isHovered || this.isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard();
			if (bl != this.wasHoveredOrFocused) {
				if (bl) {
					this.hoverOrFocusedStartTime = Util.getMillis();
				}

				this.wasHoveredOrFocused = bl;
			}

			if (bl && Util.getMillis() - this.hoverOrFocusedStartTime > (long)this.tooltipMsDelay) {
				Screen screen = Minecraft.getInstance().screen;
				if (screen != null) {
					screen.setTooltipForNextRenderPass(this.tooltip, this.createTooltipPositioner(), this.isFocused());
				}
			}
		}
	}

	protected ClientTooltipPositioner createTooltipPositioner() {
		return (ClientTooltipPositioner)(!this.isHovered && this.isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard()
			? new BelowOrAboveWidgetTooltipPositioner(this)
			: new MenuTooltipPositioner(this));
	}

	public void setTooltip(@Nullable Tooltip tooltip) {
		this.tooltip = tooltip;
	}

	public void setTooltipDelay(int i) {
		this.tooltipMsDelay = i;
	}

	protected MutableComponent createNarrationMessage() {
		return wrapDefaultNarrationMessage(this.getMessage());
	}

	public static MutableComponent wrapDefaultNarrationMessage(Component component) {
		return Component.translatable("gui.narrate.button", component);
	}

	public void renderWidget(PoseStack poseStack, int i, int j, float f) {
		this.renderButton(poseStack, i, j);
	}

	protected void renderButton(PoseStack poseStack, int i, int j) {
		Minecraft minecraft = Minecraft.getInstance();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, this.getTextureLocation());
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		this.blitNineSliced(poseStack, this.getX(), this.getY(), this.width, this.height, 4, 200, 20, 0, this.getTextureY());
		this.renderBg(poseStack, minecraft, i, j);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		int k = this.active ? 16777215 : 10526880;
		Font font = minecraft.font;
		int l = font.width(this.message);
		int m = this.width - 4;
		if (l > m) {
			double d = (double)Util.getMillis() / 1000.0;
			double e = Math.sin((Math.PI / 2) * Math.cos(d));
			int n = l - m;
			enableScissor(this.x + 2, this.y + 2, this.x + this.width - 2, this.y + this.height - 2);
			this.renderString(poseStack, font, this.getX() + this.width / 2 - (int)(e * (double)n), this.getY() + (this.height - 8) / 2, k);
			disableScissor();
		} else {
			this.renderString(poseStack, font, this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, k);
		}
	}

	public void renderString(PoseStack poseStack, Font font, int i, int j, int k) {
		drawCenteredString(poseStack, font, this.getMessage(), i, j, k | Mth.ceil(this.alpha * 255.0F) << 24);
	}

	public void renderTexture(PoseStack poseStack, ResourceLocation resourceLocation, int i, int j, int k, int l, int m, int n, int o, int p, int q) {
		RenderSystem.setShaderTexture(0, resourceLocation);
		int r = l;
		if (!this.isActive()) {
			r = l + m * 2;
		} else if (this.isHoveredOrFocused()) {
			r = l + m;
		}

		RenderSystem.enableDepthTest();
		blit(poseStack, i, j, (float)k, (float)r, n, o, p, q);
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
		if (this.active && this.visible) {
			if (this.isValidClickButton(i)) {
				boolean bl = this.clicked(d, e);
				if (bl) {
					this.playDownSound(Minecraft.getInstance().getSoundManager());
					this.onClick(d, e);
					return true;
				}
			}

			return false;
		} else {
			return false;
		}
	}

	@Override
	public boolean mouseReleased(double d, double e, int i) {
		if (this.isValidClickButton(i)) {
			this.onRelease(d, e);
			return true;
		} else {
			return false;
		}
	}

	protected boolean isValidClickButton(int i) {
		return i == 0;
	}

	@Override
	public boolean mouseDragged(double d, double e, int i, double f, double g) {
		if (this.isValidClickButton(i)) {
			this.onDrag(d, e, f, g);
			return true;
		} else {
			return false;
		}
	}

	protected boolean clicked(double d, double e) {
		return this.active
			&& this.visible
			&& d >= (double)this.getX()
			&& e >= (double)this.getY()
			&& d < (double)(this.getX() + this.width)
			&& e < (double)(this.getY() + this.height);
	}

	public boolean isHoveredOrFocused() {
		return this.isHovered || this.isFocused();
	}

	@Nullable
	@Override
	public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
		if (!this.active || !this.visible) {
			return null;
		} else {
			return !this.isFocused() ? ComponentPath.leaf(this) : null;
		}
	}

	@Override
	public boolean isMouseOver(double d, double e) {
		return this.active
			&& this.visible
			&& d >= (double)this.getX()
			&& e >= (double)this.getY()
			&& d < (double)(this.getX() + this.width)
			&& e < (double)(this.getY() + this.height);
	}

	public void playDownSound(SoundManager soundManager) {
		soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
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
		} else {
			return this.isHovered ? NarratableEntry.NarrationPriority.HOVERED : NarratableEntry.NarrationPriority.NONE;
		}
	}

	@Override
	public final void updateNarration(NarrationElementOutput narrationElementOutput) {
		this.updateWidgetNarration(narrationElementOutput);
		if (this.tooltip != null) {
			this.tooltip.updateNarration(narrationElementOutput);
		}
	}

	protected abstract void updateWidgetNarration(NarrationElementOutput narrationElementOutput);

	protected void defaultButtonNarrationText(NarrationElementOutput narrationElementOutput) {
		narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
		if (this.active) {
			if (this.isFocused()) {
				narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.focused"));
			} else {
				narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.hovered"));
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

	@Override
	public int getTabOrderGroup() {
		return this.tabOrderGroup;
	}

	public void setTabOrderGroup(int i) {
		this.tabOrderGroup = i;
	}
}
