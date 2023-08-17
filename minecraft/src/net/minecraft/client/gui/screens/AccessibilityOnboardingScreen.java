package net.minecraft.client.gui.screens;

import com.mojang.text2speech.Narrator;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommonButtons;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class AccessibilityOnboardingScreen extends Screen {
	private static final Component ONBOARDING_NARRATOR_MESSAGE = Component.translatable("accessibility.onboarding.screen.narrator");
	private static final int PADDING = 4;
	private static final int TITLE_PADDING = 16;
	private final PanoramaRenderer panorama = new PanoramaRenderer(TitleScreen.CUBE_MAP);
	private final LogoRenderer logoRenderer;
	private final Options options;
	private final boolean narratorAvailable;
	private boolean hasNarrated;
	private float timer;
	private final Runnable onClose;
	@Nullable
	private FocusableTextWidget textWidget;

	public AccessibilityOnboardingScreen(Options options, Runnable runnable) {
		super(Component.translatable("accessibility.onboarding.screen.title"));
		this.options = options;
		this.onClose = runnable;
		this.logoRenderer = new LogoRenderer(true);
		this.narratorAvailable = Minecraft.getInstance().getNarrator().isActive();
	}

	@Override
	public void init() {
		int i = this.initTitleYPos();
		FrameLayout frameLayout = new FrameLayout(this.width, this.height - i);
		frameLayout.defaultChildLayoutSetting().alignVerticallyTop().padding(4);
		LinearLayout linearLayout = frameLayout.addChild(LinearLayout.vertical());
		linearLayout.defaultCellSetting().alignHorizontallyCenter().padding(2);
		this.textWidget = new FocusableTextWidget(this.width - 16, this.title, this.font);
		linearLayout.addChild(this.textWidget, layoutSettings -> layoutSettings.paddingBottom(16));
		AbstractWidget abstractWidget = this.options.narrator().createButton(this.options, 0, 0, 150);
		abstractWidget.active = this.narratorAvailable;
		linearLayout.addChild(abstractWidget);
		if (this.narratorAvailable) {
			this.setInitialFocus(abstractWidget);
		}

		linearLayout.addChild(CommonButtons.accessibility(150, button -> this.closeAndSetScreen(new AccessibilityOptionsScreen(this, this.minecraft.options)), false));
		linearLayout.addChild(
			CommonButtons.language(
				150, button -> this.closeAndSetScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager())), false
			)
		);
		frameLayout.addChild(
			Button.builder(CommonComponents.GUI_CONTINUE, button -> this.onClose()).build(), frameLayout.newChildLayoutSettings().alignVerticallyBottom().padding(8)
		);
		frameLayout.arrangeElements();
		FrameLayout.alignInRectangle(frameLayout, 0, i, this.width, this.height, 0.5F, 0.0F);
		frameLayout.visitWidgets(this::addRenderableWidget);
	}

	private int initTitleYPos() {
		return 90;
	}

	@Override
	public void onClose() {
		this.close(this.onClose);
	}

	private void closeAndSetScreen(Screen screen) {
		this.close(() -> this.minecraft.setScreen(screen));
	}

	private void close(Runnable runnable) {
		this.options.onboardAccessibility = false;
		this.options.save();
		Narrator.getNarrator().clear();
		runnable.run();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		this.handleInitialNarrationDelay();
		this.logoRenderer.renderLogo(guiGraphics, this.width, 1.0F);
		if (this.textWidget != null) {
			this.textWidget.render(guiGraphics, i, j, f);
		}
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
		this.panorama.render(0.0F, 1.0F);
		guiGraphics.fill(0, 0, this.width, this.height, -1877995504);
	}

	private void handleInitialNarrationDelay() {
		if (!this.hasNarrated && this.narratorAvailable) {
			if (this.timer < 40.0F) {
				this.timer++;
			} else if (this.minecraft.isWindowActive()) {
				Narrator.getNarrator().say(ONBOARDING_NARRATOR_MESSAGE.getString(), true);
				this.hasNarrated = true;
			}
		}
	}
}
