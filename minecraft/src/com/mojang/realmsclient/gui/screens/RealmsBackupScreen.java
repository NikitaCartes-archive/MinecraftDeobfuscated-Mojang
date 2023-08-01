package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.Backup;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.RealmsUtil;
import com.mojang.realmsclient.util.task.DownloadTask;
import com.mojang.realmsclient.util.task.RestoreTask;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsBackupScreen extends RealmsScreen {
	static final Logger LOGGER = LogUtils.getLogger();
	static final Component RESTORE_TOOLTIP = Component.translatable("mco.backup.button.restore");
	static final Component HAS_CHANGES_TOOLTIP = Component.translatable("mco.backup.changes.tooltip");
	private static final Component TITLE = Component.translatable("mco.configure.world.backup");
	private static final Component NO_BACKUPS_LABEL = Component.translatable("mco.backup.nobackups");
	private final RealmsConfigureWorldScreen lastScreen;
	List<Backup> backups = Collections.emptyList();
	RealmsBackupScreen.BackupObjectSelectionList backupObjectSelectionList;
	int selectedBackup = -1;
	private final int slotId;
	private Button downloadButton;
	private Button restoreButton;
	private Button changesButton;
	Boolean noBackups = false;
	final RealmsServer serverData;
	private static final String UPLOADED_KEY = "uploaded";

	public RealmsBackupScreen(RealmsConfigureWorldScreen realmsConfigureWorldScreen, RealmsServer realmsServer, int i) {
		super(TITLE);
		this.lastScreen = realmsConfigureWorldScreen;
		this.serverData = realmsServer;
		this.slotId = i;
	}

	@Override
	public void init() {
		this.backupObjectSelectionList = new RealmsBackupScreen.BackupObjectSelectionList();
		(new Thread("Realms-fetch-backups") {
			public void run() {
				RealmsClient realmsClient = RealmsClient.create();

				try {
					List<Backup> list = realmsClient.backupsFor(RealmsBackupScreen.this.serverData.id).backups;
					RealmsBackupScreen.this.minecraft.execute(() -> {
						RealmsBackupScreen.this.backups = list;
						RealmsBackupScreen.this.noBackups = RealmsBackupScreen.this.backups.isEmpty();
						RealmsBackupScreen.this.backupObjectSelectionList.clear();

						for (Backup backup : RealmsBackupScreen.this.backups) {
							RealmsBackupScreen.this.backupObjectSelectionList.addEntry(backup);
						}
					});
				} catch (RealmsServiceException var3) {
					RealmsBackupScreen.LOGGER.error("Couldn't request backups", (Throwable)var3);
				}
			}
		}).start();
		this.downloadButton = this.addRenderableWidget(
			Button.builder(Component.translatable("mco.backup.button.download"), button -> this.downloadClicked()).bounds(this.width - 135, row(1), 120, 20).build()
		);
		this.restoreButton = this.addRenderableWidget(
			Button.builder(Component.translatable("mco.backup.button.restore"), button -> this.restoreClicked(this.selectedBackup))
				.bounds(this.width - 135, row(3), 120, 20)
				.build()
		);
		this.changesButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.backup.changes.tooltip"), button -> {
			this.minecraft.setScreen(new RealmsBackupInfoScreen(this, (Backup)this.backups.get(this.selectedBackup)));
			this.selectedBackup = -1;
		}).bounds(this.width - 135, row(5), 120, 20).build());
		this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_BACK, button -> this.minecraft.setScreen(this.lastScreen)).bounds(this.width - 100, this.height - 35, 85, 20).build()
		);
		this.addWidget(this.backupObjectSelectionList);
		this.magicalSpecialHackyFocus(this.backupObjectSelectionList);
		this.updateButtonStates();
	}

	void updateButtonStates() {
		this.restoreButton.visible = this.shouldRestoreButtonBeVisible();
		this.changesButton.visible = this.shouldChangesButtonBeVisible();
	}

	private boolean shouldChangesButtonBeVisible() {
		return this.selectedBackup == -1 ? false : !((Backup)this.backups.get(this.selectedBackup)).changeList.isEmpty();
	}

	private boolean shouldRestoreButtonBeVisible() {
		return this.selectedBackup == -1 ? false : !this.serverData.expired;
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

	void restoreClicked(int i) {
		if (i >= 0 && i < this.backups.size() && !this.serverData.expired) {
			this.selectedBackup = i;
			Date date = ((Backup)this.backups.get(i)).lastModifiedDate;
			String string = DateFormat.getDateTimeInstance(3, 3).format(date);
			Component component = RealmsUtil.convertToAgePresentationFromInstant(date);
			Component component2 = Component.translatable("mco.configure.world.restore.question.line1", string, component);
			Component component3 = Component.translatable("mco.configure.world.restore.question.line2");
			this.minecraft.setScreen(new RealmsLongConfirmationScreen(bl -> {
				if (bl) {
					this.restore();
				} else {
					this.selectedBackup = -1;
					this.minecraft.setScreen(this);
				}
			}, RealmsLongConfirmationScreen.Type.WARNING, component2, component3, true));
		}
	}

	private void downloadClicked() {
		Component component = Component.translatable("mco.configure.world.restore.download.question.line1");
		Component component2 = Component.translatable("mco.configure.world.restore.download.question.line2");
		this.minecraft.setScreen(new RealmsLongConfirmationScreen(bl -> {
			if (bl) {
				this.downloadWorldData();
			} else {
				this.minecraft.setScreen(this);
			}
		}, RealmsLongConfirmationScreen.Type.INFO, component, component2, true));
	}

	private void downloadWorldData() {
		this.minecraft
			.setScreen(
				new RealmsLongRunningMcoTaskScreen(
					this.lastScreen.getNewScreen(),
					new DownloadTask(
						this.serverData.id,
						this.slotId,
						this.serverData.name + " (" + ((RealmsWorldOptions)this.serverData.slots.get(this.serverData.activeSlot)).getSlotName(this.serverData.activeSlot) + ")",
						this
					)
				)
			);
	}

	private void restore() {
		Backup backup = (Backup)this.backups.get(this.selectedBackup);
		this.selectedBackup = -1;
		this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen.getNewScreen(), new RestoreTask(backup, this.serverData.id, this.lastScreen)));
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		this.backupObjectSelectionList.render(guiGraphics, i, j, f);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 12, -1);
		if (this.noBackups) {
			guiGraphics.drawString(this.font, NO_BACKUPS_LABEL, 20, this.height / 2 - 10, -1, false);
		}

		this.downloadButton.active = !this.noBackups;
	}

	@Environment(EnvType.CLIENT)
	class BackupObjectSelectionList extends RealmsObjectSelectionList<RealmsBackupScreen.Entry> {
		public BackupObjectSelectionList() {
			super(RealmsBackupScreen.this.width - 150, RealmsBackupScreen.this.height, 32, RealmsBackupScreen.this.height - 15, 36);
		}

		public void addEntry(Backup backup) {
			this.addEntry(RealmsBackupScreen.this.new Entry(backup));
		}

		@Override
		public int getRowWidth() {
			return (int)((double)this.width * 0.93);
		}

		@Override
		public int getMaxPosition() {
			return this.getItemCount() * 36;
		}

		@Override
		public int getScrollbarPosition() {
			return this.width - 5;
		}

		@Override
		public void selectItem(int i) {
			super.selectItem(i);
			this.selectInviteListItem(i);
		}

		public void selectInviteListItem(int i) {
			RealmsBackupScreen.this.selectedBackup = i;
			RealmsBackupScreen.this.updateButtonStates();
		}

		public void setSelected(@Nullable RealmsBackupScreen.Entry entry) {
			super.setSelected(entry);
			RealmsBackupScreen.this.selectedBackup = this.children().indexOf(entry);
			RealmsBackupScreen.this.updateButtonStates();
		}
	}

	@Environment(EnvType.CLIENT)
	class Entry extends ObjectSelectionList.Entry<RealmsBackupScreen.Entry> {
		private static final int Y_PADDING = 2;
		private static final int X_PADDING = 7;
		private static final WidgetSprites CHANGES_BUTTON_SPRITES = new WidgetSprites(
			new ResourceLocation("backup/changes"), new ResourceLocation("backup/changes_highlighted")
		);
		private static final WidgetSprites RESTORE_BUTTON_SPRITES = new WidgetSprites(
			new ResourceLocation("backup/restore"), new ResourceLocation("backup/restore_highlighted")
		);
		private final Backup backup;
		private final List<AbstractWidget> children = new ArrayList();
		@Nullable
		private ImageButton restoreButton;
		@Nullable
		private ImageButton changesButton;

		public Entry(Backup backup) {
			this.backup = backup;
			this.populateChangeList(backup);
			if (!backup.changeList.isEmpty()) {
				this.addChangesButton();
			}

			if (!RealmsBackupScreen.this.serverData.expired) {
				this.addRestoreButton();
			}
		}

		private void populateChangeList(Backup backup) {
			int i = RealmsBackupScreen.this.backups.indexOf(backup);
			if (i != RealmsBackupScreen.this.backups.size() - 1) {
				Backup backup2 = (Backup)RealmsBackupScreen.this.backups.get(i + 1);

				for (String string : backup.metadata.keySet()) {
					if (!string.contains("uploaded") && backup2.metadata.containsKey(string)) {
						if (!((String)backup.metadata.get(string)).equals(backup2.metadata.get(string))) {
							this.addToChangeList(string);
						}
					} else {
						this.addToChangeList(string);
					}
				}
			}
		}

		private void addToChangeList(String string) {
			if (string.contains("uploaded")) {
				String string2 = DateFormat.getDateTimeInstance(3, 3).format(this.backup.lastModifiedDate);
				this.backup.changeList.put(string, string2);
				this.backup.setUploadedVersion(true);
			} else {
				this.backup.changeList.put(string, (String)this.backup.metadata.get(string));
			}
		}

		private void addChangesButton() {
			int i = 9;
			int j = 9;
			int k = RealmsBackupScreen.this.backupObjectSelectionList.getRowRight() - 9 - 28;
			int l = RealmsBackupScreen.this.backupObjectSelectionList.getRowTop(RealmsBackupScreen.this.backups.indexOf(this.backup)) + 2;
			this.changesButton = new ImageButton(
				k,
				l,
				9,
				9,
				CHANGES_BUTTON_SPRITES,
				button -> RealmsBackupScreen.this.minecraft.setScreen(new RealmsBackupInfoScreen(RealmsBackupScreen.this, this.backup)),
				CommonComponents.EMPTY
			);
			this.changesButton.setTooltip(Tooltip.create(RealmsBackupScreen.HAS_CHANGES_TOOLTIP));
			this.children.add(this.changesButton);
		}

		private void addRestoreButton() {
			int i = 17;
			int j = 10;
			int k = RealmsBackupScreen.this.backupObjectSelectionList.getRowRight() - 17 - 7;
			int l = RealmsBackupScreen.this.backupObjectSelectionList.getRowTop(RealmsBackupScreen.this.backups.indexOf(this.backup)) + 2;
			this.restoreButton = new ImageButton(
				k,
				l,
				17,
				10,
				RESTORE_BUTTON_SPRITES,
				button -> RealmsBackupScreen.this.restoreClicked(RealmsBackupScreen.this.backups.indexOf(this.backup)),
				CommonComponents.EMPTY
			);
			this.restoreButton.setTooltip(Tooltip.create(RealmsBackupScreen.RESTORE_TOOLTIP));
			this.children.add(this.restoreButton);
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			if (this.restoreButton != null) {
				this.restoreButton.mouseClicked(d, e, i);
			}

			if (this.changesButton != null) {
				this.changesButton.mouseClicked(d, e, i);
			}

			return true;
		}

		@Override
		public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			int p = this.backup.isUploadedVersion() ? -8388737 : 16777215;
			guiGraphics.drawString(
				RealmsBackupScreen.this.font,
				Component.translatable("mco.backup.entry", RealmsUtil.convertToAgePresentationFromInstant(this.backup.lastModifiedDate)),
				k,
				j + 1,
				p,
				false
			);
			guiGraphics.drawString(RealmsBackupScreen.this.font, this.getMediumDatePresentation(this.backup.lastModifiedDate), k, j + 12, 5000268, false);
			this.children.forEach(abstractWidget -> {
				abstractWidget.setY(j + 2);
				abstractWidget.render(guiGraphics, n, o, f);
			});
		}

		private String getMediumDatePresentation(Date date) {
			return DateFormat.getDateTimeInstance(3, 3).format(date);
		}

		@Override
		public Component getNarration() {
			return Component.translatable("narrator.select", this.backup.lastModifiedDate.toString());
		}
	}
}
