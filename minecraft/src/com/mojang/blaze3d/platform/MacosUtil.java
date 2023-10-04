package com.mojang.blaze3d.platform;

import ca.weblite.objc.Client;
import ca.weblite.objc.NSObject;
import com.sun.jna.Pointer;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.packs.resources.IoSupplier;
import org.lwjgl.glfw.GLFWNativeCocoa;

@Environment(EnvType.CLIENT)
public class MacosUtil {
	private static final int NS_RESIZABLE_WINDOW_MASK = 8;
	private static final int NS_FULL_SCREEN_WINDOW_MASK = 16384;

	public static void exitNativeFullscreen(long l) {
		getNsWindow(l).filter(MacosUtil::isInNativeFullscreen).ifPresent(MacosUtil::toggleNativeFullscreen);
	}

	public static void clearResizableBit(long l) {
		getNsWindow(l).ifPresent(nSObject -> {
			long lx = getStyleMask(nSObject);
			nSObject.send("setStyleMask:", new Object[]{lx & -9L});
		});
	}

	private static Optional<NSObject> getNsWindow(long l) {
		long m = GLFWNativeCocoa.glfwGetCocoaWindow(l);
		return m != 0L ? Optional.of(new NSObject(new Pointer(m))) : Optional.empty();
	}

	private static boolean isInNativeFullscreen(NSObject nSObject) {
		return (getStyleMask(nSObject) & 16384L) != 0L;
	}

	private static long getStyleMask(NSObject nSObject) {
		return (Long)nSObject.sendRaw("styleMask", new Object[0]);
	}

	private static void toggleNativeFullscreen(NSObject nSObject) {
		nSObject.send("toggleFullScreen:", new Object[]{Pointer.NULL});
	}

	public static void loadIcon(IoSupplier<InputStream> ioSupplier) throws IOException {
		InputStream inputStream = ioSupplier.get();

		try {
			String string = Base64.getEncoder().encodeToString(inputStream.readAllBytes());
			Client client = Client.getInstance();
			Object object = client.sendProxy("NSData", "alloc").send("initWithBase64Encoding:", string);
			Object object2 = client.sendProxy("NSImage", "alloc").send("initWithData:", object);
			client.sendProxy("NSApplication", "sharedApplication").send("setApplicationIconImage:", object2);
		} catch (Throwable var7) {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Throwable var6) {
					var7.addSuppressed(var6);
				}
			}

			throw var7;
		}

		if (inputStream != null) {
			inputStream.close();
		}
	}
}
