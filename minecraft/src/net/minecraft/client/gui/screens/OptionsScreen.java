package net.minecraft.client.gui.screens;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.LockIconButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.EqualSpacingLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
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
	private static final Component TITLE = Component.translatable("options.title");
	private static final Component SKIN_CUSTOMIZATION = Component.translatable("options.skinCustomisation");
	private static final Component SOUNDS = Component.translatable("options.sounds");
	private static final Component VIDEO = Component.translatable("options.video");
	private static final Component CONTROLS = Component.translatable("options.controls");
	private static final Component LANGUAGE = Component.translatable("options.language");
	private static final Component CHAT = Component.translatable("options.chat");
	private static final Component RESOURCEPACK = Component.translatable("options.resourcepack");
	private static final Component ACCESSIBILITY = Component.translatable("options.accessibility");
	private static final Component TELEMETRY = Component.translatable("options.telemetry");
	private static final Tooltip TELEMETRY_DISABLED_TOOLTIP = Tooltip.create(Component.translatable("options.telemetry.disabled"));
	private static final Component CREDITS_AND_ATTRIBUTION = Component.translatable("options.credits_and_attribution");
	private static final int COLUMNS = 2;
	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 61, 33);
	private final Screen lastScreen;
	private final Options options;
	@Nullable
	private CycleButton<Difficulty> difficultyButton;
	@Nullable
	private LockIconButton lockButton;

	public OptionsScreen(Screen screen, Options options) {
		super(TITLE);
		this.lastScreen = screen;
		this.options = options;
	}

	@Override
	protected void init() {
		LinearLayout linearLayout = this.layout.addToHeader(LinearLayout.vertical().spacing(8));
		linearLayout.addChild(new StringWidget(TITLE, this.font), LayoutSettings::alignHorizontallyCenter);
		LinearLayout linearLayout2 = linearLayout.addChild(LinearLayout.horizontal()).spacing(8);
		linearLayout2.addChild(this.options.fov().createButton(this.minecraft.options));
		linearLayout2.addChild(this.createOnlineButton());
		GridLayout gridLayout = new GridLayout();
		gridLayout.defaultCellSetting().paddingHorizontal(4).paddingBottom(4).alignHorizontallyCenter();
		GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(2);
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
		Button button = rowHelper.addChild(this.openScreenButton(TELEMETRY, () -> new TelemetryInfoScreen(this, this.options)));
		if (!this.minecraft.allowsTelemetry()) {
			button.active = false;
			button.setTooltip(TELEMETRY_DISABLED_TOOLTIP);
		}

		rowHelper.addChild(this.openScreenButton(CREDITS_AND_ATTRIBUTION, () -> new CreditsAndAttributionScreen(this)));
		this.layout.addToContents(gridLayout);
		this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, buttonx -> this.onClose()).width(200).build());
		this.layout.visitWidgets(this::addRenderableWidget);
		this.repositionElements();
	}

	@Override
	protected void repositionElements() {
		this.layout.arrangeElements();
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
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
				EqualSpacingLayout equalSpacingLayout = new EqualSpacingLayout(150, 0, EqualSpacingLayout.Orientation.HORIZONTAL);
				equalSpacingLayout.addChild(this.difficultyButton);
				equalSpacingLayout.addChild(this.lockButton);
				return equalSpacingLayout;
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
		if (bl && this.minecraft.level != null && this.lockButton != null && this.difficultyButton != null) {
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

	private Button openScreenButton(Component component, Supplier<Screen> supplier) {
		return Button.builder(component, button -> this.minecraft.setScreen((Screen)supplier.get())).build();
	}
}
