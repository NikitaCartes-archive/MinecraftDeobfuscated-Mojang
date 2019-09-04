package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.util.RealmsTasks;
import com.mojang.realmsclient.util.RealmsTextureManager;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsMth;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsBrokenWorldScreen extends RealmsScreen {
	private static final Logger LOGGER = LogManager.getLogger();
	private final RealmsScreen lastScreen;
	private final RealmsMainScreen mainScreen;
	private RealmsServer serverData;
	private final long serverId;
	private String title = getLocalizedString("mco.brokenworld.title");
	private final String message = getLocalizedString("mco.brokenworld.message.line1") + "\\n" + getLocalizedString("mco.brokenworld.message.line2");
	private int left_x;
	private int right_x;
	private final int default_button_width = 80;
	private final int default_button_offset = 5;
	private static final List<Integer> playButtonIds = Arrays.asList(1, 2, 3);
	private static final List<Integer> resetButtonIds = Arrays.asList(4, 5, 6);
	private static final List<Integer> downloadButtonIds = Arrays.asList(7, 8, 9);
	private static final List<Integer> downloadConfirmationIds = Arrays.asList(10, 11, 12);
	private final List<Integer> slotsThatHasBeenDownloaded = Lists.<Integer>newArrayList();
	private int animTick;

	public RealmsBrokenWorldScreen(RealmsScreen realmsScreen, RealmsMainScreen realmsMainScreen, long l) {
		this.lastScreen = realmsScreen;
		this.mainScreen = realmsMainScreen;
		this.serverId = l;
	}

	public void setTitle(String string) {
		this.title = string;
	}

	@Override
	public void init() {
		this.left_x = this.width() / 2 - 150;
		this.right_x = this.width() / 2 + 190;
		this.buttonsAdd(new RealmsButton(0, this.right_x - 80 + 8, RealmsConstants.row(13) - 5, 70, 20, getLocalizedString("gui.back")) {
			@Override
			public void onPress() {
				RealmsBrokenWorldScreen.this.backButtonClicked();
			}
		});
		if (this.serverData == null) {
			this.fetchServerData(this.serverId);
		} else {
			this.addButtons();
		}

		this.setKeyboardHandlerSendRepeatsToGui(true);
	}

	public void addButtons() {
		for (Entry<Integer, RealmsWorldOptions> entry : this.serverData.slots.entrySet()) {
			RealmsWorldOptions realmsWorldOptions = (RealmsWorldOptions)entry.getValue();
			boolean bl = (Integer)entry.getKey() != this.serverData.activeSlot || this.serverData.worldType.equals(RealmsServer.WorldType.MINIGAME);
			RealmsButton realmsButton;
			if (bl) {
				realmsButton = new RealmsBrokenWorldScreen.PlayButton(
					(Integer)playButtonIds.get((Integer)entry.getKey() - 1), this.getFramePositionX((Integer)entry.getKey()), getLocalizedString("mco.brokenworld.play")
				);
			} else {
				realmsButton = new RealmsBrokenWorldScreen.DownloadButton(
					(Integer)downloadButtonIds.get((Integer)entry.getKey() - 1),
					this.getFramePositionX((Integer)entry.getKey()),
					getLocalizedString("mco.brokenworld.download")
				);
			}

			if (this.slotsThatHasBeenDownloaded.contains(entry.getKey())) {
				realmsButton.active(false);
				realmsButton.setMessage(getLocalizedString("mco.brokenworld.downloaded"));
			}

			this.buttonsAdd(realmsButton);
			this.buttonsAdd(
				new RealmsButton(
					(Integer)resetButtonIds.get((Integer)entry.getKey() - 1),
					this.getFramePositionX((Integer)entry.getKey()),
					RealmsConstants.row(10),
					80,
					20,
					getLocalizedString("mco.brokenworld.reset")
				) {
					@Override
					public void onPress() {
						int i = RealmsBrokenWorldScreen.resetButtonIds.indexOf(this.id()) + 1;
						RealmsResetWorldScreen realmsResetWorldScreen = new RealmsResetWorldScreen(
							RealmsBrokenWorldScreen.this, RealmsBrokenWorldScreen.this.serverData, RealmsBrokenWorldScreen.this
						);
						if (i != RealmsBrokenWorldScreen.this.serverData.activeSlot || RealmsBrokenWorldScreen.this.serverData.worldType.equals(RealmsServer.WorldType.MINIGAME)) {
							realmsResetWorldScreen.setSlot(i);
						}

						realmsResetWorldScreen.setConfirmationId(14);
						Realms.setScreen(realmsResetWorldScreen);
					}
				}
			);
		}
	}

	@Override
	public void tick() {
		this.animTick++;
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		super.render(i, j, f);
		this.drawCenteredString(this.title, this.width() / 2, 17, 16777215);
		String[] strings = this.message.split("\\\\n");

		for (int k = 0; k < strings.length; k++) {
			this.drawCenteredString(strings[k], this.width() / 2, RealmsConstants.row(-1) + 3 + k * 12, 10526880);
		}

		if (this.serverData != null) {
			for (Entry<Integer, RealmsWorldOptions> entry : this.serverData.slots.entrySet()) {
				if (((RealmsWorldOptions)entry.getValue()).templateImage != null && ((RealmsWorldOptions)entry.getValue()).templateId != -1L) {
					this.drawSlotFrame(
						this.getFramePositionX((Integer)entry.getKey()),
						RealmsConstants.row(1) + 5,
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
						this.getFramePositionX((Integer)entry.getKey()),
						RealmsConstants.row(1) + 5,
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
		return this.left_x + (i - 1) * 110;
	}

	@Override
	public void removed() {
		this.setKeyboardHandlerSendRepeatsToGui(false);
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
		Realms.setScreen(this.lastScreen);
	}

	private void fetchServerData(long l) {
		new Thread(() -> {
			RealmsClient realmsClient = RealmsClient.createRealmsClient();

			try {
				this.serverData = realmsClient.getOwnWorld(l);
				this.addButtons();
			} catch (RealmsServiceException var5) {
				LOGGER.error("Couldn't get own world");
				Realms.setScreen(new RealmsGenericErrorScreen(var5.getMessage(), this.lastScreen));
			} catch (IOException var6) {
				LOGGER.error("Couldn't parse response getting own world");
			}
		}).start();
	}

	@Override
	public void confirmResult(boolean bl, int i) {
		if (!bl) {
			Realms.setScreen(this);
		} else {
			if (i != 13 && i != 14) {
				if (downloadButtonIds.contains(i)) {
					this.downloadWorld(downloadButtonIds.indexOf(i) + 1);
				} else if (downloadConfirmationIds.contains(i)) {
					this.slotsThatHasBeenDownloaded.add(downloadConfirmationIds.indexOf(i) + 1);
					this.childrenClear();
					this.addButtons();
				}
			} else {
				new Thread(() -> {
					RealmsClient realmsClient = RealmsClient.createRealmsClient();
					if (this.serverData.state.equals(RealmsServer.State.CLOSED)) {
						RealmsTasks.OpenServerTask openServerTask = new RealmsTasks.OpenServerTask(this.serverData, this, this.lastScreen, true);
						RealmsLongRunningMcoTaskScreen realmsLongRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(this, openServerTask);
						realmsLongRunningMcoTaskScreen.start();
						Realms.setScreen(realmsLongRunningMcoTaskScreen);
					} else {
						try {
							this.mainScreen.newScreen().play(realmsClient.getOwnWorld(this.serverId), this);
						} catch (RealmsServiceException var4) {
							LOGGER.error("Couldn't get own world");
							Realms.setScreen(this.lastScreen);
						} catch (IOException var5) {
							LOGGER.error("Couldn't parse response getting own world");
							Realms.setScreen(this.lastScreen);
						}
					}
				}).start();
			}
		}
	}

	private void downloadWorld(int i) {
		RealmsClient realmsClient = RealmsClient.createRealmsClient();

		try {
			WorldDownload worldDownload = realmsClient.download(this.serverData.id, i);
			RealmsDownloadLatestWorldScreen realmsDownloadLatestWorldScreen = new RealmsDownloadLatestWorldScreen(
				this, worldDownload, this.serverData.name + " (" + ((RealmsWorldOptions)this.serverData.slots.get(i)).getSlotName(i) + ")"
			);
			realmsDownloadLatestWorldScreen.setConfirmationId((Integer)downloadConfirmationIds.get(i - 1));
			Realms.setScreen(realmsDownloadLatestWorldScreen);
		} catch (RealmsServiceException var5) {
			LOGGER.error("Couldn't download world data");
			Realms.setScreen(new RealmsGenericErrorScreen(var5, this));
		}
	}

	private boolean isMinigame() {
		return this.serverData != null && this.serverData.worldType.equals(RealmsServer.WorldType.MINIGAME);
	}

	private void drawSlotFrame(int i, int j, int k, int l, boolean bl, String string, int m, long n, String string2, boolean bl2) {
		if (bl2) {
			bind("realms:textures/gui/realms/empty_frame.png");
		} else if (string2 != null && n != -1L) {
			RealmsTextureManager.bindWorldTemplate(String.valueOf(n), string2);
		} else if (m == 1) {
			bind("textures/gui/title/background/panorama_0.png");
		} else if (m == 2) {
			bind("textures/gui/title/background/panorama_2.png");
		} else if (m == 3) {
			bind("textures/gui/title/background/panorama_3.png");
		} else {
			RealmsTextureManager.bindWorldTemplate(String.valueOf(this.serverData.minigameId), this.serverData.minigameImage);
		}

		if (!bl) {
			RenderSystem.color4f(0.56F, 0.56F, 0.56F, 1.0F);
		} else if (bl) {
			float f = 0.9F + 0.1F * RealmsMth.cos((float)this.animTick * 0.2F);
			RenderSystem.color4f(f, f, f, 1.0F);
		}

		RealmsScreen.blit(i + 3, j + 3, 0.0F, 0.0F, 74, 74, 74, 74);
		bind("realms:textures/gui/realms/slot_frame.png");
		if (bl) {
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		} else {
			RenderSystem.color4f(0.56F, 0.56F, 0.56F, 1.0F);
		}

		RealmsScreen.blit(i, j, 0.0F, 0.0F, 80, 80, 80, 80);
		this.drawCenteredString(string, i + 40, j + 66, 16777215);
	}

	private void switchSlot(int i) {
		RealmsTasks.SwitchSlotTask switchSlotTask = new RealmsTasks.SwitchSlotTask(this.serverData.id, i, this, 13);
		RealmsLongRunningMcoTaskScreen realmsLongRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(this.lastScreen, switchSlotTask);
		realmsLongRunningMcoTaskScreen.start();
		Realms.setScreen(realmsLongRunningMcoTaskScreen);
	}

	@Environment(EnvType.CLIENT)
	class DownloadButton extends RealmsButton {
		public DownloadButton(int i, int j, String string) {
			super(i, j, RealmsConstants.row(8), 80, 20, string);
		}

		@Override
		public void onPress() {
			String string = RealmsScreen.getLocalizedString("mco.configure.world.restore.download.question.line1");
			String string2 = RealmsScreen.getLocalizedString("mco.configure.world.restore.download.question.line2");
			Realms.setScreen(new RealmsLongConfirmationScreen(RealmsBrokenWorldScreen.this, RealmsLongConfirmationScreen.Type.Info, string, string2, true, this.id()));
		}
	}

	@Environment(EnvType.CLIENT)
	class PlayButton extends RealmsButton {
		public PlayButton(int i, int j, String string) {
			super(i, j, RealmsConstants.row(8), 80, 20, string);
		}

		@Override
		public void onPress() {
			int i = RealmsBrokenWorldScreen.playButtonIds.indexOf(this.id()) + 1;
			if (((RealmsWorldOptions)RealmsBrokenWorldScreen.this.serverData.slots.get(i)).empty) {
				RealmsResetWorldScreen realmsResetWorldScreen = new RealmsResetWorldScreen(
					RealmsBrokenWorldScreen.this,
					RealmsBrokenWorldScreen.this.serverData,
					RealmsBrokenWorldScreen.this,
					RealmsScreen.getLocalizedString("mco.configure.world.switch.slot"),
					RealmsScreen.getLocalizedString("mco.configure.world.switch.slot.subtitle"),
					10526880,
					RealmsScreen.getLocalizedString("gui.cancel")
				);
				realmsResetWorldScreen.setSlot(i);
				realmsResetWorldScreen.setResetTitle(RealmsScreen.getLocalizedString("mco.create.world.reset.title"));
				realmsResetWorldScreen.setConfirmationId(14);
				Realms.setScreen(realmsResetWorldScreen);
			} else {
				RealmsBrokenWorldScreen.this.switchSlot(i);
			}
		}
	}
}
