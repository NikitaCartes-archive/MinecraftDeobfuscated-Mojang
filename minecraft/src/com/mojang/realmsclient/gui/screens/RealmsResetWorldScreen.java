package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.WorldGenerationInfo;
import com.mojang.realmsclient.util.task.LongRunningTask;
import com.mojang.realmsclient.util.task.ResettingGeneratedWorldTask;
import com.mojang.realmsclient.util.task.ResettingTemplateWorldTask;
import com.mojang.realmsclient.util.task.SwitchSlotTask;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsResetWorldScreen extends RealmsScreen {
	static final Logger LOGGER = LogManager.getLogger();
	private final Screen lastScreen;
	private final RealmsServer serverData;
	private Component subtitle = new TranslatableComponent("mco.reset.world.warning");
	private Component buttonTitle = CommonComponents.GUI_CANCEL;
	private int subtitleColor = 16711680;
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
	private Component resetTitle = new TranslatableComponent("mco.reset.world.resetting.screen.title");
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
		this(screen, realmsServer, new TranslatableComponent("mco.reset.world.title"), runnable, runnable2);
	}

	public RealmsResetWorldScreen(
		Screen screen, RealmsServer realmsServer, Component component, Component component2, int i, Component component3, Runnable runnable, Runnable runnable2
	) {
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
		this.addRenderableWidget(new Button(this.width / 2 - 40, row(14) - 10, 80, 20, this.buttonTitle, button -> this.minecraft.setScreen(this.lastScreen)));
		(new Thread("Realms-reset-world-fetcher") {
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
				} catch (RealmsServiceException var6) {
					RealmsResetWorldScreen.LOGGER.error("Couldn't fetch templates in reset world", (Throwable)var6);
				}
			}
		}).start();
		this.addLabel(new RealmsLabel(this.subtitle, this.width / 2, 22, this.subtitleColor));
		this.addRenderableWidget(
			new RealmsResetWorldScreen.FrameButton(
				this.frame(1),
				row(0) + 10,
				new TranslatableComponent("mco.reset.world.generate"),
				NEW_WORLD_LOCATION,
				button -> this.minecraft.setScreen(new RealmsResetNormalWorldScreen(this::generationSelectionCallback, this.title))
			)
		);
		this.addRenderableWidget(
			new RealmsResetWorldScreen.FrameButton(
				this.frame(2),
				row(0) + 10,
				new TranslatableComponent("mco.reset.world.upload"),
				UPLOAD_LOCATION,
				button -> this.minecraft
						.setScreen(new RealmsSelectFileToUploadScreen(this.serverData.id, this.slot != -1 ? this.slot : this.serverData.activeSlot, this, this.callback))
			)
		);
		this.addRenderableWidget(
			new RealmsResetWorldScreen.FrameButton(
				this.frame(3),
				row(0) + 10,
				new TranslatableComponent("mco.reset.world.template"),
				SURVIVAL_SPAWN_LOCATION,
				button -> this.minecraft
						.setScreen(
							new RealmsSelectWorldTemplateScreen(
								new TranslatableComponent("mco.reset.world.template"), this::templateSelectionCallback, RealmsServer.WorldType.NORMAL, this.templates
							)
						)
			)
		);
		this.addRenderableWidget(
			new RealmsResetWorldScreen.FrameButton(
				this.frame(1),
				row(6) + 20,
				new TranslatableComponent("mco.reset.world.adventure"),
				ADVENTURE_MAP_LOCATION,
				button -> this.minecraft
						.setScreen(
							new RealmsSelectWorldTemplateScreen(
								new TranslatableComponent("mco.reset.world.adventure"), this::templateSelectionCallback, RealmsServer.WorldType.ADVENTUREMAP, this.adventuremaps
							)
						)
			)
		);
		this.addRenderableWidget(
			new RealmsResetWorldScreen.FrameButton(
				this.frame(2),
				row(6) + 20,
				new TranslatableComponent("mco.reset.world.experience"),
				EXPERIENCE_LOCATION,
				button -> this.minecraft
						.setScreen(
							new RealmsSelectWorldTemplateScreen(
								new TranslatableComponent("mco.reset.world.experience"), this::templateSelectionCallback, RealmsServer.WorldType.EXPERIENCE, this.experiences
							)
						)
			)
		);
		this.addRenderableWidget(
			new RealmsResetWorldScreen.FrameButton(
				this.frame(3),
				row(6) + 20,
				new TranslatableComponent("mco.reset.world.inspiration"),
				INSPIRATION_LOCATION,
				button -> this.minecraft
						.setScreen(
							new RealmsSelectWorldTemplateScreen(
								new TranslatableComponent("mco.reset.world.inspiration"), this::templateSelectionCallback, RealmsServer.WorldType.INSPIRATION, this.inspirations
							)
						)
			)
		);
	}

	@Override
	public Component getNarrationMessage() {
		return CommonComponents.joinForNarration(this.getTitle(), this.createLabelNarration());
	}

	@Override
	public void removed() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256) {
			this.minecraft.setScreen(this.lastScreen);
			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	private int frame(int i) {
		return this.width / 2 - 130 + (i - 1) * 100;
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 7, 16777215);
		super.render(poseStack, i, j, f);
	}

	void drawFrame(PoseStack poseStack, int i, int j, Component component, ResourceLocation resourceLocation, boolean bl, boolean bl2) {
		RenderSystem.setShaderTexture(0, resourceLocation);
		if (bl) {
			RenderSystem.setShaderColor(0.56F, 0.56F, 0.56F, 1.0F);
		} else {
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		}

		GuiComponent.blit(poseStack, i + 2, j + 14, 0.0F, 0.0F, 56, 56, 56, 56);
		RenderSystem.setShaderTexture(0, SLOT_FRAME_LOCATION);
		if (bl) {
			RenderSystem.setShaderColor(0.56F, 0.56F, 0.56F, 1.0F);
		} else {
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		}

		GuiComponent.blit(poseStack, i, j + 12, 0.0F, 0.0F, 60, 60, 60, 60);
		int k = bl ? 10526880 : 16777215;
		drawCenteredString(poseStack, this.font, component, i + 30, j, k);
	}

	private void startTask(LongRunningTask longRunningTask) {
		this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, longRunningTask));
	}

	public void switchSlot(Runnable runnable) {
		this.startTask(new SwitchSlotTask(this.serverData.id, this.slot, runnable));
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

	@Environment(EnvType.CLIENT)
	class FrameButton extends Button {
		private final ResourceLocation image;

		public FrameButton(int i, int j, Component component, ResourceLocation resourceLocation, Button.OnPress onPress) {
			super(i, j, 60, 72, component, onPress);
			this.image = resourceLocation;
		}

		@Override
		public void renderButton(PoseStack poseStack, int i, int j, float f) {
			RealmsResetWorldScreen.this.drawFrame(poseStack, this.x, this.y, this.getMessage(), this.image, this.isHovered(), this.isMouseOver((double)i, (double)j));
		}
	}
}
