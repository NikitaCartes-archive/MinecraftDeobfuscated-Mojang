/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.material;

import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.material.WorldGenMaterialRule;
import org.jetbrains.annotations.Nullable;

public class MaterialRuleList
implements WorldGenMaterialRule {
    private final List<WorldGenMaterialRule> materialRuleList;

    public MaterialRuleList(List<WorldGenMaterialRule> list) {
        this.materialRuleList = list;
    }

    @Override
    @Nullable
    public BlockState apply(NoiseChunk noiseChunk, int i, int j, int k) {
        for (WorldGenMaterialRule worldGenMaterialRule : this.materialRuleList) {
            BlockState blockState = worldGenMaterialRule.apply(noiseChunk, i, j, k);
            if (blockState == null) continue;
            return blockState;
        }
        return null;
    }
}

