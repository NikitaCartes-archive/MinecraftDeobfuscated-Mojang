package net.minecraft.client.gui.screens.worldselection;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.FileUtil;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.CreateBuffetWorldScreen;
import net.minecraft.client.gui.screens.CreateFlatWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.storage.LevelData;
import org.apache.commons.lang3.StringUtils;

@Environment(EnvType.CLIENT)
public class CreateWorldScreen extends Screen {
	private final Screen lastScreen;
	private EditBox nameEdit;
	private EditBox seedEdit;
	private String resultFolder;
	private String gameModeName = "survival";
	private String oldGameModeName;
	private boolean features = true;
	private boolean commands;
	private boolean commandsChanged;
	private boolean bonusItems;
	private boolean hardCore;
	private boolean done;
	private boolean displayOptions;
	private Button createButton;
	private Button modeButton;
	private Button moreOptionsButton;
	private Button featuresButton;
	private Button bonusItemsButton;
	private Button typeButton;
	private Button commandsButton;
	private Button customizeTypeButton;
	private String gameModeHelp1;
	private String gameModeHelp2;
	private String initSeed;
	private String initName;
	private int levelTypeIndex;
	public CompoundTag levelTypeOptions = new CompoundTag();

	public CreateWorldScreen(Screen screen) {
		super(new TranslatableComponent("selectWorld.create"));
		this.lastScreen = screen;
		this.initSeed = "";
		this.initName = I18n.get("selectWorld.newWorld");
	}

	@Override
	public void tick() {
		this.nameEdit.tick();
		this.seedEdit.tick();
	}

