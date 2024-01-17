package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.task.RealmCreationTask;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsSelectFileToUploadScreen extends RealmsScreen {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final Component TITLE = Component.translatable("mco.upload.select.world.title");
	private static final Component UNABLE_TO_LOAD_WORLD = Component.translatable("selectWorld.unable_to_load");
	static final Component WORLD_TEXT = Component.translatable("selectWorld.world");
	static final Component HARDCORE_TEXT = Component.translatable("mco.upload.hardcore").withColor(-65536);
	static final Component CHEATS_TEXT = Component.translatable("selectWorld.cheats");
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat();
	@Nullable
	private final RealmCreationTask realmCreationTask;
	private final RealmsResetWorldScreen lastScreen;
	private final long realmId;
	private final int slotId;
	Button uploadButton;
	List<LevelSummary> levelList = Lists.<LevelSummary>newArrayList();
	int selectedWorld = -1;
	RealmsSelectFileToUploadScreen.WorldSelectionList worldSelectionList;

	public RealmsSelectFileToUploadScreen(@Nullable RealmCreationTask realmCreationTask, long l, int i, RealmsResetWorldScreen realmsResetWorldScreen) {
		super(TITLE);
		this.realmCreationTask = realmCreationTask;
		this.lastScreen = realmsResetWorldScreen;
		this.realmId = l;
		this.slotId = i;
	}

	private void loadLevelList() {
		LevelStorageSource.LevelCandidates levelCandidates = this.minecraft.getLevelSource().findLevelCandidates();
		this.levelList = (List<LevelSummary>)((List)this.minecraft.getLevelSource().loadLevelSummaries(levelCandidates).join())
			.stream()
			.filter(LevelSummary::canUpload)
			.collect(Collectors.toList());

		for (LevelSummary levelSummary : this.levelList) {
			this.worldSelectionList.addEntry(levelSummary);
		}
	}

	@Override
	public void init() {
		this.worldSelectionList = this.addRenderableWidget(new RealmsSelectFileToUploadScreen.WorldSelectionList());

		try {
			this.loadLevelList();
		} catch (Exception var2) {
			LOGGER.error("Couldn't load level list", (Throwable)var2);
			this.minecraft.setScreen(new RealmsGenericErrorScreen(UNABLE_TO_LOAD_WORLD, Component.nullToEmpty(var2.getMessage()), this.lastScreen));
			return;
		}

		this.uploadButton = this.addRenderableWidget(
			Button.builder(Component.translatable("mco.upload.button.name"), button -> this.upload()).bounds(this.width / 2 - 154, this.height - 32, 153, 20).build()
		);
		this.uploadButton.active = this.selectedWorld >= 0 && this.selectedWorld < this.levelList.size();
		this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_BACK, button -> this.minecraft.setScreen(this.lastScreen)).bounds(this.width / 2 + 6, this.height - 32, 153, 20).build()
		);
		this.addLabel(new RealmsLabel(Component.translatable("mco.upload.select.world.subtitle"), this.width / 2, row(-1), -6250336));
		if (this.levelList.isEmpty()) {
			this.addLabel(new RealmsLabel(Component.translatable("mco.upload.select.world.none"), this.width / 2, this.height / 2 - 20, -1));
		}
	}

	@Override
	public Component getNarrationMessage() {
		return CommonComponents.joinForNarration(this.getTitle(), this.createLabelNarration());
	}

	private void upload() {
		if (this.selectedWorld != -1 && !((LevelSummary)this.levelList.get(this.selectedWorld)).isHardcore()) {
			LevelSummary levelSummary = (LevelSummary)this.levelList.get(this.selectedWorld);
			this.minecraft.setScreen(new RealmsUploadScreen(this.realmCreationTask, this.realmId, this.slotId, this.lastScreen, levelSummary));
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 13, -1);
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

	static Component gameModeName(LevelSummary levelSummary) {
		return levelSummary.getGameMode().getLongDisplayName();
	}

	static String formatLastPlayed(LevelSummary levelSummary) {
		return DATE_FORMAT.format(new Date(levelSummary.getLastPlayed()));
	}

	@Environment(EnvType.CLIENT)
	class Entry extends ObjectSelectionList.Entry<RealmsSelectFileToUploadScreen.Entry> {
		private final LevelSummary levelSummary;
		private final String name;
		private final Component id;
		private final Component info;

		public Entry(LevelSummary levelSummary) {
			this.levelSummary = levelSummary;
			this.name = levelSummary.getLevelName();
			this.id = Component.translatable("mco.upload.entry.id", levelSummary.getLevelId(), RealmsSelectFileToUploadScreen.formatLastPlayed(levelSummary));
			Component component;
			if (levelSummary.isHardcore()) {
				component = RealmsSelectFileToUploadScreen.HARDCORE_TEXT;
			} else {
				component = RealmsSelectFileToUploadScreen.gameModeName(levelSummary);
			}

			if (levelSummary.hasCheats()) {
				component = Component.translatable("mco.upload.entry.cheats", component.getString(), RealmsSelectFileToUploadScreen.CHEATS_TEXT);
			}

			this.info = component;
		}

		@Override
		public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			this.renderItem(guiGraphics, i, k, j);
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			RealmsSelectFileToUploadScreen.this.worldSelectionList.selectItem(RealmsSelectFileToUploadScreen.this.levelList.indexOf(this.levelSummary));
			return true;
		}

		protected void renderItem(GuiGraphics guiGraphics, int i, int j, int k) {
			String string;
			if (this.name.isEmpty()) {
				string = RealmsSelectFileToUploadScreen.WORLD_TEXT + " " + (i + 1);
			} else {
				string = this.name;
			}

			guiGraphics.drawString(RealmsSelectFileToUploadScreen.this.font, string, j + 2, k + 1, 16777215, false);
			guiGraphics.drawString(RealmsSelectFileToUploadScreen.this.font, this.id, j + 2, k + 12, -8355712, false);
			guiGraphics.drawString(RealmsSelectFileToUploadScreen.this.font, this.info, j + 2, k + 12 + 10, -8355712, false);
		}

		@Override
		public Component getNarration() {
			Component component = CommonComponents.joinLines(
				Component.literal(this.levelSummary.getLevelName()),
				Component.literal(RealmsSelectFileToUploadScreen.formatLastPlayed(this.levelSummary)),
				RealmsSelectFileToUploadScreen.gameModeName(this.levelSummary)
			);
			return Component.translatable("narrator.select", component);
		}
	}

	@Environment(EnvType.CLIENT)
	class WorldSelectionList extends RealmsObjectSelectionList<RealmsSelectFileToUploadScreen.Entry> {
		public WorldSelectionList() {
			super(
				RealmsSelectFileToUploadScreen.this.width,
				RealmsSelectFileToUploadScreen.this.height - 40 - RealmsSelectFileToUploadScreen.row(0),
				RealmsSelectFileToUploadScreen.row(0),
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

		public void setSelected(@Nullable RealmsSelectFileToUploadScreen.Entry entry) {
			super.setSelected(entry);
			RealmsSelectFileToUploadScreen.this.selectedWorld = this.children().indexOf(entry);
			RealmsSelectFileToUploadScreen.this.uploadButton.active = RealmsSelectFileToUploadScreen.this.selectedWorld >= 0
				&& RealmsSelectFileToUploadScreen.this.selectedWorld < this.getItemCount()
				&& !((LevelSummary)RealmsSelectFileToUploadScreen.this.levelList.get(RealmsSelectFileToUploadScreen.this.selectedWorld)).isHardcore();
		}
	}
}
