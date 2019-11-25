package net.minecraft.client.gui.screens.worldselection;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class SelectWorldScreen extends Screen {
	protected final Screen lastScreen;
	private String toolTip;
	private Button deleteButton;
	private Button selectButton;
	private Button renameButton;
	private Button copyButton;
	protected EditBox searchBox;
	private WorldSelectionList list;

	public SelectWorldScreen(Screen screen) {
		super(new TranslatableComponent("selectWorld.title"));
		this.lastScreen = screen;
	}

	@Override
	public boolean mouseScrolled(double d, double e, double f) {
		return super.mouseScrolled(d, e, f);
	}

	@Override
	public void tick() {
		this.searchBox.tick();
	}

	@Override
	protected void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.searchBox = new EditBox(this.font, this.width / 2 - 100, 22, 200, 20, this.searchBox, I18n.get("selectWorld.search"));
		this.searchBox.setResponder(string -> this.list.refreshList(() -> string, false));
		this.list = new WorldSelectionList(this, this.minecraft, this.width, this.height, 48, this.height - 64, 36, () -> this.searchBox.getValue(), this.list);
		this.children.add(this.searchBox);
		this.children.add(this.list);
		this.selectButton = this.addButton(
			new Button(
				this.width / 2 - 154,
				this.height - 52,
				150,
				20,
				I18n.get("selectWorld.select"),
				button -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::joinWorld)
			)
		);
		this.addButton(
			new Button(this.width / 2 + 4, this.height - 52, 150, 20, I18n.get("selectWorld.create"), button -> this.minecraft.setScreen(new CreateWorldScreen(this)))
		);
		this.renameButton = this.addButton(
			new Button(
				this.width / 2 - 154,
				this.height - 28,
				72,
				20,
				I18n.get("selectWorld.edit"),
				button -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::editWorld)
			)
		);
		this.deleteButton = this.addButton(
			new Button(
				this.width / 2 - 76,
				this.height - 28,
				72,
				20,
				I18n.get("selectWorld.delete"),
				button -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::deleteWorld)
			)
		);
		this.copyButton = this.addButton(
			new Button(
				this.width / 2 + 4,
				this.height - 28,
				72,
				20,
				I18n.get("selectWorld.recreate"),
				button -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::recreateWorld)
			)
		);
		this.addButton(new Button(this.width / 2 + 82, this.height - 28, 72, 20, I18n.get("gui.cancel"), button -> this.minecraft.setScreen(this.lastScreen)));
		this.updateButtonStatus(false);
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
	public void render(int i, int j, float f) {
		this.toolTip = null;
		this.list.render(i, j, f);
		this.searchBox.render(i, j, f);
		this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 8, 16777215);
		super.render(i, j, f);
		if (this.toolTip != null) {
			this.renderTooltip(Lists.<String>newArrayList(Splitter.on("\n").split(this.toolTip)), i, j);
		}
	}

	public void setToolTip(String string) {
		this.toolTip = string;
	}

	public void updateButtonStatus(boolean bl) {
		this.selectButton.active = bl;
		this.deleteButton.active = bl;
		this.renameButton.active = bl;
		this.copyButton.active = bl;
	}

	@Override
	public void removed() {
		if (this.list != null) {
			this.list.children().forEach(WorldSelectionList.WorldListEntry::close);
		}
	}
}
