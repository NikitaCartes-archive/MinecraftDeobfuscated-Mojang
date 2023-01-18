package net.minecraft.client.gui.components;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class MultiLineEditBox extends AbstractScrollWidget {
	private static final int CURSOR_INSERT_WIDTH = 1;
	private static final int CURSOR_INSERT_COLOR = -3092272;
	private static final String CURSOR_APPEND_CHARACTER = "_";
	private static final int TEXT_COLOR = -2039584;
	private static final int PLACEHOLDER_TEXT_COLOR = -857677600;
	private final Font font;
	private final Component placeholder;
	private final MultilineTextField textField;
	private int frame;

	public MultiLineEditBox(Font font, int i, int j, int k, int l, Component component, Component component2) {
		super(i, j, k, l, component2);
		this.font = font;
		this.placeholder = component;
		this.textField = new MultilineTextField(font, k - this.totalInnerPadding());
		this.textField.setCursorListener(this::scrollToCursor);
	}

	public void setCharacterLimit(int i) {
		this.textField.setCharacterLimit(i);
	}

	public void setValueListener(Consumer<String> consumer) {
		this.textField.setValueListener(consumer);
	}

	public void setValue(String string) {
		this.textField.setValue(string);
	}

	public String getValue() {
		return this.textField.value();
	}

	public void tick() {
		this.frame++;
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		narrationElementOutput.add(NarratedElementType.TITLE, Component.translatable("gui.narrate.editBox", this.getMessage(), this.getValue()));
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		if (super.mouseClicked(d, e, i)) {
			return true;
		} else if (this.withinContentAreaPoint(d, e) && i == 0) {
			this.textField.setSelecting(Screen.hasShiftDown());
			this.seekCursorScreen(d, e);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean mouseDragged(double d, double e, int i, double f, double g) {
		if (super.mouseDragged(d, e, i, f, g)) {
			return true;
		} else if (this.withinContentAreaPoint(d, e) && i == 0) {
			this.textField.setSelecting(true);
			this.seekCursorScreen(d, e);
			this.textField.setSelecting(Screen.hasShiftDown());
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		return this.textField.keyPressed(i);
	}

	@Override
	public boolean charTyped(char c, int i) {
		if (this.visible && this.isFocused() && SharedConstants.isAllowedChatCharacter(c)) {
			this.textField.insertText(Character.toString(c));
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void renderContents(PoseStack poseStack, int i, int j, float f) {
		String string = this.textField.value();
		if (string.isEmpty() && !this.isFocused()) {
			this.font
				.drawWordWrap(this.placeholder, this.getX() + this.innerPadding(), this.getY() + this.innerPadding(), this.width - this.totalInnerPadding(), -857677600);
		} else {
			int k = this.textField.cursor();
			boolean bl = this.isFocused() && this.frame / 6 % 2 == 0;
			boolean bl2 = k < string.length();
			int l = 0;
			int m = 0;
			int n = this.getY() + this.innerPadding();

			for (MultilineTextField.StringView stringView : this.textField.iterateLines()) {
				boolean bl3 = this.withinContentAreaTopBottom(n, n + 9);
				if (bl && bl2 && k >= stringView.beginIndex() && k <= stringView.endIndex()) {
					if (bl3) {
						l = this.font.drawShadow(poseStack, string.substring(stringView.beginIndex(), k), (float)(this.getX() + this.innerPadding()), (float)n, -2039584) - 1;
						GuiComponent.fill(poseStack, l, n - 1, l + 1, n + 1 + 9, -3092272);
						this.font.drawShadow(poseStack, string.substring(k, stringView.endIndex()), (float)l, (float)n, -2039584);
					}
				} else {
					if (bl3) {
						l = this.font
								.drawShadow(poseStack, string.substring(stringView.beginIndex(), stringView.endIndex()), (float)(this.getX() + this.innerPadding()), (float)n, -2039584)
							- 1;
					}

					m = n;
				}

				n += 9;
			}

			if (bl && !bl2 && this.withinContentAreaTopBottom(m, m + 9)) {
				this.font.drawShadow(poseStack, "_", (float)l, (float)m, -3092272);
			}

			if (this.textField.hasSelection()) {
				MultilineTextField.StringView stringView2 = this.textField.getSelected();
				int o = this.getX() + this.innerPadding();
				n = this.getY() + this.innerPadding();

				for (MultilineTextField.StringView stringView3 : this.textField.iterateLines()) {
					if (stringView2.beginIndex() > stringView3.endIndex()) {
						n += 9;
					} else {
						if (stringView3.beginIndex() > stringView2.endIndex()) {
							break;
						}

						if (this.withinContentAreaTopBottom(n, n + 9)) {
							int p = this.font.width(string.substring(stringView3.beginIndex(), Math.max(stringView2.beginIndex(), stringView3.beginIndex())));
							int q;
							if (stringView2.endIndex() > stringView3.endIndex()) {
								q = this.width - this.innerPadding();
							} else {
								q = this.font.width(string.substring(stringView3.beginIndex(), stringView2.endIndex()));
							}

							this.renderHighlight(poseStack, o + p, n, o + q, n + 9);
						}

						n += 9;
					}
				}
			}
		}
	}

	@Override
	protected void renderDecorations(PoseStack poseStack) {
		super.renderDecorations(poseStack);
		if (this.textField.hasCharacterLimit()) {
			int i = this.textField.characterLimit();
			Component component = Component.translatable("gui.multiLineEditBox.character_limit", this.textField.value().length(), i);
			drawString(poseStack, this.font, component, this.getX() + this.width - this.font.width(component), this.getY() + this.height + 4, 10526880);
		}
	}

	@Override
	public int getInnerHeight() {
		return 9 * this.textField.getLineCount();
	}

	@Override
	protected boolean scrollbarVisible() {
		return (double)this.textField.getLineCount() > this.getDisplayableLineCount();
	}

	@Override
	protected double scrollRate() {
		return 9.0 / 2.0;
	}

	private void renderHighlight(PoseStack poseStack, int i, int j, int k, int l) {
		RenderSystem.enableColorLogicOp();
		RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
		fill(poseStack, i, j, k, l, -16776961);
		RenderSystem.disableColorLogicOp();
	}

	private void scrollToCursor() {
		double d = this.scrollAmount();
		MultilineTextField.StringView stringView = this.textField.getLineView((int)(d / 9.0));
		if (this.textField.cursor() <= stringView.beginIndex()) {
			d = (double)(this.textField.getLineAtCursor() * 9);
		} else {
			MultilineTextField.StringView stringView2 = this.textField.getLineView((int)((d + (double)this.height) / 9.0) - 1);
			if (this.textField.cursor() > stringView2.endIndex()) {
				d = (double)(this.textField.getLineAtCursor() * 9 - this.height + 9 + this.totalInnerPadding());
			}
		}

		this.setScrollAmount(d);
	}

	private double getDisplayableLineCount() {
		return (double)(this.height - this.totalInnerPadding()) / 9.0;
	}

	private void seekCursorScreen(double d, double e) {
		double f = d - (double)this.getX() - (double)this.innerPadding();
		double g = e - (double)this.getY() - (double)this.innerPadding() + this.scrollAmount();
		this.textField.seekCursorToPoint(f, g);
	}
}
