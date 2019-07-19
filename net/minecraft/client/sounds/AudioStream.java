/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.sounds;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.sound.sampled.AudioFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface AudioStream
extends Closeable {
    public AudioFormat getFormat();

    public ByteBuffer readAll() throws IOException;

    @Nullable
    public ByteBuffer read(int var1) throws IOException;
}

