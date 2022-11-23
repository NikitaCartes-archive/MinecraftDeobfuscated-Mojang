package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
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

@Environment(EnvType.CLIENT)
public abstract class AbstractWidget extends GuiComponent implements Renderable, GuiEventListener, NarratableEntry {
	public static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
	protected int width;
	protected int height;
	private int x;
	private int y;
	private Component message;
	protected boolean isHovered;
	public boolean active = true;
	public boolean visible = true;
	protected float alpha = 1.0F;
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

	public int getHeight() {
		return this.height;
	}

	protected int getYImage(boolean bl) {
		int i = 1;
		if (!this.active) {
			i = 0;
		} else if (bl) {
			i = 2;
		}

		return i;
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		if (this.visible) {
			this.isHovered = i >= this.getX() && j >= this.getY() && i < this.getX() + this.width && j < this.getY() + this.height;
			this.renderButton(poseStack, i, j, f);
			this.updateTooltip();
		}
	}

	private void updateTooltip() {
		if (this.tooltip != null) {
			boolean bl = this.isHoveredOrFocused();
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
		return (ClientTooltipPositioner)(this.isFocused() ? new BelowOrAboveWidgetTooltipPositioner(this) : DefaultTooltipPositioner.INSTANCE);
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

	public void renderButton(PoseStack poseStack, int i, int j, float f) {
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
		int k = this.getYImage(this.isHoveredOrFocused());
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		this.blit(poseStack, this.getX(), this.getY(), 0, 46 + k * 20, this.width / 2, this.height);
		this.blit(poseStack, this.getX() + this.width / 2, this.getY(), 200 - this.width / 2, 46 + k * 20, this.width / 2, this.height);
		this.renderBg(poseStack, minecraft, i, j);
		int l = this.active ? 16777215 : 10526880;
		drawCenteredString(
			poseStack, font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, l | Mth.ceil(this.alpha * 255.0F) << 24
		);
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
		return this.isHovered || this.focused;
	}

	@Override
	public boolean changeFocus(boolean bl) {
		if (this.active && this.visible) {
			this.focused = !this.focused;
			this.onFocusedChanged(this.focused);
			return this.focused;
		} else {
			return false;
		}
	}

	protected void onFocusedChanged(boolean bl) {
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

	public boolean isFocused() {
		return this.focused;
	}

	@Override
	public boolean isActive() {
		return this.visible && this.active;
	}

	protected void setFocused(boolean bl) {
		this.focused = bl;
	}

	@Override
	public NarratableEntry.NarrationPriority narrationPriority() {
		if (this.focused) {
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

	public int getX() {
		return this.x;
	}

	public void setX(int i) {
		this.x = i;
	}

	public void setPosition(int i, int j) {
		this.setX(i);
		this.setY(j);
	}

	public int getY() {
		return this.y;
	}

	public void setY(int i) {
		this.y = i;
	}
}
