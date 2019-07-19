/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.metadata.pack;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.metadata.pack.PackMetadataSectionSerializer;

public class PackMetadataSection {
    public static final PackMetadataSectionSerializer SERIALIZER = new PackMetadataSectionSerializer();
    private final Component description;
    private final int packFormat;

    public PackMetadataSection(Component component, int i) {
        this.description = component;
        this.packFormat = i;
    }

    public Component getDescription() {
        return this.description;
    }

    public int getPackFormat() {
        return this.packFormat;
    }
}

