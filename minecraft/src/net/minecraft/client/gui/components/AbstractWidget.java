package net.minecraft.client.gui.components;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public abstract class AbstractWidget extends GuiComponent implements Widget, GuiEventListener {
	public static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
	private static final int NARRATE_DELAY_MOUSE = 750;
	private static final int NARRATE_DELAY_FOCUS = 200;
	protected int width;
	protected int height;
	public int x;
	public int y;
	private String message;
	private boolean wasHovered;
	protected boolean isHovered;
	public boolean active = true;
	public boolean visible = true;
	protected float alpha = 1.0F;
	protected long nextNarration = Long.MAX_VALUE;
	private boolean focused;

	public AbstractWidget(int i, int j, String string) {
		this(i, j, 200, 20, string);
	}

	public AbstractWidget(int i, int j, int k, int l, String string) {
		this.x = i;
		this.y = j;
		this.width = k;
		this.height = l;
		this.message = string;
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
	public void render(int i, int j, float f) {
		if (this.visible) {
			this.isHovered = i >= this.x && j >= this.y && i < this.x + this.width && j < this.y + this.height;
			if (this.wasHovered != this.isHovered()) {
				if (this.isHovered()) {
					if (this.focused) {
						this.nextNarration = Util.getMillis() + 200L;
					} else {
						this.nextNarration = Util.getMillis() + 750L;
					}
				} else {
					this.nextNarration = Long.MAX_VALUE;
				}
			}

			if (this.visible) {
				this.renderButton(i, j, f);
			}

			this.narrate();
			this.wasHovered = this.isHovered();
		}
	}

	protected void narrate() {
		if (this.active && this.isHovered() && Util.getMillis() > this.nextNarration) {
			String string = this.getNarrationMessage();
			if (!string.isEmpty()) {
				NarratorChatListener.INSTANCE.sayNow(string);
				this.nextNarration = Long.MAX_VALUE;
			}
		}
	}

	protected String getNarrationMessage() {
		return this.message.isEmpty() ? "" : I18n.get("gui.narrate.button", this.getMessage());
	}

	public void renderButton(int i, int j, float f) {
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		minecraft.getTextureManager().bind(WIDGETS_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
		int k = this.getYImage(this.isHovered());
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
		);
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		this.blit(this.x, this.y, 0, 46 + k * 20, this.width / 2, this.height);
		this.blit(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + k * 20, this.width / 2, this.height);
		this.renderBg(minecraft, i, j);
		int l = 14737632;
		if (!this.active) {
			l = 10526880;
		} else if (this.isHovered()) {
			l = 16777120;
		}

		this.drawCenteredString(font, this.message, this.x + this.width / 2, this.y + (this.height - 8) / 2, l | Mth.ceil(this.alpha * 255.0F) << 24);
	}

	protected void renderBg(Minecraft minecraft, int i, int j) {
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
		return this.active && this.visible && d >= (double)this.x && e >= (double)this.y && d < (double)(this.x + this.width) && e < (double)(this.y + this.height);
	}

	public boolean isHovered() {
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
		return this.active && this.visible && d >= (double)this.x && e >= (double)this.y && d < (double)(this.x + this.width) && e < (double)(this.y + this.height);
	}

	public void renderToolTip(int i, int j) {
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

	public void setMessage(String string) {
		if (!Objects.equals(string, this.message)) {
			this.nextNarration = Util.getMillis() + 250L;
		}

		this.message = string;
	}

	public String getMessage() {
		return this.message;
	}

	public boolean isFocused() {
		return this.focused;
	}

	protected void setFocused(boolean bl) {
		this.focused = bl;
	}
}
