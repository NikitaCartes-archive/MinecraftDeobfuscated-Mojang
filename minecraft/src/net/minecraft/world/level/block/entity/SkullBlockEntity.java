package net.minecraft.world.level.block.entity;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Services;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class SkullBlockEntity extends BlockEntity {
	public static final String TAG_SKULL_OWNER = "SkullOwner";
	public static final String TAG_NOTE_BLOCK_SOUND = "note_block_sound";
	@Nullable
	private static GameProfileCache profileCache;
	@Nullable
	private static MinecraftSessionService sessionService;
	@Nullable
	private static Executor mainThreadExecutor;
	@Nullable
	private GameProfile owner;
	@Nullable
	private ResourceLocation noteBlockSound;
	private int animationTickCount;
	private boolean isAnimating;

	public SkullBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.SKULL, blockPos, blockState);
	}

	public static void setup(Services services, Executor executor) {
		profileCache = services.profileCache();
		sessionService = services.sessionService();
		mainThreadExecutor = executor;
	}

	public static void clear() {
		profileCache = null;
		sessionService = null;
		mainThreadExecutor = null;
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag) {
		super.saveAdditional(compoundTag);
		if (this.owner != null) {
			CompoundTag compoundTag2 = new CompoundTag();
			NbtUtils.writeGameProfile(compoundTag2, this.owner);
			compoundTag.put("SkullOwner", compoundTag2);
		}

		if (this.noteBlockSound != null) {
			compoundTag.putString("note_block_sound", this.noteBlockSound.toString());
		}
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

		if (compoundTag.contains("note_block_sound", 8)) {
			this.noteBlockSound = ResourceLocation.tryParse(compoundTag.getString("note_block_sound"));
		}
	}

	public static void animation(Level level, BlockPos blockPos, BlockState blockState, SkullBlockEntity skullBlockEntity) {
		if (level.hasNeighborSignal(blockPos)) {
			skullBlockEntity.isAnimating = true;
			skullBlockEntity.animationTickCount++;
		} else {
			skullBlockEntity.isAnimating = false;
		}
	}

	public float getAnimation(float f) {
		return this.isAnimating ? (float)this.animationTickCount + f : (float)this.animationTickCount;
	}

	@Nullable
	public GameProfile getOwnerProfile() {
		return this.owner;
	}

	@Nullable
	public ResourceLocation getNoteBlockSound() {
		return this.noteBlockSound;
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithoutMetadata();
	}

	public void setOwner(@Nullable GameProfile gameProfile) {
		synchronized (this) {
			this.owner = gameProfile;
		}

		this.updateOwnerProfile();
	}

	private void updateOwnerProfile() {
		updateGameprofile(this.owner, gameProfile -> {
			this.owner = gameProfile;
			this.setChanged();
		});
	}

	public static void updateGameprofile(@Nullable GameProfile gameProfile, Consumer<GameProfile> consumer) {
		if (gameProfile != null
			&& !StringUtil.isNullOrEmpty(gameProfile.getName())
			&& (!gameProfile.isComplete() || !gameProfile.getProperties().containsKey("textures"))
			&& profileCache != null
			&& sessionService != null) {
			profileCache.getAsync(gameProfile.getName(), optional -> Util.backgroundExecutor().execute(() -> Util.ifElse(optional, gameProfilexxx -> {
						Property property = Iterables.getFirst(gameProfilexxx.getProperties().get("textures"), null);
						if (property == null) {
							MinecraftSessionService minecraftSessionService = sessionService;
							if (minecraftSessionService == null) {
								return;
							}

							gameProfilexxx = minecraftSessionService.fillProfileProperties(gameProfilexxx, true);
						}

						GameProfile gameProfile2 = gameProfilexxx;
						Executor executor = mainThreadExecutor;
						if (executor != null) {
							executor.execute(() -> {
								GameProfileCache gameProfileCache = profileCache;
								if (gameProfileCache != null) {
									gameProfileCache.add(gameProfile2);
									consumer.accept(gameProfile2);
								}
							});
						}
					}, () -> {
						Executor executor = mainThreadExecutor;
						if (executor != null) {
							executor.execute(() -> consumer.accept(gameProfile));
						}
					})));
		} else {
			consumer.accept(gameProfile);
		}
	}
}
