package net.minecraft.client.gui.components.toasts;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;

@Environment(EnvType.CLIENT)
public class SystemToast implements Toast {
	private static final long DISPLAY_TIME = 5000L;
	private static final int MAX_LINE_SIZE = 200;
	private final SystemToast.SystemToastIds id;
	private Component title;
	private List<FormattedCharSequence> messageLines;
	private long lastChanged;
	private boolean changed;
	private final int width;

	public SystemToast(SystemToast.SystemToastIds systemToastIds, Component component, @Nullable Component component2) {
		this(systemToastIds, component, nullToEmpty(component2), 160);
	}

	public static SystemToast multiline(Minecraft minecraft, SystemToast.SystemToastIds systemToastIds, Component component, Component component2) {
		Font font = minecraft.font;
		List<FormattedCharSequence> list = font.split(component2, 200);
		int i = Math.max(200, list.stream().mapToInt(font::width).max().orElse(200));
		return new SystemToast(systemToastIds, component, list, i + 30);
	}

	private SystemToast(SystemToast.SystemToastIds systemToastIds, Component component, List<FormattedCharSequence> list, int i) {
		this.id = systemToastIds;
		this.title = component;
		this.messageLines = list;
		this.width = i;
	}

	private static ImmutableList<FormattedCharSequence> nullToEmpty(@Nullable Component component) {
		return component == null ? ImmutableList.of() : ImmutableList.of(component.getVisualOrderText());
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

		RenderSystem.setShaderTexture(0, TEXTURE);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		int i = this.width();
		int j = 12;
		if (i == 160 && this.messageLines.size() <= 1) {
			toastComponent.blit(poseStack, 0, 0, 0, 64, i, this.height());
		} else {
			int k = this.height() + Math.max(0, this.messageLines.size() - 1) * 12;
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

			for (int k = 0; k < this.messageLines.size(); k++) {
				toastComponent.getMinecraft().font.draw(poseStack, (FormattedCharSequence)this.messageLines.get(k), 18.0F, (float)(18 + k * 12), -1);
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
		this.title = component;
		this.messageLines = nullToEmpty(component2);
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

	public static void onPackCopyFailure(Minecraft minecraft, String string) {
		add(minecraft.getToasts(), SystemToast.SystemToastIds.PACK_COPY_FAILURE, new TranslatableComponent("pack.copyFailure"), new TextComponent(string));
	}

	@Environment(EnvType.CLIENT)
	public static enum SystemToastIds {
		TUTORIAL_HINT,
		NARRATOR_TOGGLE,
		WORLD_BACKUP,
		WORLD_GEN_SETTINGS_TRANSFER,
		PACK_LOAD_FAILURE,
		WORLD_ACCESS_FAILURE,
		PACK_COPY_FAILURE;
	}
}
