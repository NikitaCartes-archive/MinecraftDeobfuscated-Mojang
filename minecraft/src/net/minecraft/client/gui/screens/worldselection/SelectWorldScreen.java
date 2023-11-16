package net.minecraft.client.gui.screens.worldselection;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelSummary;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class SelectWorldScreen extends Screen {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final WorldOptions TEST_OPTIONS = new WorldOptions((long)"test1".hashCode(), true, false);
	protected final Screen lastScreen;
	private Button deleteButton;
	private Button selectButton;
	private Button renameButton;
	private Button copyButton;
	protected EditBox searchBox;
	private WorldSelectionList list;

	public SelectWorldScreen(Screen screen) {
		super(Component.translatable("selectWorld.title"));
		this.lastScreen = screen;
	}

	@Override
	protected void init() {
		this.searchBox = new EditBox(this.font, this.width / 2 - 100, 22, 200, 20, this.searchBox, Component.translatable("selectWorld.search"));
		this.searchBox.setResponder(string -> this.list.updateFilter(string));
		this.addWidget(this.searchBox);
		this.list = this.addRenderableWidget(
			new WorldSelectionList(this, this.minecraft, this.width, this.height - 112, 48, 36, this.searchBox.getValue(), this.list)
		);
		this.selectButton = this.addRenderableWidget(
			Button.builder(LevelSummary.PLAY_WORLD, button -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::joinWorld))
				.bounds(this.width / 2 - 154, this.height - 52, 150, 20)
				.build()
		);
		this.addRenderableWidget(
			Button.builder(Component.translatable("selectWorld.create"), button -> CreateWorldScreen.openFresh(this.minecraft, this))
				.bounds(this.width / 2 + 4, this.height - 52, 150, 20)
				.build()
		);
		this.renameButton = this.addRenderableWidget(
			Button.builder(Component.translatable("selectWorld.edit"), button -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::editWorld))
				.bounds(this.width / 2 - 154, this.height - 28, 72, 20)
				.build()
		);
		this.deleteButton = this.addRenderableWidget(
			Button.builder(Component.translatable("selectWorld.delete"), button -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::deleteWorld))
				.bounds(this.width / 2 - 76, this.height - 28, 72, 20)
				.build()
		);
		this.copyButton = this.addRenderableWidget(
			Button.builder(
					Component.translatable("selectWorld.recreate"), button -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::recreateWorld)
				)
				.bounds(this.width / 2 + 4, this.height - 28, 72, 20)
				.build()
		);
		this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_BACK, button -> this.minecraft.setScreen(this.lastScreen)).bounds(this.width / 2 + 82, this.height - 28, 72, 20).build()
		);
		this.updateButtonStatus(null);
		this.setInitialFocus(this.searchBox);
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		return super.keyPressed(i, j, k) ? true : this.searchBox.keyPressed(i, j, k);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
	}

	@Override
	public boolean charTyped(char c, int i) {
		return this.searchBox.charTyped(c, i);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		this.searchBox.render(guiGraphics, i, j, f);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);
	}

	public void updateButtonStatus(@Nullable LevelSummary levelSummary) {
		if (levelSummary == null) {
			this.selectButton.setMessage(LevelSummary.PLAY_WORLD);
			this.selectButton.active = false;
			this.renameButton.active = false;
			this.copyButton.active = false;
			this.deleteButton.active = false;
		} else {
			this.selectButton.setMessage(levelSummary.primaryActionMessage());
			this.selectButton.active = levelSummary.primaryActionActive();
			this.renameButton.active = levelSummary.canEdit();
			this.copyButton.active = levelSummary.canRecreate();
			this.deleteButton.active = levelSummary.canDelete();
		}
	}

	@Override
	public void removed() {
		if (this.list != null) {
			this.list.children().forEach(WorldSelectionList.Entry::close);
		}
	}
}
