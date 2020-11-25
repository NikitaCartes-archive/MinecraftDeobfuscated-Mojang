package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;

@Environment(EnvType.CLIENT)
public class RealmsSlotOptionsScreen extends RealmsScreen {
	public static final List<Difficulty> DIFFICULTIES = ImmutableList.of(Difficulty.PEACEFUL, Difficulty.EASY, Difficulty.NORMAL, Difficulty.HARD);
	public static final List<GameType> GAME_MODES = ImmutableList.of(GameType.SURVIVAL, GameType.CREATIVE, GameType.ADVENTURE);
	private static final Component NAME_LABEL = new TranslatableComponent("mco.configure.world.edit.slot.name");
	private static final Component SPAWN_PROTECTION_TEXT = new TranslatableComponent("mco.configure.world.spawnProtection");
	private EditBox nameEdit;
	protected final RealmsConfigureWorldScreen parent;
	private int column1X;
	private int columnWidth;
	private final RealmsWorldOptions options;
	private final RealmsServer.WorldType worldType;
	private final int activeSlot;
	private Difficulty difficulty;
	private GameType gameMode;
	private boolean pvp;
	private boolean spawnNPCs;
	private boolean spawnAnimals;
	private boolean spawnMonsters;
	private int spawnProtection;
	private boolean commandBlocks;
	private boolean forceGameMode;
	private RealmsSlotOptionsScreen.SettingsSlider spawnProtectionButton;
	private RealmsLabel titleLabel;
	private RealmsLabel warningLabel;

	public RealmsSlotOptionsScreen(
		RealmsConfigureWorldScreen realmsConfigureWorldScreen, RealmsWorldOptions realmsWorldOptions, RealmsServer.WorldType worldType, int i
	) {
		this.parent = realmsConfigureWorldScreen;
		this.options = realmsWorldOptions;
		this.worldType = worldType;
		this.activeSlot = i;
	}

