package net.minecraft.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class Screenshot {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

	public static void grab(File file, int i, int j, RenderTarget renderTarget, Consumer<Component> consumer) {
		grab(file, null, i, j, renderTarget, consumer);
	}

	public static void grab(File file, @Nullable String string, int i, int j, RenderTarget renderTarget, Consumer<Component> consumer) {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(() -> _grab(file, string, i, j, renderTarget, consumer));
		} else {
			_grab(file, string, i, j, renderTarget, consumer);
		}
	}

	private static void _grab(File file, @Nullable String string, int i, int j, RenderTarget renderTarget, Consumer<Component> consumer) {
		NativeImage nativeImage = takeScreenshot(i, j, renderTarget);
		File file2 = new File(file, "screenshots");
		file2.mkdir();
		File file3;
		if (string == null) {
			file3 = getFile(file2);
		} else {
			file3 = new File(file2, string);
		}

		Util.ioPool()
			.execute(
				() -> {
					try {
						nativeImage.writeToFile(file3);
						Component component = new TextComponent(file3.getName())
							.withStyle(ChatFormatting.UNDERLINE)
							.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file3.getAbsolutePath())));
						consumer.accept(new TranslatableComponent("screenshot.success", component));
					} catch (Exception var7x) {
						LOGGER.warn("Couldn't save screenshot", (Throwable)var7x);
						consumer.accept(new TranslatableComponent("screenshot.failure", var7x.getMessage()));
					} finally {
						nativeImage.close();
					}
				}
			);
	}

	public static NativeImage takeScreenshot(int i, int j, RenderTarget renderTarget) {
		i = renderTarget.width;
		j = renderTarget.height;
		NativeImage nativeImage = new NativeImage(i, j, false);
		RenderSystem.bindTexture(renderTarget.getColorTextureId());
		nativeImage.downloadTexture(0, true);
		nativeImage.flipY();
		return nativeImage;
	}

	private static File getFile(File file) {
		String string = DATE_FORMAT.format(new Date());
		int i = 1;

		while (true) {
			File file2 = new File(file, string + (i == 1 ? "" : "_" + i) + ".png");
			if (!file2.exists()) {
				return file2;
			}

			i++;
		}
	}
}
