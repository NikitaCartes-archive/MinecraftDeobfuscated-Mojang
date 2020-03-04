package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class RealmsSlotOptionsScreen extends RealmsScreen {
	public static final String[] DIFFICULTIES = new String[]{
		"options.difficulty.peaceful", "options.difficulty.easy", "options.difficulty.normal", "options.difficulty.hard"
	};
	public static final String[] GAME_MODES = new String[]{"selectWorld.gameMode.survival", "selectWorld.gameMode.creative", "selectWorld.gameMode.adventure"};
	private EditBox nameEdit;
	protected final RealmsConfigureWorldScreen parent;
	private int column1X;
	private int columnWidth;
	private int column2X;
	private final RealmsWorldOptions options;
	private final RealmsServer.WorldType worldType;
	private final int activeSlot;
	private int difficulty;
	private int gameMode;
	private Boolean pvp;
	private Boolean spawnNPCs;
	private Boolean spawnAnimals;
	private Boolean spawnMonsters;
	private Integer spawnProtection;
	private Boolean commandBlocks;
	private Boolean forceGameMode;
	private Button pvpButton;
	private Button spawnAnimalsButton;
	private Button spawnMonstersButton;
	private Button spawnNPCsButton;
	private RealmsSlotOptionsScreen.SettingsSlider spawnProtectionButton;
	private Button commandBlocksButton;
	private Button forceGameModeButton;
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

	@Override
	public void init() {
		this.columnWidth = 170;
		this.column1X = this.width / 2 - this.columnWidth;
		this.column2X = this.width / 2 + 10;
		this.difficulty = this.options.difficulty;
		this.gameMode = this.options.gameMode;
		if (this.worldType == RealmsServer.WorldType.NORMAL) {
			this.pvp = this.options.pvp;
			this.spawnProtection = this.options.spawnProtection;
			this.forceGameMode = this.options.forceGameMode;
			this.spawnAnimals = this.options.spawnAnimals;
			this.spawnMonsters = this.options.spawnMonsters;
			this.spawnNPCs = this.options.spawnNPCs;
			this.commandBlocks = this.options.commandBlocks;
		} else {
			String string;
			if (this.worldType == RealmsServer.WorldType.ADVENTUREMAP) {
				string = I18n.get("mco.configure.world.edit.subscreen.adventuremap");
			} else if (this.worldType == RealmsServer.WorldType.INSPIRATION) {
				string = I18n.get("mco.configure.world.edit.subscreen.inspiration");
			} else {
				string = I18n.get("mco.configure.world.edit.subscreen.experience");
			}

			this.warningLabel = new RealmsLabel(string, this.width / 2, 26, 16711680);
			this.pvp = true;
			this.spawnProtection = 0;
			this.forceGameMode = false;
			this.spawnAnimals = true;
			this.spawnMonsters = true;
			this.spawnNPCs = true;
			this.commandBlocks = true;
		}

		this.nameEdit = new EditBox(this.minecraft.font, this.column1X + 2, row(1), this.columnWidth - 4, 20, null, I18n.get("mco.configure.world.edit.slot.name"));
		this.nameEdit.setMaxLength(10);
		this.nameEdit.setValue(this.options.getSlotName(this.activeSlot));
		this.magicalSpecialHackyFocus(this.nameEdit);
		this.pvpButton = this.addButton(new Button(this.column2X, row(1), this.columnWidth, 20, this.pvpTitle(), button -> {
			this.pvp = !this.pvp;
			button.setMessage(this.pvpTitle());
		}));
		this.addButton(new Button(this.column1X, row(3), this.columnWidth, 20, this.gameModeTitle(), button -> {
			this.gameMode = (this.gameMode + 1) % GAME_MODES.length;
			button.setMessage(this.gameModeTitle());
		}));
		this.spawnAnimalsButton = this.addButton(new Button(this.column2X, row(3), this.columnWidth, 20, this.spawnAnimalsTitle(), button -> {
			this.spawnAnimals = !this.spawnAnimals;
			button.setMessage(this.spawnAnimalsTitle());
		}));
		this.addButton(new Button(this.column1X, row(5), this.columnWidth, 20, this.difficultyTitle(), button -> {
			this.difficulty = (this.difficulty + 1) % DIFFICULTIES.length;
			button.setMessage(this.difficultyTitle());
			if (this.worldType == RealmsServer.WorldType.NORMAL) {
				this.spawnMonstersButton.active = this.difficulty != 0;
				this.spawnMonstersButton.setMessage(this.spawnMonstersTitle());
			}
		}));
		this.spawnMonstersButton = this.addButton(new Button(this.column2X, row(5), this.columnWidth, 20, this.spawnMonstersTitle(), button -> {
			this.spawnMonsters = !this.spawnMonsters;
			button.setMessage(this.spawnMonstersTitle());
		}));
		this.spawnProtectionButton = this.addButton(
			new RealmsSlotOptionsScreen.SettingsSlider(this.column1X, row(7), this.columnWidth, this.spawnProtection, 0.0F, 16.0F)
		);
		this.spawnNPCsButton = this.addButton(new Button(this.column2X, row(7), this.columnWidth, 20, this.spawnNPCsTitle(), button -> {
			this.spawnNPCs = !this.spawnNPCs;
			button.setMessage(this.spawnNPCsTitle());
		}));
		this.forceGameModeButton = this.addButton(new Button(this.column1X, row(9), this.columnWidth, 20, this.forceGameModeTitle(), button -> {
			this.forceGameMode = !this.forceGameMode;
			button.setMessage(this.forceGameModeTitle());
		}));
		this.commandBlocksButton = this.addButton(new Button(this.column2X, row(9), this.columnWidth, 20, this.commandBlocksTitle(), button -> {
			this.commandBlocks = !this.commandBlocks;
			button.setMessage(this.commandBlocksTitle());
		}));
		if (this.worldType != RealmsServer.WorldType.NORMAL) {
			this.pvpButton.active = false;
			this.spawnAnimalsButton.active = false;
			this.spawnNPCsButton.active = false;
			this.spawnMonstersButton.active = false;
			this.spawnProtectionButton.active = false;
			this.commandBlocksButton.active = false;
			this.forceGameModeButton.active = false;
		}

		if (this.difficulty == 0) {
			this.spawnMonstersButton.active = false;
		}

		this.addButton(new Button(this.column1X, row(13), this.columnWidth, 20, I18n.get("mco.configure.world.buttons.done"), button -> this.saveSettings()));
		this.addButton(new Button(this.column2X, row(13), this.columnWidth, 20, I18n.get("gui.cancel"), button -> this.minecraft.setScreen(this.parent)));
		this.addWidget(this.nameEdit);
		this.titleLabel = this.addWidget(new RealmsLabel(I18n.get("mco.configure.world.buttons.options"), this.width / 2, 17, 16777215));
		if (this.warningLabel != null) {
			this.addWidget(this.warningLabel);
		}

		this.narrateLabels();
	}

	private String difficultyTitle() {
		return I18n.get("options.difficulty") + ": " + I18n.get(DIFFICULTIES[this.difficulty]);
	}

	private String gameModeTitle() {
		return I18n.get("selectWorld.gameMode") + ": " + I18n.get(GAME_MODES[this.gameMode]);
	}

	private String pvpTitle() {
		return I18n.get("mco.configure.world.pvp") + ": " + getOnOff(this.pvp);
	}

	private String spawnAnimalsTitle() {
		return I18n.get("mco.configure.world.spawnAnimals") + ": " + getOnOff(this.spawnAnimals);
	}

	private String spawnMonstersTitle() {
		return this.difficulty == 0
			? I18n.get("mco.configure.world.spawnMonsters") + ": " + I18n.get("mco.configure.world.off")
			: I18n.get("mco.configure.world.spawnMonsters") + ": " + getOnOff(this.spawnMonsters);
	}

	private String spawnNPCsTitle() {
		return I18n.get("mco.configure.world.spawnNPCs") + ": " + getOnOff(this.spawnNPCs);
	}

	private String commandBlocksTitle() {
		return I18n.get("mco.configure.world.commandBlocks") + ": " + getOnOff(this.commandBlocks);
	}

	private String forceGameModeTitle() {
		return I18n.get("mco.configure.world.forceGameMode") + ": " + getOnOff(this.forceGameMode);
	}

	private static String getOnOff(boolean bl) {
		return I18n.get(bl ? "mco.configure.world.on" : "mco.configure.world.off");
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		String string = I18n.get("mco.configure.world.edit.slot.name");
		this.font.draw(string, (float)(this.column1X + this.columnWidth / 2 - this.font.width(string) / 2), (float)(row(0) - 5), 16777215);
		this.titleLabel.render(this);
		if (this.warningLabel != null) {
			this.warningLabel.render(this);
		}

		this.nameEdit.render(i, j, f);
		super.render(i, j, f);
	}

	private String getSlotName() {
		return this.nameEdit.getValue().equals(this.options.getDefaultSlotName(this.activeSlot)) ? "" : this.nameEdit.getValue();
	}

	private void saveSettings() {
		if (this.worldType != RealmsServer.WorldType.ADVENTUREMAP
			&& this.worldType != RealmsServer.WorldType.EXPERIENCE
			&& this.worldType != RealmsServer.WorldType.INSPIRATION) {
			this.parent
				.saveSlotSettings(
					new RealmsWorldOptions(
						this.pvp,
						this.spawnAnimals,
						this.spawnMonsters,
						this.spawnNPCs,
						this.spawnProtection,
						this.commandBlocks,
						this.difficulty,
						this.gameMode,
						this.forceGameMode,
						this.getSlotName()
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
						this.difficulty,
						this.gameMode,
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
			super(i, j, k, 20, "", 0.0);
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
				I18n.get("mco.configure.world.spawnProtection")
					+ ": "
					+ (RealmsSlotOptionsScreen.this.spawnProtection == 0 ? I18n.get("mco.configure.world.off") : RealmsSlotOptionsScreen.this.spawnProtection)
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
