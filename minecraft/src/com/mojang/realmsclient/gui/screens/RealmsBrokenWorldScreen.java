package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
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
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsBrokenWorldScreen extends RealmsScreen {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int DEFAULT_BUTTON_WIDTH = 80;
	private final Screen lastScreen;
	private final RealmsMainScreen mainScreen;
	@Nullable
	private RealmsServer serverData;
	private final long serverId;
	private final Component[] message = new Component[]{
		Component.translatable("mco.brokenworld.message.line1"), Component.translatable("mco.brokenworld.message.line2")
	};
	private int leftX;
	private int rightX;
	private final List<Integer> slotsThatHasBeenDownloaded = Lists.<Integer>newArrayList();
	private int animTick;

	public RealmsBrokenWorldScreen(Screen screen, RealmsMainScreen realmsMainScreen, long l, boolean bl) {
		super(bl ? Component.translatable("mco.brokenworld.minigame.title") : Component.translatable("mco.brokenworld.title"));
		this.lastScreen = screen;
		this.mainScreen = realmsMainScreen;
		this.serverId = l;
	}

	@Override
	public void init() {
		this.leftX = this.width / 2 - 150;
		this.rightX = this.width / 2 + 190;
		this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_BACK, button -> this.backButtonClicked()).bounds(this.rightX - 80 + 8, row(13) - 5, 70, 20).build()
		);
		if (this.serverData == null) {
			this.fetchServerData(this.serverId);
		} else {
			this.addButtons();
		}
	}

	@Override
	public Component getNarrationMessage() {
		return ComponentUtils.formatList(
			(Collection<? extends Component>)Stream.concat(Stream.of(this.title), Stream.of(this.message)).collect(Collectors.toList()), Component.literal(" ")
		);
	}

	private void addButtons() {
		for (Entry<Integer, RealmsWorldOptions> entry : this.serverData.slots.entrySet()) {
			int i = (Integer)entry.getKey();
			boolean bl = i != this.serverData.activeSlot || this.serverData.worldType == RealmsServer.WorldType.MINIGAME;
			Button button;
			if (bl) {
				button = Button.builder(
						Component.translatable("mco.brokenworld.play"),
						buttonx -> {
							if (((RealmsWorldOptions)this.serverData.slots.get(i)).empty) {
								RealmsResetWorldScreen realmsResetWorldScreen = new RealmsResetWorldScreen(
									this,
									this.serverData,
									Component.translatable("mco.configure.world.switch.slot"),
									Component.translatable("mco.configure.world.switch.slot.subtitle"),
									10526880,
									CommonComponents.GUI_CANCEL,
									this::doSwitchOrReset,
									() -> {
										this.minecraft.setScreen(this);
										this.doSwitchOrReset();
									}
								);
								realmsResetWorldScreen.setSlot(i);
								realmsResetWorldScreen.setResetTitle(Component.translatable("mco.create.world.reset.title"));
								this.minecraft.setScreen(realmsResetWorldScreen);
							} else {
								this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, new SwitchSlotTask(this.serverData.id, i, this::doSwitchOrReset)));
							}
						}
					)
					.bounds(this.getFramePositionX(i), row(8), 80, 20)
					.build();
			} else {
				button = Button.builder(Component.translatable("mco.brokenworld.download"), buttonx -> {
					Component component = Component.translatable("mco.configure.world.restore.download.question.line1");
					Component component2 = Component.translatable("mco.configure.world.restore.download.question.line2");
					this.minecraft.setScreen(new RealmsLongConfirmationScreen(blx -> {
						if (blx) {
							this.downloadWorld(i);
						} else {
							this.minecraft.setScreen(this);
						}
					}, RealmsLongConfirmationScreen.Type.Info, component, component2, true));
				}).bounds(this.getFramePositionX(i), row(8), 80, 20).build();
			}

			if (this.slotsThatHasBeenDownloaded.contains(i)) {
				button.active = false;
				button.setMessage(Component.translatable("mco.brokenworld.downloaded"));
			}

			this.addRenderableWidget(button);
			this.addRenderableWidget(Button.builder(Component.translatable("mco.brokenworld.reset"), buttonx -> {
				RealmsResetWorldScreen realmsResetWorldScreen = new RealmsResetWorldScreen(this, this.serverData, this::doSwitchOrReset, () -> {
					this.minecraft.setScreen(this);
					this.doSwitchOrReset();
				});
				if (i != this.serverData.activeSlot || this.serverData.worldType == RealmsServer.WorldType.MINIGAME) {
					realmsResetWorldScreen.setSlot(i);
				}

				this.minecraft.setScreen(realmsResetWorldScreen);
			}).bounds(this.getFramePositionX(i), row(10), 80, 20).build());
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
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 17, 16777215);

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
							.execute(
								() -> this.minecraft
										.setScreen(new RealmsLongRunningMcoTaskScreen(this, new OpenServerTask(this.serverData, this, this.mainScreen, true, this.minecraft)))
							);
					} else {
						try {
							RealmsServer realmsServer = realmsClient.getOwnWorld(this.serverId);
							this.minecraft.execute(() -> this.mainScreen.newScreen().play(realmsServer, this));
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
						this.clearWidgets();
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

	private void drawSlotFrame(PoseStack poseStack, int i, int j, int k, int l, boolean bl, String string, int m, long n, @Nullable String string2, boolean bl2) {
		if (bl2) {
			RenderSystem.setShaderTexture(0, RealmsWorldSlotButton.EMPTY_SLOT_LOCATION);
		} else if (string2 != null && n != -1L) {
			RealmsTextureManager.bindWorldTemplate(String.valueOf(n), string2);
		} else if (m == 1) {
			RenderSystem.setShaderTexture(0, RealmsWorldSlotButton.DEFAULT_WORLD_SLOT_1);
		} else if (m == 2) {
			RenderSystem.setShaderTexture(0, RealmsWorldSlotButton.DEFAULT_WORLD_SLOT_2);
		} else if (m == 3) {
			RenderSystem.setShaderTexture(0, RealmsWorldSlotButton.DEFAULT_WORLD_SLOT_3);
		} else {
			RealmsTextureManager.bindWorldTemplate(String.valueOf(this.serverData.minigameId), this.serverData.minigameImage);
		}

		if (!bl) {
			RenderSystem.setShaderColor(0.56F, 0.56F, 0.56F, 1.0F);
		} else if (bl) {
			float f = 0.9F + 0.1F * Mth.cos((float)this.animTick * 0.2F);
			RenderSystem.setShaderColor(f, f, f, 1.0F);
		}

		GuiComponent.blit(poseStack, i + 3, j + 3, 0.0F, 0.0F, 74, 74, 74, 74);
		RenderSystem.setShaderTexture(0, RealmsWorldSlotButton.SLOT_FRAME_LOCATION);
		if (bl) {
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		} else {
			RenderSystem.setShaderColor(0.56F, 0.56F, 0.56F, 1.0F);
		}

		GuiComponent.blit(poseStack, i, j, 0.0F, 0.0F, 80, 80, 80, 80);
		drawCenteredString(poseStack, this.font, string, i + 40, j + 66, 16777215);
	}
}
