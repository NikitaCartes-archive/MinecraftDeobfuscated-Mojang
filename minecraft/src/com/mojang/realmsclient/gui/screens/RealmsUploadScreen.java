package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.Unit;
import com.mojang.realmsclient.client.UploadStatus;
import com.mojang.realmsclient.client.worldupload.RealmsUploadException;
import com.mojang.realmsclient.client.worldupload.RealmsWorldUpload;
import com.mojang.realmsclient.client.worldupload.RealmsWorldUploadStatusTracker;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.util.task.LongRunningTask;
import com.mojang.realmsclient.util.task.RealmCreationTask;
import com.mojang.realmsclient.util.task.SwitchSlotTask;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.world.level.storage.LevelSummary;

@Environment(EnvType.CLIENT)
public class RealmsUploadScreen extends RealmsScreen implements RealmsWorldUploadStatusTracker {
	private static final int BAR_WIDTH = 200;
	private static final int BAR_TOP = 80;
	private static final int BAR_BOTTOM = 95;
	private static final int BAR_BORDER = 1;
	private static final String[] DOTS = new String[]{"", ".", ". .", ". . ."};
	private static final Component VERIFYING_TEXT = Component.translatable("mco.upload.verifying");
	private final RealmsResetWorldScreen lastScreen;
	private final LevelSummary selectedLevel;
	@Nullable
	private final RealmCreationTask realmCreationTask;
	private final long realmId;
	private final int slotId;
	final AtomicReference<RealmsWorldUpload> currentUpload = new AtomicReference();
	private final UploadStatus uploadStatus;
	private final RateLimiter narrationRateLimiter;
	@Nullable
	private volatile Component[] errorMessage;
	private volatile Component status = Component.translatable("mco.upload.preparing");
	@Nullable
	private volatile String progress;
	private volatile boolean cancelled;
	private volatile boolean uploadFinished;
	private volatile boolean showDots = true;
	private volatile boolean uploadStarted;
	@Nullable
	private Button backButton;
	@Nullable
	private Button cancelButton;
	private int tickCount;
	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

	public RealmsUploadScreen(
		@Nullable RealmCreationTask realmCreationTask, long l, int i, RealmsResetWorldScreen realmsResetWorldScreen, LevelSummary levelSummary
	) {
		super(GameNarrator.NO_TITLE);
		this.realmCreationTask = realmCreationTask;
		this.realmId = l;
		this.slotId = i;
		this.lastScreen = realmsResetWorldScreen;
		this.selectedLevel = levelSummary;
		this.uploadStatus = new UploadStatus();
		this.narrationRateLimiter = RateLimiter.create(0.1F);
	}

