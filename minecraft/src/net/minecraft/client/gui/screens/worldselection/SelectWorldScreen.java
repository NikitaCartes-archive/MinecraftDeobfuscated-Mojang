package net.minecraft.client.gui.screens.worldselection;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class SelectWorldScreen extends Screen {
	private static final Logger LOGGER = LogUtils.getLogger();
	protected final Screen lastScreen;
	@Nullable
	private List<FormattedCharSequence> toolTip;
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
		this.searchBox = new EditBox(this.font, this.width / 2 - 100, 22, 200, 20, this.searchBox, new TranslatableComponent("selectWorld.search"));
		this.searchBox.setResponder(string -> this.list.refreshList(() -> string, false));
		this.list = new WorldSelectionList(this, this.minecraft, this.width, this.height, 48, this.height - 64, 36, () -> this.searchBox.getValue(), this.list);
		this.addWidget(this.searchBox);
		this.addWidget(this.list);
		this.selectButton = this.addRenderableWidget(
			new Button(
				this.width / 2 - 154,
				this.height - 52,
				150,
				20,
				new TranslatableComponent("selectWorld.select"),
				button -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::joinWorld)
			)
		);
		this.addRenderableWidget(
			new Button(
				this.width / 2 + 4,
				this.height - 52,
				150,
				20,
				new TranslatableComponent("selectWorld.create"),
				button -> this.minecraft.setScreen(CreateWorldScreen.createFresh(this))
			)
		);
		this.renameButton = this.addRenderableWidget(
			new Button(
				this.width / 2 - 154,
				this.height - 28,
				72,
				20,
				new TranslatableComponent("selectWorld.edit"),
				button -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::editWorld)
			)
		);
		this.deleteButton = this.addRenderableWidget(
			new Button(
				this.width / 2 - 76,
				this.height - 28,
				72,
				20,
				new TranslatableComponent("selectWorld.delete"),
				button -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::deleteWorld)
			)
		);
		this.copyButton = this.addRenderableWidget(
			new Button(
				this.width / 2 + 4,
				this.height - 28,
				72,
				20,
				new TranslatableComponent("selectWorld.recreate"),
				button -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::recreateWorld)
			)
		);
		this.addRenderableWidget(
			new Button(this.width / 2 + 82, this.height - 28, 72, 20, CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.lastScreen))
		);
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
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.toolTip = null;
		this.list.render(poseStack, i, j, f);
		this.searchBox.render(poseStack, i, j, f);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, 16777215);
		super.render(poseStack, i, j, f);
		if (this.toolTip != null) {
			this.renderTooltip(poseStack, this.toolTip, i, j);
		}
	}

	public void setToolTip(List<FormattedCharSequence> list) {
		this.toolTip = list;
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
