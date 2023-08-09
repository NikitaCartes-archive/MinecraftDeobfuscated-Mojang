package net.minecraft.world.level.block.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
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
	private static final Executor CHECKED_MAIN_THREAD_EXECUTOR = runnable -> {
		Executor executor = mainThreadExecutor;
		if (executor != null) {
			executor.execute(runnable);
		}
	};
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
				this.setOwner(new GameProfile(Util.NIL_UUID, string));
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
		if (this.owner != null && !Util.isBlank(this.owner.getName()) && !hasTextures(this.owner)) {
			fetchGameProfile(this.owner.getName()).thenAcceptAsync(optional -> {
				this.owner = (GameProfile)optional.orElse(this.owner);
				this.setChanged();
			}, CHECKED_MAIN_THREAD_EXECUTOR);
		} else {
			this.setChanged();
		}
	}

	@Nullable
	public static GameProfile getOrResolveGameProfile(CompoundTag compoundTag) {
		if (compoundTag.contains("SkullOwner", 10)) {
			return NbtUtils.readGameProfile(compoundTag.getCompound("SkullOwner"));
		} else {
			if (compoundTag.contains("SkullOwner", 8)) {
				String string = compoundTag.getString("SkullOwner");
				if (!Util.isBlank(string)) {
					compoundTag.remove("SkullOwner");
					resolveGameProfile(compoundTag, string);
				}
			}

			return null;
		}
	}

	public static void resolveGameProfile(CompoundTag compoundTag) {
		String string = compoundTag.getString("SkullOwner");
		if (!Util.isBlank(string)) {
			resolveGameProfile(compoundTag, string);
		}
	}

	private static void resolveGameProfile(CompoundTag compoundTag, String string) {
		fetchGameProfile(string)
			.thenAccept(
				optional -> compoundTag.put(
						"SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), (GameProfile)optional.orElse(new GameProfile(Util.NIL_UUID, string)))
					)
			);
	}

	private static CompletableFuture<Optional<GameProfile>> fetchGameProfile(String string) {
		GameProfileCache gameProfileCache = profileCache;
		return gameProfileCache == null
			? CompletableFuture.completedFuture(Optional.empty())
			: gameProfileCache.getAsync(string)
				.thenCompose(optional -> optional.isPresent() ? fillProfileTextures((GameProfile)optional.get()) : CompletableFuture.completedFuture(Optional.empty()))
				.thenApplyAsync(optional -> {
					GameProfileCache gameProfileCachex = profileCache;
					if (gameProfileCachex != null) {
						optional.ifPresent(gameProfileCachex::add);
						return optional;
					} else {
						return Optional.empty();
					}
				}, CHECKED_MAIN_THREAD_EXECUTOR);
	}

	private static CompletableFuture<Optional<GameProfile>> fillProfileTextures(GameProfile gameProfile) {
		return hasTextures(gameProfile) ? CompletableFuture.completedFuture(Optional.of(gameProfile)) : CompletableFuture.supplyAsync(() -> {
			MinecraftSessionService minecraftSessionService = sessionService;
			if (minecraftSessionService != null) {
				GameProfile gameProfile2 = minecraftSessionService.fetchProfile(gameProfile.getId(), true);
				return Optional.of((GameProfile)Objects.requireNonNullElse(gameProfile2, gameProfile));
			} else {
				return Optional.empty();
			}
		}, Util.backgroundExecutor());
	}

	private static boolean hasTextures(GameProfile gameProfile) {
		return gameProfile.getProperties().containsKey("textures");
	}
}
