/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.structures;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElementType;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

public abstract class StructurePoolElement {
    @Nullable
    private volatile StructureTemplatePool.Projection projection;

    protected StructurePoolElement(StructureTemplatePool.Projection projection) {
        this.projection = projection;
    }

    protected StructurePoolElement(Dynamic<?> dynamic) {
        this.projection = StructureTemplatePool.Projection.byName(dynamic.get("projection").asString(StructureTemplatePool.Projection.RIGID.getName()));
    }

    public abstract List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureManager var1, BlockPos var2, Rotation var3, Random var4);

    public abstract BoundingBox getBoundingBox(StructureManager var1, BlockPos var2, Rotation var3);

    public abstract boolean place(StructureManager var1, LevelAccessor var2, ChunkGenerator<?> var3, BlockPos var4, Rotation var5, BoundingBox var6, Random var7);

    public abstract StructurePoolElementType getType();

    public void handleDataMarker(LevelAccessor levelAccessor, StructureTemplate.StructureBlockInfo structureBlockInfo, BlockPos blockPos, Rotation rotation, Random random, BoundingBox boundingBox) {
    }

    public StructurePoolElement setProjection(StructureTemplatePool.Projection projection) {
        this.projection = projection;
        return this;
    }

    public StructureTemplatePool.Projection getProjection() {
        StructureTemplatePool.Projection projection = this.projection;
        if (projection == null) {
            throw new IllegalStateException();
        }
        return projection;
    }

    protected abstract <T> Dynamic<T> getDynamic(DynamicOps<T> var1);

    public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
        T object = this.getDynamic(dynamicOps).getValue();
        T object2 = dynamicOps.mergeInto(object, dynamicOps.createString("element_type"), dynamicOps.createString(Registry.STRUCTURE_POOL_ELEMENT.getKey(this.getType()).toString()));
        return new Dynamic<T>(dynamicOps, dynamicOps.mergeInto(object2, dynamicOps.createString("projection"), dynamicOps.createString(this.projection.getName())));
    }

    public int getGroundLevelDelta() {
        return 1;
    }
}