	@Override
	public void init() {
		this.backButton = this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, button -> this.onBack()).build());
		this.backButton.visible = false;
		this.cancelButton = this.layout.addToFooter(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onCancel()).build());
		if (!this.uploadStarted) {
			if (this.lastScreen.slot == -1) {
				this.uploadStarted = true;
				this.upload();
			} else {
				List<LongRunningTask> list = new ArrayList();
				if (this.realmCreationTask != null) {
					list.add(this.realmCreationTask);
				}

				list.add(new SwitchSlotTask(this.realmId, this.lastScreen.slot, () -> {
					if (!this.uploadStarted) {
						this.uploadStarted = true;
						this.minecraft.execute(() -> {
							this.minecraft.setScreen(this);
							this.upload();
						});
					}
				}));
				this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, (LongRunningTask[])list.toArray(new LongRunningTask[0])));
			}
		}

		this.layout.visitWidgets(guiEventListener -> {
			AbstractWidget var10000 = this.addRenderableWidget(guiEventListener);
		});
		this.repositionElements();
	}

	@Override
	protected void repositionElements() {
		this.layout.arrangeElements();
	}

	private void onBack() {
		this.minecraft.setScreen(new RealmsConfigureWorldScreen(new RealmsMainScreen(new TitleScreen()), this.realmId));
	}

	private void onCancel() {
		this.cancelled = true;
		RealmsWorldUpload realmsWorldUpload = (RealmsWorldUpload)this.currentUpload.get();
		if (realmsWorldUpload != null) {
			realmsWorldUpload.cancel();
		} else {
			this.minecraft.setScreen(this.lastScreen);
		}
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256) {
			if (this.showDots) {
				this.onCancel();
			} else {
				this.onBack();
			}

			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		if (!this.uploadFinished && this.uploadStatus.uploadStarted() && this.uploadStatus.uploadCompleted() && this.cancelButton != null) {
			this.status = VERIFYING_TEXT;
			this.cancelButton.active = false;
		}

		guiGraphics.drawCenteredString(this.font, this.status, this.width / 2, 50, -1);
		if (this.showDots) {
			guiGraphics.drawString(this.font, DOTS[this.tickCount / 10 % DOTS.length], this.width / 2 + this.font.width(this.status) / 2 + 5, 50, -1, false);
		}

		if (this.uploadStatus.uploadStarted() && !this.cancelled) {
			this.drawProgressBar(guiGraphics);
			this.drawUploadSpeed(guiGraphics);
		}

		Component[] components = this.errorMessage;
		if (components != null) {
			for (int k = 0; k < components.length; k++) {
				guiGraphics.drawCenteredString(this.font, components[k], this.width / 2, 110 + 12 * k, -65536);
			}
		}
	}

	private void drawProgressBar(GuiGraphics guiGraphics) {
		double d = this.uploadStatus.getPercentage();
		this.progress = String.format(Locale.ROOT, "%.1f", d * 100.0);
		int i = (this.width - 200) / 2;
		int j = i + (int)Math.round(200.0 * d);
		guiGraphics.fill(i - 1, 79, j + 1, 96, -1);
		guiGraphics.fill(i, 80, j, 95, -8355712);
		guiGraphics.drawCenteredString(this.font, Component.translatable("mco.upload.percent", this.progress), this.width / 2, 84, -1);
	}

	private void drawUploadSpeed(GuiGraphics guiGraphics) {
		this.drawUploadSpeed0(guiGraphics, this.uploadStatus.getBytesPerSecond());
	}

	private void drawUploadSpeed0(GuiGraphics guiGraphics, long l) {
		String string = this.progress;
		if (l > 0L && string != null) {
			int i = this.font.width(string);
			String string2 = "(" + Unit.humanReadable(l) + "/s)";
			guiGraphics.drawString(this.font, string2, this.width / 2 + i / 2 + 15, 84, -1, false);
		}
	}

	@Override
	public void tick() {
		super.tick();
		this.tickCount++;
		this.uploadStatus.refreshBytesPerSecond();
		if (this.narrationRateLimiter.tryAcquire(1)) {
			Component component = this.createProgressNarrationMessage();
			this.minecraft.getNarrator().sayNow(component);
		}
	}

	private Component createProgressNarrationMessage() {
		List<Component> list = Lists.<Component>newArrayList();
		list.add(this.status);
		if (this.progress != null) {
			list.add(Component.translatable("mco.upload.percent", this.progress));
		}

		Component[] components = this.errorMessage;
		if (components != null) {
			list.addAll(Arrays.asList(components));
		}

		return CommonComponents.joinLines(list);
	}

	private void upload() {
		Path path = this.minecraft.gameDirectory.toPath().resolve("saves").resolve(this.selectedLevel.getLevelId());
		RealmsWorldOptions realmsWorldOptions = RealmsWorldOptions.createFromSettings(
			this.selectedLevel.getSettings(), this.selectedLevel.levelVersion().minecraftVersionName()
		);
		RealmsWorldUpload realmsWorldUpload = new RealmsWorldUpload(path, realmsWorldOptions, this.minecraft.getUser(), this.realmId, this.slotId, this);
		if (!this.currentUpload.compareAndSet(null, realmsWorldUpload)) {
			throw new IllegalStateException("Tried to start uploading but was already uploading");
		} else {
			realmsWorldUpload.packAndUpload().handleAsync((object, throwable) -> {
				if (throwable != null) {
					if (throwable instanceof CompletionException completionException) {
						throwable = completionException.getCause();
					}

					if (throwable instanceof RealmsUploadException realmsUploadException) {
						if (realmsUploadException.getStatusMessage() != null) {
							this.status = realmsUploadException.getStatusMessage();
						}

						this.setErrorMessage(realmsUploadException.getErrorMessages());
					} else {
						this.status = Component.translatable("mco.upload.failed", throwable.getMessage());
					}
				} else {
					this.status = Component.translatable("mco.upload.done");
					if (this.backButton != null) {
						this.backButton.setMessage(CommonComponents.GUI_DONE);
					}
				}

				this.uploadFinished = true;
				this.showDots = false;
				if (this.backButton != null) {
					this.backButton.visible = true;
				}

				if (this.cancelButton != null) {
					this.cancelButton.visible = false;
				}

				this.currentUpload.set(null);
				return null;
			}, this.minecraft);
		}
	}

	private void setErrorMessage(@Nullable Component... components) {
		this.errorMessage = components;
	}

	@Override
	public UploadStatus getUploadStatus() {
		return this.uploadStatus;
	}

	@Override
	public void setUploading() {
		this.status = Component.translatable("mco.upload.uploading", this.selectedLevel.getLevelName());
	}
}