	@Override
	public void removed() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}

	@Override
	public void tick() {
		this.nameEdit.tick();
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256) {
			this.minecraft.setScreen(this.parent);
			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	private static <T> T findByIndex(List<T> list, int i, int j) {
		try {
			return (T)list.get(i);
		} catch (IndexOutOfBoundsException var4) {
			return (T)list.get(j);
		}
	}

	private static <T> int findIndex(List<T> list, T object, int i) {
		int j = list.indexOf(object);
		return j == -1 ? i : j;
	}

	@Override
	public void init() {
		this.columnWidth = 170;
		this.column1X = this.width / 2 - this.columnWidth;
		int i = this.width / 2 + 10;
		this.difficulty = findByIndex(DIFFICULTIES, this.options.difficulty, 2);
		this.gameMode = findByIndex(GAME_MODES, this.options.gameMode, 0);
		if (this.worldType == RealmsServer.WorldType.NORMAL) {
			this.pvp = this.options.pvp;
			this.spawnProtection = this.options.spawnProtection;
			this.forceGameMode = this.options.forceGameMode;
			this.spawnAnimals = this.options.spawnAnimals;
			this.spawnMonsters = this.options.spawnMonsters;
			this.spawnNPCs = this.options.spawnNPCs;
			this.commandBlocks = this.options.commandBlocks;
		} else {
			Component component;
			if (this.worldType == RealmsServer.WorldType.ADVENTUREMAP) {
				component = new TranslatableComponent("mco.configure.world.edit.subscreen.adventuremap");
			} else if (this.worldType == RealmsServer.WorldType.INSPIRATION) {
				component = new TranslatableComponent("mco.configure.world.edit.subscreen.inspiration");
			} else {
				component = new TranslatableComponent("mco.configure.world.edit.subscreen.experience");
			}

			this.warningLabel = new RealmsLabel(component, this.width / 2, 26, 16711680);
			this.pvp = true;
			this.spawnProtection = 0;
			this.forceGameMode = false;
			this.spawnAnimals = true;
			this.spawnMonsters = true;
			this.spawnNPCs = true;
			this.commandBlocks = true;
		}

		this.nameEdit = new EditBox(
			this.minecraft.font, this.column1X + 2, row(1), this.columnWidth - 4, 20, null, new TranslatableComponent("mco.configure.world.edit.slot.name")
		);
		this.nameEdit.setMaxLength(10);
		this.nameEdit.setValue(this.options.getSlotName(this.activeSlot));
		this.magicalSpecialHackyFocus(this.nameEdit);
		CycleButton<Boolean> cycleButton = this.addButton(
			CycleButton.onOffBuilder(this.pvp)
				.create(i, row(1), this.columnWidth, 20, new TranslatableComponent("mco.configure.world.pvp"), (cycleButtonx, boolean_) -> this.pvp = boolean_)
		);
		this.addButton(
			CycleButton.<GameType>builder(GameType::getShortDisplayName)
				.withValues(GAME_MODES)
				.withInitialValue(this.gameMode)
				.create(
					this.column1X, row(3), this.columnWidth, 20, new TranslatableComponent("selectWorld.gameMode"), (cycleButtonx, gameType) -> this.gameMode = gameType
				)
		);
		CycleButton<Boolean> cycleButton2 = this.addButton(
			CycleButton.onOffBuilder(this.spawnAnimals)
				.create(
					i, row(3), this.columnWidth, 20, new TranslatableComponent("mco.configure.world.spawnAnimals"), (cycleButtonx, boolean_) -> this.spawnAnimals = boolean_
				)
		);
		CycleButton<Boolean> cycleButton3 = CycleButton.onOffBuilder(this.difficulty != Difficulty.PEACEFUL && this.spawnMonsters)
			.create(
				i, row(5), this.columnWidth, 20, new TranslatableComponent("mco.configure.world.spawnMonsters"), (cycleButtonx, boolean_) -> this.spawnMonsters = boolean_
			);
		this.addButton(
			CycleButton.<Difficulty>builder(Difficulty::getDisplayName)
				.withValues(DIFFICULTIES)
				.withInitialValue(this.difficulty)
				.create(this.column1X, row(5), this.columnWidth, 20, new TranslatableComponent("options.difficulty"), (cycleButton2x, difficulty) -> {
					this.difficulty = difficulty;
					if (this.worldType == RealmsServer.WorldType.NORMAL) {
						boolean bl = this.difficulty != Difficulty.PEACEFUL;
						cycleButton3.active = bl;
						cycleButton3.setValue(bl && this.spawnMonsters);
					}
				})
		);
		this.addButton(cycleButton3);
		this.spawnProtectionButton = this.addButton(
			new RealmsSlotOptionsScreen.SettingsSlider(this.column1X, row(7), this.columnWidth, this.spawnProtection, 0.0F, 16.0F)
		);
		CycleButton<Boolean> cycleButton4 = this.addButton(
			CycleButton.onOffBuilder(this.spawnNPCs)
				.create(i, row(7), this.columnWidth, 20, new TranslatableComponent("mco.configure.world.spawnNPCs"), (cycleButtonx, boolean_) -> this.spawnNPCs = boolean_)
		);
		CycleButton<Boolean> cycleButton5 = this.addButton(
			CycleButton.onOffBuilder(this.forceGameMode)
				.create(
					this.column1X,
					row(9),
					this.columnWidth,
					20,
					new TranslatableComponent("mco.configure.world.forceGameMode"),
					(cycleButtonx, boolean_) -> this.forceGameMode = boolean_
				)
		);
		CycleButton<Boolean> cycleButton6 = this.addButton(
			CycleButton.onOffBuilder(this.commandBlocks)
				.create(
					i, row(9), this.columnWidth, 20, new TranslatableComponent("mco.configure.world.commandBlocks"), (cycleButtonx, boolean_) -> this.commandBlocks = boolean_
				)
		);
		if (this.worldType != RealmsServer.WorldType.NORMAL) {
			cycleButton.active = false;
			cycleButton2.active = false;
			cycleButton4.active = false;
			cycleButton3.active = false;
			this.spawnProtectionButton.active = false;
			cycleButton6.active = false;
			cycleButton5.active = false;
		}

		if (this.difficulty == Difficulty.PEACEFUL) {
			cycleButton3.active = false;
		}

		this.addButton(
			new Button(this.column1X, row(13), this.columnWidth, 20, new TranslatableComponent("mco.configure.world.buttons.done"), button -> this.saveSettings())
		);
		this.addButton(new Button(i, row(13), this.columnWidth, 20, CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.parent)));
		this.addWidget(this.nameEdit);
		this.titleLabel = this.addWidget(new RealmsLabel(new TranslatableComponent("mco.configure.world.buttons.options"), this.width / 2, 17, 16777215));
		if (this.warningLabel != null) {
			this.addWidget(this.warningLabel);
		}

		this.narrateLabels();
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.font.draw(poseStack, NAME_LABEL, (float)(this.column1X + this.columnWidth / 2 - this.font.width(NAME_LABEL) / 2), (float)(row(0) - 5), 16777215);
		this.titleLabel.render(this, poseStack);
		if (this.warningLabel != null) {
			this.warningLabel.render(this, poseStack);
		}

		this.nameEdit.render(poseStack, i, j, f);
		super.render(poseStack, i, j, f);
	}

	private String getSlotName() {
		return this.nameEdit.getValue().equals(this.options.getDefaultSlotName(this.activeSlot)) ? "" : this.nameEdit.getValue();
	}

	private void saveSettings() {
		int i = findIndex(DIFFICULTIES, this.difficulty, 2);
		int j = findIndex(GAME_MODES, this.gameMode, 0);
		if (this.worldType != RealmsServer.WorldType.ADVENTUREMAP
			&& this.worldType != RealmsServer.WorldType.EXPERIENCE
			&& this.worldType != RealmsServer.WorldType.INSPIRATION) {
			this.parent
				.saveSlotSettings(
					new RealmsWorldOptions(
						this.pvp, this.spawnAnimals, this.spawnMonsters, this.spawnNPCs, this.spawnProtection, this.commandBlocks, i, j, this.forceGameMode, this.getSlotName()
					)
				);
		} else {
			this.parent
				.saveSlotSettings(
					new RealmsWorldOptions(
						this.options.pvp,
						this.options.spawnAnimals,
						this.options.spawnMonsters,
						this.options.spawnNPCs,
						this.options.spawnProtection,
						this.options.commandBlocks,
						i,
						j,
						this.options.forceGameMode,
						this.getSlotName()
					)
				);
		}
	}

	@Environment(EnvType.CLIENT)
	class SettingsSlider extends AbstractSliderButton {
		private final double minValue;
		private final double maxValue;

		public SettingsSlider(int i, int j, int k, int l, float f, float g) {
			super(i, j, k, 20, TextComponent.EMPTY, 0.0);
			this.minValue = (double)f;
			this.maxValue = (double)g;
			this.value = (double)((Mth.clamp((float)l, f, g) - f) / (g - f));
			this.updateMessage();
		}

		@Override
		public void applyValue() {
			if (RealmsSlotOptionsScreen.this.spawnProtectionButton.active) {
				RealmsSlotOptionsScreen.this.spawnProtection = (int)Mth.lerp(Mth.clamp(this.value, 0.0, 1.0), this.minValue, this.maxValue);
			}
		}

		@Override
		protected void updateMessage() {
			this.setMessage(
				CommonComponents.optionNameValue(
					RealmsSlotOptionsScreen.SPAWN_PROTECTION_TEXT,
					(Component)(RealmsSlotOptionsScreen.this.spawnProtection == 0
						? CommonComponents.OPTION_OFF
						: new TextComponent(String.valueOf(RealmsSlotOptionsScreen.this.spawnProtection)))
				)
			);
		}

		@Override
		public void onClick(double d, double e) {
		}

		@Override
		public void onRelease(double d, double e) {
		}
	}
}
