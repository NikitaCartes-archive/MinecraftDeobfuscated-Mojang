package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class ProgramManager {
	private static final Logger LOGGER = LogManager.getLogger();

	public static void glUseProgram(int i) {
		GlStateManager._glUseProgram(i);
	}

	public static void releaseProgram(Effect effect) {
		effect.getFragmentProgram().close();
		effect.getVertexProgram().close();
		GlStateManager.glDeleteProgram(effect.getId());
	}

	public static int createProgram() throws IOException {
		int i = GlStateManager.glCreateProgram();
		if (i <= 0) {
			throw new IOException("Could not create shader program (returned program ID " + i + ")");
		} else {
			return i;
		}
	}

	public static void linkProgram(Effect effect) throws IOException {
		effect.getFragmentProgram().attachToEffect(effect);
		effect.getVertexProgram().attachToEffect(effect);
		GlStateManager.glLinkProgram(effect.getId());
		int i = GlStateManager.glGetProgrami(effect.getId(), 35714);
		if (i == 0) {
			LOGGER.warn(
				"Error encountered when linking program containing VS {} and FS {}. Log output:",
				effect.getVertexProgram().getName(),
				effect.getFragmentProgram().getName()
			);
			LOGGER.warn(GlStateManager.glGetProgramInfoLog(effect.getId(), 32768));
		}
	}
}
