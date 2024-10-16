package net.minecraft.client.gui.font.providers;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.FT_Vector;
import org.lwjgl.util.freetype.FreeType;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class FreeTypeUtil {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final Object LIBRARY_LOCK = new Object();
	private static long library = 0L;

	public static long getLibrary() {
		synchronized (LIBRARY_LOCK) {
			if (library == 0L) {
				try (MemoryStack memoryStack = MemoryStack.stackPush()) {
					PointerBuffer pointerBuffer = memoryStack.mallocPointer(1);
					assertError(FreeType.FT_Init_FreeType(pointerBuffer), "Initializing FreeType library");
					library = pointerBuffer.get();
				}
			}

			return library;
		}
	}

	public static void assertError(int i, String string) {
		if (i != 0) {
			throw new IllegalStateException("FreeType error: " + describeError(i) + " (" + string + ")");
		}
	}

	public static boolean checkError(int i, String string) {
		if (i != 0) {
			LOGGER.error("FreeType error: {} ({})", describeError(i), string);
			return true;
		} else {
			return false;
		}
	}

	private static String describeError(int i) {
		String string = FreeType.FT_Error_String(i);
		return string != null ? string : "Unrecognized error: 0x" + Integer.toHexString(i);
	}

	public static FT_Vector setVector(FT_Vector fT_Vector, float f, float g) {
		long l = (long)Math.round(f * 64.0F);
		long m = (long)Math.round(g * 64.0F);
		return fT_Vector.set(l, m);
	}

	public static float x(FT_Vector fT_Vector) {
		return (float)fT_Vector.x() / 64.0F;
	}

	public static void destroy() {
		synchronized (LIBRARY_LOCK) {
			if (library != 0L) {
				FreeType.FT_Done_Library(library);
				library = 0L;
			}
		}
	}
}
