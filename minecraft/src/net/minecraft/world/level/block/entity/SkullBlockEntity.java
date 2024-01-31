package net.minecraft.world.level.block.entity;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Services;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SkullBlockEntity extends BlockEntity {
	public static final String TAG_SKULL_OWNER = "SkullOwner";
	public static final String TAG_NOTE_BLOCK_SOUND = "note_block_sound";
	@Nullable
	private static Executor mainThreadExecutor;
	@Nullable
	private static LoadingCache<String, CompletableFuture<Optional<GameProfile>>> profileCache;
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
		mainThreadExecutor = executor;
		final BooleanSupplier booleanSupplier = () -> profileCache == null;
		profileCache = CacheBuilder.newBuilder()
			.expireAfterAccess(Duration.ofMinutes(10L))
			.maximumSize(256L)
			.build(
				new CacheLoader<String, CompletableFuture<Optional<GameProfile>>>() {
					public CompletableFuture<Optional<GameProfile>> load(String string) {
						return booleanSupplier.getAsBoolean()
							? CompletableFuture.completedFuture(Optional.empty())
							: SkullBlockEntity.loadProfile(string, services, booleanSupplier);
					}
				}
			);
	}

	public static void clear() {
		mainThreadExecutor = null;
		profileCache = null;
	}

	static CompletableFuture<Optional<GameProfile>> loadProfile(String string, Services services, BooleanSupplier booleanSupplier) {
		return services.profileCache().getAsync(string).thenApplyAsync(optional -> {
			if (optional.isPresent() && !booleanSupplier.getAsBoolean()) {
				UUID uUID = ((GameProfile)optional.get()).getId();
				ProfileResult profileResult = services.sessionService().fetchProfile(uUID, true);
				return profileResult != null ? Optional.ofNullable(profileResult.profile()) : optional;
			} else {
				return Optional.empty();
			}
		}, Util.backgroundExecutor());
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.saveAdditional(compoundTag, provider);
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
	public void load(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.load(compoundTag, provider);
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
		if (blockState.hasProperty(SkullBlock.POWERED) && (Boolean)blockState.getValue(SkullBlock.POWERED)) {
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
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		return this.saveWithoutMetadata(provider);
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
		LoadingCache<String, CompletableFuture<Optional<GameProfile>>> loadingCache = profileCache;
		return loadingCache != null && Player.isValidUsername(string) ? loadingCache.getUnchecked(string) : CompletableFuture.completedFuture(Optional.empty());
	}

	private static boolean hasTextures(GameProfile gameProfile) {
		return gameProfile.getProperties().containsKey("textures");
	}
}
