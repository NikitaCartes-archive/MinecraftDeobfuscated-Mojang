/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.worldselection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonWriter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.OptimizeWorldScreen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.WorldStem;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class EditWorldScreen
extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson WORLD_GEN_SETTINGS_GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    private static final Component NAME_LABEL = Component.translatable("selectWorld.enterName");
    private Button renameButton;
    private final BooleanConsumer callback;
    private EditBox nameEdit;
    private final LevelStorageSource.LevelStorageAccess levelAccess;

    public EditWorldScreen(BooleanConsumer booleanConsumer, LevelStorageSource.LevelStorageAccess levelStorageAccess) {
        super(Component.translatable("selectWorld.edit.title"));
        this.callback = booleanConsumer;
        this.levelAccess = levelStorageAccess;
    }

    @Override
    public void tick() {
        this.nameEdit.tick();
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        Button button2 = this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 0 + 5, 200, 20, Component.translatable("selectWorld.edit.resetIcon"), button -> {
            this.levelAccess.getIconFile().ifPresent(path -> FileUtils.deleteQuietly(path.toFile()));
            button.active = false;
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 24 + 5, 200, 20, Component.translatable("selectWorld.edit.openFolder"), button -> Util.getPlatform().openFile(this.levelAccess.getLevelPath(LevelResource.ROOT).toFile())));
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 48 + 5, 200, 20, Component.translatable("selectWorld.edit.backup"), button -> {
            boolean bl = EditWorldScreen.makeBackupAndShowToast(this.levelAccess);
            this.callback.accept(!bl);
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 72 + 5, 200, 20, Component.translatable("selectWorld.edit.backupFolder"), button -> {
            LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();
            Path path = levelStorageSource.getBackupPath();
            try {
                Files.createDirectories(Files.exists(path, new LinkOption[0]) ? path.toRealPath(new LinkOption[0]) : path, new FileAttribute[0]);
            } catch (IOException iOException) {
                throw new RuntimeException(iOException);
            }
            Util.getPlatform().openFile(path.toFile());
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 96 + 5, 200, 20, Component.translatable("selectWorld.edit.optimize"), button -> this.minecraft.setScreen(new BackupConfirmScreen(this, (bl, bl2) -> {
            if (bl) {
                EditWorldScreen.makeBackupAndShowToast(this.levelAccess);
            }
            this.minecraft.setScreen(OptimizeWorldScreen.create(this.minecraft, this.callback, this.minecraft.getFixerUpper(), this.levelAccess, bl2));
        }, Component.translatable("optimizeWorld.confirm.title"), Component.translatable("optimizeWorld.confirm.description"), true))));
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 120 + 5, 200, 20, Component.translatable("selectWorld.edit.export_worldgen_settings"), button -> {
            DataResult<Object> dataResult2;
            try (WorldStem worldStem = this.minecraft.createWorldOpenFlows().loadWorldStem(this.levelAccess, false);){
                RegistryAccess.Frozen frozen = worldStem.registries().compositeAccess();
                RegistryOps<JsonElement> dynamicOps = RegistryOps.create(JsonOps.INSTANCE, frozen);
                DataResult<JsonElement> dataResult = WorldGenSettings.encode(dynamicOps, worldStem.worldData().worldGenOptions(), frozen);
                dataResult2 = dataResult.flatMap(jsonElement -> {
                    Path path = this.levelAccess.getLevelPath(LevelResource.ROOT).resolve("worldgen_settings_export.json");
                    try (JsonWriter jsonWriter = WORLD_GEN_SETTINGS_GSON.newJsonWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8, new OpenOption[0]));){
                        WORLD_GEN_SETTINGS_GSON.toJson((JsonElement)jsonElement, jsonWriter);
                    } catch (JsonIOException | IOException exception) {
                        return DataResult.error("Error writing file: " + exception.getMessage());
                    }
                    return DataResult.success(path.toString());
                });
            } catch (Exception exception) {
                LOGGER.warn("Could not parse level data", exception);
                dataResult2 = DataResult.error("Could not parse level data: " + exception.getMessage());
            }
            MutableComponent component = Component.literal(dataResult2.get().map(Function.identity(), DataResult.PartialResult::message));
            MutableComponent component2 = Component.translatable(dataResult2.result().isPresent() ? "selectWorld.edit.export_worldgen_settings.success" : "selectWorld.edit.export_worldgen_settings.failure");
            dataResult2.error().ifPresent(partialResult -> LOGGER.error("Error exporting world settings: {}", partialResult));
            this.minecraft.getToasts().addToast(SystemToast.multiline(this.minecraft, SystemToast.SystemToastIds.WORLD_GEN_SETTINGS_TRANSFER, component2, component));
        }));
        this.renameButton = this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 144 + 5, 98, 20, Component.translatable("selectWorld.edit.save"), button -> this.onRename()));
        this.addRenderableWidget(new Button(this.width / 2 + 2, this.height / 4 + 144 + 5, 98, 20, CommonComponents.GUI_CANCEL, button -> this.callback.accept(false)));
        button2.active = this.levelAccess.getIconFile().filter(path -> Files.isRegularFile(path, new LinkOption[0])).isPresent();
        LevelSummary levelSummary = this.levelAccess.getSummary();
        String string2 = levelSummary == null ? "" : levelSummary.getLevelName();
        this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 38, 200, 20, Component.translatable("selectWorld.enterName"));
        this.nameEdit.setValue(string2);
        this.nameEdit.setResponder(string -> {
            this.renameButton.active = !string.trim().isEmpty();
        });
        this.addWidget(this.nameEdit);
        this.setInitialFocus(this.nameEdit);
    }

    @Override
    public void resize(Minecraft minecraft, int i, int j) {
        String string = this.nameEdit.getValue();
        this.init(minecraft, i, j);
        this.nameEdit.setValue(string);
    }

    @Override
    public void onClose() {
        this.callback.accept(false);
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    private void onRename() {
        try {
            this.levelAccess.renameLevel(this.nameEdit.getValue().trim());
            this.callback.accept(true);
        } catch (IOException iOException) {
            LOGGER.error("Failed to access world '{}'", (Object)this.levelAccess.getLevelId(), (Object)iOException);
            SystemToast.onWorldAccessFailure(this.minecraft, this.levelAccess.getLevelId());
            this.callback.accept(true);
        }
    }

    public static void makeBackupAndShowToast(LevelStorageSource levelStorageSource, String string) {
        boolean bl = false;
        try (LevelStorageSource.LevelStorageAccess levelStorageAccess = levelStorageSource.createAccess(string);){
            bl = true;
            EditWorldScreen.makeBackupAndShowToast(levelStorageAccess);
        } catch (IOException iOException) {
            if (!bl) {
                SystemToast.onWorldAccessFailure(Minecraft.getInstance(), string);
            }
            LOGGER.warn("Failed to create backup of level {}", (Object)string, (Object)iOException);
        }
    }

    public static boolean makeBackupAndShowToast(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
        long l = 0L;
        IOException iOException = null;
        try {
            l = levelStorageAccess.makeWorldBackup();
        } catch (IOException iOException2) {
            iOException = iOException2;
        }
        if (iOException != null) {
            MutableComponent component = Component.translatable("selectWorld.edit.backupFailed");
            MutableComponent component2 = Component.literal(iOException.getMessage());
            Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.WORLD_BACKUP, component, component2));
            return false;
        }
        MutableComponent component = Component.translatable("selectWorld.edit.backupCreated", levelStorageAccess.getLevelId());
        MutableComponent component2 = Component.translatable("selectWorld.edit.backupSize", Mth.ceil((double)l / 1048576.0));
        Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.WORLD_BACKUP, component, component2));
        return true;
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        EditWorldScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        EditWorldScreen.drawString(poseStack, this.font, NAME_LABEL, this.width / 2 - 100, 24, 0xA0A0A0);
        this.nameEdit.render(poseStack, i, j, f);
        super.render(poseStack, i, j, f);
    }
}

