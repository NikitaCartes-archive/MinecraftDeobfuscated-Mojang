package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
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

@Environment(EnvType.CLIENT)
public class OptimizeWorldScreen extends Screen {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Object2IntMap<ResourceKey<Level>> DIMENSION_COLORS = Util.make(
		new Object2IntOpenCustomHashMap<>(Util.identityStrategy()), object2IntOpenCustomHashMap -> {
			object2IntOpenCustomHashMap.put(Level.OVERWORLD, -13408734);
			object2IntOpenCustomHashMap.put(Level.NETHER, -10075085);
			object2IntOpenCustomHashMap.put(Level.END, -8943531);
			object2IntOpenCustomHashMap.defaultReturnValue(-2236963);
		}
	);
	private final BooleanConsumer callback;
	private final WorldUpgrader upgrader;

	@Nullable
	public static OptimizeWorldScreen create(
		Minecraft minecraft, BooleanConsumer booleanConsumer, DataFixer dataFixer, LevelStorageSource.LevelStorageAccess levelStorageAccess, boolean bl
	) {
		RegistryAccess.RegistryHolder registryHolder = RegistryAccess.builtin();

		try (Minecraft.ServerStem serverStem = minecraft.makeServerStem(registryHolder, Minecraft::loadDataPacks, Minecraft::loadWorldData, false, levelStorageAccess)) {
			WorldData worldData = serverStem.worldData();
			levelStorageAccess.saveDataTag(registryHolder, worldData);
			ImmutableSet<ResourceKey<Level>> immutableSet = worldData.worldGenSettings().levels();
			return new OptimizeWorldScreen(booleanConsumer, dataFixer, levelStorageAccess, worldData.getLevelSettings(), bl, immutableSet);
		} catch (Exception var22) {
			LOGGER.warn("Failed to load datapacks, can't optimize world", (Throwable)var22);
			return null;
		}
	}

	private OptimizeWorldScreen(
		BooleanConsumer booleanConsumer,
		DataFixer dataFixer,
		LevelStorageSource.LevelStorageAccess levelStorageAccess,
		LevelSettings levelSettings,
		boolean bl,
		ImmutableSet<ResourceKey<Level>> immutableSet
	) {
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
		this.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, 16777215);
		int k = this.width / 2 - 150;
		int l = this.width / 2 + 150;
		int m = this.height / 4 + 100;
		int n = m + 10;
		this.drawCenteredString(poseStack, this.font, this.upgrader.getStatus(), this.width / 2, m - 9 - 2, 10526880);
		if (this.upgrader.getTotalChunks() > 0) {
			fill(poseStack, k - 1, m - 1, l + 1, n + 1, -16777216);
			this.drawString(poseStack, this.font, I18n.get("optimizeWorld.info.converted", this.upgrader.getConverted()), k, 40, 10526880);
			this.drawString(poseStack, this.font, I18n.get("optimizeWorld.info.skipped", this.upgrader.getSkipped()), k, 40 + 9 + 3, 10526880);
			this.drawString(poseStack, this.font, I18n.get("optimizeWorld.info.total", this.upgrader.getTotalChunks()), k, 40 + (9 + 3) * 2, 10526880);
			int o = 0;

			for (ResourceKey<Level> resourceKey : this.upgrader.levels()) {
				int p = Mth.floor(this.upgrader.dimensionProgress(resourceKey) * (float)(l - k));
				fill(poseStack, k + o, m, k + o + p, n, DIMENSION_COLORS.getInt(resourceKey));
				o += p;
			}

			int q = this.upgrader.getConverted() + this.upgrader.getSkipped();
			this.drawCenteredString(poseStack, this.font, q + " / " + this.upgrader.getTotalChunks(), this.width / 2, m + 2 * 9 + 2, 10526880);
			this.drawCenteredString(poseStack, this.font, Mth.floor(this.upgrader.getProgress() * 100.0F) + "%", this.width / 2, m + (n - m) / 2 - 9 / 2, 10526880);
		}

		super.render(poseStack, i, j, f);
	}
}
