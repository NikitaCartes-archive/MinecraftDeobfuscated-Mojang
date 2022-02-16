/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class JigsawBlockEntity
extends BlockEntity {
    public static final String TARGET = "target";
    public static final String POOL = "pool";
    public static final String JOINT = "joint";
    public static final String NAME = "name";
    public static final String FINAL_STATE = "final_state";
    private ResourceLocation name = new ResourceLocation("empty");
    private ResourceLocation target = new ResourceLocation("empty");
    private ResourceLocation pool = new ResourceLocation("empty");
    private JointType joint = JointType.ROLLABLE;
    private String finalState = "minecraft:air";

    public JigsawBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.JIGSAW, blockPos, blockState);
    }

    public ResourceLocation getName() {
        return this.name;
    }

    public ResourceLocation getTarget() {
        return this.target;
    }

    public ResourceLocation getPool() {
        return this.pool;
    }

    public String getFinalState() {
        return this.finalState;
    }

    public JointType getJoint() {
        return this.joint;
    }

    public void setName(ResourceLocation resourceLocation) {
        this.name = resourceLocation;
    }

    public void setTarget(ResourceLocation resourceLocation) {
        this.target = resourceLocation;
    }

    public void setPool(ResourceLocation resourceLocation) {
        this.pool = resourceLocation;
    }

    public void setFinalState(String string) {
        this.finalState = string;
    }

    public void setJoint(JointType jointType) {
        this.joint = jointType;
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        compoundTag.putString(NAME, this.name.toString());
        compoundTag.putString(TARGET, this.target.toString());
        compoundTag.putString(POOL, this.pool.toString());
        compoundTag.putString(FINAL_STATE, this.finalState);
        compoundTag.putString(JOINT, this.joint.getSerializedName());
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        this.name = new ResourceLocation(compoundTag.getString(NAME));
        this.target = new ResourceLocation(compoundTag.getString(TARGET));
        this.pool = new ResourceLocation(compoundTag.getString(POOL));
        this.finalState = compoundTag.getString(FINAL_STATE);
        this.joint = JointType.byName(compoundTag.getString(JOINT)).orElseGet(() -> JigsawBlock.getFrontFacing(this.getBlockState()).getAxis().isHorizontal() ? JointType.ALIGNED : JointType.ROLLABLE);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public void generate(ServerLevel serverLevel, int i, boolean bl) {
        ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
        StructureManager structureManager = serverLevel.getStructureManager();
        StructureFeatureManager structureFeatureManager = serverLevel.structureFeatureManager();
        Random random = serverLevel.getRandom();
        BlockPos blockPos = this.getBlockPos();
        ArrayList<PoolElementStructurePiece> list = Lists.newArrayList();
        StructureTemplate structureTemplate = new StructureTemplate();
        structureTemplate.fillFromWorld(serverLevel, blockPos, new Vec3i(1, 1, 1), false, null);
        SinglePoolElement structurePoolElement = new SinglePoolElement(structureTemplate);
        PoolElementStructurePiece poolElementStructurePiece = new PoolElementStructurePiece(structureManager, structurePoolElement, blockPos, 1, Rotation.NONE, new BoundingBox(blockPos));
        JigsawPlacement.addPieces(serverLevel.registryAccess(), poolElementStructurePiece, i, PoolElementStructurePiece::new, chunkGenerator, structureManager, list, random, serverLevel);
        for (PoolElementStructurePiece poolElementStructurePiece2 : list) {
            poolElementStructurePiece2.place(serverLevel, structureFeatureManager, chunkGenerator, random, BoundingBox.infinite(), blockPos, bl);
        }
    }

    public /* synthetic */ Packet getUpdatePacket() {
        return this.getUpdatePacket();
    }

    public static enum JointType implements StringRepresentable
    {
        ROLLABLE("rollable"),
        ALIGNED("aligned");

        private final String name;

        private JointType(String string2) {
            this.name = string2;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public static Optional<JointType> byName(String string) {
            return Arrays.stream(JointType.values()).filter(jointType -> jointType.getSerializedName().equals(string)).findFirst();
        }

        public Component getTranslatedName() {
            return new TranslatableComponent("jigsaw_block.joint." + this.name);
        }
    }
}

