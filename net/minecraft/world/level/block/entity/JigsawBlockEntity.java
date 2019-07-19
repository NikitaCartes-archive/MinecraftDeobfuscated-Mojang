/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

public class JigsawBlockEntity
extends BlockEntity {
    private ResourceLocation attachementType = new ResourceLocation("empty");
    private ResourceLocation targetPool = new ResourceLocation("empty");
    private String finalState = "minecraft:air";

    public JigsawBlockEntity(BlockEntityType<?> blockEntityType) {
        super(blockEntityType);
    }

    public JigsawBlockEntity() {
        this(BlockEntityType.JIGSAW);
    }

    @Environment(value=EnvType.CLIENT)
    public ResourceLocation getAttachementType() {
        return this.attachementType;
    }

    @Environment(value=EnvType.CLIENT)
    public ResourceLocation getTargetPool() {
        return this.targetPool;
    }

    @Environment(value=EnvType.CLIENT)
    public String getFinalState() {
        return this.finalState;
    }

    public void setAttachementType(ResourceLocation resourceLocation) {
        this.attachementType = resourceLocation;
    }

    public void setTargetPool(ResourceLocation resourceLocation) {
        this.targetPool = resourceLocation;
    }

    public void setFinalState(String string) {
        this.finalState = string;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        compoundTag.putString("attachement_type", this.attachementType.toString());
        compoundTag.putString("target_pool", this.targetPool.toString());
        compoundTag.putString("final_state", this.finalState);
        return compoundTag;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        this.attachementType = new ResourceLocation(compoundTag.getString("attachement_type"));
        this.targetPool = new ResourceLocation(compoundTag.getString("target_pool"));
        this.finalState = compoundTag.getString("final_state");
    }

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 12, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }
}

