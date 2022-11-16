package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsBackupScreen extends RealmsScreen {
	static final Logger LOGGER = LogUtils.getLogger();
	static final ResourceLocation PLUS_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/plus_icon.png");
	static final ResourceLocation RESTORE_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/restore_icon.png");
	static final Component RESTORE_TOOLTIP = Component.translatable("mco.backup.button.restore");
	static final Component HAS_CHANGES_TOOLTIP = Component.translatable("mco.backup.changes.tooltip");
	private static final Component TITLE = Component.translatable("mco.configure.world.backup");
	private static final Component NO_BACKUPS_LABEL = Component.translatable("mco.backup.nobackups");
	static int lastScrollPosition = -1;
	private final RealmsConfigureWorldScreen lastScreen;
	List<Backup> backups = Collections.emptyList();
	@Nullable
	Component toolTip;
	RealmsBackupScreen.BackupObjectSelectionList backupObjectSelectionList;
	int selectedBackup = -1;
	private final int slotId;
	private Button downloadButton;
	private Button restoreButton;
	private Button changesButton;
	Boolean noBackups = false;
	final RealmsServer serverData;
	private static final String UPLOADED_KEY = "Uploaded";

	public RealmsBackupScreen(RealmsConfigureWorldScreen realmsConfigureWorldScreen, RealmsServer realmsServer, int i) {
		super(Component.translatable("mco.configure.world.backup"));
		this.lastScreen = realmsConfigureWorldScreen;
		this.serverData = realmsServer;
		this.slotId = i;
	}

	@Override
	public void init() {
		this.backupObjectSelectionList = new RealmsBackupScreen.BackupObjectSelectionList();
		if (lastScrollPosition != -1) {
			this.backupObjectSelectionList.setScrollAmount((double)lastScrollPosition);
		}

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

						RealmsBackupScreen.this.generateChangeList();
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

	void generateChangeList() {
		if (this.backups.size() > 1) {
			for (int i = 0; i < this.backups.size() - 1; i++) {
				Backup backup = (Backup)this.backups.get(i);
				Backup backup2 = (Backup)this.backups.get(i + 1);
				if (!backup.metadata.isEmpty() && !backup2.metadata.isEmpty()) {
					for (String string : backup.metadata.keySet()) {
						if (!string.contains("Uploaded") && backup2.metadata.containsKey(string)) {
							if (!((String)backup.metadata.get(string)).equals(backup2.metadata.get(string))) {
								this.addToChangeList(backup, string);
							}
						} else {
							this.addToChangeList(backup, string);
						}
					}
				}
			}
		}
	}

	private void addToChangeList(Backup backup, String string) {
		if (string.contains("Uploaded")) {
			String string2 = DateFormat.getDateTimeInstance(3, 3).format(backup.lastModifiedDate);
			backup.changeList.put(string, string2);
			backup.setUploadedVersion(true);
		} else {
			backup.changeList.put(string, (String)backup.metadata.get(string));
		}
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
			String string2 = RealmsUtil.convertToAgePresentationFromInstant(date);
			Component component = Component.translatable("mco.configure.world.restore.question.line1", string, string2);
			Component component2 = Component.translatable("mco.configure.world.restore.question.line2");
			this.minecraft.setScreen(new RealmsLongConfirmationScreen(bl -> {
				if (bl) {
					this.restore();
				} else {
					this.selectedBackup = -1;
					this.minecraft.setScreen(this);
				}
			}, RealmsLongConfirmationScreen.Type.Warning, component, component2, true));
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
		}, RealmsLongConfirmationScreen.Type.Info, component, component2, true));
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
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.toolTip = null;
		this.renderBackground(poseStack);
		this.backupObjectSelectionList.render(poseStack, i, j, f);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 12, 16777215);
		this.font.draw(poseStack, TITLE, (float)((this.width - 150) / 2 - 90), 20.0F, 10526880);
		if (this.noBackups) {
			this.font.draw(poseStack, NO_BACKUPS_LABEL, 20.0F, (float)(this.height / 2 - 10), 16777215);
		}

		this.downloadButton.active = !this.noBackups;
		super.render(poseStack, i, j, f);
		if (this.toolTip != null) {
			this.renderMousehoverTooltip(poseStack, this.toolTip, i, j);
		}
	}

	protected void renderMousehoverTooltip(PoseStack poseStack, @Nullable Component component, int i, int j) {
		if (component != null) {
			int k = i + 12;
			int l = j - 12;
			int m = this.font.width(component);
			this.fillGradient(poseStack, k - 3, l - 3, k + m + 3, l + 8 + 3, -1073741824, -1073741824);
			this.font.drawShadow(poseStack, component, (float)k, (float)l, 16777215);
		}
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
		public boolean isFocused() {
			return RealmsBackupScreen.this.getFocused() == this;
		}

		@Override
		public int getMaxPosition() {
			return this.getItemCount() * 36;
		}

		@Override
		public void renderBackground(PoseStack poseStack) {
			RealmsBackupScreen.this.renderBackground(poseStack);
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			if (i != 0) {
				return false;
			} else if (d < (double)this.getScrollbarPosition() && e >= (double)this.y0 && e <= (double)this.y1) {
				int j = this.width / 2 - 92;
				int k = this.width;
				int l = (int)Math.floor(e - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount();
				int m = l / this.itemHeight;
				if (d >= (double)j && d <= (double)k && m >= 0 && l >= 0 && m < this.getItemCount()) {
					this.selectItem(m);
					this.itemClicked(l, m, d, e, this.width);
				}

				return true;
			} else {
				return false;
			}
		}

		@Override
		public int getScrollbarPosition() {
			return this.width - 5;
		}

		@Override
		public void itemClicked(int i, int j, double d, double e, int k) {
			int l = this.width - 35;
			int m = j * this.itemHeight + 36 - (int)this.getScrollAmount();
			int n = l + 10;
			int o = m - 3;
			if (d >= (double)l && d <= (double)(l + 9) && e >= (double)m && e <= (double)(m + 9)) {
				if (!((Backup)RealmsBackupScreen.this.backups.get(j)).changeList.isEmpty()) {
					RealmsBackupScreen.this.selectedBackup = -1;
					RealmsBackupScreen.lastScrollPosition = (int)this.getScrollAmount();
					this.minecraft.setScreen(new RealmsBackupInfoScreen(RealmsBackupScreen.this, (Backup)RealmsBackupScreen.this.backups.get(j)));
				}
			} else if (d >= (double)n && d < (double)(n + 13) && e >= (double)o && e < (double)(o + 15)) {
				RealmsBackupScreen.lastScrollPosition = (int)this.getScrollAmount();
				RealmsBackupScreen.this.restoreClicked(j);
			}
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
		private final Backup backup;

		public Entry(Backup backup) {
			this.backup = backup;
		}

		@Override
		public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			this.renderBackupItem(poseStack, this.backup, k - 40, j, n, o);
		}

		private void renderBackupItem(PoseStack poseStack, Backup backup, int i, int j, int k, int l) {
			int m = backup.isUploadedVersion() ? -8388737 : 16777215;
			RealmsBackupScreen.this.font
				.draw(poseStack, "Backup (" + RealmsUtil.convertToAgePresentationFromInstant(backup.lastModifiedDate) + ")", (float)(i + 40), (float)(j + 1), m);
			RealmsBackupScreen.this.font.draw(poseStack, this.getMediumDatePresentation(backup.lastModifiedDate), (float)(i + 40), (float)(j + 12), 5000268);
			int n = RealmsBackupScreen.this.width - 175;
			int o = -3;
			int p = n - 10;
			int q = 0;
			if (!RealmsBackupScreen.this.serverData.expired) {
				this.drawRestore(poseStack, n, j + -3, k, l);
			}

			if (!backup.changeList.isEmpty()) {
				this.drawInfo(poseStack, p, j + 0, k, l);
			}
		}

		private String getMediumDatePresentation(Date date) {
			return DateFormat.getDateTimeInstance(3, 3).format(date);
		}

		private void drawRestore(PoseStack poseStack, int i, int j, int k, int l) {
			boolean bl = k >= i && k <= i + 12 && l >= j && l <= j + 14 && l < RealmsBackupScreen.this.height - 15 && l > 32;
			RenderSystem.setShaderTexture(0, RealmsBackupScreen.RESTORE_ICON_LOCATION);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			poseStack.pushPose();
			poseStack.scale(0.5F, 0.5F, 0.5F);
			float f = bl ? 28.0F : 0.0F;
			GuiComponent.blit(poseStack, i * 2, j * 2, 0.0F, f, 23, 28, 23, 56);
			poseStack.popPose();
			if (bl) {
				RealmsBackupScreen.this.toolTip = RealmsBackupScreen.RESTORE_TOOLTIP;
			}
		}

		private void drawInfo(PoseStack poseStack, int i, int j, int k, int l) {
			boolean bl = k >= i && k <= i + 8 && l >= j && l <= j + 8 && l < RealmsBackupScreen.this.height - 15 && l > 32;
			RenderSystem.setShaderTexture(0, RealmsBackupScreen.PLUS_ICON_LOCATION);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			poseStack.pushPose();
			poseStack.scale(0.5F, 0.5F, 0.5F);
			float f = bl ? 15.0F : 0.0F;
			GuiComponent.blit(poseStack, i * 2, j * 2, 0.0F, f, 15, 15, 15, 30);
			poseStack.popPose();
			if (bl) {
				RealmsBackupScreen.this.toolTip = RealmsBackupScreen.HAS_CHANGES_TOOLTIP;
			}
		}

		@Override
		public Component getNarration() {
			return Component.translatable("narrator.select", this.backup.lastModifiedDate.toString());
		}
	}
}
