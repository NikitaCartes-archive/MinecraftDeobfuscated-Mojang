/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CreateWorldScreen
extends Screen {
    private final Screen lastScreen;
    private EditBox nameEdit;
    private EditBox seedEdit;
    private String resultFolder;
    private SelectedGameMode gameMode = SelectedGameMode.SURVIVAL;
    @Nullable
    private SelectedGameMode oldGameMode;
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
        super(new TranslatableComponent("selectWorld.create", new Object[0]));
        this.lastScreen = screen;
        this.initSeed = "";
        this.initName = I18n.get("selectWorld.newWorld", new Object[0]);
    }

    @Override
    public void tick() {
        this.nameEdit.tick();
        this.seedEdit.tick();
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, I18n.get("selectWorld.enterName", new Object[0]));
        this.nameEdit.setValue(this.initName);
        this.nameEdit.setResponder(string -> {
            this.initName = string;
            this.createButton.active = !this.nameEdit.getValue().isEmpty();
            this.updateResultFolder();
        });
        this.children.add(this.nameEdit);
        this.modeButton = this.addButton(new Button(this.width / 2 - 75, 115, 150, 20, I18n.get("selectWorld.gameMode", new Object[0]), button -> {
            switch (this.gameMode) {
                case SURVIVAL: {
                    this.setGameMode(SelectedGameMode.HARDCORE);
                    break;
                }
                case HARDCORE: {
                    this.setGameMode(SelectedGameMode.CREATIVE);
                    break;
                }
                case CREATIVE: {
                    this.setGameMode(SelectedGameMode.SURVIVAL);
                }
            }
            this.updateSelectionStrings();
        }));
        this.seedEdit = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, I18n.get("selectWorld.enterSeed", new Object[0]));
        this.seedEdit.setValue(this.initSeed);
        this.seedEdit.setResponder(string -> {
            this.initSeed = this.seedEdit.getValue();
        });
        this.children.add(this.seedEdit);
        this.featuresButton = this.addButton(new Button(this.width / 2 - 155, 100, 150, 20, I18n.get("selectWorld.mapFeatures", new Object[0]), button -> {
            this.features = !this.features;
            this.updateSelectionStrings();
        }));
        this.featuresButton.visible = false;
        this.typeButton = this.addButton(new Button(this.width / 2 + 5, 100, 150, 20, I18n.get("selectWorld.mapType", new Object[0]), button -> {
            ++this.levelTypeIndex;
            if (this.levelTypeIndex >= LevelType.LEVEL_TYPES.length) {
                this.levelTypeIndex = 0;
            }
            while (!this.isValidLevelType()) {
                ++this.levelTypeIndex;
                if (this.levelTypeIndex < LevelType.LEVEL_TYPES.length) continue;
                this.levelTypeIndex = 0;
            }
            this.levelTypeOptions = new CompoundTag();
            this.updateSelectionStrings();
            this.setDisplayOptions(this.displayOptions);
        }));
        this.typeButton.visible = false;
        this.customizeTypeButton = this.addButton(new Button(this.width / 2 + 5, 120, 150, 20, I18n.get("selectWorld.customizeType", new Object[0]), button -> {
            if (LevelType.LEVEL_TYPES[this.levelTypeIndex] == LevelType.FLAT) {
                this.minecraft.setScreen(new CreateFlatWorldScreen(this, this.levelTypeOptions));
            }
            if (LevelType.LEVEL_TYPES[this.levelTypeIndex] == LevelType.BUFFET) {
                this.minecraft.setScreen(new CreateBuffetWorldScreen(this, this.levelTypeOptions));
            }
        }));
        this.customizeTypeButton.visible = false;
        this.commandsButton = this.addButton(new Button(this.width / 2 - 155, 151, 150, 20, I18n.get("selectWorld.allowCommands", new Object[0]), button -> {
            this.commandsChanged = true;
            this.commands = !this.commands;
            this.updateSelectionStrings();
        }));
        this.commandsButton.visible = false;
        this.bonusItemsButton = this.addButton(new Button(this.width / 2 + 5, 151, 150, 20, I18n.get("selectWorld.bonusItems", new Object[0]), button -> {
            this.bonusItems = !this.bonusItems;
            this.updateSelectionStrings();
        }));
        this.bonusItemsButton.visible = false;
        this.moreOptionsButton = this.addButton(new Button(this.width / 2 - 75, 187, 150, 20, I18n.get("selectWorld.moreWorldOptions", new Object[0]), button -> this.toggleDisplayOptions()));
        this.createButton = this.addButton(new Button(this.width / 2 - 155, this.height - 28, 150, 20, I18n.get("selectWorld.create", new Object[0]), button -> this.onCreate()));
        this.createButton.active = !this.initName.isEmpty();
        this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, I18n.get("gui.cancel", new Object[0]), button -> this.minecraft.setScreen(this.lastScreen)));
        this.setDisplayOptions(this.displayOptions);
        this.setInitialFocus(this.nameEdit);
        this.setGameMode(this.gameMode);
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
        } catch (Exception exception) {
            this.resultFolder = "World";
            try {
                this.resultFolder = FileUtil.findAvailableName(this.minecraft.getLevelSource().getBaseDir(), this.resultFolder, "");
            } catch (Exception exception2) {
                throw new RuntimeException("Could not create save folder", exception2);
            }
        }
    }

    private void updateSelectionStrings() {
        this.modeButton.setMessage(I18n.get("selectWorld.gameMode", new Object[0]) + ": " + I18n.get("selectWorld.gameMode." + this.gameMode.name, new Object[0]));
        this.gameModeHelp1 = I18n.get("selectWorld.gameMode." + this.gameMode.name + ".line1", new Object[0]);
        this.gameModeHelp2 = I18n.get("selectWorld.gameMode." + this.gameMode.name + ".line2", new Object[0]);
        this.featuresButton.setMessage(I18n.get("selectWorld.mapFeatures", new Object[0]) + ' ' + I18n.get(this.features ? "options.on" : "options.off", new Object[0]));
        this.bonusItemsButton.setMessage(I18n.get("selectWorld.bonusItems", new Object[0]) + ' ' + I18n.get(this.bonusItems && !this.hardCore ? "options.on" : "options.off", new Object[0]));
        this.typeButton.setMessage(I18n.get("selectWorld.mapType", new Object[0]) + ' ' + I18n.get(LevelType.LEVEL_TYPES[this.levelTypeIndex].getDescriptionId(), new Object[0]));
        this.commandsButton.setMessage(I18n.get("selectWorld.allowCommands", new Object[0]) + ' ' + I18n.get(this.commands && !this.hardCore ? "options.on" : "options.off", new Object[0]));
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    private void onCreate() {
        this.minecraft.setScreen(null);
        if (this.done) {
            return;
        }
        this.done = true;
        long l = new Random().nextLong();
        String string = this.seedEdit.getValue();
        if (!StringUtils.isEmpty(string)) {
            try {
                long m = Long.parseLong(string);
                if (m != 0L) {
                    l = m;
                }
            } catch (NumberFormatException numberFormatException) {
                l = string.hashCode();
            }
        }
        LevelSettings levelSettings = new LevelSettings(l, this.gameMode.gameType, this.features, this.hardCore, LevelType.LEVEL_TYPES[this.levelTypeIndex]);
        levelSettings.setLevelTypeOptions(Dynamic.convert(NbtOps.INSTANCE, JsonOps.INSTANCE, this.levelTypeOptions));
        if (this.bonusItems && !this.hardCore) {
            levelSettings.enableStartingBonusItems();
        }
        if (this.commands && !this.hardCore) {
            levelSettings.enableSinglePlayerCommands();
        }
        this.minecraft.selectLevel(this.resultFolder, this.nameEdit.getValue().trim(), levelSettings);
    }

    private boolean isValidLevelType() {
        LevelType levelType = LevelType.LEVEL_TYPES[this.levelTypeIndex];
        if (levelType == null || !levelType.isSelectable()) {
            return false;
        }
        if (levelType == LevelType.DEBUG_ALL_BLOCK_STATES) {
            return CreateWorldScreen.hasShiftDown();
        }
        return true;
    }

    private void toggleDisplayOptions() {
        this.setDisplayOptions(!this.displayOptions);
    }

    private void setGameMode(SelectedGameMode selectedGameMode) {
        if (!this.commandsChanged) {
            boolean bl = this.commands = selectedGameMode == SelectedGameMode.CREATIVE;
        }
        if (selectedGameMode == SelectedGameMode.HARDCORE) {
            this.hardCore = true;
            this.commandsButton.active = false;
            this.bonusItemsButton.active = false;
        } else {
            this.hardCore = false;
            this.commandsButton.active = true;
            this.bonusItemsButton.active = true;
        }
        this.gameMode = selectedGameMode;
        this.updateSelectionStrings();
    }

    private void setDisplayOptions(boolean bl) {
        this.displayOptions = bl;
        this.modeButton.visible = !this.displayOptions;
        this.typeButton.visible = this.displayOptions;
        if (LevelType.LEVEL_TYPES[this.levelTypeIndex] == LevelType.DEBUG_ALL_BLOCK_STATES) {
            this.modeButton.active = false;
            if (this.oldGameMode == null) {
                this.oldGameMode = this.gameMode;
            }
            this.setGameMode(SelectedGameMode.DEBUG);
            this.featuresButton.visible = false;
            this.bonusItemsButton.visible = false;
            this.commandsButton.visible = false;
            this.customizeTypeButton.visible = false;
        } else {
            this.modeButton.active = true;
            if (this.oldGameMode != null) {
                this.setGameMode(this.oldGameMode);
            }
            this.featuresButton.visible = this.displayOptions && LevelType.LEVEL_TYPES[this.levelTypeIndex] != LevelType.CUSTOMIZED;
            this.bonusItemsButton.visible = this.displayOptions;
            this.commandsButton.visible = this.displayOptions;
            this.customizeTypeButton.visible = this.displayOptions && LevelType.LEVEL_TYPES[this.levelTypeIndex].hasCustomOptions();
        }
        this.seedEdit.setVisible(this.displayOptions);
        this.nameEdit.setVisible(!this.displayOptions);
        if (this.displayOptions) {
            this.moreOptionsButton.setMessage(I18n.get("gui.done", new Object[0]));
        } else {
            this.moreOptionsButton.setMessage(I18n.get("selectWorld.moreWorldOptions", new Object[0]));
        }
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (super.keyPressed(i, j, k)) {
            return true;
        }
        if (i == 257 || i == 335) {
            this.onCreate();
            return true;
        }
        return false;
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 20, -1);
        if (this.displayOptions) {
            this.drawString(this.font, I18n.get("selectWorld.enterSeed", new Object[0]), this.width / 2 - 100, 47, -6250336);
            this.drawString(this.font, I18n.get("selectWorld.seedInfo", new Object[0]), this.width / 2 - 100, 85, -6250336);
            if (this.featuresButton.visible) {
                this.drawString(this.font, I18n.get("selectWorld.mapFeatures.info", new Object[0]), this.width / 2 - 150, 122, -6250336);
            }
            if (this.commandsButton.visible) {
                this.drawString(this.font, I18n.get("selectWorld.allowCommands.info", new Object[0]), this.width / 2 - 150, 172, -6250336);
            }
            this.seedEdit.render(i, j, f);
            if (LevelType.LEVEL_TYPES[this.levelTypeIndex].hasHelpText()) {
                this.font.drawWordWrap(I18n.get(LevelType.LEVEL_TYPES[this.levelTypeIndex].getHelpTextId(), new Object[0]), this.typeButton.x + 2, this.typeButton.y + 22, this.typeButton.getWidth(), 0xA0A0A0);
            }
        } else {
            this.drawString(this.font, I18n.get("selectWorld.enterName", new Object[0]), this.width / 2 - 100, 47, -6250336);
            this.drawString(this.font, I18n.get("selectWorld.resultFolder", new Object[0]) + " " + this.resultFolder, this.width / 2 - 100, 85, -6250336);
            this.nameEdit.render(i, j, f);
            this.drawCenteredString(this.font, this.gameModeHelp1, this.width / 2, 137, -6250336);
            this.drawCenteredString(this.font, this.gameModeHelp2, this.width / 2, 149, -6250336);
        }
        super.render(i, j, f);
    }

    public void copyFromWorld(LevelData levelData) {
        this.initName = levelData.getLevelName();
        this.initSeed = Long.toString(levelData.getSeed());
        LevelType levelType = levelData.getGeneratorType() == LevelType.CUSTOMIZED ? LevelType.NORMAL : levelData.getGeneratorType();
        this.levelTypeIndex = levelType.getId();
        this.levelTypeOptions = levelData.getGeneratorOptions();
        this.features = levelData.isGenerateMapFeatures();
        this.commands = levelData.getAllowCommands();
        if (levelData.isHardcore()) {
            this.gameMode = SelectedGameMode.HARDCORE;
        } else if (levelData.getGameType().isSurvival()) {
            this.gameMode = SelectedGameMode.SURVIVAL;
        } else if (levelData.getGameType().isCreative()) {
            this.gameMode = SelectedGameMode.CREATIVE;
        }
    }

    @Environment(value=EnvType.CLIENT)
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

