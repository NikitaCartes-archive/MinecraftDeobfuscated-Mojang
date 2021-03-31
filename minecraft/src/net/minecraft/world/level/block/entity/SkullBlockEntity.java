package net.minecraft.world.level.block.entity;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class SkullBlockEntity extends BlockEntity {
	public static final String TAG_SKULL_OWNER = "SkullOwner";
	@Nullable
	private static GameProfileCache profileCache;
	@Nullable
	private static MinecraftSessionService sessionService;
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
		super.load(compoundTag);
		if (compoundTag.contains("SkullOwner", 10)) {
			this.setOwner(NbtUtils.readGameProfile(compoundTag.getCompound("SkullOwner")));
		} else if (compoundTag.contains("ExtraType", 8)) {
			String string = compoundTag.getString("ExtraType");
			if (!StringUtil.isNullOrEmpty(string)) {
				this.setOwner(new GameProfile(null, string));
			}
		}
	}

	public static void dragonHeadAnimation(Level level, BlockPos blockPos, BlockState blockState, SkullBlockEntity skullBlockEntity) {
		if (level.hasNeighborSignal(blockPos)) {
			skullBlockEntity.isMovingMouth = true;
			skullBlockEntity.mouthTickCount++;
		} else {
			skullBlockEntity.isMovingMouth = false;
		}
	}

	public float getMouthAnimation(float f) {
		return this.isMovingMouth ? (float)this.mouthTickCount + f : (float)this.mouthTickCount;
	}

	@Nullable
	public GameProfile getOwnerProfile() {
		return this.owner;
	}

	@Nullable
	@Override
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
		this.owner = updateGameprofile(this.owner);
		this.setChanged();
	}

	@Nullable
	public static GameProfile updateGameprofile(@Nullable GameProfile gameProfile) {
		if (gameProfile != null && !StringUtil.isNullOrEmpty(gameProfile.getName())) {
			if (gameProfile.isComplete() && gameProfile.getProperties().containsKey("textures")) {
				return gameProfile;
			} else if (profileCache != null && sessionService != null) {
				GameProfile gameProfile2 = profileCache.get(gameProfile.getName());
				if (gameProfile2 == null) {
					return gameProfile;
				} else {
					Property property = Iterables.getFirst(gameProfile2.getProperties().get("textures"), null);
					if (property == null) {
						gameProfile2 = sessionService.fillProfileProperties(gameProfile2, true);
					}

					return gameProfile2;
				}
			} else {
				return gameProfile;
			}
		} else {
			return gameProfile;
		}
	}
}
