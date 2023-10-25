package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class Checkbox extends AbstractButton {
	private static final ResourceLocation CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE = new ResourceLocation("widget/checkbox_selected_highlighted");
	private static final ResourceLocation CHECKBOX_SELECTED_SPRITE = new ResourceLocation("widget/checkbox_selected");
	private static final ResourceLocation CHECKBOX_HIGHLIGHTED_SPRITE = new ResourceLocation("widget/checkbox_highlighted");
	private static final ResourceLocation CHECKBOX_SPRITE = new ResourceLocation("widget/checkbox");
	private static final int TEXT_COLOR = 14737632;
	private static final int SPACING = 4;
	private static final int BOX_PADDING = 8;
	private boolean selected;
	private final Checkbox.OnValueChange onValueChange;

	Checkbox(int i, int j, Component component, Font font, boolean bl, Checkbox.OnValueChange onValueChange) {
		super(i, j, boxSize(font) + 4 + font.width(component), boxSize(font), component);
		this.selected = bl;
		this.onValueChange = onValueChange;
	}

	public static Checkbox.Builder builder(Component component, Font font) {
		return new Checkbox.Builder(component, font);
	}

	private static int boxSize(Font font) {
		return 9 + 8;
	}

	@Override
	public void onPress() {
		this.selected = !this.selected;
		this.onValueChange.onValueChange(this, this.selected);
	}

	public boolean selected() {
		return this.selected;
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
		if (this.active) {
			if (this.isFocused()) {
				narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.focused"));
			} else {
				narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.hovered"));
			}
		}
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
		Minecraft minecraft = Minecraft.getInstance();
		RenderSystem.enableDepthTest();
		Font font = minecraft.font;
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		ResourceLocation resourceLocation;
		if (this.selected) {
			resourceLocation = this.isFocused() ? CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE : CHECKBOX_SELECTED_SPRITE;
		} else {
			resourceLocation = this.isFocused() ? CHECKBOX_HIGHLIGHTED_SPRITE : CHECKBOX_SPRITE;
		}

		int k = boxSize(font);
		int l = this.getX() + k + 4;
		int m = this.getY() + (this.height >> 1) - (9 >> 1);
		guiGraphics.blitSprite(resourceLocation, this.getX(), this.getY(), k, k);
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
		guiGraphics.drawString(font, this.getMessage(), l, m, 14737632 | Mth.ceil(this.alpha * 255.0F) << 24);
	}

	@Environment(EnvType.CLIENT)
	public static class Builder {
		private final Component message;
		private final Font font;
		private int x = 0;
		private int y = 0;
		private Checkbox.OnValueChange onValueChange = Checkbox.OnValueChange.NOP;
		private boolean selected = false;
		@Nullable
		private OptionInstance<Boolean> option = null;
		@Nullable
		private Tooltip tooltip = null;

		Builder(Component component, Font font) {
			this.message = component;
			this.font = font;
		}

		public Checkbox.Builder pos(int i, int j) {
			this.x = i;
			this.y = j;
			return this;
		}

		public Checkbox.Builder onValueChange(Checkbox.OnValueChange onValueChange) {
			this.onValueChange = onValueChange;
			return this;
		}

		public Checkbox.Builder selected(boolean bl) {
			this.selected = bl;
			this.option = null;
			return this;
		}

		public Checkbox.Builder selected(OptionInstance<Boolean> optionInstance) {
			this.option = optionInstance;
			this.selected = optionInstance.get();
			return this;
		}

		public Checkbox.Builder tooltip(Tooltip tooltip) {
			this.tooltip = tooltip;
			return this;
		}

		public Checkbox build() {
			Checkbox.OnValueChange onValueChange = this.option == null ? this.onValueChange : (checkboxx, bl) -> {
				this.option.set(bl);
				this.onValueChange.onValueChange(checkboxx, bl);
			};
			Checkbox checkbox = new Checkbox(this.x, this.y, this.message, this.font, this.selected, onValueChange);
			checkbox.setTooltip(this.tooltip);
			return checkbox;
		}
	}

	@Environment(EnvType.CLIENT)
	public interface OnValueChange {
		Checkbox.OnValueChange NOP = (checkbox, bl) -> {
		};

		void onValueChange(Checkbox checkbox, boolean bl);
	}
}
