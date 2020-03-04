package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.task.ResettingWorldTask;
import com.mojang.realmsclient.util.task.SwitchSlotTask;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsResetWorldScreen extends RealmsScreenWithCallback {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Screen lastScreen;
	private final RealmsServer serverData;
	private RealmsLabel titleLabel;
	private RealmsLabel subtitleLabel;
	private String title = I18n.get("mco.reset.world.title");
	private String subtitle = I18n.get("mco.reset.world.warning");
	private String buttonTitle = I18n.get("gui.cancel");
	private int subtitleColor = 16711680;
	private static final ResourceLocation SLOT_FRAME_LOCATION = new ResourceLocation("realms", "textures/gui/realms/slot_frame.png");
	private static final ResourceLocation UPLOAD_LOCATION = new ResourceLocation("realms", "textures/gui/realms/upload.png");
	private static final ResourceLocation ADVENTURE_MAP_LOCATION = new ResourceLocation("realms", "textures/gui/realms/adventure.png");
	private static final ResourceLocation SURVIVAL_SPAWN_LOCATION = new ResourceLocation("realms", "textures/gui/realms/survival_spawn.png");
	private static final ResourceLocation NEW_WORLD_LOCATION = new ResourceLocation("realms", "textures/gui/realms/new_world.png");
	private static final ResourceLocation EXPERIENCE_LOCATION = new ResourceLocation("realms", "textures/gui/realms/experience.png");
	private static final ResourceLocation INSPIRATION_LOCATION = new ResourceLocation("realms", "textures/gui/realms/inspiration.png");
	private WorldTemplatePaginatedList templates;
	private WorldTemplatePaginatedList adventuremaps;
	private WorldTemplatePaginatedList experiences;
	private WorldTemplatePaginatedList inspirations;
	public int slot = -1;
	private RealmsResetWorldScreen.ResetType typeToReset = RealmsResetWorldScreen.ResetType.NONE;
	private RealmsResetWorldScreen.ResetWorldInfo worldInfoToReset;
	private WorldTemplate worldTemplateToReset;
	private String resetTitle;
	private final Runnable resetWorldRunnable;
	private final Runnable callback;

	public RealmsResetWorldScreen(Screen screen, RealmsServer realmsServer, Runnable runnable, Runnable runnable2) {
		this.lastScreen = screen;
		this.serverData = realmsServer;
		this.resetWorldRunnable = runnable;
		this.callback = runnable2;
	}

	public RealmsResetWorldScreen(
		Screen screen, RealmsServer realmsServer, String string, String string2, int i, String string3, Runnable runnable, Runnable runnable2
	) {
		this(screen, realmsServer, runnable, runnable2);
		this.title = string;
		this.subtitle = string2;
		this.subtitleColor = i;
		this.buttonTitle = string3;
	}

	public void setSlot(int i) {
		this.slot = i;
	}

	public void setResetTitle(String string) {
		this.resetTitle = string;
	}

	@Override
	public void init() {
		this.addButton(new Button(this.width / 2 - 40, row(14) - 10, 80, 20, this.buttonTitle, button -> this.minecraft.setScreen(this.lastScreen)));
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
		this.titleLabel = this.addWidget(new RealmsLabel(this.title, this.width / 2, 7, 16777215));
		this.subtitleLabel = this.addWidget(new RealmsLabel(this.subtitle, this.width / 2, 22, this.subtitleColor));
		this.addButton(
			new RealmsResetWorldScreen.FrameButton(
				this.frame(1),
				row(0) + 10,
				I18n.get("mco.reset.world.generate"),
				NEW_WORLD_LOCATION,
				button -> this.minecraft.setScreen(new RealmsResetNormalWorldScreen(this, this.title))
			)
		);
		this.addButton(new RealmsResetWorldScreen.FrameButton(this.frame(2), row(0) + 10, I18n.get("mco.reset.world.upload"), UPLOAD_LOCATION, button -> {
			Screen screen = new RealmsSelectFileToUploadScreen(this.serverData.id, this.slot != -1 ? this.slot : this.serverData.activeSlot, this, this.callback);
			this.minecraft.setScreen(screen);
		}));
		this.addButton(new RealmsResetWorldScreen.FrameButton(this.frame(3), row(0) + 10, I18n.get("mco.reset.world.template"), SURVIVAL_SPAWN_LOCATION, button -> {
			RealmsSelectWorldTemplateScreen realmsSelectWorldTemplateScreen = new RealmsSelectWorldTemplateScreen(this, RealmsServer.WorldType.NORMAL, this.templates);
			realmsSelectWorldTemplateScreen.setTitle(I18n.get("mco.reset.world.template"));
			this.minecraft.setScreen(realmsSelectWorldTemplateScreen);
		}));
		this.addButton(
			new RealmsResetWorldScreen.FrameButton(
				this.frame(1),
				row(6) + 20,
				I18n.get("mco.reset.world.adventure"),
				ADVENTURE_MAP_LOCATION,
				button -> {
					RealmsSelectWorldTemplateScreen realmsSelectWorldTemplateScreen = new RealmsSelectWorldTemplateScreen(
						this, RealmsServer.WorldType.ADVENTUREMAP, this.adventuremaps
					);
					realmsSelectWorldTemplateScreen.setTitle(I18n.get("mco.reset.world.adventure"));
					this.minecraft.setScreen(realmsSelectWorldTemplateScreen);
				}
			)
		);
		this.addButton(
			new RealmsResetWorldScreen.FrameButton(
				this.frame(2),
				row(6) + 20,
				I18n.get("mco.reset.world.experience"),
				EXPERIENCE_LOCATION,
				button -> {
					RealmsSelectWorldTemplateScreen realmsSelectWorldTemplateScreen = new RealmsSelectWorldTemplateScreen(
						this, RealmsServer.WorldType.EXPERIENCE, this.experiences
					);
					realmsSelectWorldTemplateScreen.setTitle(I18n.get("mco.reset.world.experience"));
					this.minecraft.setScreen(realmsSelectWorldTemplateScreen);
				}
			)
		);
		this.addButton(
			new RealmsResetWorldScreen.FrameButton(
				this.frame(3),
				row(6) + 20,
				I18n.get("mco.reset.world.inspiration"),
				INSPIRATION_LOCATION,
				button -> {
					RealmsSelectWorldTemplateScreen realmsSelectWorldTemplateScreen = new RealmsSelectWorldTemplateScreen(
						this, RealmsServer.WorldType.INSPIRATION, this.inspirations
					);
					realmsSelectWorldTemplateScreen.setTitle(I18n.get("mco.reset.world.inspiration"));
					this.minecraft.setScreen(realmsSelectWorldTemplateScreen);
				}
			)
		);
		this.narrateLabels();
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
	public void render(int i, int j, float f) {
		this.renderBackground();
		this.titleLabel.render(this);
		this.subtitleLabel.render(this);
		super.render(i, j, f);
	}

	private void drawFrame(int i, int j, String string, ResourceLocation resourceLocation, boolean bl, boolean bl2) {
		this.minecraft.getTextureManager().bind(resourceLocation);
		if (bl) {
			RenderSystem.color4f(0.56F, 0.56F, 0.56F, 1.0F);
		} else {
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		}

		GuiComponent.blit(i + 2, j + 14, 0.0F, 0.0F, 56, 56, 56, 56);
		this.minecraft.getTextureManager().bind(SLOT_FRAME_LOCATION);
		if (bl) {
			RenderSystem.color4f(0.56F, 0.56F, 0.56F, 1.0F);
		} else {
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		}

		GuiComponent.blit(i, j + 12, 0.0F, 0.0F, 60, 60, 60, 60);
		int k = bl ? 10526880 : 16777215;
		this.drawCenteredString(this.font, string, i + 30, j, k);
	}

	@Override
	protected void callback(@Nullable WorldTemplate worldTemplate) {
		if (worldTemplate != null) {
			if (this.slot == -1) {
				this.resetWorldWithTemplate(worldTemplate);
			} else {
				switch (worldTemplate.type) {
					case WORLD_TEMPLATE:
						this.typeToReset = RealmsResetWorldScreen.ResetType.SURVIVAL_SPAWN;
						break;
					case ADVENTUREMAP:
						this.typeToReset = RealmsResetWorldScreen.ResetType.ADVENTURE;
						break;
					case EXPERIENCE:
						this.typeToReset = RealmsResetWorldScreen.ResetType.EXPERIENCE;
						break;
					case INSPIRATION:
						this.typeToReset = RealmsResetWorldScreen.ResetType.INSPIRATION;
				}

				this.worldTemplateToReset = worldTemplate;
				this.switchSlot();
			}
		}
	}

	private void switchSlot() {
		this.switchSlot(() -> {
			switch (this.typeToReset) {
				case ADVENTURE:
				case SURVIVAL_SPAWN:
				case EXPERIENCE:
				case INSPIRATION:
					if (this.worldTemplateToReset != null) {
						this.resetWorldWithTemplate(this.worldTemplateToReset);
					}
					break;
				case GENERATE:
					if (this.worldInfoToReset != null) {
						this.triggerResetWorld(this.worldInfoToReset);
					}
			}
		});
	}

	public void switchSlot(Runnable runnable) {
		this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, new SwitchSlotTask(this.serverData.id, this.slot, runnable)));
	}

	public void resetWorldWithTemplate(WorldTemplate worldTemplate) {
		this.resetWorld(null, worldTemplate, -1, true);
	}

	private void triggerResetWorld(RealmsResetWorldScreen.ResetWorldInfo resetWorldInfo) {
		this.resetWorld(resetWorldInfo.seed, null, resetWorldInfo.levelType, resetWorldInfo.generateStructures);
	}

	private void resetWorld(@Nullable String string, @Nullable WorldTemplate worldTemplate, int i, boolean bl) {
		this.minecraft
			.setScreen(
				new RealmsLongRunningMcoTaskScreen(
					this.lastScreen, new ResettingWorldTask(string, worldTemplate, i, bl, this.serverData.id, this.resetTitle, this.resetWorldRunnable)
				)
			);
	}

	public void resetWorld(RealmsResetWorldScreen.ResetWorldInfo resetWorldInfo) {
		if (this.slot == -1) {
			this.triggerResetWorld(resetWorldInfo);
		} else {
			this.typeToReset = RealmsResetWorldScreen.ResetType.GENERATE;
			this.worldInfoToReset = resetWorldInfo;
			this.switchSlot();
		}
	}

	@Environment(EnvType.CLIENT)
	class FrameButton extends Button {
		private final ResourceLocation image;

		public FrameButton(int i, int j, String string, ResourceLocation resourceLocation, Button.OnPress onPress) {
			super(i, j, 60, 72, string, onPress);
			this.image = resourceLocation;
		}

		@Override
		public void renderButton(int i, int j, float f) {
			RealmsResetWorldScreen.this.drawFrame(this.x, this.y, this.getMessage(), this.image, this.isHovered(), this.isMouseOver((double)i, (double)j));
		}
	}

	@Environment(EnvType.CLIENT)
	static enum ResetType {
		NONE,
		GENERATE,
		UPLOAD,
		ADVENTURE,
		SURVIVAL_SPAWN,
		EXPERIENCE,
		INSPIRATION;
	}

	@Environment(EnvType.CLIENT)
	public static class ResetWorldInfo {
		private final String seed;
		private final int levelType;
		private final boolean generateStructures;

		public ResetWorldInfo(String string, int i, boolean bl) {
			this.seed = string;
			this.levelType = i;
			this.generateStructures = bl;
		}
	}
}
