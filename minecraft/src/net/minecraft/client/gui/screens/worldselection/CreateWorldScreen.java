package net.minecraft.client.gui.screens.worldselection;

import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.FileUtil;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.CreateBuffetWorldScreen;
import net.minecraft.client.gui.screens.CreateFlatWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.levelgen.ChunkGeneratorProvider;
import net.minecraft.world.level.storage.LevelData;
import org.apache.commons.lang3.StringUtils;

@Environment(EnvType.CLIENT)
public class CreateWorldScreen extends Screen {
	private final Screen lastScreen;
	private EditBox nameEdit;
	private EditBox seedEdit;
	private String resultFolder;
	private CreateWorldScreen.SelectedGameMode gameMode = CreateWorldScreen.SelectedGameMode.SURVIVAL;
	@Nullable
	private CreateWorldScreen.SelectedGameMode oldGameMode;
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
	public ChunkGeneratorProvider levelTypeOptions = LevelType.NORMAL.getDefaultProvider();

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
		this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, I18n.get("selectWorld.enterName")) {
			@Override
			protected String getNarrationMessage() {
				return super.getNarrationMessage() + ". " + I18n.get("selectWorld.resultFolder") + " " + CreateWorldScreen.this.resultFolder;
			}
		};
		this.nameEdit.setValue(this.initName);
		this.nameEdit.setResponder(string -> {
			this.initName = string;
			this.createButton.active = !this.nameEdit.getValue().isEmpty();
			this.updateResultFolder();
		});
		this.children.add(this.nameEdit);
		this.modeButton = this.addButton(new Button(this.width / 2 - 75, 115, 150, 20, I18n.get("selectWorld.gameMode"), button -> {
			switch (this.gameMode) {
				case SURVIVAL:
					this.setGameMode(CreateWorldScreen.SelectedGameMode.HARDCORE);
					break;
				case HARDCORE:
					this.setGameMode(CreateWorldScreen.SelectedGameMode.CREATIVE);
					break;
				case CREATIVE:
					this.setGameMode(CreateWorldScreen.SelectedGameMode.SURVIVAL);
			}

			button.queueNarration(250);
		}) {
			@Override
			public String getMessage() {
				return I18n.get("selectWorld.gameMode") + ": " + I18n.get("selectWorld.gameMode." + CreateWorldScreen.this.gameMode.name);
			}

			@Override
			protected String getNarrationMessage() {
				return super.getNarrationMessage() + ". " + CreateWorldScreen.this.gameModeHelp1 + " " + CreateWorldScreen.this.gameModeHelp2;
			}
		});
		this.seedEdit = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, I18n.get("selectWorld.enterSeed"));
		this.seedEdit.setValue(this.initSeed);
		this.seedEdit.setResponder(string -> this.initSeed = this.seedEdit.getValue());
		this.children.add(this.seedEdit);
		this.featuresButton = this.addButton(new Button(this.width / 2 - 155, 100, 150, 20, I18n.get("selectWorld.mapFeatures"), button -> {
			this.features = !this.features;
			button.queueNarration(250);
		}) {
			@Override
			public String getMessage() {
				return I18n.get("selectWorld.mapFeatures") + ' ' + I18n.get(CreateWorldScreen.this.features ? "options.on" : "options.off");
			}

			@Override
			protected String getNarrationMessage() {
				return super.getNarrationMessage() + ". " + I18n.get("selectWorld.mapFeatures.info");
			}
		});
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

			this.levelTypeOptions = this.getLevelType().getDefaultProvider();
			this.setDisplayOptions(this.displayOptions);
			button.queueNarration(250);
		}) {
			@Override
			public String getMessage() {
				return I18n.get("selectWorld.mapType") + ' ' + I18n.get(CreateWorldScreen.this.getLevelType().getDescriptionId());
			}

			@Override
			protected String getNarrationMessage() {
				LevelType levelType = CreateWorldScreen.this.getLevelType();
				return levelType.hasHelpText() ? super.getNarrationMessage() + ". " + I18n.get(levelType.getHelpTextId()) : super.getNarrationMessage();
			}
		});
		this.typeButton.visible = false;
		this.customizeTypeButton = this.addButton(new Button(this.width / 2 + 5, 120, 150, 20, I18n.get("selectWorld.customizeType"), button -> {
			if (this.getLevelType() == LevelType.FLAT) {
				this.minecraft.setScreen(new CreateFlatWorldScreen(this, this.levelTypeOptions));
			}

			if (this.getLevelType() == LevelType.BUFFET) {
				this.minecraft.setScreen(new CreateBuffetWorldScreen(this, this.levelTypeOptions));
			}
		}));
		this.customizeTypeButton.visible = false;
		this.commandsButton = this.addButton(
			new Button(this.width / 2 - 155, 151, 150, 20, I18n.get("selectWorld.allowCommands"), button -> {
				this.commandsChanged = true;
				this.commands = !this.commands;
				button.queueNarration(250);
			}) {
				@Override
				public String getMessage() {
					return I18n.get("selectWorld.allowCommands")
						+ ' '
						+ I18n.get(CreateWorldScreen.this.commands && !CreateWorldScreen.this.hardCore ? "options.on" : "options.off");
				}

				@Override
				protected String getNarrationMessage() {
					return super.getNarrationMessage() + ". " + I18n.get("selectWorld.allowCommands.info");
				}
			}
		);
		this.commandsButton.visible = false;
		this.bonusItemsButton = this.addButton(
			new Button(this.width / 2 + 5, 151, 150, 20, I18n.get("selectWorld.bonusItems"), button -> {
				this.bonusItems = !this.bonusItems;
				button.queueNarration(250);
			}) {
				@Override
				public String getMessage() {
					return I18n.get("selectWorld.bonusItems")
						+ ' '
						+ I18n.get(CreateWorldScreen.this.bonusItems && !CreateWorldScreen.this.hardCore ? "options.on" : "options.off");
				}
			}
		);
		this.bonusItemsButton.visible = false;
		this.moreOptionsButton = this.addButton(
			new Button(this.width / 2 - 75, 187, 150, 20, I18n.get("selectWorld.moreWorldOptions"), button -> this.toggleDisplayOptions())
		);
		this.createButton = this.addButton(new Button(this.width / 2 - 155, this.height - 28, 150, 20, I18n.get("selectWorld.create"), button -> this.onCreate()));
		this.createButton.active = !this.initName.isEmpty();
		this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, I18n.get("gui.cancel"), button -> this.minecraft.setScreen(this.lastScreen)));
		this.setDisplayOptions(this.displayOptions);
		this.setInitialFocus(this.nameEdit);
		this.setGameMode(this.gameMode);
		this.updateResultFolder();
	}

	private LevelType getLevelType() {
		return LevelType.LEVEL_TYPES[this.levelTypeIndex];
	}

	private void updateGameModeHelp() {
		this.gameModeHelp1 = I18n.get("selectWorld.gameMode." + this.gameMode.name + ".line1");
		this.gameModeHelp2 = I18n.get("selectWorld.gameMode." + this.gameMode.name + ".line2");
	}

	private void updateResultFolder() {
		this.resultFolder = this.nameEdit.getValue().trim();
		if (this.resultFolder.isEmpty()) {
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

			LevelSettings levelSettings = new LevelSettings(l, this.gameMode.gameType, this.features, this.hardCore, this.levelTypeOptions);
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
		LevelType levelType = this.getLevelType();
		if (levelType == null || !levelType.isSelectable()) {
			return false;
		} else {
			return levelType == LevelType.DEBUG_ALL_BLOCK_STATES ? hasShiftDown() : true;
		}
	}

	private void toggleDisplayOptions() {
		this.setDisplayOptions(!this.displayOptions);
	}

	private void setGameMode(CreateWorldScreen.SelectedGameMode selectedGameMode) {
		if (!this.commandsChanged) {
			this.commands = selectedGameMode == CreateWorldScreen.SelectedGameMode.CREATIVE;
		}

		if (selectedGameMode == CreateWorldScreen.SelectedGameMode.HARDCORE) {
			this.hardCore = true;
			this.commandsButton.active = false;
			this.bonusItemsButton.active = false;
		} else {
			this.hardCore = false;
			this.commandsButton.active = true;
			this.bonusItemsButton.active = true;
		}

		this.gameMode = selectedGameMode;
		this.updateGameModeHelp();
	}

	private void setDisplayOptions(boolean bl) {
		this.displayOptions = bl;
		this.modeButton.visible = !this.displayOptions;
		this.typeButton.visible = this.displayOptions;
		if (this.getLevelType() == LevelType.DEBUG_ALL_BLOCK_STATES) {
			this.modeButton.active = false;
			if (this.oldGameMode == null) {
				this.oldGameMode = this.gameMode;
			}

			this.setGameMode(CreateWorldScreen.SelectedGameMode.DEBUG);
			this.featuresButton.visible = false;
			this.bonusItemsButton.visible = false;
			this.commandsButton.visible = false;
			this.customizeTypeButton.visible = false;
		} else {
			this.modeButton.active = true;
			if (this.oldGameMode != null) {
				this.setGameMode(this.oldGameMode);
			}

			this.featuresButton.visible = this.displayOptions && this.getLevelType() != LevelType.CUSTOMIZED;
			this.bonusItemsButton.visible = this.displayOptions;
			this.commandsButton.visible = this.displayOptions;
			this.customizeTypeButton.visible = this.displayOptions && this.getLevelType().hasCustomOptions();
		}

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
	public void onClose() {
		if (this.displayOptions) {
			this.setDisplayOptions(false);
		} else {
			this.minecraft.setScreen(this.lastScreen);
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
			if (this.getLevelType().hasHelpText()) {
				this.font.drawWordWrap(I18n.get(this.getLevelType().getHelpTextId()), this.typeButton.x + 2, this.typeButton.y + 22, this.typeButton.getWidth(), 10526880);
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
		this.initSeed = Long.toString(levelData.getSeed());
		this.levelTypeOptions = levelData.getGeneratorProvider();
		LevelType levelType = this.levelTypeOptions.getType() == LevelType.CUSTOMIZED ? LevelType.NORMAL : levelData.getGeneratorType();
		this.levelTypeIndex = levelType.getId();
		this.features = levelData.isGenerateMapFeatures();
		this.commands = levelData.getAllowCommands();
		if (levelData.isHardcore()) {
			this.gameMode = CreateWorldScreen.SelectedGameMode.HARDCORE;
		} else if (levelData.getGameType().isSurvival()) {
			this.gameMode = CreateWorldScreen.SelectedGameMode.SURVIVAL;
		} else if (levelData.getGameType().isCreative()) {
			this.gameMode = CreateWorldScreen.SelectedGameMode.CREATIVE;
		}
	}

	@Environment(EnvType.CLIENT)
	static enum SelectedGameMode {
		SURVIVAL("survival", GameType.SURVIVAL),
		HARDCORE("hardcore", GameType.SURVIVAL),
		CREATIVE("creative", GameType.CREATIVE),
		DEBUG("spectator", GameType.SPECTATOR);

		private final String name;
		private final GameType gameType;

		private SelectedGameMode(String string2, GameType gameType) {
			this.name = string2;
			this.gameType = gameType;
		}
	}
}
