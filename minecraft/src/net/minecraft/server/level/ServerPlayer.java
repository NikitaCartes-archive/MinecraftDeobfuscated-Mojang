package net.minecraft.server.level;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundHorseScreenOpenPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ComplexItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ServerItemCooldowns;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerPlayer extends Player implements ContainerListener {
	private static final Logger LOGGER = LogManager.getLogger();
	private String language = "en_US";
	public ServerGamePacketListenerImpl connection;
	public final MinecraftServer server;
	public final ServerPlayerGameMode gameMode;
	private final List<Integer> entitiesToRemove = Lists.<Integer>newLinkedList();
	private final PlayerAdvancements advancements;
	private final ServerStatsCounter stats;
	private float lastRecordedHealthAndAbsorption = Float.MIN_VALUE;
	private int lastRecordedFoodLevel = Integer.MIN_VALUE;
	private int lastRecordedAirLevel = Integer.MIN_VALUE;
	private int lastRecordedArmor = Integer.MIN_VALUE;
	private int lastRecordedLevel = Integer.MIN_VALUE;
	private int lastRecordedExperience = Integer.MIN_VALUE;
	private float lastSentHealth = -1.0E8F;
	private int lastSentFood = -99999999;
	private boolean lastFoodSaturationZero = true;
	private int lastSentExp = -99999999;
	private int spawnInvulnerableTime = 60;
	private ChatVisiblity chatVisibility;
	private boolean canChatColor = true;
	private long lastActionTime = Util.getMillis();
	private Entity camera;
	private boolean isChangingDimension;
	private boolean seenCredits;
	private final ServerRecipeBook recipeBook;
	private Vec3 levitationStartPos;
	private int levitationStartTime;
	private boolean disconnected;
	@Nullable
	private Vec3 enteredNetherPosition;
	private SectionPos lastSectionPos = SectionPos.of(0, 0, 0);
	private int containerCounter;
	public boolean ignoreSlotUpdateHack;
	public int latency;
	public boolean wonGame;

	public ServerPlayer(MinecraftServer minecraftServer, ServerLevel serverLevel, GameProfile gameProfile, ServerPlayerGameMode serverPlayerGameMode) {
		super(serverLevel, gameProfile);
		serverPlayerGameMode.player = this;
		this.gameMode = serverPlayerGameMode;
		this.server = minecraftServer;
		this.recipeBook = new ServerRecipeBook(minecraftServer.getRecipeManager());
		this.stats = minecraftServer.getPlayerList().getPlayerStats(this);
		this.advancements = minecraftServer.getPlayerList().getPlayerAdvancements(this);
		this.maxUpStep = 1.0F;
		this.fudgeSpawnLocation(serverLevel);
	}

	private void fudgeSpawnLocation(ServerLevel serverLevel) {
		BlockPos blockPos = serverLevel.getSharedSpawnPos();
		if (serverLevel.dimension.isHasSkyLight() && serverLevel.getLevelData().getGameType() != GameType.ADVENTURE) {
			int i = Math.max(0, this.server.getSpawnRadius(serverLevel));
			int j = Mth.floor(serverLevel.getWorldBorder().getDistanceToBorder((double)blockPos.getX(), (double)blockPos.getZ()));
			if (j < i) {
				i = j;
			}

			if (j <= 1) {
				i = 1;
			}

			long l = (long)(i * 2 + 1);
			long m = l * l;
			int k = m > 2147483647L ? Integer.MAX_VALUE : (int)m;
			int n = this.getCoprime(k);
			int o = new Random().nextInt(k);

			for (int p = 0; p < k; p++) {
				int q = (o + n * p) % k;
				int r = q % (i * 2 + 1);
				int s = q / (i * 2 + 1);
				BlockPos blockPos2 = serverLevel.getDimension().getValidSpawnPosition(blockPos.getX() + r - i, blockPos.getZ() + s - i, false);
				if (blockPos2 != null) {
					this.moveTo(blockPos2, 0.0F, 0.0F);
					if (serverLevel.noCollision(this)) {
						break;
					}
				}
			}
		} else {
			this.moveTo(blockPos, 0.0F, 0.0F);

			while (!serverLevel.noCollision(this) && this.getY() < 255.0) {
				this.setPos(this.getX(), this.getY() + 1.0, this.getZ());
			}
		}
	}

	private int getCoprime(int i) {
		return i <= 16 ? i - 1 : 17;
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		if (compoundTag.contains("playerGameType", 99)) {
			if (this.getServer().getForceGameType()) {
				this.gameMode.setGameModeForPlayer(this.getServer().getDefaultGameType());
			} else {
				this.gameMode.setGameModeForPlayer(GameType.byId(compoundTag.getInt("playerGameType")));
			}
		}

		if (compoundTag.contains("enteredNetherPosition", 10)) {
			CompoundTag compoundTag2 = compoundTag.getCompound("enteredNetherPosition");
			this.enteredNetherPosition = new Vec3(compoundTag2.getDouble("x"), compoundTag2.getDouble("y"), compoundTag2.getDouble("z"));
		}

		this.seenCredits = compoundTag.getBoolean("seenCredits");
		if (compoundTag.contains("recipeBook", 10)) {
			this.recipeBook.fromNbt(compoundTag.getCompound("recipeBook"));
		}

		if (this.isSleeping()) {
			this.stopSleeping();
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("playerGameType", this.gameMode.getGameModeForPlayer().getId());
		compoundTag.putBoolean("seenCredits", this.seenCredits);
		if (this.enteredNetherPosition != null) {
			CompoundTag compoundTag2 = new CompoundTag();
			compoundTag2.putDouble("x", this.enteredNetherPosition.x);
			compoundTag2.putDouble("y", this.enteredNetherPosition.y);
			compoundTag2.putDouble("z", this.enteredNetherPosition.z);
			compoundTag.put("enteredNetherPosition", compoundTag2);
		}

		Entity entity = this.getRootVehicle();
		Entity entity2 = this.getVehicle();
		if (entity2 != null && entity != this && entity.hasOnePlayerPassenger()) {
			CompoundTag compoundTag3 = new CompoundTag();
			CompoundTag compoundTag4 = new CompoundTag();
			entity.save(compoundTag4);
			compoundTag3.putUUID("Attach", entity2.getUUID());
			compoundTag3.put("Entity", compoundTag4);
			compoundTag.put("RootVehicle", compoundTag3);
		}

		compoundTag.put("recipeBook", this.recipeBook.toNbt());
	}

	public void setExperiencePoints(int i) {
		float f = (float)this.getXpNeededForNextLevel();
		float g = (f - 1.0F) / f;
		this.experienceProgress = Mth.clamp((float)i / f, 0.0F, g);
		this.lastSentExp = -1;
	}

	public void setExperienceLevels(int i) {
		this.experienceLevel = i;
		this.lastSentExp = -1;
	}

	@Override
	public void giveExperienceLevels(int i) {
		super.giveExperienceLevels(i);
		this.lastSentExp = -1;
	}

	@Override
	public void onEnchantmentPerformed(ItemStack itemStack, int i) {
		super.onEnchantmentPerformed(itemStack, i);
		this.lastSentExp = -1;
	}

	public void initMenu() {
		this.containerMenu.addSlotListener(this);
	}

	@Override
	public void onEnterCombat() {
		super.onEnterCombat();
		this.connection.send(new ClientboundPlayerCombatPacket(this.getCombatTracker(), ClientboundPlayerCombatPacket.Event.ENTER_COMBAT));
	}

	@Override
	public void onLeaveCombat() {
		super.onLeaveCombat();
		this.connection.send(new ClientboundPlayerCombatPacket(this.getCombatTracker(), ClientboundPlayerCombatPacket.Event.END_COMBAT));
	}

	@Override
	protected void onInsideBlock(BlockState blockState) {
		CriteriaTriggers.ENTER_BLOCK.trigger(this, blockState);
	}

	@Override
	protected ItemCooldowns createItemCooldowns() {
		return new ServerItemCooldowns(this);
	}

	@Override
	public void tick() {
		this.gameMode.tick();
		this.spawnInvulnerableTime--;
		if (this.invulnerableTime > 0) {
			this.invulnerableTime--;
		}

		this.containerMenu.broadcastChanges();
		if (!this.level.isClientSide && !this.containerMenu.stillValid(this)) {
			this.closeContainer();
			this.containerMenu = this.inventoryMenu;
		}

		while (!this.entitiesToRemove.isEmpty()) {
			int i = Math.min(this.entitiesToRemove.size(), Integer.MAX_VALUE);
			int[] is = new int[i];
			Iterator<Integer> iterator = this.entitiesToRemove.iterator();
			int j = 0;

			while (iterator.hasNext() && j < i) {
				is[j++] = (Integer)iterator.next();
				iterator.remove();
			}

			this.connection.send(new ClientboundRemoveEntitiesPacket(is));
		}

		Entity entity = this.getCamera();
		if (entity != this) {
			if (entity.isAlive()) {
				this.absMoveTo(entity.getX(), entity.getY(), entity.getZ(), entity.yRot, entity.xRot);
				this.getLevel().getChunkSource().move(this);
				if (this.wantsToStopRiding()) {
					this.setCamera(this);
				}
			} else {
				this.setCamera(this);
			}
		}

		CriteriaTriggers.TICK.trigger(this);
		if (this.levitationStartPos != null) {
			CriteriaTriggers.LEVITATION.trigger(this, this.levitationStartPos, this.tickCount - this.levitationStartTime);
		}

		this.advancements.flushDirty(this);
	}

	public void doTick() {
		try {
			if (!this.isSpectator() || this.level.hasChunkAt(new BlockPos(this))) {
				super.tick();
			}

			for (int i = 0; i < this.inventory.getContainerSize(); i++) {
				ItemStack itemStack = this.inventory.getItem(i);
				if (itemStack.getItem().isComplex()) {
					Packet<?> packet = ((ComplexItem)itemStack.getItem()).getUpdatePacket(itemStack, this.level, this);
					if (packet != null) {
						this.connection.send(packet);
					}
				}
			}

			if (this.getHealth() != this.lastSentHealth
				|| this.lastSentFood != this.foodData.getFoodLevel()
				|| this.foodData.getSaturationLevel() == 0.0F != this.lastFoodSaturationZero) {
				this.connection.send(new ClientboundSetHealthPacket(this.getHealth(), this.foodData.getFoodLevel(), this.foodData.getSaturationLevel()));
				this.lastSentHealth = this.getHealth();
				this.lastSentFood = this.foodData.getFoodLevel();
				this.lastFoodSaturationZero = this.foodData.getSaturationLevel() == 0.0F;
			}

			if (this.getHealth() + this.getAbsorptionAmount() != this.lastRecordedHealthAndAbsorption) {
				this.lastRecordedHealthAndAbsorption = this.getHealth() + this.getAbsorptionAmount();
				this.updateScoreForCriteria(ObjectiveCriteria.HEALTH, Mth.ceil(this.lastRecordedHealthAndAbsorption));
			}

			if (this.foodData.getFoodLevel() != this.lastRecordedFoodLevel) {
				this.lastRecordedFoodLevel = this.foodData.getFoodLevel();
				this.updateScoreForCriteria(ObjectiveCriteria.FOOD, Mth.ceil((float)this.lastRecordedFoodLevel));
			}

			if (this.getAirSupply() != this.lastRecordedAirLevel) {
				this.lastRecordedAirLevel = this.getAirSupply();
				this.updateScoreForCriteria(ObjectiveCriteria.AIR, Mth.ceil((float)this.lastRecordedAirLevel));
			}

			if (this.getArmorValue() != this.lastRecordedArmor) {
				this.lastRecordedArmor = this.getArmorValue();
				this.updateScoreForCriteria(ObjectiveCriteria.ARMOR, Mth.ceil((float)this.lastRecordedArmor));
			}

			if (this.totalExperience != this.lastRecordedExperience) {
				this.lastRecordedExperience = this.totalExperience;
				this.updateScoreForCriteria(ObjectiveCriteria.EXPERIENCE, Mth.ceil((float)this.lastRecordedExperience));
			}

			if (this.experienceLevel != this.lastRecordedLevel) {
				this.lastRecordedLevel = this.experienceLevel;
				this.updateScoreForCriteria(ObjectiveCriteria.LEVEL, Mth.ceil((float)this.lastRecordedLevel));
			}

			if (this.totalExperience != this.lastSentExp) {
				this.lastSentExp = this.totalExperience;
				this.connection.send(new ClientboundSetExperiencePacket(this.experienceProgress, this.totalExperience, this.experienceLevel));
			}

			if (this.tickCount % 20 == 0) {
				CriteriaTriggers.LOCATION.trigger(this);
			}
		} catch (Throwable var4) {
			CrashReport crashReport = CrashReport.forThrowable(var4, "Ticking player");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Player being ticked");
			this.fillCrashReportCategory(crashReportCategory);
			throw new ReportedException(crashReport);
		}
	}

	private void updateScoreForCriteria(ObjectiveCriteria objectiveCriteria, int i) {
		this.getScoreboard().forAllObjectives(objectiveCriteria, this.getScoreboardName(), score -> score.setScore(i));
	}

	@Override
	public void die(DamageSource damageSource) {
		boolean bl = this.level.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES);
		if (bl) {
			Component component = this.getCombatTracker().getDeathMessage();
			this.connection
				.send(
					new ClientboundPlayerCombatPacket(this.getCombatTracker(), ClientboundPlayerCombatPacket.Event.ENTITY_DIED, component),
					future -> {
						if (!future.isSuccess()) {
							int i = 256;
							String string = component.getString(256);
							Component component2 = new TranslatableComponent("death.attack.message_too_long", new TextComponent(string).withStyle(ChatFormatting.YELLOW));
							Component component3 = new TranslatableComponent("death.attack.even_more_magic", this.getDisplayName())
								.withStyle(style -> style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, component2)));
							this.connection.send(new ClientboundPlayerCombatPacket(this.getCombatTracker(), ClientboundPlayerCombatPacket.Event.ENTITY_DIED, component3));
						}
					}
				);
			Team team = this.getTeam();
			if (team == null || team.getDeathMessageVisibility() == Team.Visibility.ALWAYS) {
				this.server.getPlayerList().broadcastMessage(component);
			} else if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OTHER_TEAMS) {
				this.server.getPlayerList().broadcastToTeam(this, component);
			} else if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OWN_TEAM) {
				this.server.getPlayerList().broadcastToAllExceptTeam(this, component);
			}
		} else {
			this.connection.send(new ClientboundPlayerCombatPacket(this.getCombatTracker(), ClientboundPlayerCombatPacket.Event.ENTITY_DIED));
		}

		this.removeEntitiesOnShoulder();
		if (!this.isSpectator()) {
			this.dropAllDeathLoot(damageSource);
		}

		this.getScoreboard().forAllObjectives(ObjectiveCriteria.DEATH_COUNT, this.getScoreboardName(), Score::increment);
		LivingEntity livingEntity = this.getKillCredit();
		if (livingEntity != null) {
			this.awardStat(Stats.ENTITY_KILLED_BY.get(livingEntity.getType()));
			livingEntity.awardKillScore(this, this.deathScore, damageSource);
			this.createWitherRose(livingEntity);
		}

		this.level.broadcastEntityEvent(this, (byte)3);
		this.awardStat(Stats.DEATHS);
		this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH));
		this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
		this.clearFire();
		this.setSharedFlag(0, false);
		this.getCombatTracker().recheckStatus();
	}

	@Override
	public void awardKillScore(Entity entity, int i, DamageSource damageSource) {
		if (entity != this) {
			super.awardKillScore(entity, i, damageSource);
			this.increaseScore(i);
			String string = this.getScoreboardName();
			String string2 = entity.getScoreboardName();
			this.getScoreboard().forAllObjectives(ObjectiveCriteria.KILL_COUNT_ALL, string, Score::increment);
			if (entity instanceof Player) {
				this.awardStat(Stats.PLAYER_KILLS);
				this.getScoreboard().forAllObjectives(ObjectiveCriteria.KILL_COUNT_PLAYERS, string, Score::increment);
			} else {
				this.awardStat(Stats.MOB_KILLS);
			}

			this.handleTeamKill(string, string2, ObjectiveCriteria.TEAM_KILL);
			this.handleTeamKill(string2, string, ObjectiveCriteria.KILLED_BY_TEAM);
			CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(this, entity, damageSource);
		}
	}

	private void handleTeamKill(String string, String string2, ObjectiveCriteria[] objectiveCriterias) {
		PlayerTeam playerTeam = this.getScoreboard().getPlayersTeam(string2);
		if (playerTeam != null) {
			int i = playerTeam.getColor().getId();
			if (i >= 0 && i < objectiveCriterias.length) {
				this.getScoreboard().forAllObjectives(objectiveCriterias[i], string, Score::increment);
			}
		}
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (this.isInvulnerableTo(damageSource)) {
			return false;
		} else {
			boolean bl = this.server.isDedicatedServer() && this.isPvpAllowed() && "fall".equals(damageSource.msgId);
			if (!bl && this.spawnInvulnerableTime > 0 && damageSource != DamageSource.OUT_OF_WORLD) {
				return false;
			} else {
				if (damageSource instanceof EntityDamageSource) {
					Entity entity = damageSource.getEntity();
					if (entity instanceof Player && !this.canHarmPlayer((Player)entity)) {
						return false;
					}

					if (entity instanceof AbstractArrow) {
						AbstractArrow abstractArrow = (AbstractArrow)entity;
						Entity entity2 = abstractArrow.getOwner();
						if (entity2 instanceof Player && !this.canHarmPlayer((Player)entity2)) {
							return false;
						}
					}
				}

				return super.hurt(damageSource, f);
			}
		}
	}

	@Override
	public boolean canHarmPlayer(Player player) {
		return !this.isPvpAllowed() ? false : super.canHarmPlayer(player);
	}

	private boolean isPvpAllowed() {
		return this.server.isPvpAllowed();
	}

	@Nullable
	@Override
	public Entity changeDimension(DimensionType dimensionType) {
		this.isChangingDimension = true;
		DimensionType dimensionType2 = this.dimension;
		if (dimensionType2 == DimensionType.THE_END && dimensionType == DimensionType.OVERWORLD) {
			this.unRide();
			this.getLevel().removePlayerImmediately(this);
			if (!this.wonGame) {
				this.wonGame = true;
				this.connection.send(new ClientboundGameEventPacket(4, this.seenCredits ? 0.0F : 1.0F));
				this.seenCredits = true;
			}

			return this;
		} else {
			ServerLevel serverLevel = this.server.getLevel(dimensionType2);
			this.dimension = dimensionType;
			ServerLevel serverLevel2 = this.server.getLevel(dimensionType);
			LevelData levelData = serverLevel2.getLevelData();
			this.connection
				.send(
					new ClientboundRespawnPacket(
						dimensionType, LevelData.obfuscateSeed(levelData.getSeed()), levelData.getGeneratorType(), this.gameMode.getGameModeForPlayer()
					)
				);
			this.connection.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));
			PlayerList playerList = this.server.getPlayerList();
			playerList.sendPlayerPermissionLevel(this);
			serverLevel.removePlayerImmediately(this);
			this.removed = false;
			double d = this.getX();
			double e = this.getY();
			double f = this.getZ();
			float g = this.xRot;
			float h = this.yRot;
			double i = 8.0;
			float j = h;
			serverLevel.getProfiler().push("moving");
			if (dimensionType2 == DimensionType.OVERWORLD && dimensionType == DimensionType.NETHER) {
				this.enteredNetherPosition = this.position();
				d /= 8.0;
				f /= 8.0;
			} else if (dimensionType2 == DimensionType.NETHER && dimensionType == DimensionType.OVERWORLD) {
				d *= 8.0;
				f *= 8.0;
			} else if (dimensionType2 == DimensionType.OVERWORLD && dimensionType == DimensionType.THE_END) {
				BlockPos blockPos = serverLevel2.getDimensionSpecificSpawn();
				d = (double)blockPos.getX();
				e = (double)blockPos.getY();
				f = (double)blockPos.getZ();
				h = 90.0F;
				g = 0.0F;
			}

			this.moveTo(d, e, f, h, g);
			serverLevel.getProfiler().pop();
			serverLevel.getProfiler().push("placing");
			double k = Math.min(-2.9999872E7, serverLevel2.getWorldBorder().getMinX() + 16.0);
			double l = Math.min(-2.9999872E7, serverLevel2.getWorldBorder().getMinZ() + 16.0);
			double m = Math.min(2.9999872E7, serverLevel2.getWorldBorder().getMaxX() - 16.0);
			double n = Math.min(2.9999872E7, serverLevel2.getWorldBorder().getMaxZ() - 16.0);
			d = Mth.clamp(d, k, m);
			f = Mth.clamp(f, l, n);
			this.moveTo(d, e, f, h, g);
			if (dimensionType == DimensionType.THE_END) {
				int o = Mth.floor(this.getX());
				int p = Mth.floor(this.getY()) - 1;
				int q = Mth.floor(this.getZ());
				int r = 1;
				int s = 0;

				for (int t = -2; t <= 2; t++) {
					for (int u = -2; u <= 2; u++) {
						for (int v = -1; v < 3; v++) {
							int w = o + u * 1 + t * 0;
							int x = p + v;
							int y = q + u * 0 - t * 1;
							boolean bl = v < 0;
							serverLevel2.setBlockAndUpdate(new BlockPos(w, x, y), bl ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.AIR.defaultBlockState());
						}
					}
				}

				this.moveTo((double)o, (double)p, (double)q, h, 0.0F);
				this.setDeltaMovement(Vec3.ZERO);
			} else if (!serverLevel2.getPortalForcer().findAndMoveToPortal(this, j)) {
				serverLevel2.getPortalForcer().createPortal(this);
				serverLevel2.getPortalForcer().findAndMoveToPortal(this, j);
			}

			serverLevel.getProfiler().pop();
			this.setLevel(serverLevel2);
			serverLevel2.addDuringPortalTeleport(this);
			this.triggerDimensionChangeTriggers(serverLevel);
			this.connection.teleport(this.getX(), this.getY(), this.getZ(), h, g);
			this.gameMode.setLevel(serverLevel2);
			this.connection.send(new ClientboundPlayerAbilitiesPacket(this.abilities));
			playerList.sendLevelInfo(this, serverLevel2);
			playerList.sendAllPlayerInfo(this);

			for (MobEffectInstance mobEffectInstance : this.getActiveEffects()) {
				this.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), mobEffectInstance));
			}

			this.connection.send(new ClientboundLevelEventPacket(1032, BlockPos.ZERO, 0, false));
			this.lastSentExp = -1;
			this.lastSentHealth = -1.0F;
			this.lastSentFood = -1;
			return this;
		}
	}

	private void triggerDimensionChangeTriggers(ServerLevel serverLevel) {
		DimensionType dimensionType = serverLevel.dimension.getType();
		DimensionType dimensionType2 = this.level.dimension.getType();
		CriteriaTriggers.CHANGED_DIMENSION.trigger(this, dimensionType, dimensionType2);
		if (dimensionType == DimensionType.NETHER && dimensionType2 == DimensionType.OVERWORLD && this.enteredNetherPosition != null) {
			CriteriaTriggers.NETHER_TRAVEL.trigger(this, this.enteredNetherPosition);
		}

		if (dimensionType2 != DimensionType.NETHER) {
			this.enteredNetherPosition = null;
		}
	}

	@Override
	public boolean broadcastToPlayer(ServerPlayer serverPlayer) {
		if (serverPlayer.isSpectator()) {
			return this.getCamera() == this;
		} else {
			return this.isSpectator() ? false : super.broadcastToPlayer(serverPlayer);
		}
	}

	private void broadcast(BlockEntity blockEntity) {
		if (blockEntity != null) {
			ClientboundBlockEntityDataPacket clientboundBlockEntityDataPacket = blockEntity.getUpdatePacket();
			if (clientboundBlockEntityDataPacket != null) {
				this.connection.send(clientboundBlockEntityDataPacket);
			}
		}
	}

	@Override
	public void take(Entity entity, int i) {
		super.take(entity, i);
		this.containerMenu.broadcastChanges();
	}

	@Override
	public Either<Player.BedSleepingProblem, Unit> startSleepInBed(BlockPos blockPos) {
		return super.startSleepInBed(blockPos).ifRight(unit -> {
			this.awardStat(Stats.SLEEP_IN_BED);
			CriteriaTriggers.SLEPT_IN_BED.trigger(this);
		});
	}

	@Override
	public void stopSleepInBed(boolean bl, boolean bl2) {
		if (this.isSleeping()) {
			this.getLevel().getChunkSource().broadcastAndSend(this, new ClientboundAnimatePacket(this, 2));
		}

		super.stopSleepInBed(bl, bl2);
		if (this.connection != null) {
			this.connection.teleport(this.getX(), this.getY(), this.getZ(), this.yRot, this.xRot);
		}
	}

	@Override
	public boolean startRiding(Entity entity, boolean bl) {
		Entity entity2 = this.getVehicle();
		if (!super.startRiding(entity, bl)) {
			return false;
		} else {
			Entity entity3 = this.getVehicle();
			if (entity3 != entity2 && this.connection != null) {
				this.connection.teleport(this.getX(), this.getY(), this.getZ(), this.yRot, this.xRot);
			}

			return true;
		}
	}

	@Override
	public void stopRiding() {
		Entity entity = this.getVehicle();
		super.stopRiding();
		Entity entity2 = this.getVehicle();
		if (entity2 != entity && this.connection != null) {
			this.connection.teleport(this.getX(), this.getY(), this.getZ(), this.yRot, this.xRot);
		}
	}

	@Override
	public boolean isInvulnerableTo(DamageSource damageSource) {
		return super.isInvulnerableTo(damageSource) || this.isChangingDimension() || this.abilities.invulnerable && damageSource == DamageSource.WITHER;
	}

	@Override
	protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
	}

	@Override
	protected void onChangedBlock(BlockPos blockPos) {
		if (!this.isSpectator()) {
			super.onChangedBlock(blockPos);
		}
	}

	public void doCheckFallDamage(double d, boolean bl) {
		BlockPos blockPos = this.getOnPos();
		if (this.level.hasChunkAt(blockPos)) {
			BlockState blockState = this.level.getBlockState(blockPos);
			super.checkFallDamage(d, bl, blockState, blockPos);
		}
	}

	@Override
	public void openTextEdit(SignBlockEntity signBlockEntity) {
		signBlockEntity.setAllowedPlayerEditor(this);
		this.connection.send(new ClientboundOpenSignEditorPacket(signBlockEntity.getBlockPos()));
	}

	private void nextContainerCounter() {
		this.containerCounter = this.containerCounter % 100 + 1;
	}

	@Override
	public OptionalInt openMenu(@Nullable MenuProvider menuProvider) {
		if (menuProvider == null) {
			return OptionalInt.empty();
		} else {
			if (this.containerMenu != this.inventoryMenu) {
				this.closeContainer();
			}

			this.nextContainerCounter();
			AbstractContainerMenu abstractContainerMenu = menuProvider.createMenu(this.containerCounter, this.inventory, this);
			if (abstractContainerMenu == null) {
				if (this.isSpectator()) {
					this.displayClientMessage(new TranslatableComponent("container.spectatorCantOpen").withStyle(ChatFormatting.RED), true);
				}

				return OptionalInt.empty();
			} else {
				this.connection.send(new ClientboundOpenScreenPacket(abstractContainerMenu.containerId, abstractContainerMenu.getType(), menuProvider.getDisplayName()));
				abstractContainerMenu.addSlotListener(this);
				this.containerMenu = abstractContainerMenu;
				return OptionalInt.of(this.containerCounter);
			}
		}
	}

	@Override
	public void sendMerchantOffers(int i, MerchantOffers merchantOffers, int j, int k, boolean bl, boolean bl2) {
		this.connection.send(new ClientboundMerchantOffersPacket(i, merchantOffers, j, k, bl, bl2));
	}

	@Override
	public void openHorseInventory(AbstractHorse abstractHorse, Container container) {
		if (this.containerMenu != this.inventoryMenu) {
			this.closeContainer();
		}

		this.nextContainerCounter();
		this.connection.send(new ClientboundHorseScreenOpenPacket(this.containerCounter, container.getContainerSize(), abstractHorse.getId()));
		this.containerMenu = new HorseInventoryMenu(this.containerCounter, this.inventory, container, abstractHorse);
		this.containerMenu.addSlotListener(this);
	}

	@Override
	public void openItemGui(ItemStack itemStack, InteractionHand interactionHand) {
		Item item = itemStack.getItem();
		if (item == Items.WRITTEN_BOOK) {
			if (WrittenBookItem.resolveBookComponents(itemStack, this.createCommandSourceStack(), this)) {
				this.containerMenu.broadcastChanges();
			}

			this.connection.send(new ClientboundOpenBookPacket(interactionHand));
		}
	}

	@Override
	public void openCommandBlock(CommandBlockEntity commandBlockEntity) {
		commandBlockEntity.setSendToClient(true);
		this.broadcast(commandBlockEntity);
	}

	@Override
	public void slotChanged(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack) {
		if (!(abstractContainerMenu.getSlot(i) instanceof ResultSlot)) {
			if (abstractContainerMenu == this.inventoryMenu) {
				CriteriaTriggers.INVENTORY_CHANGED.trigger(this, this.inventory);
			}

			if (!this.ignoreSlotUpdateHack) {
				this.connection.send(new ClientboundContainerSetSlotPacket(abstractContainerMenu.containerId, i, itemStack));
			}
		}
	}

	public void refreshContainer(AbstractContainerMenu abstractContainerMenu) {
		this.refreshContainer(abstractContainerMenu, abstractContainerMenu.getItems());
	}

	@Override
	public void refreshContainer(AbstractContainerMenu abstractContainerMenu, NonNullList<ItemStack> nonNullList) {
		this.connection.send(new ClientboundContainerSetContentPacket(abstractContainerMenu.containerId, nonNullList));
		this.connection.send(new ClientboundContainerSetSlotPacket(-1, -1, this.inventory.getCarried()));
	}

	@Override
	public void setContainerData(AbstractContainerMenu abstractContainerMenu, int i, int j) {
		this.connection.send(new ClientboundContainerSetDataPacket(abstractContainerMenu.containerId, i, j));
	}

	@Override
	public void closeContainer() {
		this.connection.send(new ClientboundContainerClosePacket(this.containerMenu.containerId));
		this.doCloseContainer();
	}

	public void broadcastCarriedItem() {
		if (!this.ignoreSlotUpdateHack) {
			this.connection.send(new ClientboundContainerSetSlotPacket(-1, -1, this.inventory.getCarried()));
		}
	}

	public void doCloseContainer() {
		this.containerMenu.removed(this);
		this.containerMenu = this.inventoryMenu;
	}

	public void setPlayerInput(float f, float g, boolean bl, boolean bl2) {
		if (this.isPassenger()) {
			if (f >= -1.0F && f <= 1.0F) {
				this.xxa = f;
			}

			if (g >= -1.0F && g <= 1.0F) {
				this.zza = g;
			}

			this.jumping = bl;
			this.setShiftKeyDown(bl2);
		}
	}

	@Override
	public void awardStat(Stat<?> stat, int i) {
		this.stats.increment(this, stat, i);
		this.getScoreboard().forAllObjectives(stat, this.getScoreboardName(), score -> score.add(i));
	}

	@Override
	public void resetStat(Stat<?> stat) {
		this.stats.setValue(this, stat, 0);
		this.getScoreboard().forAllObjectives(stat, this.getScoreboardName(), Score::reset);
	}

	@Override
	public int awardRecipes(Collection<Recipe<?>> collection) {
		return this.recipeBook.addRecipes(collection, this);
	}

	@Override
	public void awardRecipesByKey(ResourceLocation[] resourceLocations) {
		List<Recipe<?>> list = Lists.<Recipe<?>>newArrayList();

		for (ResourceLocation resourceLocation : resourceLocations) {
			this.server.getRecipeManager().byKey(resourceLocation).ifPresent(list::add);
		}

		this.awardRecipes(list);
	}

	@Override
	public int resetRecipes(Collection<Recipe<?>> collection) {
		return this.recipeBook.removeRecipes(collection, this);
	}

	@Override
	public void giveExperiencePoints(int i) {
		super.giveExperiencePoints(i);
		this.lastSentExp = -1;
	}

	public void disconnect() {
		this.disconnected = true;
		this.ejectPassengers();
		if (this.isSleeping()) {
			this.stopSleepInBed(true, false);
		}
	}

	public boolean hasDisconnected() {
		return this.disconnected;
	}

	public void resetSentInfo() {
		this.lastSentHealth = -1.0E8F;
	}

	@Override
	public void displayClientMessage(Component component, boolean bl) {
		this.connection.send(new ClientboundChatPacket(component, bl ? ChatType.GAME_INFO : ChatType.CHAT));
	}

	@Override
	protected void completeUsingItem() {
		if (!this.useItem.isEmpty() && this.isUsingItem()) {
			this.connection.send(new ClientboundEntityEventPacket(this, (byte)9));
			super.completeUsingItem();
		}
	}

	@Override
	public void lookAt(EntityAnchorArgument.Anchor anchor, Vec3 vec3) {
		super.lookAt(anchor, vec3);
		this.connection.send(new ClientboundPlayerLookAtPacket(anchor, vec3.x, vec3.y, vec3.z));
	}

	public void lookAt(EntityAnchorArgument.Anchor anchor, Entity entity, EntityAnchorArgument.Anchor anchor2) {
		Vec3 vec3 = anchor2.apply(entity);
		super.lookAt(anchor, vec3);
		this.connection.send(new ClientboundPlayerLookAtPacket(anchor, entity, anchor2));
	}

	public void restoreFrom(ServerPlayer serverPlayer, boolean bl) {
		if (bl) {
			this.inventory.replaceWith(serverPlayer.inventory);
			this.setHealth(serverPlayer.getHealth());
			this.foodData = serverPlayer.foodData;
			this.experienceLevel = serverPlayer.experienceLevel;
			this.totalExperience = serverPlayer.totalExperience;
			this.experienceProgress = serverPlayer.experienceProgress;
			this.setScore(serverPlayer.getScore());
			this.portalEntranceBlock = serverPlayer.portalEntranceBlock;
			this.portalEntranceOffset = serverPlayer.portalEntranceOffset;
			this.portalEntranceForwards = serverPlayer.portalEntranceForwards;
		} else if (this.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) || serverPlayer.isSpectator()) {
			this.inventory.replaceWith(serverPlayer.inventory);
			this.experienceLevel = serverPlayer.experienceLevel;
			this.totalExperience = serverPlayer.totalExperience;
			this.experienceProgress = serverPlayer.experienceProgress;
			this.setScore(serverPlayer.getScore());
		}

		this.enchantmentSeed = serverPlayer.enchantmentSeed;
		this.enderChestInventory = serverPlayer.enderChestInventory;
		this.getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, serverPlayer.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION));
		this.lastSentExp = -1;
		this.lastSentHealth = -1.0F;
		this.lastSentFood = -1;
		this.recipeBook.copyOverData(serverPlayer.recipeBook);
		this.entitiesToRemove.addAll(serverPlayer.entitiesToRemove);
		this.seenCredits = serverPlayer.seenCredits;
		this.enteredNetherPosition = serverPlayer.enteredNetherPosition;
		this.setShoulderEntityLeft(serverPlayer.getShoulderEntityLeft());
		this.setShoulderEntityRight(serverPlayer.getShoulderEntityRight());
	}

	@Override
	protected void onEffectAdded(MobEffectInstance mobEffectInstance) {
		super.onEffectAdded(mobEffectInstance);
		this.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), mobEffectInstance));
		if (mobEffectInstance.getEffect() == MobEffects.LEVITATION) {
			this.levitationStartTime = this.tickCount;
			this.levitationStartPos = this.position();
		}

		CriteriaTriggers.EFFECTS_CHANGED.trigger(this);
	}

	@Override
	protected void onEffectUpdated(MobEffectInstance mobEffectInstance, boolean bl) {
		super.onEffectUpdated(mobEffectInstance, bl);
		this.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), mobEffectInstance));
		CriteriaTriggers.EFFECTS_CHANGED.trigger(this);
	}

	@Override
	protected void onEffectRemoved(MobEffectInstance mobEffectInstance) {
		super.onEffectRemoved(mobEffectInstance);
		this.connection.send(new ClientboundRemoveMobEffectPacket(this.getId(), mobEffectInstance.getEffect()));
		if (mobEffectInstance.getEffect() == MobEffects.LEVITATION) {
			this.levitationStartPos = null;
		}

		CriteriaTriggers.EFFECTS_CHANGED.trigger(this);
	}

	@Override
	public void teleportTo(double d, double e, double f) {
		this.connection.teleport(d, e, f, this.yRot, this.xRot);
	}

	@Override
	public void forceMove(double d, double e, double f) {
		this.connection.teleport(d, e, f, this.yRot, this.xRot);
		this.connection.resetPosition();
	}

	@Override
	public void crit(Entity entity) {
		this.getLevel().getChunkSource().broadcastAndSend(this, new ClientboundAnimatePacket(entity, 4));
	}

	@Override
	public void magicCrit(Entity entity) {
		this.getLevel().getChunkSource().broadcastAndSend(this, new ClientboundAnimatePacket(entity, 5));
	}

	@Override
	public void onUpdateAbilities() {
		if (this.connection != null) {
			this.connection.send(new ClientboundPlayerAbilitiesPacket(this.abilities));
			this.updateInvisibilityStatus();
		}
	}

	public ServerLevel getLevel() {
		return (ServerLevel)this.level;
	}

	@Override
	public void setGameMode(GameType gameType) {
		this.gameMode.setGameModeForPlayer(gameType);
		this.connection.send(new ClientboundGameEventPacket(3, (float)gameType.getId()));
		if (gameType == GameType.SPECTATOR) {
			this.removeEntitiesOnShoulder();
			this.stopRiding();
		} else {
			this.setCamera(this);
		}

		this.onUpdateAbilities();
		this.updateEffectVisibility();
	}

	@Override
	public boolean isSpectator() {
		return this.gameMode.getGameModeForPlayer() == GameType.SPECTATOR;
	}

	@Override
	public boolean isCreative() {
		return this.gameMode.getGameModeForPlayer() == GameType.CREATIVE;
	}

	@Override
	public void sendMessage(Component component) {
		this.sendMessage(component, ChatType.SYSTEM);
	}

	public void sendMessage(Component component, ChatType chatType) {
		this.connection
			.send(
				new ClientboundChatPacket(component, chatType),
				future -> {
					if (!future.isSuccess() && (chatType == ChatType.GAME_INFO || chatType == ChatType.SYSTEM)) {
						int i = 256;
						String string = component.getString(256);
						Component component2 = new TextComponent(string).withStyle(ChatFormatting.YELLOW);
						this.connection
							.send(
								new ClientboundChatPacket(new TranslatableComponent("multiplayer.message_not_delivered", component2).withStyle(ChatFormatting.RED), ChatType.SYSTEM)
							);
					}
				}
			);
	}

	public String getIpAddress() {
		String string = this.connection.connection.getRemoteAddress().toString();
		string = string.substring(string.indexOf("/") + 1);
		return string.substring(0, string.indexOf(":"));
	}

	public void updateOptions(ServerboundClientInformationPacket serverboundClientInformationPacket) {
		this.language = serverboundClientInformationPacket.getLanguage();
		this.chatVisibility = serverboundClientInformationPacket.getChatVisibility();
		this.canChatColor = serverboundClientInformationPacket.getChatColors();
		this.enableShieldOnCrouch = serverboundClientInformationPacket.getUseShieldOnCrouch();
		System.out.println(" asddsf s ServerboundClientInformationPacket");
		this.getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, (byte)serverboundClientInformationPacket.getModelCustomisation());
		this.getEntityData().set(DATA_PLAYER_MAIN_HAND, (byte)(serverboundClientInformationPacket.getMainHand() == HumanoidArm.LEFT ? 0 : 1));
	}

	public ChatVisiblity getChatVisibility() {
		return this.chatVisibility;
	}

	public void sendTexturePack(String string, String string2) {
		this.connection.send(new ClientboundResourcePackPacket(string, string2));
	}

	@Override
	protected int getPermissionLevel() {
		return this.server.getProfilePermissions(this.getGameProfile());
	}

	public void resetLastActionTime() {
		this.lastActionTime = Util.getMillis();
	}

	public ServerStatsCounter getStats() {
		return this.stats;
	}

	public ServerRecipeBook getRecipeBook() {
		return this.recipeBook;
	}

	public void sendRemoveEntity(Entity entity) {
		if (entity instanceof Player) {
			this.connection.send(new ClientboundRemoveEntitiesPacket(entity.getId()));
		} else {
			this.entitiesToRemove.add(entity.getId());
		}
	}

	public void cancelRemoveEntity(Entity entity) {
		this.entitiesToRemove.remove(entity.getId());
	}

	@Override
	protected void updateInvisibilityStatus() {
		if (this.isSpectator()) {
			this.removeEffectParticles();
			this.setInvisible(true);
		} else {
			super.updateInvisibilityStatus();
		}
	}

	public Entity getCamera() {
		return (Entity)(this.camera == null ? this : this.camera);
	}

	public void setCamera(Entity entity) {
		Entity entity2 = this.getCamera();
		this.camera = (Entity)(entity == null ? this : entity);
		if (entity2 != this.camera) {
			this.connection.send(new ClientboundSetCameraPacket(this.camera));
			this.teleportTo(this.camera.getX(), this.camera.getY(), this.camera.getZ());
		}
	}

	@Override
	protected void processDimensionDelay() {
		if (this.changingDimensionDelay > 0 && !this.isChangingDimension) {
			this.changingDimensionDelay--;
		}
	}

	@Override
	public void attack(Entity entity) {
		if (this.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
			this.setCamera(entity);
		} else {
			super.attack(entity);
		}
	}

	public long getLastActionTime() {
		return this.lastActionTime;
	}

	@Nullable
	public Component getTabListDisplayName() {
		return null;
	}

	@Override
	public void swing(InteractionHand interactionHand) {
		super.swing(interactionHand);
	}

	public boolean isChangingDimension() {
		return this.isChangingDimension;
	}

	public void hasChangedDimension() {
		this.isChangingDimension = false;
	}

	public PlayerAdvancements getAdvancements() {
		return this.advancements;
	}

	public void teleportTo(ServerLevel serverLevel, double d, double e, double f, float g, float h) {
		this.setCamera(this);
		this.stopRiding();
		if (serverLevel == this.level) {
			this.connection.teleport(d, e, f, g, h);
		} else {
			ServerLevel serverLevel2 = this.getLevel();
			this.dimension = serverLevel.dimension.getType();
			LevelData levelData = serverLevel.getLevelData();
			this.connection
				.send(
					new ClientboundRespawnPacket(
						this.dimension, LevelData.obfuscateSeed(levelData.getSeed()), levelData.getGeneratorType(), this.gameMode.getGameModeForPlayer()
					)
				);
			this.connection.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));
			this.server.getPlayerList().sendPlayerPermissionLevel(this);
			serverLevel2.removePlayerImmediately(this);
			this.removed = false;
			this.moveTo(d, e, f, g, h);
			this.setLevel(serverLevel);
			serverLevel.addDuringCommandTeleport(this);
			this.triggerDimensionChangeTriggers(serverLevel2);
			this.connection.teleport(d, e, f, g, h);
			this.gameMode.setLevel(serverLevel);
			this.server.getPlayerList().sendLevelInfo(this, serverLevel);
			this.server.getPlayerList().sendAllPlayerInfo(this);
		}
	}

	public void trackChunk(ChunkPos chunkPos, Packet<?> packet, Packet<?> packet2) {
		this.connection.send(packet2);
		this.connection.send(packet);
	}

	public void untrackChunk(ChunkPos chunkPos) {
		if (this.isAlive()) {
			this.connection.send(new ClientboundForgetLevelChunkPacket(chunkPos.x, chunkPos.z));
		}
	}

	public SectionPos getLastSectionPos() {
		return this.lastSectionPos;
	}

	public void setLastSectionPos(SectionPos sectionPos) {
		this.lastSectionPos = sectionPos;
	}

	@Override
	public void playNotifySound(SoundEvent soundEvent, SoundSource soundSource, float f, float g) {
		this.connection.send(new ClientboundSoundPacket(soundEvent, soundSource, this.getX(), this.getY(), this.getZ(), f, g));
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return new ClientboundAddPlayerPacket(this);
	}

	@Override
	public ItemEntity drop(ItemStack itemStack, boolean bl, boolean bl2) {
		ItemEntity itemEntity = super.drop(itemStack, bl, bl2);
		if (itemEntity == null) {
			return null;
		} else {
			this.level.addFreshEntity(itemEntity);
			ItemStack itemStack2 = itemEntity.getItem();
			if (bl2) {
				if (!itemStack2.isEmpty()) {
					this.awardStat(Stats.ITEM_DROPPED.get(itemStack2.getItem()), itemStack.getCount());
				}

				this.awardStat(Stats.DROP);
			}

			return itemEntity;
		}
	}
}