	@Override
	protected void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, I18n.get("selectWorld.enterName"));
		this.nameEdit.setValue(this.initName);
		this.nameEdit.setResponder(string -> {
			this.initName = string;
			this.createButton.active = !this.nameEdit.getValue().isEmpty();
			this.updateResultFolder();
		});
		this.children.add(this.nameEdit);
		this.modeButton = this.addButton(new Button(this.width / 2 - 75, 115, 150, 20, I18n.get("selectWorld.gameMode"), button -> {
			if ("survival".equals(this.gameModeName)) {
				if (!this.commandsChanged) {
					this.commands = false;
				}

				this.hardCore = false;
				this.gameModeName = "hardcore";
				this.hardCore = true;
				this.commandsButton.active = false;
				this.bonusItemsButton.active = false;
				this.updateSelectionStrings();
			} else if ("hardcore".equals(this.gameModeName)) {
				if (!this.commandsChanged) {
					this.commands = true;
				}

				this.hardCore = false;
				this.gameModeName = "creative";
				this.updateSelectionStrings();
				this.hardCore = false;
				this.commandsButton.active = true;
				this.bonusItemsButton.active = true;
			} else {
				if (!this.commandsChanged) {
					this.commands = false;
				}

				this.gameModeName = "survival";
				this.updateSelectionStrings();
				this.commandsButton.active = true;
				this.bonusItemsButton.active = true;
				this.hardCore = false;
			}

			this.updateSelectionStrings();
		}));
		this.seedEdit = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, I18n.get("selectWorld.enterSeed"));
		this.seedEdit.setValue(this.initSeed);
		this.seedEdit.setResponder(string -> this.initSeed = this.seedEdit.getValue());
		this.children.add(this.seedEdit);
		this.featuresButton = this.addButton(new Button(this.width / 2 - 155, 100, 150, 20, I18n.get("selectWorld.mapFeatures"), button -> {
			this.features = !this.features;
			this.updateSelectionStrings();
		}));
		this.featuresButton.visible = false;
		this.typeButton = this.addButton(new Button(this.width / 2 + 5, 100, 150, 20, I18n.get("selectWorld.mapType"), button -> {
			this.levelTypeIndex++;
			if (this.levelTypeIndex >= LevelType.LEVEL_TYPES.length) {
				this.levelTypeIndex = 0;
			}

			while (!this.isValidLevelType()) {
				this.levelTypeIndex++;
				if (this.levelTypeIndex >= LevelType.LEVEL_TYPES.length) {
					this.levelTypeIndex = 0;
				}
			}

			this.levelTypeOptions = new CompoundTag();
			this.updateSelectionStrings();
			this.setDisplayOptions(this.displayOptions);
		}));
		this.typeButton.visible = false;
		this.customizeTypeButton = this.addButton(new Button(this.width / 2 + 5, 120, 150, 20, I18n.get("selectWorld.customizeType"), button -> {
			if (LevelType.LEVEL_TYPES[this.levelTypeIndex] == LevelType.FLAT) {
				this.minecraft.setScreen(new CreateFlatWorldScreen(this, this.levelTypeOptions));
			}

			if (LevelType.LEVEL_TYPES[this.levelTypeIndex] == LevelType.BUFFET) {
				this.minecraft.setScreen(new CreateBuffetWorldScreen(this, this.levelTypeOptions));
			}
		}));
		this.customizeTypeButton.visible = false;
		this.commandsButton = this.addButton(new Button(this.width / 2 - 155, 151, 150, 20, I18n.get("selectWorld.allowCommands"), button -> {
			this.commandsChanged = true;
			this.commands = !this.commands;
			this.updateSelectionStrings();
		}));
		this.commandsButton.visible = false;
		this.bonusItemsButton = this.addButton(new Button(this.width / 2 + 5, 151, 150, 20, I18n.get("selectWorld.bonusItems"), button -> {
			this.bonusItems = !this.bonusItems;
			this.updateSelectionStrings();
		}));
		this.bonusItemsButton.visible = false;
		this.moreOptionsButton = this.addButton(
			new Button(this.width / 2 - 75, 187, 150, 20, I18n.get("selectWorld.moreWorldOptions"), button -> this.toggleDisplayOptions())
		);
		this.createButton = this.addButton(new Button(this.width / 2 - 155, this.height - 28, 150, 20, I18n.get("selectWorld.create"), button -> this.onCreate()));
		this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, I18n.get("gui.cancel"), button -> this.minecraft.setScreen(this.lastScreen)));
		this.setDisplayOptions(this.displayOptions);
		this.setInitialFocus(this.nameEdit);
		this.updateResultFolder();
		this.updateSelectionStrings();
	}

	private void updateResultFolder() {
		this.resultFolder = this.nameEdit.getValue().trim();
		if (this.resultFolder.length() == 0) {
			this.resultFolder = "World";
		}

		try {
			this.resultFolder = FileUtil.findAvailableName(this.minecraft.getLevelSource().getBaseDir(), this.resultFolder, "");
		} catch (Exception var4) {
			this.resultFolder = "World";

			try {
				this.resultFolder = FileUtil.findAvailableName(this.minecraft.getLevelSource().getBaseDir(), this.resultFolder, "");
			} catch (Exception var3) {
				throw new RuntimeException("Could not create save folder", var3);
			}
		}
	}

	private void updateSelectionStrings() {
		this.modeButton.setMessage(I18n.get("selectWorld.gameMode") + ": " + I18n.get("selectWorld.gameMode." + this.gameModeName));
		this.gameModeHelp1 = I18n.get("selectWorld.gameMode." + this.gameModeName + ".line1");
		this.gameModeHelp2 = I18n.get("selectWorld.gameMode." + this.gameModeName + ".line2");
		this.featuresButton.setMessage(I18n.get("selectWorld.mapFeatures") + ' ' + I18n.get(this.features ? "options.on" : "options.off"));
		this.bonusItemsButton.setMessage(I18n.get("selectWorld.bonusItems") + ' ' + I18n.get(this.bonusItems && !this.hardCore ? "options.on" : "options.off"));
		this.typeButton.setMessage(I18n.get("selectWorld.mapType") + ' ' + I18n.get(LevelType.LEVEL_TYPES[this.levelTypeIndex].getDescriptionId()));
		this.commandsButton.setMessage(I18n.get("selectWorld.allowCommands") + ' ' + I18n.get(this.commands && !this.hardCore ? "options.on" : "options.off"));
	}

	@Override
	public void removed() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}

	private void onCreate() {
		this.minecraft.setScreen(null);
		if (!this.done) {
			this.done = true;
			long l = new Random().nextLong();
			String string = this.seedEdit.getValue();
			if (!StringUtils.isEmpty(string)) {
				try {
					long m = Long.parseLong(string);
					if (m != 0L) {
						l = m;
					}
				} catch (NumberFormatException var6) {
					l = (long)string.hashCode();
				}
			}

			LevelSettings levelSettings = new LevelSettings(
				l, GameType.byName(this.gameModeName), this.features, this.hardCore, LevelType.LEVEL_TYPES[this.levelTypeIndex]
			);
			levelSettings.setLevelTypeOptions(Dynamic.convert(NbtOps.INSTANCE, JsonOps.INSTANCE, this.levelTypeOptions));
			if (this.bonusItems && !this.hardCore) {
				levelSettings.enableStartingBonusItems();
			}

			if (this.commands && !this.hardCore) {
				levelSettings.enableSinglePlayerCommands();
			}

			this.minecraft.selectLevel(this.resultFolder, this.nameEdit.getValue().trim(), levelSettings);
		}
	}

	private boolean isValidLevelType() {
		LevelType levelType = LevelType.LEVEL_TYPES[this.levelTypeIndex];
		if (levelType == null || !levelType.isSelectable()) {
			return false;
		} else {
			return levelType == LevelType.DEBUG_ALL_BLOCK_STATES ? hasShiftDown() : true;
		}
	}

	private void toggleDisplayOptions() {
		this.setDisplayOptions(!this.displayOptions);
	}

	private void setDisplayOptions(boolean bl) {
		this.displayOptions = bl;
		if (LevelType.LEVEL_TYPES[this.levelTypeIndex] == LevelType.DEBUG_ALL_BLOCK_STATES) {
			this.modeButton.visible = !this.displayOptions;
			this.modeButton.active = false;
			if (this.oldGameModeName == null) {
				this.oldGameModeName = this.gameModeName;
			}

			this.gameModeName = "spectator";
			this.featuresButton.visible = false;
			this.bonusItemsButton.visible = false;
			this.typeButton.visible = this.displayOptions;
			this.commandsButton.visible = false;
			this.customizeTypeButton.visible = false;
		} else {
			this.modeButton.visible = !this.displayOptions;
			this.modeButton.active = true;
			if (this.oldGameModeName != null) {
				this.gameModeName = this.oldGameModeName;
				this.oldGameModeName = null;
			}

			this.featuresButton.visible = this.displayOptions && LevelType.LEVEL_TYPES[this.levelTypeIndex] != LevelType.CUSTOMIZED;
			this.bonusItemsButton.visible = this.displayOptions;
			this.typeButton.visible = this.displayOptions;
			this.commandsButton.visible = this.displayOptions;
			this.customizeTypeButton.visible = this.displayOptions && LevelType.LEVEL_TYPES[this.levelTypeIndex].hasCustomOptions();
		}

		this.updateSelectionStrings();
		this.seedEdit.setVisible(this.displayOptions);
		this.nameEdit.setVisible(!this.displayOptions);
		if (this.displayOptions) {
			this.moreOptionsButton.setMessage(I18n.get("gui.done"));
		} else {
			this.moreOptionsButton.setMessage(I18n.get("selectWorld.moreWorldOptions"));
		}
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (super.keyPressed(i, j, k)) {
			return true;
		} else if (i != 257 && i != 335) {
			return false;
		} else {
			this.onCreate();
			return true;
		}
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 20, -1);
		if (this.displayOptions) {
			this.drawString(this.font, I18n.get("selectWorld.enterSeed"), this.width / 2 - 100, 47, -6250336);
			this.drawString(this.font, I18n.get("selectWorld.seedInfo"), this.width / 2 - 100, 85, -6250336);
			if (this.featuresButton.visible) {
				this.drawString(this.font, I18n.get("selectWorld.mapFeatures.info"), this.width / 2 - 150, 122, -6250336);
			}

			if (this.commandsButton.visible) {
				this.drawString(this.font, I18n.get("selectWorld.allowCommands.info"), this.width / 2 - 150, 172, -6250336);
			}

			this.seedEdit.render(i, j, f);
			if (LevelType.LEVEL_TYPES[this.levelTypeIndex].hasHelpText()) {
				this.font
					.drawWordWrap(
						I18n.get(LevelType.LEVEL_TYPES[this.levelTypeIndex].getHelpTextId()), this.typeButton.x + 2, this.typeButton.y + 22, this.typeButton.getWidth(), 10526880
					);
			}
		} else {
			this.drawString(this.font, I18n.get("selectWorld.enterName"), this.width / 2 - 100, 47, -6250336);
			this.drawString(this.font, I18n.get("selectWorld.resultFolder") + " " + this.resultFolder, this.width / 2 - 100, 85, -6250336);
			this.nameEdit.render(i, j, f);
			this.drawCenteredString(this.font, this.gameModeHelp1, this.width / 2, 137, -6250336);
			this.drawCenteredString(this.font, this.gameModeHelp2, this.width / 2, 149, -6250336);
		}

		super.render(i, j, f);
	}

	public void copyFromWorld(LevelData levelData) {
		this.initName = levelData.getLevelName();
		this.initSeed = levelData.getSeed() + "";
		LevelType levelType = levelData.getGeneratorType() == LevelType.CUSTOMIZED ? LevelType.NORMAL : levelData.getGeneratorType();
		this.levelTypeIndex = levelType.getId();
		this.levelTypeOptions = levelData.getGeneratorOptions();
		this.features = levelData.isGenerateMapFeatures();
		this.commands = levelData.getAllowCommands();
		if (levelData.isHardcore()) {
			this.gameModeName = "hardcore";
		} else if (levelData.getGameType().isSurvival()) {
			this.gameModeName = "survival";
		} else if (levelData.getGameType().isCreative()) {
			this.gameModeName = "creative";
		}
	}
}
