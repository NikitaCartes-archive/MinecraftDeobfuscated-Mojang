/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsResetNormalWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsSelectFileToUploadScreen;
import com.mojang.realmsclient.gui.screens.RealmsSelectWorldTemplateScreen;
import com.mojang.realmsclient.util.WorldGenerationInfo;
import com.mojang.realmsclient.util.task.LongRunningTask;
import com.mojang.realmsclient.util.task.ResettingGeneratedWorldTask;
import com.mojang.realmsclient.util.task.ResettingTemplateWorldTask;
import com.mojang.realmsclient.util.task.SwitchSlotTask;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsResetWorldScreen
extends RealmsScreen {
    static final Logger LOGGER = LogUtils.getLogger();
    private final Screen lastScreen;
    private final RealmsServer serverData;
    private Component subtitle = Component.translatable("mco.reset.world.warning");
    private Component buttonTitle = CommonComponents.GUI_CANCEL;
    private int subtitleColor = 0xFF0000;
    private static final ResourceLocation SLOT_FRAME_LOCATION = new ResourceLocation("realms", "textures/gui/realms/slot_frame.png");
    private static final ResourceLocation UPLOAD_LOCATION = new ResourceLocation("realms", "textures/gui/realms/upload.png");
    private static final ResourceLocation ADVENTURE_MAP_LOCATION = new ResourceLocation("realms", "textures/gui/realms/adventure.png");
    private static final ResourceLocation SURVIVAL_SPAWN_LOCATION = new ResourceLocation("realms", "textures/gui/realms/survival_spawn.png");
    private static final ResourceLocation NEW_WORLD_LOCATION = new ResourceLocation("realms", "textures/gui/realms/new_world.png");
    private static final ResourceLocation EXPERIENCE_LOCATION = new ResourceLocation("realms", "textures/gui/realms/experience.png");
    private static final ResourceLocation INSPIRATION_LOCATION = new ResourceLocation("realms", "textures/gui/realms/inspiration.png");
    WorldTemplatePaginatedList templates;
    WorldTemplatePaginatedList adventuremaps;
    WorldTemplatePaginatedList experiences;
    WorldTemplatePaginatedList inspirations;
    public int slot = -1;
    private Component resetTitle = Component.translatable("mco.reset.world.resetting.screen.title");
    private final Runnable resetWorldRunnable;
    private final Runnable callback;

    public RealmsResetWorldScreen(Screen screen, RealmsServer realmsServer, Component component, Runnable runnable, Runnable runnable2) {
        super(component);
        this.lastScreen = screen;
        this.serverData = realmsServer;
        this.resetWorldRunnable = runnable;
        this.callback = runnable2;
    }

    public RealmsResetWorldScreen(Screen screen, RealmsServer realmsServer, Runnable runnable, Runnable runnable2) {
        this(screen, realmsServer, Component.translatable("mco.reset.world.title"), runnable, runnable2);
    }

    public RealmsResetWorldScreen(Screen screen, RealmsServer realmsServer, Component component, Component component2, int i, Component component3, Runnable runnable, Runnable runnable2) {
        this(screen, realmsServer, component, runnable, runnable2);
        this.subtitle = component2;
        this.subtitleColor = i;
        this.buttonTitle = component3;
    }

    public void setSlot(int i) {
        this.slot = i;
    }

    public void setResetTitle(Component component) {
        this.resetTitle = component;
    }

    @Override
    public void init() {
        this.addRenderableWidget(Button.builder(this.buttonTitle, button -> this.minecraft.setScreen(this.lastScreen)).bounds(this.width / 2 - 40, RealmsResetWorldScreen.row(14) - 10, 80, 20).build());
        new Thread("Realms-reset-world-fetcher"){

            @Override
            public void run() {
                RealmsClient realmsClient = RealmsClient.create();
                try {
                    WorldTemplatePaginatedList worldTemplatePaginatedList = realmsClient.fetchWorldTemplates(1, 10, RealmsServer.WorldType.NORMAL);
                    WorldTemplatePaginatedList worldTemplatePaginatedList2 = realmsClient.fetchWorldTemplates(1, 10, RealmsServer.WorldType.ADVENTUREMAP);
                    WorldTemplatePaginatedList worldTemplatePaginatedList3 = realmsClient.fetchWorldTemplates(1, 10, RealmsServer.WorldType.EXPERIENCE);
                    WorldTemplatePaginatedList worldTemplatePaginatedList4 = realmsClient.fetchWorldTemplates(1, 10, RealmsServer.WorldType.INSPIRATION);
                    RealmsResetWorldScreen.this.minecraft.execute(() -> {
                        RealmsResetWorldScreen.this.templates = worldTemplatePaginatedList;
                        RealmsResetWorldScreen.this.adventuremaps = worldTemplatePaginatedList2;
                        RealmsResetWorldScreen.this.experiences = worldTemplatePaginatedList3;
                        RealmsResetWorldScreen.this.inspirations = worldTemplatePaginatedList4;
                    });
                } catch (RealmsServiceException realmsServiceException) {
                    LOGGER.error("Couldn't fetch templates in reset world", realmsServiceException);
                }
            }
        }.start();
        this.addLabel(new RealmsLabel(this.subtitle, this.width / 2, 22, this.subtitleColor));
        this.addRenderableWidget(new FrameButton(this.frame(1), RealmsResetWorldScreen.row(0) + 10, Component.translatable("mco.reset.world.generate"), NEW_WORLD_LOCATION, button -> this.minecraft.setScreen(new RealmsResetNormalWorldScreen(this::generationSelectionCallback, this.title))));
        this.addRenderableWidget(new FrameButton(this.frame(2), RealmsResetWorldScreen.row(0) + 10, Component.translatable("mco.reset.world.upload"), UPLOAD_LOCATION, button -> this.minecraft.setScreen(new RealmsSelectFileToUploadScreen(this.serverData.id, this.slot != -1 ? this.slot : this.serverData.activeSlot, this, this.callback))));
        this.addRenderableWidget(new FrameButton(this.frame(3), RealmsResetWorldScreen.row(0) + 10, Component.translatable("mco.reset.world.template"), SURVIVAL_SPAWN_LOCATION, button -> this.minecraft.setScreen(new RealmsSelectWorldTemplateScreen(Component.translatable("mco.reset.world.template"), this::templateSelectionCallback, RealmsServer.WorldType.NORMAL, this.templates))));
        this.addRenderableWidget(new FrameButton(this.frame(1), RealmsResetWorldScreen.row(6) + 20, Component.translatable("mco.reset.world.adventure"), ADVENTURE_MAP_LOCATION, button -> this.minecraft.setScreen(new RealmsSelectWorldTemplateScreen(Component.translatable("mco.reset.world.adventure"), this::templateSelectionCallback, RealmsServer.WorldType.ADVENTUREMAP, this.adventuremaps))));
        this.addRenderableWidget(new FrameButton(this.frame(2), RealmsResetWorldScreen.row(6) + 20, Component.translatable("mco.reset.world.experience"), EXPERIENCE_LOCATION, button -> this.minecraft.setScreen(new RealmsSelectWorldTemplateScreen(Component.translatable("mco.reset.world.experience"), this::templateSelectionCallback, RealmsServer.WorldType.EXPERIENCE, this.experiences))));
        this.addRenderableWidget(new FrameButton(this.frame(3), RealmsResetWorldScreen.row(6) + 20, Component.translatable("mco.reset.world.inspiration"), INSPIRATION_LOCATION, button -> this.minecraft.setScreen(new RealmsSelectWorldTemplateScreen(Component.translatable("mco.reset.world.inspiration"), this::templateSelectionCallback, RealmsServer.WorldType.INSPIRATION, this.inspirations))));
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(this.getTitle(), this.createLabelNarration());
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 256) {
            this.minecraft.setScreen(this.lastScreen);
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    private int frame(int i) {
        return this.width / 2 - 130 + (i - 1) * 100;
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        RealmsResetWorldScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 7, 0xFFFFFF);
        super.render(poseStack, i, j, f);
    }

    void drawFrame(PoseStack poseStack, int i, int j, Component component, ResourceLocation resourceLocation, boolean bl, boolean bl2) {
        RenderSystem.setShaderTexture(0, resourceLocation);
        if (bl) {
            RenderSystem.setShaderColor(0.56f, 0.56f, 0.56f, 1.0f);
        } else {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
        GuiComponent.blit(poseStack, i + 2, j + 14, 0.0f, 0.0f, 56, 56, 56, 56);
        RenderSystem.setShaderTexture(0, SLOT_FRAME_LOCATION);
        if (bl) {
            RenderSystem.setShaderColor(0.56f, 0.56f, 0.56f, 1.0f);
        } else {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
        GuiComponent.blit(poseStack, i, j + 12, 0.0f, 0.0f, 60, 60, 60, 60);
        int k = bl ? 0xA0A0A0 : 0xFFFFFF;
        RealmsResetWorldScreen.drawCenteredString(poseStack, this.font, component, i + 30, j, k);
    }

    private void startTask(LongRunningTask longRunningTask) {
        this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, longRunningTask));
    }

    public void switchSlot(Runnable runnable) {
        this.startTask(new SwitchSlotTask(this.serverData.id, this.slot, () -> this.minecraft.execute(runnable)));
    }

    private void templateSelectionCallback(@Nullable WorldTemplate worldTemplate) {
        this.minecraft.setScreen(this);
        if (worldTemplate != null) {
            this.resetWorld(() -> this.startTask(new ResettingTemplateWorldTask(worldTemplate, this.serverData.id, this.resetTitle, this.resetWorldRunnable)));
        }
    }

    private void generationSelectionCallback(@Nullable WorldGenerationInfo worldGenerationInfo) {
        this.minecraft.setScreen(this);
        if (worldGenerationInfo != null) {
            this.resetWorld(() -> this.startTask(new ResettingGeneratedWorldTask(worldGenerationInfo, this.serverData.id, this.resetTitle, this.resetWorldRunnable)));
        }
    }

    private void resetWorld(Runnable runnable) {
        if (this.slot == -1) {
            runnable.run();
        } else {
            this.switchSlot(runnable);
        }
    }

    @Environment(value=EnvType.CLIENT)
    class FrameButton
    extends Button {
        private final ResourceLocation image;

        public FrameButton(int i, int j, Component component, ResourceLocation resourceLocation, Button.OnPress onPress) {
            super(i, j, 60, 72, component, onPress, DEFAULT_NARRATION);
            this.image = resourceLocation;
        }

        @Override
        public void renderButton(PoseStack poseStack, int i, int j, float f) {
            RealmsResetWorldScreen.this.drawFrame(poseStack, this.getX(), this.getY(), this.getMessage(), this.image, this.isHoveredOrFocused(), this.isMouseOver(i, j));
        }
    }
}

