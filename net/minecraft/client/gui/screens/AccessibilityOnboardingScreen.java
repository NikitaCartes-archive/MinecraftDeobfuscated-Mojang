/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.text2speech.Narrator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.AccessibilityOnboardingTextWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommonButtons;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class AccessibilityOnboardingScreen
extends Screen {
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
        int i = this.initTitleYPos();
        FrameLayout frameLayout = new FrameLayout(this.width, this.height - i);
        frameLayout.defaultChildLayoutSetting().alignVerticallyTop().padding(4);
        GridLayout gridLayout = frameLayout.addChild(new GridLayout());
        gridLayout.defaultCellSetting().alignHorizontallyCenter().padding(4);
        GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(1);
        rowHelper.defaultCellSetting().padding(2);
        this.textWidget = new AccessibilityOnboardingTextWidget(this.font, this.title, this.width);
        rowHelper.addChild(this.textWidget, rowHelper.newCellSettings().paddingBottom(16));
        AbstractWidget abstractWidget = this.options.narrator().createButton(this.options, 0, 0, 150);
        abstractWidget.active = this.narratorAvailable;
        rowHelper.addChild(abstractWidget);
        if (this.narratorAvailable) {
            this.setInitialFocus(abstractWidget);
        }
        rowHelper.addChild(CommonButtons.accessibilityTextAndImage(button -> this.closeAndSetScreen(new AccessibilityOptionsScreen(this, this.minecraft.options))));
        rowHelper.addChild(CommonButtons.languageTextAndImage(button -> this.closeAndSetScreen(new LanguageSelectScreen((Screen)this, this.minecraft.options, this.minecraft.getLanguageManager()))));
        frameLayout.addChild(Button.builder(CommonComponents.GUI_CONTINUE, button -> this.onClose()).build(), frameLayout.newChildLayoutSettings().alignVerticallyBottom().padding(8));
        frameLayout.arrangeElements();
        FrameLayout.alignInRectangle(frameLayout, 0, i, this.width, this.height, 0.5f, 0.0f);
        frameLayout.visitWidgets(this::addRenderableWidget);
    }

    private int initTitleYPos() {
        return 90;
    }

    @Override
    public void onClose() {
        this.closeAndSetScreen(new TitleScreen(true, this.logoRenderer));
    }

    private void closeAndSetScreen(Screen screen) {
        this.options.onboardAccessibility = false;
        this.options.save();
        Narrator.getNarrator().clear();
        this.minecraft.setScreen(screen);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.handleInitialNarrationDelay();
        this.panorama.render(0.0f, 1.0f);
        AccessibilityOnboardingScreen.fill(poseStack, 0, 0, this.width, this.height, -1877995504);
        this.logoRenderer.renderLogo(poseStack, this.width, 1.0f);
        if (this.textWidget != null) {
            this.textWidget.render(poseStack, i, j, f);
        }
        super.render(poseStack, i, j, f);
    }

    private void handleInitialNarrationDelay() {
        if (!this.hasNarrated && this.narratorAvailable) {
            if (this.timer < 40.0f) {
                this.timer += 1.0f;
            } else if (this.minecraft.isWindowActive()) {
                Narrator.getNarrator().say(ONBOARDING_NARRATOR_MESSAGE.getString(), true);
                this.hasNarrated = true;
            }
        }
    }
}

