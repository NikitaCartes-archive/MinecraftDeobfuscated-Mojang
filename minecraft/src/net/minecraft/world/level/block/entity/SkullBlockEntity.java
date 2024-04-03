package net.minecraft.world.level.block.entity;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.logging.LogUtils;
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
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Services;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class SkullBlockEntity extends BlockEntity {
	private static final String TAG_PROFILE = "profile";
	private static final String TAG_NOTE_BLOCK_SOUND = "note_block_sound";
	private static final String TAG_CUSTOM_NAME = "custom_name";
	private static final Logger LOGGER = LogUtils.getLogger();
	@Nullable
	private static Executor mainThreadExecutor;
	@Nullable
	private static LoadingCache<String, CompletableFuture<Optional<GameProfile>>> profileCacheByName;
	@Nullable
	private static LoadingCache<UUID, CompletableFuture<Optional<GameProfile>>> profileCacheById;
	public static final Executor CHECKED_MAIN_THREAD_EXECUTOR = runnable -> {
		Executor executor = mainThreadExecutor;
		if (executor != null) {
			executor.execute(runnable);
		}
	};
	@Nullable
	private ResolvableProfile owner;
	@Nullable
	private ResourceLocation noteBlockSound;
	private int animationTickCount;
	private boolean isAnimating;
	@Nullable
	private Component customName;

	public SkullBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.SKULL, blockPos, blockState);
	}

	public static void setup(Services services, Executor executor) {
		mainThreadExecutor = executor;
		final BooleanSupplier booleanSupplier = () -> profileCacheById == null;
		profileCacheByName = CacheBuilder.newBuilder()
			.expireAfterAccess(Duration.ofMinutes(10L))
			.maximumSize(256L)
			.build(new CacheLoader<String, CompletableFuture<Optional<GameProfile>>>() {
				public CompletableFuture<Optional<GameProfile>> load(String string) {
					return SkullBlockEntity.fetchProfileByName(string, services);
				}
			});
		profileCacheById = CacheBuilder.newBuilder()
			.expireAfterAccess(Duration.ofMinutes(10L))
			.maximumSize(256L)
			.build(new CacheLoader<UUID, CompletableFuture<Optional<GameProfile>>>() {
				public CompletableFuture<Optional<GameProfile>> load(UUID uUID) {
					return SkullBlockEntity.fetchProfileById(uUID, services, booleanSupplier);
				}
			});
	}

	static CompletableFuture<Optional<GameProfile>> fetchProfileByName(String string, Services services) {
		return services.profileCache()
			.getAsync(string)
			.thenCompose(
				optional -> {
					LoadingCache<UUID, CompletableFuture<Optional<GameProfile>>> loadingCache = profileCacheById;
					return loadingCache != null && !optional.isEmpty()
						? loadingCache.getUnchecked(((GameProfile)optional.get()).getId()).thenApply(optional2 -> optional2.or(() -> optional))
						: CompletableFuture.completedFuture(Optional.empty());
				}
			);
	}

	static CompletableFuture<Optional<GameProfile>> fetchProfileById(UUID uUID, Services services, BooleanSupplier booleanSupplier) {
		return CompletableFuture.supplyAsync(() -> {
			if (booleanSupplier.getAsBoolean()) {
				return Optional.empty();
			} else {
				ProfileResult profileResult = services.sessionService().fetchProfile(uUID, true);
				return Optional.ofNullable(profileResult).map(ProfileResult::profile);
			}
		}, Util.backgroundExecutor());
	}

	public static void clear() {
		mainThreadExecutor = null;
		profileCacheByName = null;
		profileCacheById = null;
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.saveAdditional(compoundTag, provider);
		if (this.owner != null) {
			compoundTag.put("profile", ResolvableProfile.CODEC.encodeStart(NbtOps.INSTANCE, this.owner).getOrThrow());
		}

		if (this.noteBlockSound != null) {
			compoundTag.putString("note_block_sound", this.noteBlockSound.toString());
		}

		if (this.customName != null) {
			compoundTag.putString("custom_name", Component.Serializer.toJson(this.customName, provider));
		}
	}

	@Override
	protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.loadAdditional(compoundTag, provider);
		if (compoundTag.contains("profile")) {
			ResolvableProfile.CODEC
				.parse(NbtOps.INSTANCE, compoundTag.get("profile"))
				.resultOrPartial(string -> LOGGER.error("Failed to load profile from player head: {}", string))
				.ifPresent(this::setOwner);
		}

		if (compoundTag.contains("note_block_sound", 8)) {
			this.noteBlockSound = ResourceLocation.tryParse(compoundTag.getString("note_block_sound"));
		}

		if (compoundTag.contains("custom_name", 8)) {
			this.customName = Component.Serializer.fromJson(compoundTag.getString("custom_name"), provider);
		} else {
			this.customName = null;
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
	public ResolvableProfile getOwnerProfile() {
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
		return this.saveCustomOnly(provider);
	}

	public void setOwner(@Nullable ResolvableProfile resolvableProfile) {
		synchronized (this) {
			this.owner = resolvableProfile;
		}

		this.updateOwnerProfile();
	}

	private void updateOwnerProfile() {
		if (this.owner != null && !this.owner.isResolved()) {
			this.owner.resolve().thenAcceptAsync(resolvableProfile -> {
				this.owner = resolvableProfile;
				this.setChanged();
			}, CHECKED_MAIN_THREAD_EXECUTOR);
		} else {
			this.setChanged();
		}
	}

	public static CompletableFuture<Optional<GameProfile>> fetchGameProfile(String string) {
		LoadingCache<String, CompletableFuture<Optional<GameProfile>>> loadingCache = profileCacheByName;
		return loadingCache != null && StringUtil.isValidPlayerName(string) ? loadingCache.getUnchecked(string) : CompletableFuture.completedFuture(Optional.empty());
	}

	public static CompletableFuture<Optional<GameProfile>> fetchGameProfile(UUID uUID) {
		LoadingCache<UUID, CompletableFuture<Optional<GameProfile>>> loadingCache = profileCacheById;
		return loadingCache != null ? loadingCache.getUnchecked(uUID) : CompletableFuture.completedFuture(Optional.empty());
	}

	@Override
	protected void applyImplicitComponents(BlockEntity.DataComponentInput dataComponentInput) {
		super.applyImplicitComponents(dataComponentInput);
		this.setOwner(dataComponentInput.get(DataComponents.PROFILE));
		this.noteBlockSound = dataComponentInput.get(DataComponents.NOTE_BLOCK_SOUND);
		this.customName = dataComponentInput.get(DataComponents.CUSTOM_NAME);
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder builder) {
		super.collectImplicitComponents(builder);
		builder.set(DataComponents.PROFILE, this.owner);
		builder.set(DataComponents.NOTE_BLOCK_SOUND, this.noteBlockSound);
		builder.set(DataComponents.CUSTOM_NAME, this.customName);
	}

	@Override
	public void removeComponentsFromTag(CompoundTag compoundTag) {
		super.removeComponentsFromTag(compoundTag);
		compoundTag.remove("profile");
		compoundTag.remove("note_block_sound");
		compoundTag.remove("custom_name");
	}
}
