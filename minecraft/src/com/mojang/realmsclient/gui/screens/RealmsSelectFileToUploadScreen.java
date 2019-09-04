package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.mojang.realmsclient.gui.RealmsConstants;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.RealmListEntry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsAnvilLevelStorageSource;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsLevelSummary;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.Tezzelator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsSelectFileToUploadScreen extends RealmsScreen {
	private static final Logger LOGGER = LogManager.getLogger();
	private final RealmsResetWorldScreen lastScreen;
	private final long worldId;
	private final int slotId;
	private RealmsButton uploadButton;
	private final DateFormat DATE_FORMAT = new SimpleDateFormat();
	private List<RealmsLevelSummary> levelList = Lists.<RealmsLevelSummary>newArrayList();
	private int selectedWorld = -1;
	private RealmsSelectFileToUploadScreen.WorldSelectionList worldSelectionList;
	private String worldLang;
	private String conversionLang;
	private final String[] gameModesLang = new String[4];
	private RealmsLabel titleLabel;
	private RealmsLabel subtitleLabel;
	private RealmsLabel noWorldsLabel;

	public RealmsSelectFileToUploadScreen(long l, int i, RealmsResetWorldScreen realmsResetWorldScreen) {
		this.lastScreen = realmsResetWorldScreen;
		this.worldId = l;
		this.slotId = i;
	}

	private void loadLevelList() throws Exception {
		RealmsAnvilLevelStorageSource realmsAnvilLevelStorageSource = this.getLevelStorageSource();
		this.levelList = realmsAnvilLevelStorageSource.getLevelList();
		Collections.sort(this.levelList);

		for (RealmsLevelSummary realmsLevelSummary : this.levelList) {
			this.worldSelectionList.addEntry(realmsLevelSummary);
		}
	}

	@Override
	public void init() {
		this.setKeyboardHandlerSendRepeatsToGui(true);
		this.worldSelectionList = new RealmsSelectFileToUploadScreen.WorldSelectionList();

		try {
			this.loadLevelList();
		} catch (Exception var2) {
			LOGGER.error("Couldn't load level list", (Throwable)var2);
			Realms.setScreen(new RealmsGenericErrorScreen("Unable to load worlds", var2.getMessage(), this.lastScreen));
			return;
		}

		this.worldLang = getLocalizedString("selectWorld.world");
		this.conversionLang = getLocalizedString("selectWorld.conversion");
		this.gameModesLang[Realms.survivalId()] = getLocalizedString("gameMode.survival");
		this.gameModesLang[Realms.creativeId()] = getLocalizedString("gameMode.creative");
		this.gameModesLang[Realms.adventureId()] = getLocalizedString("gameMode.adventure");
		this.gameModesLang[Realms.spectatorId()] = getLocalizedString("gameMode.spectator");
		this.addWidget(this.worldSelectionList);
		this.buttonsAdd(new RealmsButton(1, this.width() / 2 + 6, this.height() - 32, 153, 20, getLocalizedString("gui.back")) {
			@Override
			public void onPress() {
				Realms.setScreen(RealmsSelectFileToUploadScreen.this.lastScreen);
			}
		});
		this.buttonsAdd(this.uploadButton = new RealmsButton(2, this.width() / 2 - 154, this.height() - 32, 153, 20, getLocalizedString("mco.upload.button.name")) {
			@Override
			public void onPress() {
				RealmsSelectFileToUploadScreen.this.upload();
			}
		});
		this.uploadButton.active(this.selectedWorld >= 0 && this.selectedWorld < this.levelList.size());
		this.addWidget(this.titleLabel = new RealmsLabel(getLocalizedString("mco.upload.select.world.title"), this.width() / 2, 13, 16777215));
		this.addWidget(
			this.subtitleLabel = new RealmsLabel(getLocalizedString("mco.upload.select.world.subtitle"), this.width() / 2, RealmsConstants.row(-1), 10526880)
		);
		if (this.levelList.isEmpty()) {
			this.addWidget(this.noWorldsLabel = new RealmsLabel(getLocalizedString("mco.upload.select.world.none"), this.width() / 2, this.height() / 2 - 20, 16777215));
		} else {
			this.noWorldsLabel = null;
		}

		this.narrateLabels();
	}

	@Override
	public void removed() {
		this.setKeyboardHandlerSendRepeatsToGui(false);
	}

	private void upload() {
		if (this.selectedWorld != -1 && !((RealmsLevelSummary)this.levelList.get(this.selectedWorld)).isHardcore()) {
			RealmsLevelSummary realmsLevelSummary = (RealmsLevelSummary)this.levelList.get(this.selectedWorld);
			Realms.setScreen(new RealmsUploadScreen(this.worldId, this.slotId, this.lastScreen, realmsLevelSummary));
		}
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		this.worldSelectionList.render(i, j, f);
		this.titleLabel.render(this);
		this.subtitleLabel.render(this);
		if (this.noWorldsLabel != null) {
			this.noWorldsLabel.render(this);
		}

		super.render(i, j, f);
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256) {
			Realms.setScreen(this.lastScreen);
			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	@Override
	public void tick() {
		super.tick();
	}

	private String gameModeName(RealmsLevelSummary realmsLevelSummary) {
		return this.gameModesLang[realmsLevelSummary.getGameMode()];
	}

	private String formatLastPlayed(RealmsLevelSummary realmsLevelSummary) {
		return this.DATE_FORMAT.format(new Date(realmsLevelSummary.getLastPlayed()));
	}

	@Environment(EnvType.CLIENT)
	class WorldListEntry extends RealmListEntry {
		final RealmsLevelSummary levelSummary;

		public WorldListEntry(RealmsLevelSummary realmsLevelSummary) {
			this.levelSummary = realmsLevelSummary;
		}

		@Override
		public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			this.renderItem(this.levelSummary, i, k, j, m, Tezzelator.instance, n, o);
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			RealmsSelectFileToUploadScreen.this.worldSelectionList.selectItem(RealmsSelectFileToUploadScreen.this.levelList.indexOf(this.levelSummary));
			return true;
		}

		protected void renderItem(RealmsLevelSummary realmsLevelSummary, int i, int j, int k, int l, Tezzelator tezzelator, int m, int n) {
			String string = realmsLevelSummary.getLevelName();
			if (string == null || string.isEmpty()) {
				string = RealmsSelectFileToUploadScreen.this.worldLang + " " + (i + 1);
			}

			String string2 = realmsLevelSummary.getLevelId();
			string2 = string2 + " (" + RealmsSelectFileToUploadScreen.this.formatLastPlayed(realmsLevelSummary);
			string2 = string2 + ")";
			String string3 = "";
			if (realmsLevelSummary.isRequiresConversion()) {
				string3 = RealmsSelectFileToUploadScreen.this.conversionLang + " " + string3;
			} else {
				string3 = RealmsSelectFileToUploadScreen.this.gameModeName(realmsLevelSummary);
				if (realmsLevelSummary.isHardcore()) {
					string3 = ChatFormatting.DARK_RED + RealmsScreen.getLocalizedString("mco.upload.hardcore") + ChatFormatting.RESET;
				}

				if (realmsLevelSummary.hasCheats()) {
					string3 = string3 + ", " + RealmsScreen.getLocalizedString("selectWorld.cheats");
				}
			}

			RealmsSelectFileToUploadScreen.this.drawString(string, j + 2, k + 1, 16777215);
			RealmsSelectFileToUploadScreen.this.drawString(string2, j + 2, k + 12, 8421504);
			RealmsSelectFileToUploadScreen.this.drawString(string3, j + 2, k + 12 + 10, 8421504);
		}
	}

	@Environment(EnvType.CLIENT)
	class WorldSelectionList extends RealmsObjectSelectionList {
		public WorldSelectionList() {
			super(
				RealmsSelectFileToUploadScreen.this.width(),
				RealmsSelectFileToUploadScreen.this.height(),
				RealmsConstants.row(0),
				RealmsSelectFileToUploadScreen.this.height() - 40,
				36
			);
		}

		public void addEntry(RealmsLevelSummary realmsLevelSummary) {
			this.addEntry(RealmsSelectFileToUploadScreen.this.new WorldListEntry(realmsLevelSummary));
		}

		@Override
		public int getItemCount() {
			return RealmsSelectFileToUploadScreen.this.levelList.size();
		}

		@Override
		public int getMaxPosition() {
			return RealmsSelectFileToUploadScreen.this.levelList.size() * 36;
		}

		@Override
		public boolean isFocused() {
			return RealmsSelectFileToUploadScreen.this.isFocused(this);
		}

		@Override
		public void renderBackground() {
			RealmsSelectFileToUploadScreen.this.renderBackground();
		}

		@Override
		public void selectItem(int i) {
			this.setSelected(i);
			if (i != -1) {
				RealmsLevelSummary realmsLevelSummary = (RealmsLevelSummary)RealmsSelectFileToUploadScreen.this.levelList.get(i);
				String string = RealmsScreen.getLocalizedString("narrator.select.list.position", i + 1, RealmsSelectFileToUploadScreen.this.levelList.size());
				String string2 = Realms.joinNarrations(
					Arrays.asList(
						realmsLevelSummary.getLevelName(),
						RealmsSelectFileToUploadScreen.this.formatLastPlayed(realmsLevelSummary),
						RealmsSelectFileToUploadScreen.this.gameModeName(realmsLevelSummary),
						string
					)
				);
				Realms.narrateNow(RealmsScreen.getLocalizedString("narrator.select", string2));
			}

			RealmsSelectFileToUploadScreen.this.selectedWorld = i;
			RealmsSelectFileToUploadScreen.this.uploadButton
				.active(
					RealmsSelectFileToUploadScreen.this.selectedWorld >= 0
						&& RealmsSelectFileToUploadScreen.this.selectedWorld < this.getItemCount()
						&& !((RealmsLevelSummary)RealmsSelectFileToUploadScreen.this.levelList.get(RealmsSelectFileToUploadScreen.this.selectedWorld)).isHardcore()
				);
		}
	}
}
