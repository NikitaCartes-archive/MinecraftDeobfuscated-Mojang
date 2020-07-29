/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import java.util.List;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;

public class StructureProcessorList {
    private final List<StructureProcessor> list;

    public StructureProcessorList(List<StructureProcessor> list) {
        this.list = list;
    }

    public List<StructureProcessor> list() {
        return this.list;
    }

    public String toString() {
        return "ProcessorList[" + this.list + "]";
    }
}

