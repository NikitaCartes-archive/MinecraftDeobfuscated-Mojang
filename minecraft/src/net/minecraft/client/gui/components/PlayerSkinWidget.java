package net.minecraft.client.gui.components;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class PlayerSkinWidget extends AbstractWidget {
	private static final float MODEL_OFFSET = 0.0625F;
	private static final float MODEL_HEIGHT = 2.125F;
	private static final float Z_OFFSET = 100.0F;
	private static final float ROTATION_SENSITIVITY = 2.5F;
	private static final float DEFAULT_ROTATION_X = -5.0F;
	private static final float DEFAULT_ROTATION_Y = 30.0F;
	private static final float ROTATION_X_LIMIT = 50.0F;
	private final PlayerSkinWidget.Model model;
	private final Supplier<PlayerSkin> skin;
	private float rotationX = -5.0F;
	private float rotationY = 30.0F;

	public PlayerSkinWidget(int i, int j, EntityModelSet entityModelSet, Supplier<PlayerSkin> supplier) {
		super(0, 0, i, j, CommonComponents.EMPTY);
		this.model = PlayerSkinWidget.Model.bake(entityModelSet);
		this.skin = supplier;
	}

	@Override
	protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate((float)this.getX() + (float)this.getWidth() / 2.0F, (float)(this.getY() + this.getHeight()), 100.0F);
		float g = (float)this.getHeight() / 2.125F;
		guiGraphics.pose().scale(g, g, g);
		guiGraphics.pose().translate(0.0F, -0.0625F, 0.0F);
		guiGraphics.pose().rotateAround(Axis.XP.rotationDegrees(this.rotationX), 0.0F, -1.0625F, 0.0F);
		guiGraphics.pose().mulPose(Axis.YP.rotationDegrees(this.rotationY));
		guiGraphics.flush();
		Lighting.setupForEntityInInventory(Axis.XP.rotationDegrees(this.rotationX));
		this.model.render(guiGraphics, (PlayerSkin)this.skin.get());
		guiGraphics.flush();
		Lighting.setupFor3DItems();
		guiGraphics.pose().popPose();
	}

	@Override
	protected void onDrag(double d, double e, double f, double g) {
		this.rotationX = Mth.clamp(this.rotationX - (float)g * 2.5F, -50.0F, 50.0F);
		this.rotationY += (float)f * 2.5F;
	}

	@Override
	public void playDownSound(SoundManager soundManager) {
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
	}

	@Override
	public boolean isActive() {
		return false;
	}

	@Nullable
	@Override
	public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
		return null;
	}

	@Environment(EnvType.CLIENT)
	static record Model(PlayerModel<?> wideModel, PlayerModel<?> slimModel) {
		public static PlayerSkinWidget.Model bake(EntityModelSet entityModelSet) {
			PlayerModel<?> playerModel = new PlayerModel(entityModelSet.bakeLayer(ModelLayers.PLAYER), false);
			PlayerModel<?> playerModel2 = new PlayerModel(entityModelSet.bakeLayer(ModelLayers.PLAYER_SLIM), true);
			playerModel.young = false;
			playerModel2.young = false;
			return new PlayerSkinWidget.Model(playerModel, playerModel2);
		}

		public void render(GuiGraphics guiGraphics, PlayerSkin playerSkin) {
			guiGraphics.pose().pushPose();
			guiGraphics.pose().scale(1.0F, 1.0F, -1.0F);
			guiGraphics.pose().translate(0.0F, -1.5F, 0.0F);
			PlayerModel<?> playerModel = playerSkin.model() == PlayerSkin.Model.SLIM ? this.slimModel : this.wideModel;
			RenderType renderType = playerModel.renderType(playerSkin.texture());
			playerModel.renderToBuffer(guiGraphics.pose(), guiGraphics.bufferSource().getBuffer(renderType), 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			guiGraphics.pose().popPose();
		}
	}
}
