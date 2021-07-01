/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SkullBlockEntity
extends BlockEntity {
    public static final String TAG_SKULL_OWNER = "SkullOwner";
    @Nullable
    private static GameProfileCache profileCache;
    @Nullable
    private static MinecraftSessionService sessionService;
    @Nullable
    private static Executor mainThreadExecutor;
    @Nullable
    private GameProfile owner;
    private int mouthTickCount;
    private boolean isMovingMouth;

    public SkullBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.SKULL, blockPos, blockState);
    }

    public static void setProfileCache(GameProfileCache gameProfileCache) {
        profileCache = gameProfileCache;
    }

    public static void setSessionService(MinecraftSessionService minecraftSessionService) {
        sessionService = minecraftSessionService;
    }

    public static void setMainThreadExecutor(Executor executor) {
        mainThreadExecutor = executor;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        if (this.owner != null) {
            CompoundTag compoundTag2 = new CompoundTag();
            NbtUtils.writeGameProfile(compoundTag2, this.owner);
            compoundTag.put(TAG_SKULL_OWNER, compoundTag2);
        }
        return compoundTag;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        String string;
        super.load(compoundTag);
        if (compoundTag.contains(TAG_SKULL_OWNER, 10)) {
            this.setOwner(NbtUtils.readGameProfile(compoundTag.getCompound(TAG_SKULL_OWNER)));
        } else if (compoundTag.contains("ExtraType", 8) && !StringUtil.isNullOrEmpty(string = compoundTag.getString("ExtraType"))) {
            this.setOwner(new GameProfile(null, string));
        }
    }

    public static void dragonHeadAnimation(Level level, BlockPos blockPos, BlockState blockState, SkullBlockEntity skullBlockEntity) {
        if (level.hasNeighborSignal(blockPos)) {
            skullBlockEntity.isMovingMouth = true;
            ++skullBlockEntity.mouthTickCount;
        } else {
            skullBlockEntity.isMovingMouth = false;
        }
    }

    public float getMouthAnimation(float f) {
        if (this.isMovingMouth) {
            return (float)this.mouthTickCount + f;
        }
        return this.mouthTickCount;
    }

    @Nullable
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setOwner(@Nullable GameProfile gameProfile) {
        SkullBlockEntity skullBlockEntity = this;
        synchronized (skullBlockEntity) {
            this.owner = gameProfile;
        }
        this.updateOwnerProfile();
    }

    private void updateOwnerProfile() {
        SkullBlockEntity.updateGameprofile(this.owner, gameProfile -> {
            this.owner = gameProfile;
            this.setChanged();
        });
    }

    public static void updateGameprofile(@Nullable GameProfile gameProfile, Consumer<GameProfile> consumer) {
        if (gameProfile == null || StringUtil.isNullOrEmpty(gameProfile.getName()) || gameProfile.isComplete() && gameProfile.getProperties().containsKey("textures") || profileCache == null || sessionService == null) {
            consumer.accept(gameProfile);
            return;
        }
        profileCache.getAsync(gameProfile.getName(), optional -> Util.backgroundExecutor().execute(() -> Util.ifElse(optional, gameProfile -> {
            Property property = Iterables.getFirst(gameProfile.getProperties().get("textures"), null);
            if (property == null) {
                gameProfile = sessionService.fillProfileProperties((GameProfile)gameProfile, true);
            }
            GameProfile gameProfile2 = gameProfile;
            mainThreadExecutor.execute(() -> {
                profileCache.add(gameProfile2);
                consumer.accept(gameProfile2);
            });
        }, () -> mainThreadExecutor.execute(() -> consumer.accept(gameProfile)))));
    }
}

