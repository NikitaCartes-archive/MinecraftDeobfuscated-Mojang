package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsSelectFileToUploadScreen extends RealmsScreen {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat();
	private final RealmsResetWorldScreen lastScreen;
	private final long worldId;
	private final int slotId;
	private Button uploadButton;
	private List<LevelSummary> levelList = Lists.<LevelSummary>newArrayList();
	private int selectedWorld = -1;
	private RealmsSelectFileToUploadScreen.WorldSelectionList worldSelectionList;
	private String worldLang;
	private String conversionLang;
	private RealmsLabel titleLabel;
	private RealmsLabel subtitleLabel;
	private RealmsLabel noWorldsLabel;
	private final Runnable callback;

	public RealmsSelectFileToUploadScreen(long l, int i, RealmsResetWorldScreen realmsResetWorldScreen, Runnable runnable) {
		this.lastScreen = realmsResetWorldScreen;
		this.worldId = l;
		this.slotId = i;
		this.callback = runnable;
	}

	private void loadLevelList() throws Exception {
		this.levelList = (List<LevelSummary>)this.minecraft.getLevelSource().getLevelList().stream().sorted((levelSummaryx, levelSummary2) -> {
			if (levelSummaryx.getLastPlayed() < levelSummary2.getLastPlayed()) {
				return 1;
			} else {
				return levelSummaryx.getLastPlayed() > levelSummary2.getLastPlayed() ? -1 : levelSummaryx.getLevelId().compareTo(levelSummary2.getLevelId());
			}
		}).collect(Collectors.toList());

		for (LevelSummary levelSummary : this.levelList) {
			this.worldSelectionList.addEntry(levelSummary);
		}
	}

	@Override
	public void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.worldSelectionList = new RealmsSelectFileToUploadScreen.WorldSelectionList();

		try {
			this.loadLevelList();
		} catch (Exception var2) {
			LOGGER.error("Couldn't load level list", (Throwable)var2);
			this.minecraft
				.setScreen(new RealmsGenericErrorScreen(new TextComponent("Unable to load worlds"), Component.nullToEmpty(var2.getMessage()), this.lastScreen));
			return;
		}

		this.worldLang = I18n.get("selectWorld.world");
		this.conversionLang = I18n.get("selectWorld.conversion");
		this.addWidget(this.worldSelectionList);
		this.uploadButton = this.addButton(
			new Button(this.width / 2 - 154, this.height - 32, 153, 20, new TranslatableComponent("mco.upload.button.name"), button -> this.upload())
		);
		this.uploadButton.active = this.selectedWorld >= 0 && this.selectedWorld < this.levelList.size();
		this.addButton(new Button(this.width / 2 + 6, this.height - 32, 153, 20, CommonComponents.GUI_BACK, button -> this.minecraft.setScreen(this.lastScreen)));
		this.titleLabel = this.addWidget(new RealmsLabel(new TranslatableComponent("mco.upload.select.world.title"), this.width / 2, 13, 16777215));
		this.subtitleLabel = this.addWidget(new RealmsLabel(new TranslatableComponent("mco.upload.select.world.subtitle"), this.width / 2, row(-1), 10526880));
		if (this.levelList.isEmpty()) {
			this.noWorldsLabel = this.addWidget(
				new RealmsLabel(new TranslatableComponent("mco.upload.select.world.none"), this.width / 2, this.height / 2 - 20, 16777215)
			);
		} else {
			this.noWorldsLabel = null;
		}

		this.narrateLabels();
	}

	@Override
	public void removed() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}

	private void upload() {
		if (this.selectedWorld != -1 && !((LevelSummary)this.levelList.get(this.selectedWorld)).isHardcore()) {
			LevelSummary levelSummary = (LevelSummary)this.levelList.get(this.selectedWorld);
			this.minecraft.setScreen(new RealmsUploadScreen(this.worldId, this.slotId, this.lastScreen, levelSummary, this.callback));
		}
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.worldSelectionList.render(poseStack, i, j, f);
		this.titleLabel.render(this, poseStack);
		this.subtitleLabel.render(this, poseStack);
		if (this.noWorldsLabel != null) {
			this.noWorldsLabel.render(this, poseStack);
		}

		super.render(poseStack, i, j, f);
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

	private static String gameModeName(LevelSummary levelSummary) {
		return levelSummary.getGameMode().getDisplayName().getString();
	}

	private static String formatLastPlayed(LevelSummary levelSummary) {
		return DATE_FORMAT.format(new Date(levelSummary.getLastPlayed()));
	}

	@Environment(EnvType.CLIENT)
	class Entry extends ObjectSelectionList.Entry<RealmsSelectFileToUploadScreen.Entry> {
		private final LevelSummary levelSummary;

		public Entry(LevelSummary levelSummary) {
			this.levelSummary = levelSummary;
		}

		@Override
		public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			this.renderItem(poseStack, this.levelSummary, i, k, j);
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			RealmsSelectFileToUploadScreen.this.worldSelectionList.selectItem(RealmsSelectFileToUploadScreen.this.levelList.indexOf(this.levelSummary));
			return true;
		}

		protected void renderItem(PoseStack poseStack, LevelSummary levelSummary, int i, int j, int k) {
			String string = levelSummary.getLevelName();
			if (string == null || string.isEmpty()) {
				string = RealmsSelectFileToUploadScreen.this.worldLang + " " + (i + 1);
			}

			String string2 = levelSummary.getLevelId();
			string2 = string2 + " (" + RealmsSelectFileToUploadScreen.formatLastPlayed(levelSummary);
			string2 = string2 + ")";
			String string3 = "";
			if (levelSummary.isRequiresConversion()) {
				string3 = RealmsSelectFileToUploadScreen.this.conversionLang + " " + string3;
			} else {
				string3 = RealmsSelectFileToUploadScreen.gameModeName(levelSummary);
				if (levelSummary.isHardcore()) {
					string3 = ChatFormatting.DARK_RED + I18n.get("mco.upload.hardcore") + ChatFormatting.RESET;
				}

				if (levelSummary.hasCheats()) {
					string3 = string3 + ", " + I18n.get("selectWorld.cheats");
				}
			}

			RealmsSelectFileToUploadScreen.this.font.draw(poseStack, string, (float)(j + 2), (float)(k + 1), 16777215);
			RealmsSelectFileToUploadScreen.this.font.draw(poseStack, string2, (float)(j + 2), (float)(k + 12), 8421504);
			RealmsSelectFileToUploadScreen.this.font.draw(poseStack, string3, (float)(j + 2), (float)(k + 12 + 10), 8421504);
		}
	}

	@Environment(EnvType.CLIENT)
	class WorldSelectionList extends RealmsObjectSelectionList<RealmsSelectFileToUploadScreen.Entry> {
		public WorldSelectionList() {
			super(
				RealmsSelectFileToUploadScreen.this.width,
				RealmsSelectFileToUploadScreen.this.height,
				RealmsSelectFileToUploadScreen.row(0),
				RealmsSelectFileToUploadScreen.this.height - 40,
				36
			);
		}

		public void addEntry(LevelSummary levelSummary) {
			this.addEntry(RealmsSelectFileToUploadScreen.this.new Entry(levelSummary));
		}

		@Override
		public int getMaxPosition() {
			return RealmsSelectFileToUploadScreen.this.levelList.size() * 36;
		}

		@Override
		public boolean isFocused() {
			return RealmsSelectFileToUploadScreen.this.getFocused() == this;
		}

		@Override
		public void renderBackground(PoseStack poseStack) {
			RealmsSelectFileToUploadScreen.this.renderBackground(poseStack);
		}

		@Override
		public void selectItem(int i) {
			this.setSelectedItem(i);
			if (i != -1) {
				LevelSummary levelSummary = (LevelSummary)RealmsSelectFileToUploadScreen.this.levelList.get(i);
				String string = I18n.get("narrator.select.list.position", i + 1, RealmsSelectFileToUploadScreen.this.levelList.size());
				String string2 = NarrationHelper.join(
					Arrays.asList(
						levelSummary.getLevelName(),
						RealmsSelectFileToUploadScreen.formatLastPlayed(levelSummary),
						RealmsSelectFileToUploadScreen.gameModeName(levelSummary),
						string
					)
				);
				NarrationHelper.now(I18n.get("narrator.select", string2));
			}
		}

		public void setSelected(@Nullable RealmsSelectFileToUploadScreen.Entry entry) {
			super.setSelected(entry);
			RealmsSelectFileToUploadScreen.this.selectedWorld = this.children().indexOf(entry);
			RealmsSelectFileToUploadScreen.this.uploadButton.active = RealmsSelectFileToUploadScreen.this.selectedWorld >= 0
				&& RealmsSelectFileToUploadScreen.this.selectedWorld < this.getItemCount()
				&& !((LevelSummary)RealmsSelectFileToUploadScreen.this.levelList.get(RealmsSelectFileToUploadScreen.this.selectedWorld)).isHardcore();
		}
	}
}
