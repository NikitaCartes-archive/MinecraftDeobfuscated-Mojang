/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.worldselection;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.WorldStem;
import net.minecraft.util.Mth;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class OptimizeWorldScreen
extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Object2IntMap<ResourceKey<Level>> DIMENSION_COLORS = Util.make(new Object2IntOpenCustomHashMap(Util.identityStrategy()), object2IntOpenCustomHashMap -> {
        object2IntOpenCustomHashMap.put(Level.OVERWORLD, -13408734);
        object2IntOpenCustomHashMap.put(Level.NETHER, -10075085);
        object2IntOpenCustomHashMap.put(Level.END, -8943531);
        object2IntOpenCustomHashMap.defaultReturnValue(-2236963);
    });
    private final BooleanConsumer callback;
    private final WorldUpgrader upgrader;

    @Nullable
    public static OptimizeWorldScreen create(Minecraft minecraft, BooleanConsumer booleanConsumer, DataFixer dataFixer, LevelStorageSource.LevelStorageAccess levelStorageAccess, boolean bl) {
        WorldStem worldStem = minecraft.createWorldOpenFlows().loadWorldStem(levelStorageAccess, false);
        try {
            WorldData worldData = worldStem.worldData();
            RegistryAccess.Frozen frozen = worldStem.registries().compositeAccess();
            levelStorageAccess.saveDataTag(frozen, worldData);
            OptimizeWorldScreen optimizeWorldScreen = new OptimizeWorldScreen(booleanConsumer, dataFixer, levelStorageAccess, worldData.getLevelSettings(), bl, frozen.registryOrThrow(Registry.LEVEL_STEM_REGISTRY));
            if (worldStem != null) {
                worldStem.close();
            }
            return optimizeWorldScreen;
        } catch (Throwable throwable) {
            try {
                if (worldStem != null) {
                    try {
                        worldStem.close();
                    } catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            } catch (Exception exception) {
                LOGGER.warn("Failed to load datapacks, can't optimize world", exception);
                return null;
            }
        }
    }

    private OptimizeWorldScreen(BooleanConsumer booleanConsumer, DataFixer dataFixer, LevelStorageSource.LevelStorageAccess levelStorageAccess, LevelSettings levelSettings, boolean bl, Registry<LevelStem> registry) {
        super(Component.translatable("optimizeWorld.title", levelSettings.levelName()));
        this.callback = booleanConsumer;
        this.upgrader = new WorldUpgrader(levelStorageAccess, dataFixer, registry, bl);
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 150, 200, 20, CommonComponents.GUI_CANCEL, button -> {
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
            OptimizeWorldScreen.drawString(poseStack, this.font, Component.translatable("optimizeWorld.info.converted", this.upgrader.getConverted()), k, 40, 0xA0A0A0);
            OptimizeWorldScreen.drawString(poseStack, this.font, Component.translatable("optimizeWorld.info.skipped", this.upgrader.getSkipped()), k, 40 + this.font.lineHeight + 3, 0xA0A0A0);
            OptimizeWorldScreen.drawString(poseStack, this.font, Component.translatable("optimizeWorld.info.total", this.upgrader.getTotalChunks()), k, 40 + (this.font.lineHeight + 3) * 2, 0xA0A0A0);
            int o = 0;
            for (ResourceKey<Level> resourceKey : this.upgrader.levels()) {
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

