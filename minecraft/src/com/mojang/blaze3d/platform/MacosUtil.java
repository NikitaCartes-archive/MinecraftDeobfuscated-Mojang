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
import org.lwjgl.glfw.GLFWNativeCocoa;

@Environment(EnvType.CLIENT)
public class MacosUtil {
	private static final int NS_FULL_SCREEN_WINDOW_MASK = 16384;

	public static void toggleFullscreen(long l) {
		getNsWindow(l).filter(MacosUtil::isInKioskMode).ifPresent(MacosUtil::toggleFullscreen);
	}

	private static Optional<NSObject> getNsWindow(long l) {
		long m = GLFWNativeCocoa.glfwGetCocoaWindow(l);
		return m != 0L ? Optional.of(new NSObject(new Pointer(m))) : Optional.empty();
	}

	private static boolean isInKioskMode(NSObject nSObject) {
		return ((Long)nSObject.sendRaw("styleMask", new Object[0]) & 16384L) == 16384L;
	}

	private static void toggleFullscreen(NSObject nSObject) {
		nSObject.send("toggleFullScreen:", new Object[0]);
	}

	public static void loadIcon(InputStream inputStream) throws IOException {
		String string = Base64.getEncoder().encodeToString(inputStream.readAllBytes());
		Client client = Client.getInstance();
		Object object = client.sendProxy("NSData", "alloc").send("initWithBase64Encoding:", string);
		Object object2 = client.sendProxy("NSImage", "alloc").send("initWithData:", object);
		client.sendProxy("NSApplication", "sharedApplication").send("setApplicationIconImage:", object2);
	}
}
