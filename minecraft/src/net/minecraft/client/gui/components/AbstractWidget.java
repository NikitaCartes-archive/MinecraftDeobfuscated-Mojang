package net.minecraft.client.gui.components;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public abstract class AbstractWidget implements Renderable, GuiEventListener, LayoutElement, NarratableEntry {
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
	protected float alpha = 1.0F;
	private int tabOrderGroup;
	private boolean focused;
	@Nullable
	private Tooltip tooltip;

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
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		if (this.visible) {
			this.isHovered = i >= this.getX() && j >= this.getY() && i < this.getX() + this.width && j < this.getY() + this.height;
			this.renderWidget(guiGraphics, i, j, f);
			if (this.tooltip != null) {
				this.tooltip.refreshTooltipForNextRenderPass(this.isHovered(), this.isFocused(), this.getRectangle());
			}
		}
	}

	public void setTooltip(@Nullable Tooltip tooltip) {
		this.tooltip = tooltip;
	}

	@Nullable
	public Tooltip getTooltip() {
		return this.tooltip;
	}

	public void setTooltipDelay(int i) {
		if (this.tooltip != null) {
			this.tooltip.setDelay(i);
		}
	}

	protected MutableComponent createNarrationMessage() {
		return wrapDefaultNarrationMessage(this.getMessage());
	}

	public static MutableComponent wrapDefaultNarrationMessage(Component component) {
		return Component.translatable("gui.narrate.button", component);
	}

	protected abstract void renderWidget(GuiGraphics guiGraphics, int i, int j, float f);

	protected static void renderScrollingString(GuiGraphics guiGraphics, Font font, Component component, int i, int j, int k, int l, int m) {
		renderScrollingString(guiGraphics, font, component, (i + k) / 2, i, j, k, l, m);
	}

	protected static void renderScrollingString(GuiGraphics guiGraphics, Font font, Component component, int i, int j, int k, int l, int m, int n) {
		int o = font.width(component);
		int p = (k + m - 9) / 2 + 1;
		int q = l - j;
		if (o > q) {
			int r = o - q;
			double d = (double)Util.getMillis() / 1000.0;
			double e = Math.max((double)r * 0.5, 3.0);
			double f = Math.sin((Math.PI / 2) * Math.cos((Math.PI * 2) * d / e)) / 2.0 + 0.5;
			double g = Mth.lerp(f, 0.0, (double)r);
			guiGraphics.enableScissor(j, k, l, m);
			guiGraphics.drawString(font, component, j - (int)g, p, n);
			guiGraphics.disableScissor();
		} else {
			int r = Mth.clamp(i, j + o / 2, l - o / 2);
			guiGraphics.drawCenteredString(font, component, r, p, n);
		}
	}

	protected void renderScrollingString(GuiGraphics guiGraphics, Font font, int i, int j) {
		int k = this.getX() + i;
		int l = this.getX() + this.getWidth() - i;
		renderScrollingString(guiGraphics, font, this.getMessage(), k, this.getY(), l, this.getY() + this.getHeight(), j);
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
			&& d < (double)(this.getX() + this.getWidth())
			&& e < (double)(this.getY() + this.getHeight());
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

	public void setHeight(int i) {
		this.height = i;
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
