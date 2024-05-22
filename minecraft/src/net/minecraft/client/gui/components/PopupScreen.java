package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class PopupScreen extends Screen {
	private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("popup/background");
	private static final int SPACING = 12;
	private static final int BG_BORDER_WITH_SPACING = 18;
	private static final int BUTTON_SPACING = 6;
	private static final int IMAGE_SIZE_X = 130;
	private static final int IMAGE_SIZE_Y = 64;
	private static final int POPUP_DEFAULT_WIDTH = 250;
	private final Screen backgroundScreen;
	@Nullable
	private final ResourceLocation image;
	private final Component message;
	private final List<PopupScreen.ButtonOption> buttons;
	@Nullable
	private final Runnable onClose;
	private final int contentWidth;
	private final LinearLayout layout = LinearLayout.vertical();

	PopupScreen(
		Screen screen,
		int i,
		@Nullable ResourceLocation resourceLocation,
		Component component,
		Component component2,
		List<PopupScreen.ButtonOption> list,
		@Nullable Runnable runnable
	) {
		super(component);
		this.backgroundScreen = screen;
		this.image = resourceLocation;
		this.message = component2;
		this.buttons = list;
		this.onClose = runnable;
		this.contentWidth = i - 36;
	}

	@Override
	public void added() {
		super.added();
		this.backgroundScreen.clearFocus();
	}

	@Override
	protected void init() {
		this.backgroundScreen.init(this.minecraft, this.width, this.height);
		this.layout.spacing(12).defaultCellSetting().alignHorizontallyCenter();
		this.layout.addChild(new MultiLineTextWidget(this.title.copy().withStyle(ChatFormatting.BOLD), this.font).setMaxWidth(this.contentWidth).setCentered(true));
		if (this.image != null) {
			this.layout.addChild(ImageWidget.texture(130, 64, this.image, 130, 64));
		}

		this.layout.addChild(new MultiLineTextWidget(this.message, this.font).setMaxWidth(this.contentWidth).setCentered(true));
		this.layout.addChild(this.buildButtonRow());
		this.layout.visitWidgets(guiEventListener -> {
			AbstractWidget var10000 = this.addRenderableWidget(guiEventListener);
		});
		this.repositionElements();
	}

	private LinearLayout buildButtonRow() {
		int i = 6 * (this.buttons.size() - 1);
		int j = Math.min((this.contentWidth - i) / this.buttons.size(), 150);
		LinearLayout linearLayout = LinearLayout.horizontal();
		linearLayout.spacing(6);

		for (PopupScreen.ButtonOption buttonOption : this.buttons) {
			linearLayout.addChild(Button.builder(buttonOption.message(), button -> buttonOption.action().accept(this)).width(j).build());
		}

		return linearLayout;
	}

	@Override
	protected void repositionElements() {
		this.backgroundScreen.resize(this.minecraft, this.width, this.height);
		this.layout.arrangeElements();
		FrameLayout.centerInRectangle(this.layout, this.getRectangle());
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
		this.backgroundScreen.render(guiGraphics, -1, -1, f);
		guiGraphics.flush();
		RenderSystem.clear(256, Minecraft.ON_OSX);
		this.renderTransparentBackground(guiGraphics);
		guiGraphics.blitSprite(BACKGROUND_SPRITE, this.layout.getX() - 18, this.layout.getY() - 18, this.layout.getWidth() + 36, this.layout.getHeight() + 36);
	}

	@Override
	public Component getNarrationMessage() {
		return CommonComponents.joinForNarration(this.title, this.message);
	}

	@Override
	public void onClose() {
		if (this.onClose != null) {
			this.onClose.run();
		}

		this.minecraft.setScreen(this.backgroundScreen);
	}

	@Environment(EnvType.CLIENT)
	public static class Builder {
		private final Screen backgroundScreen;
		private final Component title;
		private Component message = CommonComponents.EMPTY;
		private int width = 250;
		@Nullable
		private ResourceLocation image;
		private final List<PopupScreen.ButtonOption> buttons = new ArrayList();
		@Nullable
		private Runnable onClose = null;

		public Builder(Screen screen, Component component) {
			this.backgroundScreen = screen;
			this.title = component;
		}

		public PopupScreen.Builder setWidth(int i) {
			this.width = i;
			return this;
		}

		public PopupScreen.Builder setImage(ResourceLocation resourceLocation) {
			this.image = resourceLocation;
			return this;
		}

		public PopupScreen.Builder setMessage(Component component) {
			this.message = component;
			return this;
		}

		public PopupScreen.Builder addButton(Component component, Consumer<PopupScreen> consumer) {
			this.buttons.add(new PopupScreen.ButtonOption(component, consumer));
			return this;
		}

		public PopupScreen.Builder onClose(Runnable runnable) {
			this.onClose = runnable;
			return this;
		}

		public PopupScreen build() {
			if (this.buttons.isEmpty()) {
				throw new IllegalStateException("Popup must have at least one button");
			} else {
				return new PopupScreen(this.backgroundScreen, this.width, this.image, this.title, this.message, List.copyOf(this.buttons), this.onClose);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	static record ButtonOption(Component message, Consumer<PopupScreen> action) {
	}
}
