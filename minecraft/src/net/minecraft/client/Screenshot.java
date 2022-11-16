package net.minecraft.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class Screenshot {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final String SCREENSHOT_DIR = "screenshots";
	private int rowHeight;
	private final DataOutputStream outputStream;
	private final byte[] bytes;
	private final int width;
	private final int height;
	private File file;

	public static void grab(File file, RenderTarget renderTarget, Consumer<Component> consumer) {
		grab(file, null, renderTarget, consumer);
	}

	public static void grab(File file, @Nullable String string, RenderTarget renderTarget, Consumer<Component> consumer) {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(() -> _grab(file, string, renderTarget, consumer));
		} else {
			_grab(file, string, renderTarget, consumer);
		}
	}

	private static void _grab(File file, @Nullable String string, RenderTarget renderTarget, Consumer<Component> consumer) {
		NativeImage nativeImage = takeScreenshot(renderTarget);
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
						Component component = Component.literal(file3.getName())
							.withStyle(ChatFormatting.UNDERLINE)
							.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file3.getAbsolutePath())));
						consumer.accept(Component.translatable("screenshot.success", component));
					} catch (Exception var7) {
						LOGGER.warn("Couldn't save screenshot", (Throwable)var7);
						consumer.accept(Component.translatable("screenshot.failure", var7.getMessage()));
					} finally {
						nativeImage.close();
					}
				}
			);
	}

	public static NativeImage takeScreenshot(RenderTarget renderTarget) {
		int i = renderTarget.width;
		int j = renderTarget.height;
		NativeImage nativeImage = new NativeImage(i, j, false);
		RenderSystem.bindTexture(renderTarget.getColorTextureId());
		nativeImage.downloadTexture(0, true);
		nativeImage.flipY();
		return nativeImage;
	}

	private static File getFile(File file) {
		String string = Util.getFilenameFormattedDateTime();
		int i = 1;

		while (true) {
			File file2 = new File(file, string + (i == 1 ? "" : "_" + i) + ".png");
			if (!file2.exists()) {
				return file2;
			}

			i++;
		}
	}

	public Screenshot(File file, int i, int j, int k) throws IOException {
		this.width = i;
		this.height = j;
		this.rowHeight = k;
		File file2 = new File(file, "screenshots");
		file2.mkdir();
		String string = "huge_" + Util.getFilenameFormattedDateTime();
		int l = 1;

		while ((this.file = new File(file2, string + (l == 1 ? "" : "_" + l) + ".tga")).exists()) {
			l++;
		}

		byte[] bs = new byte[18];
		bs[2] = 2;
		bs[12] = (byte)(i % 256);
		bs[13] = (byte)(i / 256);
		bs[14] = (byte)(j % 256);
		bs[15] = (byte)(j / 256);
		bs[16] = 24;
		this.bytes = new byte[i * k * 3];
		this.outputStream = new DataOutputStream(new FileOutputStream(this.file));
		this.outputStream.write(bs);
	}

	public void addRegion(ByteBuffer byteBuffer, int i, int j, int k, int l) {
		int m = k;
		int n = l;
		if (k > this.width - i) {
			m = this.width - i;
		}

		if (l > this.height - j) {
			n = this.height - j;
		}

		this.rowHeight = n;

		for (int o = 0; o < n; o++) {
			byteBuffer.position((l - n) * k * 3 + o * k * 3);
			int p = (i + o * this.width) * 3;
			byteBuffer.get(this.bytes, p, m * 3);
		}
	}

	public void saveRow() throws IOException {
		this.outputStream.write(this.bytes, 0, this.width * 3 * this.rowHeight);
	}

	public File close() throws IOException {
		this.outputStream.close();
		return this.file;
	}
}
