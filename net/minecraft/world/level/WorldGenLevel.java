/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.jetbrains.annotations.Nullable;

public interface WorldGenLevel
extends ServerLevelAccessor {
    public long getSeed();

    public Stream<? extends StructureStart<?>> startsForFeature(SectionPos var1, StructureFeature<?> var2);

    default public boolean ensureCanWrite(BlockPos blockPos) {
        return true;
    }

    default public void setCurrentlyGenerating(@Nullable Supplier<String> supplier) {
    }
}

