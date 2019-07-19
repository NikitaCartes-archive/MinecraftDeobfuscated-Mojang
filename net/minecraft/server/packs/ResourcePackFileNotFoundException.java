/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs;

import java.io.File;
import java.io.FileNotFoundException;

public class ResourcePackFileNotFoundException
extends FileNotFoundException {
    public ResourcePackFileNotFoundException(File file, String string) {
        super(String.format("'%s' in ResourcePack '%s'", string, file));
    }
}

