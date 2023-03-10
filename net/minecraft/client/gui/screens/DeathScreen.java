/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class DeathScreen
extends Screen {
    private int delayTicker;
    private final Component causeOfDeath;
    private final boolean hardcore;
    private Component deathScore;
    private final List<Button> exitButtons = Lists.newArrayList();
    @Nullable
    private Button exitToTitleButton;

    public DeathScreen(@Nullable Component component, boolean bl) {
        super(Component.translatable(bl ? "deathScreen.title.hardcore" : "deathScreen.title"));
        this.causeOfDeath = component;
        this.hardcore = bl;
    }

    @Override
    protected void init() {
        this.delayTicker = 0;
        this.exitButtons.clear();
        MutableComponent component = this.hardcore ? Component.translatable("deathScreen.spectate") : Component.translatable("deathScreen.respawn");
        this.exitButtons.add(this.addRenderableWidget(Button.builder(component, button -> {
            this.minecraft.player.respawn();
            button.active = false;
        }).bounds(this.width / 2 - 100, this.height / 4 + 72, 200, 20).build()));
        this.exitToTitleButton = this.addRenderableWidget(Button.builder(Component.translatable("deathScreen.titleScreen"), button -> this.minecraft.getReportingContext().draftReportHandled(this.minecraft, this, this::handleExitToTitleScreen, true)).bounds(this.width / 2 - 100, this.height / 4 + 96, 200, 20).build());
        this.exitButtons.add(this.exitToTitleButton);
        this.setButtonsActive(false);
        this.deathScore = Component.translatable("deathScreen.score").append(": ").append(Component.literal(Integer.toString(this.minecraft.player.getScore())).withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private void handleExitToTitleScreen() {
        if (this.hardcore) {
            this.exitToTitleScreen();
            return;
        }
        TitleConfirmScreen confirmScreen = new TitleConfirmScreen(bl -> {
            if (bl) {
                this.exitToTitleScreen();
            } else {
                this.minecraft.player.respawn();
                this.minecraft.setScreen(null);
            }
        }, Component.translatable("deathScreen.quit.confirm"), CommonComponents.EMPTY, Component.translatable("deathScreen.titleScreen"), Component.translatable("deathScreen.respawn"));
        this.minecraft.setScreen(confirmScreen);
        confirmScreen.setDelay(20);
    }

    private void exitToTitleScreen() {
        if (this.minecraft.level != null) {
            this.minecraft.level.disconnect();
        }
        this.minecraft.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
        this.minecraft.setScreen(new TitleScreen());
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        DeathScreen.fillGradient(poseStack, 0, 0, this.width, this.height, 0x60500000, -1602211792);
        poseStack.pushPose();
        poseStack.scale(2.0f, 2.0f, 2.0f);
        DeathScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2 / 2, 30, 0xFFFFFF);
        poseStack.popPose();
        if (this.causeOfDeath != null) {
            DeathScreen.drawCenteredString(poseStack, this.font, this.causeOfDeath, this.width / 2, 85, 0xFFFFFF);
        }
        DeathScreen.drawCenteredString(poseStack, this.font, this.deathScore, this.width / 2, 100, 0xFFFFFF);
        if (this.causeOfDeath != null && j > 85 && j < 85 + this.font.lineHeight) {
            Style style = this.getClickedComponentStyleAt(i);
            this.renderComponentHoverEffect(poseStack, style, i, j);
        }
        super.render(poseStack, i, j, f);
        if (this.exitToTitleButton != null && this.minecraft.getReportingContext().hasDraftReport()) {
            RenderSystem.setShaderTexture(0, AbstractWidget.WIDGETS_LOCATION);
            DeathScreen.blit(poseStack, this.exitToTitleButton.getX() + this.exitToTitleButton.getWidth() - 17, this.exitToTitleButton.getY() + 3, 182, 24, 15, 15);
        }
    }

    @Nullable
    private Style getClickedComponentStyleAt(int i) {
        if (this.causeOfDeath == null) {
            return null;
        }
        int j = this.minecraft.font.width(this.causeOfDeath);
        int k = this.width / 2 - j / 2;
        int l = this.width / 2 + j / 2;
        if (i < k || i > l) {
            return null;
        }
        return this.minecraft.font.getSplitter().componentStyleAtWidth(this.causeOfDeath, i - k);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        Style style;
        if (this.causeOfDeath != null && e > 85.0 && e < (double)(85 + this.font.lineHeight) && (style = this.getClickedComponentStyleAt((int)d)) != null && style.getClickEvent() != null && style.getClickEvent().getAction() == ClickEvent.Action.OPEN_URL) {
            this.handleComponentClicked(style);
            return false;
        }
        return super.mouseClicked(d, e, i);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        ++this.delayTicker;
        if (this.delayTicker == 20) {
            this.setButtonsActive(true);
        }
    }

    private void setButtonsActive(boolean bl) {
        for (Button button : this.exitButtons) {
            button.active = bl;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class TitleConfirmScreen
    extends ConfirmScreen {
        public TitleConfirmScreen(BooleanConsumer booleanConsumer, Component component, Component component2, Component component3, Component component4) {
            super(booleanConsumer, component, component2, component3, component4);
        }
    }
}

