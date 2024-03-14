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
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class AccessibilityOnboardingScreen extends Screen {
	private static final Component TITLE = Component.translatable("accessibility.onboarding.screen.title");
	private static final Component ONBOARDING_NARRATOR_MESSAGE = Component.translatable("accessibility.onboarding.screen.narrator");
	private static final int PADDING = 4;
	private static final int TITLE_PADDING = 16;
	private final LogoRenderer logoRenderer;
	private final Options options;
	private final boolean narratorAvailable;
	private boolean hasNarrated;
	private float timer;
	private final Runnable onClose;
	@Nullable
	private FocusableTextWidget textWidget;
	@Nullable
	private AbstractWidget narrationButton;
	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, this.initTitleYPos(), 33);

	public AccessibilityOnboardingScreen(Options options, Runnable runnable) {
		super(TITLE);
		this.options = options;
		this.onClose = runnable;
		this.logoRenderer = new LogoRenderer(true);
		this.narratorAvailable = Minecraft.getInstance().getNarrator().isActive();
	}

	@Override
	public void init() {
		LinearLayout linearLayout = this.layout.addToContents(LinearLayout.vertical());
		linearLayout.defaultCellSetting().alignHorizontallyCenter().padding(4);
		this.textWidget = linearLayout.addChild(new FocusableTextWidget(this.width, this.title, this.font), layoutSettings -> layoutSettings.padding(8));
		this.narrationButton = this.options.narrator().createButton(this.options);
		this.narrationButton.active = this.narratorAvailable;
		linearLayout.addChild(this.narrationButton);
		linearLayout.addChild(CommonButtons.accessibility(150, button -> this.closeAndSetScreen(new AccessibilityOptionsScreen(this, this.minecraft.options)), false));
		linearLayout.addChild(
			CommonButtons.language(
				150, button -> this.closeAndSetScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager())), false
			)
		);
		this.layout.addToFooter(Button.builder(CommonComponents.GUI_CONTINUE, button -> this.onClose()).build());
		this.layout.visitWidgets(this::addRenderableWidget);
		this.repositionElements();
	}

	@Override
	protected void repositionElements() {
		if (this.textWidget != null) {
			this.textWidget.containWithin(this.width);
		}

		this.layout.arrangeElements();
	}

	@Override
	protected void setInitialFocus() {
		if (this.narratorAvailable && this.narrationButton != null) {
			this.setInitialFocus(this.narrationButton);
		} else {
			super.setInitialFocus();
		}
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
	}

	@Override
	protected void renderPanorama(GuiGraphics guiGraphics, float f) {
		PANORAMA.render(guiGraphics, this.width, this.height, 1.0F, 0.0F);
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
