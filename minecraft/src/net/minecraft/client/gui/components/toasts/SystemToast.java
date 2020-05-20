package net.minecraft.client.gui.components.toasts;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.commons.lang3.text.WordUtils;

@Environment(EnvType.CLIENT)
public class SystemToast implements Toast {
	private final SystemToast.SystemToastIds id;
	private String title;
	private String[] messageLines;
	private long lastChanged;
	private boolean changed;
	private final int width;

	public SystemToast(SystemToast.SystemToastIds systemToastIds, Component component, @Nullable Component component2) {
		this(systemToastIds, component, component2 == null ? new String[0] : new String[]{component2.getString()}, 160);
	}

	public static SystemToast multiline(SystemToast.SystemToastIds systemToastIds, Component component, Component component2) {
		String[] strings = WordUtils.wrap(component2.getString(), 80).split("\n");
		int i = Math.max(130, Arrays.stream(strings).mapToInt(string -> Minecraft.getInstance().font.width(string)).max().orElse(130));
		return new SystemToast(systemToastIds, component, strings, i + 30);
	}

	private SystemToast(SystemToast.SystemToastIds systemToastIds, Component component, String[] strings, int i) {
		this.id = systemToastIds;
		this.title = component.getString();
		this.messageLines = strings;
		this.width = i;
	}

	@Override
	public int width() {
		return this.width;
	}

	@Override
	public Toast.Visibility render(PoseStack poseStack, ToastComponent toastComponent, long l) {
		if (this.changed) {
			this.lastChanged = l;
			this.changed = false;
		}

		toastComponent.getMinecraft().getTextureManager().bind(TEXTURE);
		RenderSystem.color3f(1.0F, 1.0F, 1.0F);
		int i = this.width();
		int j = 12;
		if (i == 160 && this.messageLines.length <= 1) {
			toastComponent.blit(poseStack, 0, 0, 0, 64, i, this.height());
		} else {
			int k = this.height() + Math.max(0, this.messageLines.length - 1) * 12;
			int m = 28;
			int n = Math.min(4, k - 28);
			this.renderBackgroundRow(poseStack, toastComponent, i, 0, 0, 28);

			for (int o = 28; o < k - n; o += 10) {
				this.renderBackgroundRow(poseStack, toastComponent, i, 16, o, Math.min(16, k - o - n));
			}

			this.renderBackgroundRow(poseStack, toastComponent, i, 32 - n, k - n, n);
		}

		if (this.messageLines == null) {
			toastComponent.getMinecraft().font.draw(poseStack, this.title, 18.0F, 12.0F, -256);
		} else {
			toastComponent.getMinecraft().font.draw(poseStack, this.title, 18.0F, 7.0F, -256);

			for (int k = 0; k < this.messageLines.length; k++) {
				String string = this.messageLines[k];
				toastComponent.getMinecraft().font.draw(poseStack, string, 18.0F, (float)(18 + k * 12), -1);
			}
		}

		return l - this.lastChanged < 5000L ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
	}

	private void renderBackgroundRow(PoseStack poseStack, ToastComponent toastComponent, int i, int j, int k, int l) {
		int m = j == 0 ? 20 : 5;
		int n = Math.min(60, i - m);
		toastComponent.blit(poseStack, 0, k, 0, 64 + j, m, l);

		for (int o = m; o < i - n; o += 64) {
			toastComponent.blit(poseStack, o, k, 32, 64 + j, Math.min(64, i - o - n), l);
		}

		toastComponent.blit(poseStack, i - n, k, 160 - n, 64 + j, n, l);
	}

	public void reset(Component component, @Nullable Component component2) {
		this.title = component.getString();
		this.messageLines = component2 == null ? new String[0] : new String[]{component2.getString()};
		this.changed = true;
	}

	public SystemToast.SystemToastIds getToken() {
		return this.id;
	}

	public static void add(ToastComponent toastComponent, SystemToast.SystemToastIds systemToastIds, Component component, @Nullable Component component2) {
		toastComponent.addToast(new SystemToast(systemToastIds, component, component2));
	}

	public static void addOrUpdate(ToastComponent toastComponent, SystemToast.SystemToastIds systemToastIds, Component component, @Nullable Component component2) {
		SystemToast systemToast = toastComponent.getToast(SystemToast.class, systemToastIds);
		if (systemToast == null) {
			add(toastComponent, systemToastIds, component, component2);
		} else {
			systemToast.reset(component, component2);
		}
	}

	public static void onWorldAccessFailure(Minecraft minecraft, String string) {
		add(
			minecraft.getToasts(), SystemToast.SystemToastIds.WORLD_ACCESS_FAILURE, new TranslatableComponent("selectWorld.access_failure"), new TextComponent(string)
		);
	}

	public static void onWorldDeleteFailure(Minecraft minecraft, String string) {
		add(
			minecraft.getToasts(), SystemToast.SystemToastIds.WORLD_ACCESS_FAILURE, new TranslatableComponent("selectWorld.delete_failure"), new TextComponent(string)
		);
	}

	@Environment(EnvType.CLIENT)
	public static enum SystemToastIds {
		TUTORIAL_HINT,
		NARRATOR_TOGGLE,
		WORLD_BACKUP,
		WORLD_GEN_SETTINGS_TRANSFER,
		PACK_LOAD_FAILURE,
		WORLD_ACCESS_FAILURE;
	}
}
