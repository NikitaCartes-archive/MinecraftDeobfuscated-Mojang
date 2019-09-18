/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

@Environment(value=EnvType.CLIENT)
public class StructureRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private final Map<DimensionType, Map<String, BoundingBox>> postMainBoxes = Maps.newIdentityHashMap();
    private final Map<DimensionType, Map<String, BoundingBox>> postPiecesBoxes = Maps.newIdentityHashMap();
    private final Map<DimensionType, Map<String, Boolean>> startPiecesMap = Maps.newIdentityHashMap();

    public StructureRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void addBoundingBox(BoundingBox boundingBox, List<BoundingBox> list, List<Boolean> list2, DimensionType dimensionType) {
        if (!this.postMainBoxes.containsKey(dimensionType)) {
            this.postMainBoxes.put(dimensionType, Maps.newHashMap());
        }
        if (!this.postPiecesBoxes.containsKey(dimensionType)) {
            this.postPiecesBoxes.put(dimensionType, Maps.newHashMap());
            this.startPiecesMap.put(dimensionType, Maps.newHashMap());
        }
        this.postMainBoxes.get(dimensionType).put(boundingBox.toString(), boundingBox);
        for (int i = 0; i < list.size(); ++i) {
            BoundingBox boundingBox2 = list.get(i);
            Boolean boolean_ = list2.get(i);
            this.postPiecesBoxes.get(dimensionType).put(boundingBox2.toString(), boundingBox2);
            this.startPiecesMap.get(dimensionType).put(boundingBox2.toString(), boolean_);
        }
    }

    @Override
    public void clear() {
        this.postMainBoxes.clear();
        this.postPiecesBoxes.clear();
        this.startPiecesMap.clear();
    }
}

