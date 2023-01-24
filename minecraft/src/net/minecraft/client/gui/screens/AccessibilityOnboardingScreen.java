package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.text2speech.Narrator;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.AccessibilityOnboardingTextWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
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
	@Nullable
	private AccessibilityOnboardingTextWidget textWidget;

	public AccessibilityOnboardingScreen(Options options) {
		super(Component.translatable("accessibility.onboarding.screen.title"));
		this.options = options;
		this.logoRenderer = new LogoRenderer(true);
		this.narratorAvailable = Minecraft.getInstance().getNarrator().isActive();
	}

	@Override
	public void init() {
		FrameLayout frameLayout = new FrameLayout();
		frameLayout.defaultChildLayoutSetting().alignVerticallyTop().padding(4);
		frameLayout.setMinDimensions(this.width, this.height - this.initTitleYPos());
		GridLayout gridLayout = frameLayout.addChild(new GridLayout());
		gridLayout.defaultCellSetting().alignHorizontallyCenter().padding(4);
		GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(1);
		this.textWidget = new AccessibilityOnboardingTextWidget(this.font, this.title, this.width);
		rowHelper.addChild(this.textWidget, rowHelper.newCellSettings().padding(16));
		AbstractWidget abstractWidget = this.options.narrator().createButton(this.options, 0, 0, 150);
		abstractWidget.active = this.narratorAvailable;
		rowHelper.addChild(abstractWidget);
		if (this.narratorAvailable) {
			this.setInitialFocus(abstractWidget);
		}

		rowHelper.addChild(
			Button.builder(
					Component.translatable("options.accessibility.title"),
					button -> this.minecraft.setScreen(new AccessibilityOptionsScreen(new TitleScreen(true), this.minecraft.options))
				)
				.build()
		);
		frameLayout.addChild(
			Button.builder(CommonComponents.GUI_CONTINUE, button -> this.minecraft.setScreen(new TitleScreen(true, this.logoRenderer))).build(),
			frameLayout.newChildLayoutSettings().alignVerticallyBottom().padding(8)
		);
		frameLayout.arrangeElements();
		FrameLayout.alignInRectangle(frameLayout, 0, this.initTitleYPos(), this.width, this.height, 0.5F, 0.0F);
		frameLayout.visitWidgets(this::addRenderableWidget);
	}

	private int initTitleYPos() {
		return 90;
	}

	@Override
	public void onClose() {
		this.minecraft.getNarrator().clear();
		this.minecraft.setScreen(new TitleScreen(true, this.logoRenderer));
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.handleInitialNarrationDelay();
		this.panorama.render(0.0F, 1.0F);
		fill(poseStack, 0, 0, this.width, this.height, -1877995504);
		this.logoRenderer.renderLogo(poseStack, this.width, 1.0F);
		if (this.textWidget != null) {
			this.textWidget.render(poseStack, i, j, f);
		}

		super.render(poseStack, i, j, f);
	}

	private void handleInitialNarrationDelay() {
		if (!this.hasNarrated && this.narratorAvailable) {
			if (this.timer < 40.0F) {
				this.timer++;
			} else {
				Narrator.getNarrator().say(ONBOARDING_NARRATOR_MESSAGE.getString(), true);
				this.hasNarrated = true;
			}
		}
	}
}
