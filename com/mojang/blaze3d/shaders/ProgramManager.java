/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.shaders.Effect;
import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ProgramManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static ProgramManager instance;

    public static void createInstance() {
        instance = new ProgramManager();
    }

    public static ProgramManager getInstance() {
        return instance;
    }

    private ProgramManager() {
    }

    public void releaseProgram(Effect effect) {
        effect.getFragmentProgram().close();
        effect.getVertexProgram().close();
        GLX.glDeleteProgram(effect.getId());
    }

    public int createProgram() throws IOException {
        int i = GLX.glCreateProgram();
        if (i <= 0) {
            throw new IOException("Could not create shader program (returned program ID " + i + ")");
        }
        return i;
    }

    public void linkProgram(Effect effect) throws IOException {
        effect.getFragmentProgram().attachToEffect(effect);
        effect.getVertexProgram().attachToEffect(effect);
        GLX.glLinkProgram(effect.getId());
        int i = GLX.glGetProgrami(effect.getId(), GLX.GL_LINK_STATUS);
        if (i == 0) {
            LOGGER.warn("Error encountered when linking program containing VS {} and FS {}. Log output:", (Object)effect.getVertexProgram().getName(), (Object)effect.getFragmentProgram().getName());
            LOGGER.warn(GLX.glGetProgramInfoLog(effect.getId(), 32768));
        }
    }
}

