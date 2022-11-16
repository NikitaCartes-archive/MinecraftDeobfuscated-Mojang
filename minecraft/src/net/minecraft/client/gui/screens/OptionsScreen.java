package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.FrameWidget;
import net.minecraft.client.gui.components.GridWidget;
import net.minecraft.client.gui.components.LinearLayoutWidget;
import net.minecraft.client.gui.components.LockIconButton;
import net.minecraft.client.gui.components.SpacerWidget;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.telemetry.TelemetryInfoScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.Difficulty;

@Environment(EnvType.CLIENT)
public class OptionsScreen extends Screen {
	private static final Component SKIN_CUSTOMIZATION = Component.translatable("options.skinCustomisation");
	private static final Component SOUNDS = Component.translatable("options.sounds");
	private static final Component VIDEO = Component.translatable("options.video");
	private static final Component CONTROLS = Component.translatable("options.controls");
	private static final Component LANGUAGE = Component.translatable("options.language");
	private static final Component CHAT = Component.translatable("options.chat.title");
	private static final Component RESOURCEPACK = Component.translatable("options.resourcepack");
	private static final Component ACCESSIBILITY = Component.translatable("options.accessibility.title");
	private static final Component TELEMETRY = Component.translatable("options.telemetry");
	private static final int COLUMNS = 2;
	private final Screen lastScreen;
	private final Options options;
	private CycleButton<Difficulty> difficultyButton;
	private LockIconButton lockButton;

	public OptionsScreen(Screen screen, Options options) {
		super(Component.translatable("options.title"));
		this.lastScreen = screen;
		this.options = options;
	}

	@Override
	protected void init() {
		GridWidget gridWidget = new GridWidget();
		gridWidget.defaultCellSetting().paddingHorizontal(5).paddingBottom(4).alignHorizontallyCenter();
		GridWidget.RowHelper rowHelper = gridWidget.createRowHelper(2);
		rowHelper.addChild(this.options.fov().createButton(this.minecraft.options, 0, 0, 150));
		rowHelper.addChild(this.createOnlineButton());
		rowHelper.addChild(SpacerWidget.height(26), 2);
		rowHelper.addChild(this.openScreenButton(SKIN_CUSTOMIZATION, () -> new SkinCustomizationScreen(this, this.options)));
		rowHelper.addChild(this.openScreenButton(SOUNDS, () -> new SoundOptionsScreen(this, this.options)));
		rowHelper.addChild(this.openScreenButton(VIDEO, () -> new VideoSettingsScreen(this, this.options)));
		rowHelper.addChild(this.openScreenButton(CONTROLS, () -> new ControlsScreen(this, this.options)));
		rowHelper.addChild(this.openScreenButton(LANGUAGE, () -> new LanguageSelectScreen(this, this.options, this.minecraft.getLanguageManager())));
		rowHelper.addChild(this.openScreenButton(CHAT, () -> new ChatOptionsScreen(this, this.options)));
		rowHelper.addChild(
			this.openScreenButton(
				RESOURCEPACK,
				() -> new PackSelectionScreen(
						this,
						this.minecraft.getResourcePackRepository(),
						this::updatePackList,
						this.minecraft.getResourcePackDirectory(),
						Component.translatable("resourcePack.title")
					)
			)
		);
		rowHelper.addChild(this.openScreenButton(ACCESSIBILITY, () -> new AccessibilityOptionsScreen(this, this.options)));
		rowHelper.addChild(this.openScreenButton(TELEMETRY, () -> new TelemetryInfoScreen(this, this.options)));
		rowHelper.addChild(
			Button.builder(CommonComponents.GUI_DONE, button -> this.minecraft.setScreen(this.lastScreen)).width(200).build(),
			2,
			rowHelper.newCellSettings().paddingTop(6)
		);
		gridWidget.pack();
		FrameWidget.alignInRectangle(gridWidget, 0, this.height / 6 - 12, this.width, this.height, 0.5F, 0.0F);
		this.addRenderableWidget(gridWidget);
	}

	private AbstractWidget createOnlineButton() {
		if (this.minecraft.level != null && this.minecraft.hasSingleplayerServer()) {
			this.difficultyButton = createDifficultyButton(0, 0, "options.difficulty", this.minecraft);
			if (!this.minecraft.level.getLevelData().isHardcore()) {
				this.lockButton = new LockIconButton(
					0,
					0,
					button -> this.minecraft
							.setScreen(
								new ConfirmScreen(
									this::lockCallback,
									Component.translatable("difficulty.lock.title"),
									Component.translatable("difficulty.lock.question", this.minecraft.level.getLevelData().getDifficulty().getDisplayName())
								)
							)
				);
				this.difficultyButton.setWidth(this.difficultyButton.getWidth() - this.lockButton.getWidth());
				this.lockButton.setLocked(this.minecraft.level.getLevelData().isDifficultyLocked());
				this.lockButton.active = !this.lockButton.isLocked();
				this.difficultyButton.active = !this.lockButton.isLocked();
				LinearLayoutWidget linearLayoutWidget = new LinearLayoutWidget(150, 0, LinearLayoutWidget.Orientation.HORIZONTAL);
				linearLayoutWidget.addChild(this.difficultyButton);
				linearLayoutWidget.addChild(this.lockButton);
				linearLayoutWidget.pack();
				return linearLayoutWidget;
			} else {
				this.difficultyButton.active = false;
				return this.difficultyButton;
			}
		} else {
			return Button.builder(
					Component.translatable("options.online"),
					button -> this.minecraft.setScreen(OnlineOptionsScreen.createOnlineOptionsScreen(this.minecraft, this, this.options))
				)
				.bounds(this.width / 2 + 5, this.height / 6 - 12 + 24, 150, 20)
				.build();
		}
	}

	public static CycleButton<Difficulty> createDifficultyButton(int i, int j, String string, Minecraft minecraft) {
		return CycleButton.<Difficulty>builder(Difficulty::getDisplayName)
			.withValues(Difficulty.values())
			.withInitialValue(minecraft.level.getDifficulty())
			.create(
				i,
				j,
				150,
				20,
				Component.translatable(string),
				(cycleButton, difficulty) -> minecraft.getConnection().send(new ServerboundChangeDifficultyPacket(difficulty))
			);
	}

	private void updatePackList(PackRepository packRepository) {
		List<String> list = ImmutableList.copyOf(this.options.resourcePacks);
		this.options.resourcePacks.clear();
		this.options.incompatibleResourcePacks.clear();

		for (Pack pack : packRepository.getSelectedPacks()) {
			if (!pack.isFixedPosition()) {
				this.options.resourcePacks.add(pack.getId());
				if (!pack.getCompatibility().isCompatible()) {
					this.options.incompatibleResourcePacks.add(pack.getId());
				}
			}
		}

		this.options.save();
		List<String> list2 = ImmutableList.copyOf(this.options.resourcePacks);
		if (!list2.equals(list)) {
			this.minecraft.reloadResourcePacks();
		}
	}

	private void lockCallback(boolean bl) {
		this.minecraft.setScreen(this);
		if (bl && this.minecraft.level != null) {
			this.minecraft.getConnection().send(new ServerboundLockDifficultyPacket(true));
			this.lockButton.setLocked(true);
			this.lockButton.active = false;
			this.difficultyButton.active = false;
		}
	}

	@Override
	public void removed() {
		this.options.save();
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 15, 16777215);
		super.render(poseStack, i, j, f);
	}

	private Button openScreenButton(Component component, Supplier<Screen> supplier) {
		return Button.builder(component, button -> this.minecraft.setScreen((Screen)supplier.get())).build();
	}
}
