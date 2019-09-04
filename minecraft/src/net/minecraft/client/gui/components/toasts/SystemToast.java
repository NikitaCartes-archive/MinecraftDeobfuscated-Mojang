package net.minecraft.client.gui.components.toasts;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class SystemToast implements Toast {
	private final SystemToast.SystemToastIds id;
	private String title;
	private String message;
	private long lastChanged;
	private boolean changed;

	public SystemToast(SystemToast.SystemToastIds systemToastIds, Component component, @Nullable Component component2) {
		this.id = systemToastIds;
		this.title = component.getString();
		this.message = component2 == null ? null : component2.getString();
	}

	@Override
	public Toast.Visibility render(ToastComponent toastComponent, long l) {
		if (this.changed) {
			this.lastChanged = l;
			this.changed = false;
		}

		toastComponent.getMinecraft().getTextureManager().bind(TEXTURE);
		RenderSystem.color3f(1.0F, 1.0F, 1.0F);
		toastComponent.blit(0, 0, 0, 64, 160, 32);
		if (this.message == null) {
			toastComponent.getMinecraft().font.draw(this.title, 18.0F, 12.0F, -256);
		} else {
			toastComponent.getMinecraft().font.draw(this.title, 18.0F, 7.0F, -256);
			toastComponent.getMinecraft().font.draw(this.message, 18.0F, 18.0F, -1);
		}

		return l - this.lastChanged < 5000L ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
	}

	public void reset(Component component, @Nullable Component component2) {
		this.title = component.getString();
		this.message = component2 == null ? null : component2.getString();
		this.changed = true;
	}

	public SystemToast.SystemToastIds getToken() {
		return this.id;
	}

	public static void addOrUpdate(ToastComponent toastComponent, SystemToast.SystemToastIds systemToastIds, Component component, @Nullable Component component2) {
		SystemToast systemToast = toastComponent.getToast(SystemToast.class, systemToastIds);
		if (systemToast == null) {
			toastComponent.addToast(new SystemToast(systemToastIds, component, component2));
		} else {
			systemToast.reset(component, component2);
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum SystemToastIds {
		TUTORIAL_HINT,
		NARRATOR_TOGGLE,
		WORLD_BACKUP;
	}
}
