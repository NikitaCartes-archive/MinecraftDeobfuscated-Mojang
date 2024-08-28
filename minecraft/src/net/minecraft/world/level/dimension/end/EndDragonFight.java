package net.minecraft.world.level.dimension.end;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.EndFeatures;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockPredicate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public class EndDragonFight {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int MAX_TICKS_BEFORE_DRAGON_RESPAWN = 1200;
	private static final int TIME_BETWEEN_CRYSTAL_SCANS = 100;
	public static final int TIME_BETWEEN_PLAYER_SCANS = 20;
	private static final int ARENA_SIZE_CHUNKS = 8;
	public static final int ARENA_TICKET_LEVEL = 9;
	private static final int GATEWAY_COUNT = 20;
	private static final int GATEWAY_DISTANCE = 96;
	public static final int DRAGON_SPAWN_Y = 128;
	private final Predicate<Entity> validPlayer;
	private final ServerBossEvent dragonEvent = (ServerBossEvent)new ServerBossEvent(
			Component.translatable("entity.minecraft.ender_dragon"), BossEvent.BossBarColor.PINK, BossEvent.BossBarOverlay.PROGRESS
		)
		.setPlayBossMusic(true)
		.setCreateWorldFog(true);
	private final ServerLevel level;
	private final BlockPos origin;
	private final ObjectArrayList<Integer> gateways = new ObjectArrayList<>();
	private final BlockPattern exitPortalPattern;
	private int ticksSinceDragonSeen;
	private int crystalsAlive;
	private int ticksSinceCrystalsScanned;
	private int ticksSinceLastPlayerScan = 21;
	private boolean dragonKilled;
	private boolean previouslyKilled;
	private boolean skipArenaLoadedCheck = false;
	@Nullable
	private UUID dragonUUID;
	private boolean needsStateScanning = true;
	@Nullable
	private BlockPos portalLocation;
	@Nullable
	private DragonRespawnAnimation respawnStage;
	private int respawnTime;
	@Nullable
	private List<EndCrystal> respawnCrystals;

	public EndDragonFight(ServerLevel serverLevel, long l, EndDragonFight.Data data) {
		this(serverLevel, l, data, BlockPos.ZERO);
	}

	public EndDragonFight(ServerLevel serverLevel, long l, EndDragonFight.Data data, BlockPos blockPos) {
		this.level = serverLevel;
		this.origin = blockPos;
		this.validPlayer = EntitySelector.ENTITY_STILL_ALIVE
			.and(EntitySelector.withinDistance((double)blockPos.getX(), (double)(128 + blockPos.getY()), (double)blockPos.getZ(), 192.0));
		this.needsStateScanning = data.needsStateScanning;
		this.dragonUUID = (UUID)data.dragonUUID.orElse(null);
		this.dragonKilled = data.dragonKilled;
		this.previouslyKilled = data.previouslyKilled;
		if (data.isRespawning) {
			this.respawnStage = DragonRespawnAnimation.START;
		}

		this.portalLocation = (BlockPos)data.exitPortalLocation.orElse(null);
		this.gateways.addAll((Collection<? extends Integer>)data.gateways.orElseGet(() -> {
			ObjectArrayList<Integer> objectArrayList = new ObjectArrayList<>(ContiguousSet.create(Range.closedOpen(0, 20), DiscreteDomain.integers()));
			Util.shuffle(objectArrayList, RandomSource.create(l));
			return objectArrayList;
		}));
		this.exitPortalPattern = BlockPatternBuilder.start()
			.aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ")
			.aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ")
			.aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ")
			.aisle("  ###  ", " #   # ", "#     #", "#  #  #", "#     #", " #   # ", "  ###  ")
			.aisle("       ", "  ###  ", " ##### ", " ##### ", " ##### ", "  ###  ", "       ")
			.where('#', BlockInWorld.hasState(BlockPredicate.forBlock(Blocks.BEDROCK)))
			.build();
	}

	@Deprecated
	@VisibleForTesting
	public void skipArenaLoadedCheck() {
		this.skipArenaLoadedCheck = true;
	}

	public EndDragonFight.Data saveData() {
		return new EndDragonFight.Data(
			this.needsStateScanning,
			this.dragonKilled,
			this.previouslyKilled,
			false,
			Optional.ofNullable(this.dragonUUID),
			Optional.ofNullable(this.portalLocation),
			Optional.of(this.gateways)
		);
	}

	public void tick() {
		this.dragonEvent.setVisible(!this.dragonKilled);
		if (++this.ticksSinceLastPlayerScan >= 20) {
			this.updatePlayers();
			this.ticksSinceLastPlayerScan = 0;
		}

		if (!this.dragonEvent.getPlayers().isEmpty()) {
			this.level.getChunkSource().addRegionTicket(TicketType.DRAGON, new ChunkPos(0, 0), 9, Unit.INSTANCE);
			boolean bl = this.isArenaLoaded();
			if (this.needsStateScanning && bl) {
				this.scanState();
				this.needsStateScanning = false;
			}

			if (this.respawnStage != null) {
				if (this.respawnCrystals == null && bl) {
					this.respawnStage = null;
					this.tryRespawn();
				}

				this.respawnStage.tick(this.level, this, this.respawnCrystals, this.respawnTime++, this.portalLocation);
			}

			if (!this.dragonKilled) {
				if ((this.dragonUUID == null || ++this.ticksSinceDragonSeen >= 1200) && bl) {
					this.findOrCreateDragon();
					this.ticksSinceDragonSeen = 0;
				}

				if (++this.ticksSinceCrystalsScanned >= 100 && bl) {
					this.updateCrystalCount();
					this.ticksSinceCrystalsScanned = 0;
				}
			}
		} else {
			this.level.getChunkSource().removeRegionTicket(TicketType.DRAGON, new ChunkPos(0, 0), 9, Unit.INSTANCE);
		}
	}

	private void scanState() {
		LOGGER.info("Scanning for legacy world dragon fight...");
		boolean bl = this.hasActiveExitPortal();
		if (bl) {
			LOGGER.info("Found that the dragon has been killed in this world already.");
			this.previouslyKilled = true;
		} else {
			LOGGER.info("Found that the dragon has not yet been killed in this world.");
			this.previouslyKilled = false;
			if (this.findExitPortal() == null) {
				this.spawnExitPortal(false);
			}
		}

		List<? extends EnderDragon> list = this.level.getDragons();
		if (list.isEmpty()) {
			this.dragonKilled = true;
		} else {
			EnderDragon enderDragon = (EnderDragon)list.get(0);
			this.dragonUUID = enderDragon.getUUID();
			LOGGER.info("Found that there's a dragon still alive ({})", enderDragon);
			this.dragonKilled = false;
			if (!bl) {
				LOGGER.info("But we didn't have a portal, let's remove it.");
				enderDragon.discard();
				this.dragonUUID = null;
			}
		}

		if (!this.previouslyKilled && this.dragonKilled) {
			this.dragonKilled = false;
		}
	}

	private void findOrCreateDragon() {
		List<? extends EnderDragon> list = this.level.getDragons();
		if (list.isEmpty()) {
			LOGGER.debug("Haven't seen the dragon, respawning it");
			this.createNewDragon();
		} else {
			LOGGER.debug("Haven't seen our dragon, but found another one to use.");
			this.dragonUUID = ((EnderDragon)list.get(0)).getUUID();
		}
	}

	protected void setRespawnStage(DragonRespawnAnimation dragonRespawnAnimation) {
		if (this.respawnStage == null) {
			throw new IllegalStateException("Dragon respawn isn't in progress, can't skip ahead in the animation.");
		} else {
			this.respawnTime = 0;
			if (dragonRespawnAnimation == DragonRespawnAnimation.END) {
				this.respawnStage = null;
				this.dragonKilled = false;
				EnderDragon enderDragon = this.createNewDragon();
				if (enderDragon != null) {
					for (ServerPlayer serverPlayer : this.dragonEvent.getPlayers()) {
						CriteriaTriggers.SUMMONED_ENTITY.trigger(serverPlayer, enderDragon);
					}
				}
			} else {
				this.respawnStage = dragonRespawnAnimation;
			}
		}
	}

	private boolean hasActiveExitPortal() {
		for (int i = -8; i <= 8; i++) {
			for (int j = -8; j <= 8; j++) {
				LevelChunk levelChunk = this.level.getChunk(i, j);

				for (BlockEntity blockEntity : levelChunk.getBlockEntities().values()) {
					if (blockEntity instanceof TheEndPortalBlockEntity) {
						return true;
					}
				}
			}
		}

		return false;
	}

	@Nullable
	private BlockPattern.BlockPatternMatch findExitPortal() {
		ChunkPos chunkPos = new ChunkPos(this.origin);

		for (int i = -8 + chunkPos.x; i <= 8 + chunkPos.x; i++) {
			for (int j = -8 + chunkPos.z; j <= 8 + chunkPos.z; j++) {
				LevelChunk levelChunk = this.level.getChunk(i, j);

				for (BlockEntity blockEntity : levelChunk.getBlockEntities().values()) {
					if (blockEntity instanceof TheEndPortalBlockEntity) {
						BlockPattern.BlockPatternMatch blockPatternMatch = this.exitPortalPattern.find(this.level, blockEntity.getBlockPos());
						if (blockPatternMatch != null) {
							BlockPos blockPos = blockPatternMatch.getBlock(3, 3, 3).getPos();
							if (this.portalLocation == null) {
								this.portalLocation = blockPos;
							}

							return blockPatternMatch;
						}
					}
				}
			}
		}

		BlockPos blockPos2 = EndPodiumFeature.getLocation(this.origin);
		int j = this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos2).getY();

		for (int k = j; k >= this.level.getMinY(); k--) {
			BlockPattern.BlockPatternMatch blockPatternMatch2 = this.exitPortalPattern.find(this.level, new BlockPos(blockPos2.getX(), k, blockPos2.getZ()));
			if (blockPatternMatch2 != null) {
				if (this.portalLocation == null) {
					this.portalLocation = blockPatternMatch2.getBlock(3, 3, 3).getPos();
				}

				return blockPatternMatch2;
			}
		}

		return null;
	}

	private boolean isArenaLoaded() {
		if (this.skipArenaLoadedCheck) {
			return true;
		} else {
			ChunkPos chunkPos = new ChunkPos(this.origin);

			for (int i = -8 + chunkPos.x; i <= 8 + chunkPos.x; i++) {
				for (int j = 8 + chunkPos.z; j <= 8 + chunkPos.z; j++) {
					ChunkAccess chunkAccess = this.level.getChunk(i, j, ChunkStatus.FULL, false);
					if (!(chunkAccess instanceof LevelChunk)) {
						return false;
					}

					FullChunkStatus fullChunkStatus = ((LevelChunk)chunkAccess).getFullStatus();
					if (!fullChunkStatus.isOrAfter(FullChunkStatus.BLOCK_TICKING)) {
						return false;
					}
				}
			}

			return true;
		}
	}

	private void updatePlayers() {
		Set<ServerPlayer> set = Sets.<ServerPlayer>newHashSet();

		for (ServerPlayer serverPlayer : this.level.getPlayers(this.validPlayer)) {
			this.dragonEvent.addPlayer(serverPlayer);
			set.add(serverPlayer);
		}

		Set<ServerPlayer> set2 = Sets.<ServerPlayer>newHashSet(this.dragonEvent.getPlayers());
		set2.removeAll(set);

		for (ServerPlayer serverPlayer2 : set2) {
			this.dragonEvent.removePlayer(serverPlayer2);
		}
	}

	private void updateCrystalCount() {
		this.ticksSinceCrystalsScanned = 0;
		this.crystalsAlive = 0;

		for (SpikeFeature.EndSpike endSpike : SpikeFeature.getSpikesForLevel(this.level)) {
			this.crystalsAlive = this.crystalsAlive + this.level.getEntitiesOfClass(EndCrystal.class, endSpike.getTopBoundingBox()).size();
		}

		LOGGER.debug("Found {} end crystals still alive", this.crystalsAlive);
	}

	public void setDragonKilled(EnderDragon enderDragon) {
		if (enderDragon.getUUID().equals(this.dragonUUID)) {
			this.dragonEvent.setProgress(0.0F);
			this.dragonEvent.setVisible(false);
			this.spawnExitPortal(true);
			this.spawnNewGateway();
			if (!this.previouslyKilled) {
				this.level
					.setBlockAndUpdate(
						this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, EndPodiumFeature.getLocation(this.origin)), Blocks.DRAGON_EGG.defaultBlockState()
					);
			}

			this.previouslyKilled = true;
			this.dragonKilled = true;
		}
	}

	@Deprecated
	@VisibleForTesting
	public void removeAllGateways() {
		this.gateways.clear();
	}

	private void spawnNewGateway() {
		if (!this.gateways.isEmpty()) {
			int i = this.gateways.remove(this.gateways.size() - 1);
			int j = Mth.floor(96.0 * Math.cos(2.0 * (-Math.PI + (Math.PI / 20) * (double)i)));
			int k = Mth.floor(96.0 * Math.sin(2.0 * (-Math.PI + (Math.PI / 20) * (double)i)));
			this.spawnNewGateway(new BlockPos(j, 75, k));
		}
	}

	private void spawnNewGateway(BlockPos blockPos) {
		this.level.levelEvent(3000, blockPos, 0);
		this.level
			.registryAccess()
			.lookup(Registries.CONFIGURED_FEATURE)
			.flatMap(registry -> registry.get(EndFeatures.END_GATEWAY_DELAYED))
			.ifPresent(
				reference -> ((ConfiguredFeature)reference.value()).place(this.level, this.level.getChunkSource().getGenerator(), RandomSource.create(), blockPos)
			);
	}

	private void spawnExitPortal(boolean bl) {
		EndPodiumFeature endPodiumFeature = new EndPodiumFeature(bl);
		if (this.portalLocation == null) {
			this.portalLocation = this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.getLocation(this.origin)).below();

			while (this.level.getBlockState(this.portalLocation).is(Blocks.BEDROCK) && this.portalLocation.getY() > 63) {
				this.portalLocation = this.portalLocation.below();
			}
		}

		if (endPodiumFeature.place(FeatureConfiguration.NONE, this.level, this.level.getChunkSource().getGenerator(), RandomSource.create(), this.portalLocation)) {
			int i = Mth.positiveCeilDiv(4, 16);
			this.level.getChunkSource().chunkMap.waitForLightBeforeSending(new ChunkPos(this.portalLocation), i);
		}
	}

	@Nullable
	private EnderDragon createNewDragon() {
		this.level.getChunkAt(new BlockPos(this.origin.getX(), 128 + this.origin.getY(), this.origin.getZ()));
		EnderDragon enderDragon = EntityType.ENDER_DRAGON.create(this.level, EntitySpawnReason.EVENT);
		if (enderDragon != null) {
			enderDragon.setDragonFight(this);
			enderDragon.setFightOrigin(this.origin);
			enderDragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
			enderDragon.moveTo((double)this.origin.getX(), (double)(128 + this.origin.getY()), (double)this.origin.getZ(), this.level.random.nextFloat() * 360.0F, 0.0F);
			this.level.addFreshEntity(enderDragon);
			this.dragonUUID = enderDragon.getUUID();
		}

		return enderDragon;
	}

	public void updateDragon(EnderDragon enderDragon) {
		if (enderDragon.getUUID().equals(this.dragonUUID)) {
			this.dragonEvent.setProgress(enderDragon.getHealth() / enderDragon.getMaxHealth());
			this.ticksSinceDragonSeen = 0;
			if (enderDragon.hasCustomName()) {
				this.dragonEvent.setName(enderDragon.getDisplayName());
			}
		}
	}

	public int getCrystalsAlive() {
		return this.crystalsAlive;
	}

	public void onCrystalDestroyed(EndCrystal endCrystal, DamageSource damageSource) {
		if (this.respawnStage != null && this.respawnCrystals.contains(endCrystal)) {
			LOGGER.debug("Aborting respawn sequence");
			this.respawnStage = null;
			this.respawnTime = 0;
			this.resetSpikeCrystals();
			this.spawnExitPortal(true);
		} else {
			this.updateCrystalCount();
			Entity entity = this.level.getEntity(this.dragonUUID);
			if (entity instanceof EnderDragon) {
				((EnderDragon)entity).onCrystalDestroyed(endCrystal, endCrystal.blockPosition(), damageSource);
			}
		}
	}

	public boolean hasPreviouslyKilledDragon() {
		return this.previouslyKilled;
	}

	public void tryRespawn() {
		if (this.dragonKilled && this.respawnStage == null) {
			BlockPos blockPos = this.portalLocation;
			if (blockPos == null) {
				LOGGER.debug("Tried to respawn, but need to find the portal first.");
				BlockPattern.BlockPatternMatch blockPatternMatch = this.findExitPortal();
				if (blockPatternMatch == null) {
					LOGGER.debug("Couldn't find a portal, so we made one.");
					this.spawnExitPortal(true);
				} else {
					LOGGER.debug("Found the exit portal & saved its location for next time.");
				}

				blockPos = this.portalLocation;
			}

			List<EndCrystal> list = Lists.<EndCrystal>newArrayList();
			BlockPos blockPos2 = blockPos.above(1);

			for (Direction direction : Direction.Plane.HORIZONTAL) {
				List<EndCrystal> list2 = this.level.getEntitiesOfClass(EndCrystal.class, new AABB(blockPos2.relative(direction, 2)));
				if (list2.isEmpty()) {
					return;
				}

				list.addAll(list2);
			}

			LOGGER.debug("Found all crystals, respawning dragon.");
			this.respawnDragon(list);
		}
	}

	private void respawnDragon(List<EndCrystal> list) {
		if (this.dragonKilled && this.respawnStage == null) {
			for (BlockPattern.BlockPatternMatch blockPatternMatch = this.findExitPortal(); blockPatternMatch != null; blockPatternMatch = this.findExitPortal()) {
				for (int i = 0; i < this.exitPortalPattern.getWidth(); i++) {
					for (int j = 0; j < this.exitPortalPattern.getHeight(); j++) {
						for (int k = 0; k < this.exitPortalPattern.getDepth(); k++) {
							BlockInWorld blockInWorld = blockPatternMatch.getBlock(i, j, k);
							if (blockInWorld.getState().is(Blocks.BEDROCK) || blockInWorld.getState().is(Blocks.END_PORTAL)) {
								this.level.setBlockAndUpdate(blockInWorld.getPos(), Blocks.END_STONE.defaultBlockState());
							}
						}
					}
				}
			}

			this.respawnStage = DragonRespawnAnimation.START;
			this.respawnTime = 0;
			this.spawnExitPortal(false);
			this.respawnCrystals = list;
		}
	}

	public void resetSpikeCrystals() {
		for (SpikeFeature.EndSpike endSpike : SpikeFeature.getSpikesForLevel(this.level)) {
			for (EndCrystal endCrystal : this.level.getEntitiesOfClass(EndCrystal.class, endSpike.getTopBoundingBox())) {
				endCrystal.setInvulnerable(false);
				endCrystal.setBeamTarget(null);
			}
		}
	}

	@Nullable
	public UUID getDragonUUID() {
		return this.dragonUUID;
	}

	public static record Data(
		boolean needsStateScanning,
		boolean dragonKilled,
		boolean previouslyKilled,
		boolean isRespawning,
		Optional<UUID> dragonUUID,
		Optional<BlockPos> exitPortalLocation,
		Optional<List<Integer>> gateways
	) {
		public static final Codec<EndDragonFight.Data> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.BOOL.fieldOf("NeedsStateScanning").orElse(true).forGetter(EndDragonFight.Data::needsStateScanning),
						Codec.BOOL.fieldOf("DragonKilled").orElse(false).forGetter(EndDragonFight.Data::dragonKilled),
						Codec.BOOL.fieldOf("PreviouslyKilled").orElse(false).forGetter(EndDragonFight.Data::previouslyKilled),
						Codec.BOOL.lenientOptionalFieldOf("IsRespawning", Boolean.valueOf(false)).forGetter(EndDragonFight.Data::isRespawning),
						UUIDUtil.CODEC.lenientOptionalFieldOf("Dragon").forGetter(EndDragonFight.Data::dragonUUID),
						BlockPos.CODEC.lenientOptionalFieldOf("ExitPortalLocation").forGetter(EndDragonFight.Data::exitPortalLocation),
						Codec.list(Codec.INT).lenientOptionalFieldOf("Gateways").forGetter(EndDragonFight.Data::gateways)
					)
					.apply(instance, EndDragonFight.Data::new)
		);
		public static final EndDragonFight.Data DEFAULT = new EndDragonFight.Data(true, false, false, false, Optional.empty(), Optional.empty(), Optional.empty());
	}
}
