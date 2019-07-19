/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.state.predicate;

import java.util.function.Predicate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;

public class BlockMaterialPredicate
implements Predicate<BlockState> {
    private static final BlockMaterialPredicate AIR = new BlockMaterialPredicate(Material.AIR){

        @Override
        public boolean test(@Nullable BlockState blockState) {
            return blockState != null && blockState.isAir();
        }

        @Override
        public /* synthetic */ boolean test(@Nullable Object object) {
            return this.test((BlockState)object);
        }
    };
    private final Material material;

    private BlockMaterialPredicate(Material material) {
        this.material = material;
    }

    public static BlockMaterialPredicate forMaterial(Material material) {
        return material == Material.AIR ? AIR : new BlockMaterialPredicate(material);
    }

    @Override
    public boolean test(@Nullable BlockState blockState) {
        return blockState != null && blockState.getMaterial() == this.material;
    }

    @Override
    public /* synthetic */ boolean test(@Nullable Object object) {
        return this.test((BlockState)object);
    }
}

