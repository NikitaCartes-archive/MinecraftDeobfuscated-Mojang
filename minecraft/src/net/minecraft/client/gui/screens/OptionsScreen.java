package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.LockIconButton;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.telemetry.TelemetryInfoScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket;
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
	private static final Component CREDITS_AND_ATTRIBUTION = Component.translatable("options.credits_and_attribution");
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
		GridLayout gridLayout = new GridLayout();
		gridLayout.defaultCellSetting().paddingHorizontal(5).paddingBottom(4).alignHorizontallyCenter();
		GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(2);
		rowHelper.addChild(this.options.fov().createButton(this.minecraft.options, 0, 0, 150));
		rowHelper.addChild(this.createOnlineButton());
		rowHelper.addChild(SpacerElement.height(26), 2);
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
						this.minecraft.getResourcePackRepository(), this::applyPacks, this.minecraft.getResourcePackDirectory(), Component.translatable("resourcePack.title")
					)
			)
		);
		rowHelper.addChild(this.openScreenButton(ACCESSIBILITY, () -> new AccessibilityOptionsScreen(this, this.options)));
		rowHelper.addChild(this.openScreenButton(TELEMETRY, () -> new TelemetryInfoScreen(this, this.options)));
		rowHelper.addChild(this.openScreenButton(CREDITS_AND_ATTRIBUTION, () -> new CreditsAndAttributionScreen(this)));
		rowHelper.addChild(
			Button.builder(CommonComponents.GUI_DONE, button -> this.minecraft.setScreen(this.lastScreen)).width(200).build(),
			2,
			rowHelper.newCellSettings().paddingTop(6)
		);
		gridLayout.arrangeElements();
		FrameLayout.alignInRectangle(gridLayout, 0, this.height / 6 - 12, this.width, this.height, 0.5F, 0.0F);
		gridLayout.visitWidgets(this::addRenderableWidget);
	}

	private void applyPacks(PackRepository packRepository) {
		this.options.updateResourcePacks(packRepository);
		this.minecraft.setScreen(this);
	}

	private LayoutElement createOnlineButton() {
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
				LinearLayout linearLayout = new LinearLayout(150, 0, LinearLayout.Orientation.HORIZONTAL);
				linearLayout.addChild(this.difficultyButton);
				linearLayout.addChild(this.lockButton);
				return linearLayout;
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
