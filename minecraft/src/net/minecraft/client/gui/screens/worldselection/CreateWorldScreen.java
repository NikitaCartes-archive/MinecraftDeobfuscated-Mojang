package net.minecraft.client.gui.screens.worldselection;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.levelgen.ChunkGeneratorProvider;
import net.minecraft.world.level.storage.WorldData;
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
	private Difficulty selectedDifficulty = Difficulty.NORMAL;
	private Difficulty effectiveDifficulty = Difficulty.NORMAL;
	private boolean features = true;
	private boolean commands;
	private boolean commandsChanged;
	private boolean bonusItems;
	private boolean hardCore;
	private boolean done;
	private boolean displayOptions;
	private Button createButton;
	private Button modeButton;
	private Button difficultyButton;
	private Button moreOptionsButton;
	private Button gameRulesButton;
	private Button featuresButton;
	private Button bonusItemsButton;
	private Button typeButton;
	private Button commandsButton;
	private Button customizeTypeButton;
	private Component gameModeHelp1;
	private Component gameModeHelp2;
	private String initSeed;
	private String initName;
	private GameRules gameRules = new GameRules();
	private int levelTypeIndex;
	public ChunkGeneratorProvider levelTypeOptions = LevelType.NORMAL.getDefaultProvider();

	public CreateWorldScreen(@Nullable Screen screen) {
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
		this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, new TranslatableComponent("selectWorld.enterName")) {
			@Override
			protected MutableComponent createNarrationMessage() {
				return super.createNarrationMessage()
					.append(". ")
					.append(new TranslatableComponent("selectWorld.resultFolder"))
					.append(" ")
					.append(CreateWorldScreen.this.resultFolder);
			}
		};
		this.nameEdit.setValue(this.initName);
		this.nameEdit.setResponder(string -> {
			this.initName = string;
			this.createButton.active = !this.nameEdit.getValue().isEmpty();
			this.updateResultFolder();
		});
		this.children.add(this.nameEdit);
		this.modeButton = this.addButton(new Button(this.width / 2 - 155, 115, 150, 20, new TranslatableComponent("selectWorld.gameMode"), button -> {
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
			public Component getMessage() {
				return super.getMessage().mutableCopy().append(": ").append(new TranslatableComponent("selectWorld.gameMode." + CreateWorldScreen.this.gameMode.name));
			}

			@Override
			protected MutableComponent createNarrationMessage() {
				return super.createNarrationMessage().append(". ").append(CreateWorldScreen.this.gameModeHelp1).append(" ").append(CreateWorldScreen.this.gameModeHelp2);
			}
		});
		this.difficultyButton = this.addButton(new Button(this.width / 2 + 5, 115, 150, 20, new TranslatableComponent("options.difficulty"), button -> {
			this.selectedDifficulty = this.selectedDifficulty.nextById();
			this.effectiveDifficulty = this.selectedDifficulty;
			button.queueNarration(250);
		}) {
			@Override
			public Component getMessage() {
				return new TranslatableComponent("options.difficulty").append(": ").append(CreateWorldScreen.this.effectiveDifficulty.getDisplayName());
			}
		});
		this.seedEdit = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, new TranslatableComponent("selectWorld.enterSeed"));
		this.seedEdit.setValue(this.initSeed);
		this.seedEdit.setResponder(string -> this.initSeed = this.seedEdit.getValue());
		this.children.add(this.seedEdit);
		this.featuresButton = this.addButton(new Button(this.width / 2 - 155, 100, 150, 20, new TranslatableComponent("selectWorld.mapFeatures"), button -> {
			this.features = !this.features;
			button.queueNarration(250);
		}) {
			@Override
			public Component getMessage() {
				return super.getMessage().mutableCopy().append(" ").append(CommonComponents.optionStatus(CreateWorldScreen.this.features));
			}

			@Override
			protected MutableComponent createNarrationMessage() {
				return super.createNarrationMessage().append(". ").append(new TranslatableComponent("selectWorld.mapFeatures.info"));
			}
		});
		this.featuresButton.visible = false;
		this.typeButton = this.addButton(new Button(this.width / 2 + 5, 100, 150, 20, new TranslatableComponent("selectWorld.mapType"), button -> {
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
			public Component getMessage() {
				return super.getMessage().mutableCopy().append(" ").append(CreateWorldScreen.this.getLevelType().getDescription());
			}

			@Override
			protected MutableComponent createNarrationMessage() {
				LevelType levelType = CreateWorldScreen.this.getLevelType();
				return levelType.hasHelpText() ? super.createNarrationMessage().append(". ").append(levelType.getHelpText()) : super.createNarrationMessage();
			}
		});
		this.typeButton.visible = false;
		this.customizeTypeButton = this.addButton(new Button(this.width / 2 + 5, 120, 150, 20, new TranslatableComponent("selectWorld.customizeType"), button -> {
			if (this.getLevelType() == LevelType.FLAT) {
				this.minecraft.setScreen(new CreateFlatWorldScreen(this, this.levelTypeOptions));
			}

			if (this.getLevelType() == LevelType.BUFFET) {
				this.minecraft.setScreen(new CreateBuffetWorldScreen(this, this.levelTypeOptions));
			}
		}));
		this.customizeTypeButton.visible = false;
		this.commandsButton = this.addButton(
			new Button(this.width / 2 - 155, 151, 150, 20, new TranslatableComponent("selectWorld.allowCommands"), button -> {
				this.commandsChanged = true;
				this.commands = !this.commands;
				button.queueNarration(250);
			}) {
				@Override
				public Component getMessage() {
					return super.getMessage()
						.mutableCopy()
						.append(" ")
						.append(CommonComponents.optionStatus(CreateWorldScreen.this.commands && !CreateWorldScreen.this.hardCore));
				}

				@Override
				protected MutableComponent createNarrationMessage() {
					return super.createNarrationMessage().append(". ").append(new TranslatableComponent("selectWorld.allowCommands.info"));
				}
			}
		);
		this.commandsButton.visible = false;
		this.bonusItemsButton = this.addButton(
			new Button(this.width / 2 + 5, 151, 150, 20, new TranslatableComponent("selectWorld.bonusItems"), button -> {
				this.bonusItems = !this.bonusItems;
				button.queueNarration(250);
			}) {
				@Override
				public Component getMessage() {
					return super.getMessage()
						.mutableCopy()
						.append(" ")
						.append(CommonComponents.optionStatus(CreateWorldScreen.this.bonusItems && !CreateWorldScreen.this.hardCore));
				}
			}
		);
		this.bonusItemsButton.visible = false;
		this.createButton = this.addButton(
			new Button(this.width / 2 - 155, this.height - 28, 150, 20, new TranslatableComponent("selectWorld.create"), button -> this.onCreate())
		);
		this.createButton.active = !this.initName.isEmpty();
		this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.lastScreen)));
		this.moreOptionsButton = this.addButton(
			new Button(this.width / 2 + 5, 185, 150, 20, new TranslatableComponent("selectWorld.moreWorldOptions"), button -> this.toggleDisplayOptions())
		);
		this.gameRulesButton = this.addButton(
			new Button(
				this.width / 2 - 155,
				185,
				150,
				20,
				new TranslatableComponent("selectWorld.gameRules"),
				button -> this.minecraft.setScreen(new EditGameRulesScreen(this.gameRules.copy(), optional -> {
						this.minecraft.setScreen(this);
						optional.ifPresent(gameRules -> this.gameRules = gameRules);
					}))
			)
		);
		this.setDisplayOptions(this.displayOptions);
		this.setInitialFocus(this.nameEdit);
		this.setGameMode(this.gameMode);
		this.updateResultFolder();
	}

	private LevelType getLevelType() {
		return LevelType.LEVEL_TYPES[this.levelTypeIndex];
	}

	private void updateGameModeHelp() {
		this.gameModeHelp1 = new TranslatableComponent("selectWorld.gameMode." + this.gameMode.name + ".line1");
		this.gameModeHelp2 = new TranslatableComponent("selectWorld.gameMode." + this.gameMode.name + ".line2");
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

			LevelSettings levelSettings;
			if (this.getLevelType() == LevelType.DEBUG_ALL_BLOCK_STATES) {
				GameRules gameRules = new GameRules();
				gameRules.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
				levelSettings = new LevelSettings(
						this.nameEdit.getValue().trim(), l, GameType.SPECTATOR, false, false, Difficulty.PEACEFUL, this.levelTypeOptions, gameRules
					)
					.enableSinglePlayerCommands();
			} else {
				levelSettings = new LevelSettings(
					this.nameEdit.getValue().trim(), l, this.gameMode.gameType, this.features, this.hardCore, this.effectiveDifficulty, this.levelTypeOptions, this.gameRules
				);
				if (this.bonusItems && !this.hardCore) {
					levelSettings.enableStartingBonusItems();
				}

				if (this.commands && !this.hardCore) {
					levelSettings.enableSinglePlayerCommands();
				}
			}

			this.minecraft.selectLevel(this.resultFolder, levelSettings);
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
			this.effectiveDifficulty = Difficulty.HARD;
			this.difficultyButton.active = false;
		} else {
			this.hardCore = false;
			this.commandsButton.active = true;
			this.bonusItemsButton.active = true;
			this.effectiveDifficulty = this.selectedDifficulty;
			this.difficultyButton.active = true;
		}

		this.gameMode = selectedGameMode;
		this.updateGameModeHelp();
	}

	private void setDisplayOptions(boolean bl) {
		this.displayOptions = bl;
		this.modeButton.visible = !this.displayOptions;
		this.difficultyButton.visible = !this.displayOptions;
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
			this.moreOptionsButton.setMessage(CommonComponents.GUI_DONE);
		} else {
			this.moreOptionsButton.setMessage(new TranslatableComponent("selectWorld.moreWorldOptions"));
		}

		this.gameRulesButton.visible = !this.displayOptions;
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
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, -1);
		if (this.displayOptions) {
			this.drawString(poseStack, this.font, I18n.get("selectWorld.enterSeed"), this.width / 2 - 100, 47, -6250336);
			this.drawString(poseStack, this.font, I18n.get("selectWorld.seedInfo"), this.width / 2 - 100, 85, -6250336);
			if (this.featuresButton.visible) {
				this.drawString(poseStack, this.font, I18n.get("selectWorld.mapFeatures.info"), this.width / 2 - 150, 122, -6250336);
			}

			if (this.commandsButton.visible) {
				this.drawString(poseStack, this.font, I18n.get("selectWorld.allowCommands.info"), this.width / 2 - 150, 172, -6250336);
			}

			this.seedEdit.render(poseStack, i, j, f);
			if (LevelType.LEVEL_TYPES[this.levelTypeIndex].hasHelpText()) {
				this.font.drawWordWrap(this.getLevelType().getHelpText(), this.typeButton.x + 2, this.typeButton.y + 22, this.typeButton.getWidth(), 10526880);
			}
		} else {
			this.drawString(poseStack, this.font, I18n.get("selectWorld.enterName"), this.width / 2 - 100, 47, -6250336);
			this.drawString(poseStack, this.font, I18n.get("selectWorld.resultFolder") + " " + this.resultFolder, this.width / 2 - 100, 85, -6250336);
			this.nameEdit.render(poseStack, i, j, f);
			this.drawCenteredString(poseStack, this.font, this.gameModeHelp1, this.width / 2 - 155 + 75, 137, -6250336);
			this.drawCenteredString(poseStack, this.font, this.gameModeHelp2, this.width / 2 - 155 + 75, 149, -6250336);
		}

		super.render(poseStack, i, j, f);
	}

	public void copyFromWorld(WorldData worldData) {
		LevelSettings levelSettings = worldData.getLevelSettings();
		this.initName = levelSettings.getLevelName();
		this.initSeed = Long.toString(levelSettings.getSeed());
		this.levelTypeOptions = levelSettings.getGeneratorProvider();
		LevelType levelType = this.levelTypeOptions.getType() == LevelType.CUSTOMIZED ? LevelType.NORMAL : levelSettings.getGeneratorProvider().getType();
		this.levelTypeIndex = levelType.getId();
		this.features = levelSettings.shouldGenerateMapFeatures();
		this.commands = levelSettings.getAllowCommands();
		this.commandsChanged = true;
		this.bonusItems = levelSettings.hasStartingBonusItems();
		this.selectedDifficulty = levelSettings.getDifficulty();
		this.effectiveDifficulty = this.selectedDifficulty;
		this.gameRules.assignFrom(worldData.getGameRules(), null);
		if (levelSettings.isHardcore()) {
			this.gameMode = CreateWorldScreen.SelectedGameMode.HARDCORE;
		} else if (levelSettings.getGameType().isSurvival()) {
			this.gameMode = CreateWorldScreen.SelectedGameMode.SURVIVAL;
		} else if (levelSettings.getGameType().isCreative()) {
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
