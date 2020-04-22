package net.minecraft.client.gui.screens.worldselection;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;

@Environment(EnvType.CLIENT)
public class OptimizeWorldScreen extends Screen {
	private static final Object2IntMap<DimensionType> DIMENSION_COLORS = Util.make(
		new Object2IntOpenCustomHashMap<>(Util.identityStrategy()), object2IntOpenCustomHashMap -> {
			object2IntOpenCustomHashMap.put(DimensionType.OVERWORLD, -13408734);
			object2IntOpenCustomHashMap.put(DimensionType.NETHER, -10075085);
			object2IntOpenCustomHashMap.put(DimensionType.THE_END, -8943531);
			object2IntOpenCustomHashMap.defaultReturnValue(-2236963);
		}
	);
	private final BooleanConsumer callback;
	private final WorldUpgrader upgrader;

	public static OptimizeWorldScreen create(BooleanConsumer booleanConsumer, LevelStorageSource.LevelStorageAccess levelStorageAccess, boolean bl) {
		WorldData worldData = levelStorageAccess.getDataTag();
		return new OptimizeWorldScreen(booleanConsumer, levelStorageAccess, worldData, bl);
	}

	private OptimizeWorldScreen(BooleanConsumer booleanConsumer, LevelStorageSource.LevelStorageAccess levelStorageAccess, WorldData worldData, boolean bl) {
		super(new TranslatableComponent("optimizeWorld.title", worldData.getLevelName()));
		this.callback = booleanConsumer;
		this.upgrader = new WorldUpgrader(levelStorageAccess, this.minecraft.getFixerUpper(), worldData, bl);
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

			for (DimensionType dimensionType : DimensionType.getAllTypes()) {
				int p = Mth.floor(this.upgrader.dimensionProgress(dimensionType) * (float)(l - k));
				fill(poseStack, k + o, m, k + o + p, n, DIMENSION_COLORS.getInt(dimensionType));
				o += p;
			}

			int q = this.upgrader.getConverted() + this.upgrader.getSkipped();
			this.drawCenteredString(poseStack, this.font, q + " / " + this.upgrader.getTotalChunks(), this.width / 2, m + 2 * 9 + 2, 10526880);
			this.drawCenteredString(poseStack, this.font, Mth.floor(this.upgrader.getProgress() * 100.0F) + "%", this.width / 2, m + (n - m) / 2 - 9 / 2, 10526880);
		}

		super.render(poseStack, i, j, f);
	}
}
