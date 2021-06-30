package com.mojang.blaze3d.platform;

import ca.weblite.objc.NSObject;
import com.sun.jna.Pointer;
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
}
