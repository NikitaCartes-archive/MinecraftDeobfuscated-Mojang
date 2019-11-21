package net.minecraft.client.gui.components.toasts;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Arrays;
import java.util.Deque;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class ToastComponent extends GuiComponent {
	private final Minecraft minecraft;
	private final ToastComponent.ToastInstance<?>[] visible = new ToastComponent.ToastInstance[5];
	private final Deque<Toast> queued = Queues.<Toast>newArrayDeque();

	public ToastComponent(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	public void render() {
		if (!this.minecraft.options.hideGui) {
			for (int i = 0; i < this.visible.length; i++) {
				ToastComponent.ToastInstance<?> toastInstance = this.visible[i];
				if (toastInstance != null && toastInstance.render(this.minecraft.getWindow().getGuiScaledWidth(), i)) {
					this.visible[i] = null;
				}

				if (this.visible[i] == null && !this.queued.isEmpty()) {
					this.visible[i] = new ToastComponent.ToastInstance((Toast)this.queued.removeFirst());
				}
			}
		}
	}

	@Nullable
	public <T extends Toast> T getToast(Class<? extends T> class_, Object object) {
		for (ToastComponent.ToastInstance<?> toastInstance : this.visible) {
			if (toastInstance != null && class_.isAssignableFrom(toastInstance.getToast().getClass()) && toastInstance.getToast().getToken().equals(object)) {
				return (T)toastInstance.getToast();
			}
		}

		for (Toast toast : this.queued) {
			if (class_.isAssignableFrom(toast.getClass()) && toast.getToken().equals(object)) {
				return (T)toast;
			}
		}

		return null;
	}

	public void clear() {
		Arrays.fill(this.visible, null);
		this.queued.clear();
	}

	public void addToast(Toast toast) {
		this.queued.add(toast);
	}

	public Minecraft getMinecraft() {
		return this.minecraft;
	}

	@Environment(EnvType.CLIENT)
	class ToastInstance<T extends Toast> {
		private final T toast;
		private long animationTime = -1L;
		private long visibleTime = -1L;
		private Toast.Visibility visibility = Toast.Visibility.SHOW;

		private ToastInstance(T toast) {
			this.toast = toast;
		}

		public T getToast() {
			return this.toast;
		}

		private float getVisibility(long l) {
			float f = Mth.clamp((float)(l - this.animationTime) / 600.0F, 0.0F, 1.0F);
			f *= f;
			return this.visibility == Toast.Visibility.HIDE ? 1.0F - f : f;
		}

		public boolean render(int i, int j) {
			long l = Util.getMillis();
			if (this.animationTime == -1L) {
				this.animationTime = l;
				this.visibility.playSound(ToastComponent.this.minecraft.getSoundManager());
			}

			if (this.visibility == Toast.Visibility.SHOW && l - this.animationTime <= 600L) {
				this.visibleTime = l;
			}

			RenderSystem.pushMatrix();
			RenderSystem.translatef((float)i - 160.0F * this.getVisibility(l), (float)(j * 32), (float)(800 + j));
			Toast.Visibility visibility = this.toast.render(ToastComponent.this, l - this.visibleTime);
			RenderSystem.popMatrix();
			if (visibility != this.visibility) {
				this.animationTime = l - (long)((int)((1.0F - this.getVisibility(l)) * 600.0F));
				this.visibility = visibility;
				this.visibility.playSound(ToastComponent.this.minecraft.getSoundManager());
			}

			return this.visibility == Toast.Visibility.HIDE && l - this.animationTime > 600L;
		}
	}
}
