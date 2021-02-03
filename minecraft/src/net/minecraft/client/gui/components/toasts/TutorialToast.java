package net.minecraft.client.gui.components.toasts;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class TutorialToast implements Toast {
	private final TutorialToast.Icons icon;
	private final Component title;
	private final Component message;
	private Toast.Visibility visibility = Toast.Visibility.SHOW;
	private long lastProgressTime;
	private float lastProgress;
	private float progress;
	private final boolean progressable;

	public TutorialToast(TutorialToast.Icons icons, Component component, @Nullable Component component2, boolean bl) {
		this.icon = icons;
		this.title = component;
		this.message = component2;
		this.progressable = bl;
	}

	@Override
	public Toast.Visibility render(PoseStack poseStack, ToastComponent toastComponent, long l) {
		toastComponent.getMinecraft().getTextureManager().bind(TEXTURE);
		RenderSystem.color3f(1.0F, 1.0F, 1.0F);
		toastComponent.blit(poseStack, 0, 0, 0, 96, this.width(), this.height());
		this.icon.render(poseStack, toastComponent, 6, 6);
		if (this.message == null) {
			toastComponent.getMinecraft().font.draw(poseStack, this.title, 30.0F, 12.0F, -11534256);
		} else {
			toastComponent.getMinecraft().font.draw(poseStack, this.title, 30.0F, 7.0F, -11534256);
			toastComponent.getMinecraft().font.draw(poseStack, this.message, 30.0F, 18.0F, -16777216);
		}

		if (this.progressable) {
			GuiComponent.fill(poseStack, 3, 28, 157, 29, -1);
			float f = (float)Mth.clampedLerp((double)this.lastProgress, (double)this.progress, (double)((float)(l - this.lastProgressTime) / 100.0F));
			int i;
			if (this.progress >= this.lastProgress) {
				i = -16755456;
			} else {
				i = -11206656;
			}

			GuiComponent.fill(poseStack, 3, 28, (int)(3.0F + 154.0F * f), 29, i);
			this.lastProgress = f;
			this.lastProgressTime = l;
		}

		return this.visibility;
	}

	public void hide() {
		this.visibility = Toast.Visibility.HIDE;
	}

	public void updateProgress(float f) {
		this.progress = f;
	}

	@Environment(EnvType.CLIENT)
	public static enum Icons {
		MOVEMENT_KEYS(0, 0),
		MOUSE(1, 0),
		TREE(2, 0),
		RECIPE_BOOK(0, 1),
		WOODEN_PLANKS(1, 1),
		SOCIAL_INTERACTIONS(2, 1),
		RIGHT_CLICK(3, 1);

		private final int x;
		private final int y;

		private Icons(int j, int k) {
			this.x = j;
			this.y = k;
		}

		public void render(PoseStack poseStack, GuiComponent guiComponent, int i, int j) {
			RenderSystem.enableBlend();
			guiComponent.blit(poseStack, i, j, 176 + this.x * 20, this.y * 20, 20, 20);
			RenderSystem.enableBlend();
		}
	}
}
