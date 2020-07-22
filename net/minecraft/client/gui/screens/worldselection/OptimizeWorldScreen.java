/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class OptimizeWorldScreen
extends Screen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Object2IntMap<ResourceKey<Level>> DIMENSION_COLORS = Util.make(new Object2IntOpenCustomHashMap(Util.identityStrategy()), object2IntOpenCustomHashMap -> {
        object2IntOpenCustomHashMap.put(Level.OVERWORLD, -13408734);
        object2IntOpenCustomHashMap.put(Level.NETHER, -10075085);
        object2IntOpenCustomHashMap.put(Level.END, -8943531);
        object2IntOpenCustomHashMap.defaultReturnValue(-2236963);
    });
    private final BooleanConsumer callback;
    private final WorldUpgrader upgrader;

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Nullable
    public static OptimizeWorldScreen create(Minecraft minecraft, BooleanConsumer booleanConsumer, DataFixer dataFixer, LevelStorageSource.LevelStorageAccess levelStorageAccess, boolean bl) {
        RegistryAccess.RegistryHolder registryHolder = RegistryAccess.builtin();
        try (Minecraft.ServerStem serverStem = minecraft.makeServerStem(registryHolder, Minecraft::loadDataPacks, Minecraft::loadWorldData, false, levelStorageAccess);){
            WorldData worldData = serverStem.worldData();
            levelStorageAccess.saveDataTag(registryHolder, worldData);
            ImmutableSet<ResourceKey<Level>> immutableSet = worldData.worldGenSettings().levels();
            OptimizeWorldScreen optimizeWorldScreen = new OptimizeWorldScreen(booleanConsumer, dataFixer, levelStorageAccess, worldData.getLevelSettings(), bl, immutableSet);
            return optimizeWorldScreen;
        } catch (Exception exception) {
            LOGGER.warn("Failed to load datapacks, can't optimize world", (Throwable)exception);
            return null;
        }
    }

    private OptimizeWorldScreen(BooleanConsumer booleanConsumer, DataFixer dataFixer, LevelStorageSource.LevelStorageAccess levelStorageAccess, LevelSettings levelSettings, boolean bl, ImmutableSet<ResourceKey<Level>> immutableSet) {
        super(new TranslatableComponent("optimizeWorld.title", levelSettings.levelName()));
        this.callback = booleanConsumer;
        this.upgrader = new WorldUpgrader(levelStorageAccess, dataFixer, immutableSet, bl);
    }

    @Override
    protected void init() {
        super.init();
        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 150, 200, 20, CommonComponents.GUI_CANCEL, button -> {
            this.upgrader.cancel();
            this.callback.accept(false);
        }));
    }

    @Override
    public void tick() {
        if (this.upgrader.isFinished()) {
            this.callback.accept(true);
        }
    }

    @Override
    public void onClose() {
        this.callback.accept(false);
    }

    @Override
    public void removed() {
        this.upgrader.cancel();
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        OptimizeWorldScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        int k = this.width / 2 - 150;
        int l = this.width / 2 + 150;
        int m = this.height / 4 + 100;
        int n = m + 10;
        OptimizeWorldScreen.drawCenteredString(poseStack, this.font, this.upgrader.getStatus(), this.width / 2, m - this.font.lineHeight - 2, 0xA0A0A0);
        if (this.upgrader.getTotalChunks() > 0) {
            OptimizeWorldScreen.fill(poseStack, k - 1, m - 1, l + 1, n + 1, -16777216);
            OptimizeWorldScreen.drawString(poseStack, this.font, new TranslatableComponent("optimizeWorld.info.converted", this.upgrader.getConverted()), k, 40, 0xA0A0A0);
            OptimizeWorldScreen.drawString(poseStack, this.font, new TranslatableComponent("optimizeWorld.info.skipped", this.upgrader.getSkipped()), k, 40 + this.font.lineHeight + 3, 0xA0A0A0);
            OptimizeWorldScreen.drawString(poseStack, this.font, new TranslatableComponent("optimizeWorld.info.total", this.upgrader.getTotalChunks()), k, 40 + (this.font.lineHeight + 3) * 2, 0xA0A0A0);
            int o = 0;
            for (ResourceKey resourceKey : this.upgrader.levels()) {
                int p = Mth.floor(this.upgrader.dimensionProgress(resourceKey) * (float)(l - k));
                OptimizeWorldScreen.fill(poseStack, k + o, m, k + o + p, n, DIMENSION_COLORS.getInt(resourceKey));
                o += p;
            }
            int q = this.upgrader.getConverted() + this.upgrader.getSkipped();
            OptimizeWorldScreen.drawCenteredString(poseStack, this.font, q + " / " + this.upgrader.getTotalChunks(), this.width / 2, m + 2 * this.font.lineHeight + 2, 0xA0A0A0);
            OptimizeWorldScreen.drawCenteredString(poseStack, this.font, Mth.floor(this.upgrader.getProgress() * 100.0f) + "%", this.width / 2, m + (n - m) / 2 - this.font.lineHeight / 2, 0xA0A0A0);
        }
        super.render(poseStack, i, j, f);
    }
}

