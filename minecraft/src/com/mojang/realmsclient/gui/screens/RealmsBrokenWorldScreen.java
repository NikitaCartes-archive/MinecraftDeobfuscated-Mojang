package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsWorldSlotButton;
import com.mojang.realmsclient.util.RealmsTextureManager;
import com.mojang.realmsclient.util.task.OpenServerTask;
import com.mojang.realmsclient.util.task.SwitchSlotTask;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsBrokenWorldScreen extends RealmsScreen {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Screen lastScreen;
	private final RealmsMainScreen mainScreen;
	private RealmsServer serverData;
	private final long serverId;
	private final Component header;
	private final Component[] message = new Component[]{
		new TranslatableComponent("mco.brokenworld.message.line1"), new TranslatableComponent("mco.brokenworld.message.line2")
	};
	private int leftX;
	private int rightX;
	private final List<Integer> slotsThatHasBeenDownloaded = Lists.<Integer>newArrayList();
	private int animTick;

	public RealmsBrokenWorldScreen(Screen screen, RealmsMainScreen realmsMainScreen, long l, boolean bl) {
		this.lastScreen = screen;
		this.mainScreen = realmsMainScreen;
		this.serverId = l;
		this.header = bl ? new TranslatableComponent("mco.brokenworld.minigame.title") : new TranslatableComponent("mco.brokenworld.title");
	}

	@Override
	public void init() {
		this.leftX = this.width / 2 - 150;
		this.rightX = this.width / 2 + 190;
		this.addButton(new Button(this.rightX - 80 + 8, row(13) - 5, 70, 20, CommonComponents.GUI_BACK, button -> this.backButtonClicked()));
		if (this.serverData == null) {
			this.fetchServerData(this.serverId);
		} else {
			this.addButtons();
		}

		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		NarrationHelper.now((String)Stream.concat(Stream.of(this.header), Stream.of(this.message)).map(Component::getString).collect(Collectors.joining(" ")));
	}

	private void addButtons() {
		for (Entry<Integer, RealmsWorldOptions> entry : this.serverData.slots.entrySet()) {
			int i = (Integer)entry.getKey();
			boolean bl = i != this.serverData.activeSlot || this.serverData.worldType == RealmsServer.WorldType.MINIGAME;
			Button button;
			if (bl) {
				button = new Button(
					this.getFramePositionX(i),
					row(8),
					80,
					20,
					new TranslatableComponent("mco.brokenworld.play"),
					buttonx -> {
						if (((RealmsWorldOptions)this.serverData.slots.get(i)).empty) {
							RealmsResetWorldScreen realmsResetWorldScreen = new RealmsResetWorldScreen(
								this,
								this.serverData,
								new TranslatableComponent("mco.configure.world.switch.slot"),
								new TranslatableComponent("mco.configure.world.switch.slot.subtitle"),
								10526880,
								CommonComponents.GUI_CANCEL,
								this::doSwitchOrReset,
								() -> {
									this.minecraft.setScreen(this);
									this.doSwitchOrReset();
								}
							);
							realmsResetWorldScreen.setSlot(i);
							realmsResetWorldScreen.setResetTitle(new TranslatableComponent("mco.create.world.reset.title"));
							this.minecraft.setScreen(realmsResetWorldScreen);
						} else {
							this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, new SwitchSlotTask(this.serverData.id, i, this::doSwitchOrReset)));
						}
					}
				);
			} else {
				button = new Button(this.getFramePositionX(i), row(8), 80, 20, new TranslatableComponent("mco.brokenworld.download"), buttonx -> {
					Component component = new TranslatableComponent("mco.configure.world.restore.download.question.line1");
					Component component2 = new TranslatableComponent("mco.configure.world.restore.download.question.line2");
					this.minecraft.setScreen(new RealmsLongConfirmationScreen(blx -> {
						if (blx) {
							this.downloadWorld(i);
						} else {
							this.minecraft.setScreen(this);
						}
					}, RealmsLongConfirmationScreen.Type.Info, component, component2, true));
				});
			}

			if (this.slotsThatHasBeenDownloaded.contains(i)) {
				button.active = false;
				button.setMessage(new TranslatableComponent("mco.brokenworld.downloaded"));
			}

			this.addButton(button);
			this.addButton(new Button(this.getFramePositionX(i), row(10), 80, 20, new TranslatableComponent("mco.brokenworld.reset"), buttonx -> {
				RealmsResetWorldScreen realmsResetWorldScreen = new RealmsResetWorldScreen(this, this.serverData, this::doSwitchOrReset, () -> {
					this.minecraft.setScreen(this);
					this.doSwitchOrReset();
				});
				if (i != this.serverData.activeSlot || this.serverData.worldType == RealmsServer.WorldType.MINIGAME) {
					realmsResetWorldScreen.setSlot(i);
				}

				this.minecraft.setScreen(realmsResetWorldScreen);
			}));
		}
	}

	@Override
	public void tick() {
		this.animTick++;
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		super.render(poseStack, i, j, f);
		drawCenteredString(poseStack, this.font, this.header, this.width / 2, 17, 16777215);

		for (int k = 0; k < this.message.length; k++) {
			drawCenteredString(poseStack, this.font, this.message[k], this.width / 2, row(-1) + 3 + k * 12, 10526880);
		}

		if (this.serverData != null) {
			for (Entry<Integer, RealmsWorldOptions> entry : this.serverData.slots.entrySet()) {
				if (((RealmsWorldOptions)entry.getValue()).templateImage != null && ((RealmsWorldOptions)entry.getValue()).templateId != -1L) {
					this.drawSlotFrame(
						poseStack,
						this.getFramePositionX((Integer)entry.getKey()),
						row(1) + 5,
						i,
						j,
						this.serverData.activeSlot == (Integer)entry.getKey() && !this.isMinigame(),
						((RealmsWorldOptions)entry.getValue()).getSlotName((Integer)entry.getKey()),
						(Integer)entry.getKey(),
						((RealmsWorldOptions)entry.getValue()).templateId,
						((RealmsWorldOptions)entry.getValue()).templateImage,
						((RealmsWorldOptions)entry.getValue()).empty
					);
				} else {
					this.drawSlotFrame(
						poseStack,
						this.getFramePositionX((Integer)entry.getKey()),
						row(1) + 5,
						i,
						j,
						this.serverData.activeSlot == (Integer)entry.getKey() && !this.isMinigame(),
						((RealmsWorldOptions)entry.getValue()).getSlotName((Integer)entry.getKey()),
						(Integer)entry.getKey(),
						-1L,
						null,
						((RealmsWorldOptions)entry.getValue()).empty
					);
				}
			}
		}
	}

	private int getFramePositionX(int i) {
		return this.leftX + (i - 1) * 110;
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
		this.minecraft.setScreen(this.lastScreen);
	}

	private void fetchServerData(long l) {
		new Thread(() -> {
			RealmsClient realmsClient = RealmsClient.create();

			try {
				this.serverData = realmsClient.getOwnWorld(l);
				this.addButtons();
			} catch (RealmsServiceException var5) {
				LOGGER.error("Couldn't get own world");
				this.minecraft.setScreen(new RealmsGenericErrorScreen(Component.nullToEmpty(var5.getMessage()), this.lastScreen));
			}
		}).start();
	}

	public void doSwitchOrReset() {
		new Thread(
				() -> {
					RealmsClient realmsClient = RealmsClient.create();
					if (this.serverData.state == RealmsServer.State.CLOSED) {
						this.minecraft
							.execute(() -> this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this, new OpenServerTask(this.serverData, this, this.mainScreen, true))));
					} else {
						try {
							this.mainScreen.newScreen().play(realmsClient.getOwnWorld(this.serverId), this);
						} catch (RealmsServiceException var3) {
							LOGGER.error("Couldn't get own world");
							this.minecraft.execute(() -> this.minecraft.setScreen(this.lastScreen));
						}
					}
				}
			)
			.start();
	}

	private void downloadWorld(int i) {
		RealmsClient realmsClient = RealmsClient.create();

		try {
			WorldDownload worldDownload = realmsClient.requestDownloadInfo(this.serverData.id, i);
			RealmsDownloadLatestWorldScreen realmsDownloadLatestWorldScreen = new RealmsDownloadLatestWorldScreen(
				this, worldDownload, this.serverData.getWorldName(i), bl -> {
					if (bl) {
						this.slotsThatHasBeenDownloaded.add(i);
						this.children.clear();
						this.addButtons();
					} else {
						this.minecraft.setScreen(this);
					}
				}
			);
			this.minecraft.setScreen(realmsDownloadLatestWorldScreen);
		} catch (RealmsServiceException var5) {
			LOGGER.error("Couldn't download world data");
			this.minecraft.setScreen(new RealmsGenericErrorScreen(var5, this));
		}
	}

	private boolean isMinigame() {
		return this.serverData != null && this.serverData.worldType == RealmsServer.WorldType.MINIGAME;
	}

	private void drawSlotFrame(PoseStack poseStack, int i, int j, int k, int l, boolean bl, String string, int m, long n, String string2, boolean bl2) {
		if (bl2) {
			this.minecraft.getTextureManager().bind(RealmsWorldSlotButton.EMPTY_SLOT_LOCATION);
		} else if (string2 != null && n != -1L) {
			RealmsTextureManager.bindWorldTemplate(String.valueOf(n), string2);
		} else if (m == 1) {
			this.minecraft.getTextureManager().bind(RealmsWorldSlotButton.DEFAULT_WORLD_SLOT_1);
		} else if (m == 2) {
			this.minecraft.getTextureManager().bind(RealmsWorldSlotButton.DEFAULT_WORLD_SLOT_2);
		} else if (m == 3) {
			this.minecraft.getTextureManager().bind(RealmsWorldSlotButton.DEFAULT_WORLD_SLOT_3);
		} else {
			RealmsTextureManager.bindWorldTemplate(String.valueOf(this.serverData.minigameId), this.serverData.minigameImage);
		}

		if (!bl) {
			RenderSystem.color4f(0.56F, 0.56F, 0.56F, 1.0F);
		} else if (bl) {
			float f = 0.9F + 0.1F * Mth.cos((float)this.animTick * 0.2F);
			RenderSystem.color4f(f, f, f, 1.0F);
		}

		GuiComponent.blit(poseStack, i + 3, j + 3, 0.0F, 0.0F, 74, 74, 74, 74);
		this.minecraft.getTextureManager().bind(RealmsWorldSlotButton.SLOT_FRAME_LOCATION);
		if (bl) {
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		} else {
			RenderSystem.color4f(0.56F, 0.56F, 0.56F, 1.0F);
		}

		GuiComponent.blit(poseStack, i, j, 0.0F, 0.0F, 80, 80, 80, 80);
		drawCenteredString(poseStack, this.font, string, i + 40, j + 66, 16777215);
	}
}
