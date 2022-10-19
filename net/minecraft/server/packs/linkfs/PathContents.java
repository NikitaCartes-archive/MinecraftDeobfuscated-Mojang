/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.linkfs;

import java.nio.file.Path;
import java.util.Map;
import net.minecraft.server.packs.linkfs.LinkFSPath;

interface PathContents {
    public static final PathContents MISSING = new PathContents(){

        public String toString() {
            return "empty";
        }
    };
    public static final PathContents RELATIVE = new PathContents(){

        public String toString() {
            return "relative";
        }
    };

    public record DirectoryContents(Map<String, LinkFSPath> children) implements PathContents
    {
    }

    public record FileContents(Path contents) implements PathContents
    {
    }
}

