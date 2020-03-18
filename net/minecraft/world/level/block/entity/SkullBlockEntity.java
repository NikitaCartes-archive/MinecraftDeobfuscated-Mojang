/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import org.jetbrains.annotations.Nullable;

public class SkullBlockEntity
extends BlockEntity
implements TickableBlockEntity {
    private GameProfile owner;
    private int mouthTickCount;
    private boolean isMovingMouth;
    private static GameProfileCache profileCache;
    private static MinecraftSessionService sessionService;

    public SkullBlockEntity() {
        super(BlockEntityType.SKULL);
    }

    public static void setProfileCache(GameProfileCache gameProfileCache) {
        profileCache = gameProfileCache;
    }

    public static void setSessionService(MinecraftSessionService minecraftSessionService) {
        sessionService = minecraftSessionService;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        if (this.owner != null) {
            CompoundTag compoundTag2 = new CompoundTag();
            NbtUtils.writeGameProfile(compoundTag2, this.owner);
            compoundTag.put("SkullOwner", compoundTag2);
        }
        return compoundTag;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        String string;
        super.load(compoundTag);
        if (compoundTag.contains("SkullOwner", 10)) {
            this.setOwner(NbtUtils.readGameProfile(compoundTag.getCompound("SkullOwner")));
        } else if (compoundTag.contains("ExtraType", 8) && !StringUtil.isNullOrEmpty(string = compoundTag.getString("ExtraType"))) {
            this.setOwner(new GameProfile(null, string));
        }
    }

    @Override
    public void tick() {
        Block block = this.getBlockState().getBlock();
        if (block == Blocks.DRAGON_HEAD || block == Blocks.DRAGON_WALL_HEAD) {
            if (this.level.hasNeighborSignal(this.worldPosition)) {
                this.isMovingMouth = true;
                ++this.mouthTickCount;
            } else {
                this.isMovingMouth = false;
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public float getMouthAnimation(float f) {
        if (this.isMovingMouth) {
            return (float)this.mouthTickCount + f;
        }
        return this.mouthTickCount;
    }

    @Nullable
    @Environment(value=EnvType.CLIENT)
    public GameProfile getOwnerProfile() {
        return this.owner;
    }

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 4, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }

    public void setOwner(@Nullable GameProfile gameProfile) {
        this.owner = gameProfile;
        this.updateOwnerProfile();
    }

    private void updateOwnerProfile() {
        this.owner = SkullBlockEntity.updateGameprofile(this.owner);
        this.setChanged();
    }

    public static GameProfile updateGameprofile(GameProfile gameProfile) {
        if (gameProfile == null || StringUtil.isNullOrEmpty(gameProfile.getName())) {
            return gameProfile;
        }
        if (gameProfile.isComplete() && gameProfile.getProperties().containsKey("textures")) {
            return gameProfile;
        }
        if (profileCache == null || sessionService == null) {
            return gameProfile;
        }
        GameProfile gameProfile2 = profileCache.get(gameProfile.getName());
        if (gameProfile2 == null) {
            return gameProfile;
        }
        Property property = Iterables.getFirst(gameProfile2.getProperties().get("textures"), null);
        if (property == null) {
            gameProfile2 = sessionService.fillProfileProperties(gameProfile2, true);
        }
        return gameProfile2;
    }
}

