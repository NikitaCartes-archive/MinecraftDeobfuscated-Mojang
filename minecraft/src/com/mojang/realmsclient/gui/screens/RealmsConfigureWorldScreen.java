package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsWorldSlotButton;
import com.mojang.realmsclient.util.task.CloseServerTask;
import com.mojang.realmsclient.util.task.OpenServerTask;
import com.mojang.realmsclient.util.task.SwitchMinigameTask;
import com.mojang.realmsclient.util.task.SwitchSlotTask;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsConfigureWorldScreen extends RealmsScreen {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ResourceLocation ON_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/on_icon.png");
	private static final ResourceLocation OFF_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/off_icon.png");
	private static final ResourceLocation EXPIRED_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/expired_icon.png");
	private static final ResourceLocation EXPIRES_SOON_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/expires_soon_icon.png");
	private static final Component WORLD_LIST_TITLE = new TranslatableComponent("mco.configure.worlds.title");
	private static final Component TITLE = new TranslatableComponent("mco.configure.world.title");
	private static final Component MINIGAME_PREFIX = new TranslatableComponent("mco.configure.current.minigame").append(": ");
	private static final Component SERVER_EXPIRED_TOOLTIP = new TranslatableComponent("mco.selectServer.expired");
	private static final Component SERVER_EXPIRING_SOON_TOOLTIP = new TranslatableComponent("mco.selectServer.expires.soon");
	private static final Component SERVER_EXPIRING_IN_DAY_TOOLTIP = new TranslatableComponent("mco.selectServer.expires.day");
	private static final Component SERVER_OPEN_TOOLTIP = new TranslatableComponent("mco.selectServer.open");
	private static final Component SERVER_CLOSED_TOOLTIP = new TranslatableComponent("mco.selectServer.closed");
	private static final int DEFAULT_BUTTON_WIDTH = 80;
	private static final int DEFAULT_BUTTON_OFFSET = 5;
	@Nullable
	private Component toolTip;
	private final RealmsMainScreen lastScreen;
	@Nullable
	private RealmsServer serverData;
	private final long serverId;
	private int leftX;
	private int rightX;
	private Button playersButton;
	private Button settingsButton;
	private Button subscriptionButton;
	private Button optionsButton;
	private Button backupButton;
	private Button resetWorldButton;
	private Button switchMinigameButton;
	private boolean stateChanged;
	private int animTick;
	private int clicks;
	private final List<RealmsWorldSlotButton> slotButtonList = Lists.<RealmsWorldSlotButton>newArrayList();

	public RealmsConfigureWorldScreen(RealmsMainScreen realmsMainScreen, long l) {
		super(TITLE);
		this.lastScreen = realmsMainScreen;
		this.serverId = l;
	}

	@Override
	public void init() {
		if (this.serverData == null) {
			this.fetchServerData(this.serverId);
		}

		this.leftX = this.width / 2 - 187;
		this.rightX = this.width / 2 + 190;
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.playersButton = this.addRenderableWidget(
			new Button(
				this.centerButton(0, 3),
				row(0),
				100,
				20,
				new TranslatableComponent("mco.configure.world.buttons.players"),
				button -> this.minecraft.setScreen(new RealmsPlayerScreen(this, this.serverData))
			)
		);
		this.settingsButton = this.addRenderableWidget(
			new Button(
				this.centerButton(1, 3),
				row(0),
				100,
				20,
				new TranslatableComponent("mco.configure.world.buttons.settings"),
				button -> this.minecraft.setScreen(new RealmsSettingsScreen(this, this.serverData.clone()))
			)
		);
		this.subscriptionButton = this.addRenderableWidget(
			new Button(
				this.centerButton(2, 3),
				row(0),
				100,
				20,
				new TranslatableComponent("mco.configure.world.buttons.subscription"),
				button -> this.minecraft.setScreen(new RealmsSubscriptionInfoScreen(this, this.serverData.clone(), this.lastScreen))
			)
		);
		this.slotButtonList.clear();

		for (int i = 1; i < 5; i++) {
			this.slotButtonList.add(this.addSlotButton(i));
		}

		this.switchMinigameButton = this.addRenderableWidget(
			new Button(
				this.leftButton(0),
				row(13) - 5,
				100,
				20,
				new TranslatableComponent("mco.configure.world.buttons.switchminigame"),
				button -> this.minecraft
						.setScreen(
							new RealmsSelectWorldTemplateScreen(
								new TranslatableComponent("mco.template.title.minigame"), this::templateSelectionCallback, RealmsServer.WorldType.MINIGAME
							)
						)
			)
		);
		this.optionsButton = this.addRenderableWidget(
			new Button(
				this.leftButton(0),
				row(13) - 5,
				90,
				20,
				new TranslatableComponent("mco.configure.world.buttons.options"),
				button -> this.minecraft
						.setScreen(
							new RealmsSlotOptionsScreen(
								this, ((RealmsWorldOptions)this.serverData.slots.get(this.serverData.activeSlot)).clone(), this.serverData.worldType, this.serverData.activeSlot
							)
						)
			)
		);
		this.backupButton = this.addRenderableWidget(
			new Button(
				this.leftButton(1),
				row(13) - 5,
				90,
				20,
				new TranslatableComponent("mco.configure.world.backup"),
				button -> this.minecraft.setScreen(new RealmsBackupScreen(this, this.serverData.clone(), this.serverData.activeSlot))
			)
		);
		this.resetWorldButton = this.addRenderableWidget(
			new Button(
				this.leftButton(2),
				row(13) - 5,
				90,
				20,
				new TranslatableComponent("mco.configure.world.buttons.resetworld"),
				button -> this.minecraft
						.setScreen(
							new RealmsResetWorldScreen(
								this,
								this.serverData.clone(),
								() -> this.minecraft.execute(() -> this.minecraft.setScreen(this.getNewScreen())),
								() -> this.minecraft.setScreen(this.getNewScreen())
							)
						)
			)
		);
		this.addRenderableWidget(new Button(this.rightX - 80 + 8, row(13) - 5, 70, 20, CommonComponents.GUI_BACK, button -> this.backButtonClicked()));
		this.backupButton.active = true;
		if (this.serverData == null) {
			this.hideMinigameButtons();
			this.hideRegularButtons();
			this.playersButton.active = false;
			this.settingsButton.active = false;
			this.subscriptionButton.active = false;
		} else {
			this.disableButtons();
			if (this.isMinigame()) {
				this.hideRegularButtons();
			} else {
				this.hideMinigameButtons();
			}
		}
	}

	private RealmsWorldSlotButton addSlotButton(int i) {
		int j = this.frame(i);
		int k = row(5) + 5;
		RealmsWorldSlotButton realmsWorldSlotButton = new RealmsWorldSlotButton(
			j, k, 80, 80, () -> this.serverData, component -> this.toolTip = component, i, button -> {
				RealmsWorldSlotButton.State state = ((RealmsWorldSlotButton)button).getState();
				if (state != null) {
					switch (state.action) {
						case NOTHING:
							break;
						case JOIN:
							this.joinRealm(this.serverData);
							break;
						case SWITCH_SLOT:
							if (state.minigame) {
								this.switchToMinigame();
							} else if (state.empty) {
								this.switchToEmptySlot(i, this.serverData);
							} else {
								this.switchToFullSlot(i, this.serverData);
							}
							break;
						default:
							throw new IllegalStateException("Unknown action " + state.action);
					}
				}
			}
		);
		return this.addRenderableWidget(realmsWorldSlotButton);
	}

	private int leftButton(int i) {
		return this.leftX + i * 95;
	}

	private int centerButton(int i, int j) {
		return this.width / 2 - (j * 105 - 5) / 2 + i * 105;
	}

	@Override
	public void tick() {
		super.tick();
		this.animTick++;
		this.clicks--;
		if (this.clicks < 0) {
			this.clicks = 0;
		}

		this.slotButtonList.forEach(RealmsWorldSlotButton::tick);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.toolTip = null;
		this.renderBackground(poseStack);
		drawCenteredString(poseStack, this.font, WORLD_LIST_TITLE, this.width / 2, row(4), 16777215);
		super.render(poseStack, i, j, f);
		if (this.serverData == null) {
			drawCenteredString(poseStack, this.font, this.title, this.width / 2, 17, 16777215);
		} else {
			String string = this.serverData.getName();
			int k = this.font.width(string);
			int l = this.serverData.state == RealmsServer.State.CLOSED ? 10526880 : 8388479;
			int m = this.font.width(this.title);
			drawCenteredString(poseStack, this.font, this.title, this.width / 2, 12, 16777215);
			drawCenteredString(poseStack, this.font, string, this.width / 2, 24, l);
			int n = Math.min(this.centerButton(2, 3) + 80 - 11, this.width / 2 + k / 2 + m / 2 + 10);
			this.drawServerStatus(poseStack, n, 7, i, j);
			if (this.isMinigame()) {
				this.font.draw(poseStack, MINIGAME_PREFIX.copy().append(this.serverData.getMinigameName()), (float)(this.leftX + 80 + 20 + 10), (float)row(13), 16777215);
			}

			if (this.toolTip != null) {
				this.renderMousehoverTooltip(poseStack, this.toolTip, i, j);
			}
		}
	}

	private int frame(int i) {
		return this.leftX + (i - 1) * 98;
	}

	@Override
	public void removed() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256) {
			this.backButtonClicked();
			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	private void backButtonClicked() {
		if (this.stateChanged) {
			this.lastScreen.resetScreen();
		}

		this.minecraft.setScreen(this.lastScreen);
	}

	private void fetchServerData(long l) {
		new Thread(() -> {
			RealmsClient realmsClient = RealmsClient.create();

			try {
				RealmsServer realmsServer = realmsClient.getOwnWorld(l);
				this.minecraft.execute(() -> {
					this.serverData = realmsServer;
					this.disableButtons();
					if (this.isMinigame()) {
						this.show(this.switchMinigameButton);
					} else {
						this.show(this.optionsButton);
						this.show(this.backupButton);
						this.show(this.resetWorldButton);
					}
				});
			} catch (RealmsServiceException var5) {
				LOGGER.error("Couldn't get own world");
				this.minecraft.execute(() -> this.minecraft.setScreen(new RealmsGenericErrorScreen(Component.nullToEmpty(var5.getMessage()), this.lastScreen)));
			}
		}).start();
	}

	private void disableButtons() {
		this.playersButton.active = !this.serverData.expired;
		this.settingsButton.active = !this.serverData.expired;
		this.subscriptionButton.active = true;
		this.switchMinigameButton.active = !this.serverData.expired;
		this.optionsButton.active = !this.serverData.expired;
		this.resetWorldButton.active = !this.serverData.expired;
	}

	private void joinRealm(RealmsServer realmsServer) {
		if (this.serverData.state == RealmsServer.State.OPEN) {
			this.lastScreen.play(realmsServer, new RealmsConfigureWorldScreen(this.lastScreen.newScreen(), this.serverId));
		} else {
			this.openTheWorld(true, new RealmsConfigureWorldScreen(this.lastScreen.newScreen(), this.serverId));
		}
	}

	private void switchToMinigame() {
		RealmsSelectWorldTemplateScreen realmsSelectWorldTemplateScreen = new RealmsSelectWorldTemplateScreen(
			new TranslatableComponent("mco.template.title.minigame"), this::templateSelectionCallback, RealmsServer.WorldType.MINIGAME
		);
		realmsSelectWorldTemplateScreen.setWarning(
			new TranslatableComponent("mco.minigame.world.info.line1"), new TranslatableComponent("mco.minigame.world.info.line2")
		);
		this.minecraft.setScreen(realmsSelectWorldTemplateScreen);
	}

	private void switchToFullSlot(int i, RealmsServer realmsServer) {
		Component component = new TranslatableComponent("mco.configure.world.slot.switch.question.line1");
		Component component2 = new TranslatableComponent("mco.configure.world.slot.switch.question.line2");
		this.minecraft
			.setScreen(
				new RealmsLongConfirmationScreen(
					bl -> {
						if (bl) {
							this.minecraft
								.setScreen(
									new RealmsLongRunningMcoTaskScreen(
										this.lastScreen, new SwitchSlotTask(realmsServer.id, i, () -> this.minecraft.execute(() -> this.minecraft.setScreen(this.getNewScreen())))
									)
								);
						} else {
							this.minecraft.setScreen(this);
						}
					},
					RealmsLongConfirmationScreen.Type.Info,
					component,
					component2,
					true
				)
			);
	}

	private void switchToEmptySlot(int i, RealmsServer realmsServer) {
		Component component = new TranslatableComponent("mco.configure.world.slot.switch.question.line1");
		Component component2 = new TranslatableComponent("mco.configure.world.slot.switch.question.line2");
		this.minecraft
			.setScreen(
				new RealmsLongConfirmationScreen(
					bl -> {
						if (bl) {
							RealmsResetWorldScreen realmsResetWorldScreen = new RealmsResetWorldScreen(
								this,
								realmsServer,
								new TranslatableComponent("mco.configure.world.switch.slot"),
								new TranslatableComponent("mco.configure.world.switch.slot.subtitle"),
								10526880,
								CommonComponents.GUI_CANCEL,
								() -> this.minecraft.execute(() -> this.minecraft.setScreen(this.getNewScreen())),
								() -> this.minecraft.setScreen(this.getNewScreen())
							);
							realmsResetWorldScreen.setSlot(i);
							realmsResetWorldScreen.setResetTitle(new TranslatableComponent("mco.create.world.reset.title"));
							this.minecraft.setScreen(realmsResetWorldScreen);
						} else {
							this.minecraft.setScreen(this);
						}
					},
					RealmsLongConfirmationScreen.Type.Info,
					component,
					component2,
					true
				)
			);
	}

	protected void renderMousehoverTooltip(PoseStack poseStack, @Nullable Component component, int i, int j) {
		int k = i + 12;
		int l = j - 12;
		int m = this.font.width(component);
		if (k + m + 3 > this.rightX) {
			k = k - m - 20;
		}

		this.fillGradient(poseStack, k - 3, l - 3, k + m + 3, l + 8 + 3, -1073741824, -1073741824);
		this.font.drawShadow(poseStack, component, (float)k, (float)l, 16777215);
	}

	private void drawServerStatus(PoseStack poseStack, int i, int j, int k, int l) {
		if (this.serverData.expired) {
			this.drawExpired(poseStack, i, j, k, l);
		} else if (this.serverData.state == RealmsServer.State.CLOSED) {
			this.drawClose(poseStack, i, j, k, l);
		} else if (this.serverData.state == RealmsServer.State.OPEN) {
			if (this.serverData.daysLeft < 7) {
				this.drawExpiring(poseStack, i, j, k, l, this.serverData.daysLeft);
			} else {
				this.drawOpen(poseStack, i, j, k, l);
			}
		}
	}

	private void drawExpired(PoseStack poseStack, int i, int j, int k, int l) {
		RenderSystem.setShaderTexture(0, EXPIRED_ICON_LOCATION);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		GuiComponent.blit(poseStack, i, j, 0.0F, 0.0F, 10, 28, 10, 28);
		if (k >= i && k <= i + 9 && l >= j && l <= j + 27) {
			this.toolTip = SERVER_EXPIRED_TOOLTIP;
		}
	}

	private void drawExpiring(PoseStack poseStack, int i, int j, int k, int l, int m) {
		RenderSystem.setShaderTexture(0, EXPIRES_SOON_ICON_LOCATION);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		if (this.animTick % 20 < 10) {
			GuiComponent.blit(poseStack, i, j, 0.0F, 0.0F, 10, 28, 20, 28);
		} else {
			GuiComponent.blit(poseStack, i, j, 10.0F, 0.0F, 10, 28, 20, 28);
		}

		if (k >= i && k <= i + 9 && l >= j && l <= j + 27) {
			if (m <= 0) {
				this.toolTip = SERVER_EXPIRING_SOON_TOOLTIP;
			} else if (m == 1) {
				this.toolTip = SERVER_EXPIRING_IN_DAY_TOOLTIP;
			} else {
				this.toolTip = new TranslatableComponent("mco.selectServer.expires.days", m);
			}
		}
	}

	private void drawOpen(PoseStack poseStack, int i, int j, int k, int l) {
		RenderSystem.setShaderTexture(0, ON_ICON_LOCATION);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		GuiComponent.blit(poseStack, i, j, 0.0F, 0.0F, 10, 28, 10, 28);
		if (k >= i && k <= i + 9 && l >= j && l <= j + 27) {
			this.toolTip = SERVER_OPEN_TOOLTIP;
		}
	}

	private void drawClose(PoseStack poseStack, int i, int j, int k, int l) {
		RenderSystem.setShaderTexture(0, OFF_ICON_LOCATION);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		GuiComponent.blit(poseStack, i, j, 0.0F, 0.0F, 10, 28, 10, 28);
		if (k >= i && k <= i + 9 && l >= j && l <= j + 27) {
			this.toolTip = SERVER_CLOSED_TOOLTIP;
		}
	}

	private boolean isMinigame() {
		return this.serverData != null && this.serverData.worldType == RealmsServer.WorldType.MINIGAME;
	}

	private void hideRegularButtons() {
		this.hide(this.optionsButton);
		this.hide(this.backupButton);
		this.hide(this.resetWorldButton);
	}

	private void hide(Button button) {
		button.visible = false;
		this.removeWidget(button);
	}

	private void show(Button button) {
		button.visible = true;
		this.addRenderableWidget(button);
	}

	private void hideMinigameButtons() {
		this.hide(this.switchMinigameButton);
	}

	public void saveSlotSettings(RealmsWorldOptions realmsWorldOptions) {
		RealmsWorldOptions realmsWorldOptions2 = (RealmsWorldOptions)this.serverData.slots.get(this.serverData.activeSlot);
		realmsWorldOptions.templateId = realmsWorldOptions2.templateId;
		realmsWorldOptions.templateImage = realmsWorldOptions2.templateImage;
		RealmsClient realmsClient = RealmsClient.create();

		try {
			realmsClient.updateSlot(this.serverData.id, this.serverData.activeSlot, realmsWorldOptions);
			this.serverData.slots.put(this.serverData.activeSlot, realmsWorldOptions);
		} catch (RealmsServiceException var5) {
			LOGGER.error("Couldn't save slot settings");
			this.minecraft.setScreen(new RealmsGenericErrorScreen(var5, this));
			return;
		}

		this.minecraft.setScreen(this);
	}

	public void saveSettings(String string, String string2) {
		String string3 = string2.trim().isEmpty() ? null : string2;
		RealmsClient realmsClient = RealmsClient.create();

		try {
			realmsClient.update(this.serverData.id, string, string3);
			this.serverData.setName(string);
			this.serverData.setDescription(string3);
		} catch (RealmsServiceException var6) {
			LOGGER.error("Couldn't save settings");
			this.minecraft.setScreen(new RealmsGenericErrorScreen(var6, this));
			return;
		}

		this.minecraft.setScreen(this);
	}

	public void openTheWorld(boolean bl, Screen screen) {
		this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(screen, new OpenServerTask(this.serverData, this, this.lastScreen, bl, this.minecraft)));
	}

	public void closeTheWorld(Screen screen) {
		this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(screen, new CloseServerTask(this.serverData, this)));
	}

	public void stateChanged() {
		this.stateChanged = true;
	}

	private void templateSelectionCallback(@Nullable WorldTemplate worldTemplate) {
		if (worldTemplate != null && WorldTemplate.WorldTemplateType.MINIGAME == worldTemplate.type) {
			this.minecraft
				.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, new SwitchMinigameTask(this.serverData.id, worldTemplate, this.getNewScreen())));
		} else {
			this.minecraft.setScreen(this);
		}
	}

	public RealmsConfigureWorldScreen getNewScreen() {
		return new RealmsConfigureWorldScreen(this.lastScreen, this.serverId);
	}
}
