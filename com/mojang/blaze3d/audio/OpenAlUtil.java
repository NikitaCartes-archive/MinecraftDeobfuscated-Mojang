/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.audio;

import com.mojang.logging.LogUtils;
import javax.sound.sampled.AudioFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class OpenAlUtil {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static String alErrorToString(int i) {
        switch (i) {
            case 40961: {
                return "Invalid name parameter.";
            }
            case 40962: {
                return "Invalid enumerated parameter value.";
            }
            case 40963: {
                return "Invalid parameter parameter value.";
            }
            case 40964: {
                return "Invalid operation.";
            }
            case 40965: {
                return "Unable to allocate memory.";
            }
        }
        return "An unrecognized error occurred.";
    }

    static boolean checkALError(String string) {
        int i = AL10.alGetError();
        if (i != 0) {
            LOGGER.error("{}: {}", (Object)string, (Object)OpenAlUtil.alErrorToString(i));
            return true;
        }
        return false;
    }

    private static String alcErrorToString(int i) {
        switch (i) {
            case 40961: {
                return "Invalid device.";
            }
            case 40962: {
                return "Invalid context.";
            }
            case 40964: {
                return "Invalid value.";
            }
            case 40963: {
                return "Illegal enum.";
            }
            case 40965: {
                return "Unable to allocate memory.";
            }
        }
        return "An unrecognized error occurred.";
    }

    static boolean checkALCError(long l, String string) {
        int i = ALC10.alcGetError(l);
        if (i != 0) {
            LOGGER.error("{}{}: {}", string, l, OpenAlUtil.alcErrorToString(i));
            return true;
        }
        return false;
    }

    static int audioFormatToOpenAl(AudioFormat audioFormat) {
        AudioFormat.Encoding encoding = audioFormat.getEncoding();
        int i = audioFormat.getChannels();
        int j = audioFormat.getSampleSizeInBits();
        if (encoding.equals(AudioFormat.Encoding.PCM_UNSIGNED) || encoding.equals(AudioFormat.Encoding.PCM_SIGNED)) {
            if (i == 1) {
                if (j == 8) {
                    return 4352;
                }
                if (j == 16) {
                    return 4353;
                }
            } else if (i == 2) {
                if (j == 8) {
                    return 4354;
                }
                if (j == 16) {
                    return 4355;
                }
            }
        }
        throw new IllegalArgumentException("Invalid audio format: " + audioFormat);
    }
}

