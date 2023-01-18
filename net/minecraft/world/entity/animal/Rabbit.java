/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.animal;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.ClimbOnTopOfPowderSnowGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarrotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class Rabbit
extends Animal
implements VariantHolder<Variant> {
    public static final double STROLL_SPEED_MOD = 0.6;
    public static final double BREED_SPEED_MOD = 0.8;
    public static final double FOLLOW_SPEED_MOD = 1.0;
    public static final double FLEE_SPEED_MOD = 2.2;
    public static final double ATTACK_SPEED_MOD = 1.4;
    private static final EntityDataAccessor<Integer> DATA_TYPE_ID = SynchedEntityData.defineId(Rabbit.class, EntityDataSerializers.INT);
    private static final ResourceLocation KILLER_BUNNY = new ResourceLocation("killer_bunny");
    public static final int EVIL_ATTACK_POWER = 8;
    public static final int EVIL_ARMOR_VALUE = 8;
    private static final int MORE_CARROTS_DELAY = 40;
    private int jumpTicks;
    private int jumpDuration;
    private boolean wasOnGround;
    private int jumpDelayTicks;
    int moreCarrotTicks;

    public Rabbit(EntityType<? extends Rabbit> entityType, Level level) {
        super((EntityType<? extends Animal>)entityType, level);
        this.jumpControl = new RabbitJumpControl(this);
        this.moveControl = new RabbitMoveControl(this);
        this.setSpeedModifier(0.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ClimbOnTopOfPowderSnowGoal(this, this.level));
        this.goalSelector.addGoal(1, new RabbitPanicGoal(this, 2.2));
        this.goalSelector.addGoal(2, new BreedGoal(this, 0.8));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.0, Ingredient.of(Items.CARROT, Items.GOLDEN_CARROT, Blocks.DANDELION), false));
        this.goalSelector.addGoal(4, new RabbitAvoidEntityGoal<Player>(this, Player.class, 8.0f, 2.2, 2.2));
        this.goalSelector.addGoal(4, new RabbitAvoidEntityGoal<Wolf>(this, Wolf.class, 10.0f, 2.2, 2.2));
        this.goalSelector.addGoal(4, new RabbitAvoidEntityGoal<Monster>(this, Monster.class, 4.0f, 2.2, 2.2));
        this.goalSelector.addGoal(5, new RaidGardenGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 10.0f));
    }

    @Override
    protected float getJumpPower() {
        if (this.horizontalCollision || this.moveControl.hasWanted() && this.moveControl.getWantedY() > this.getY() + 0.5) {
            return 0.5f;
        }
        Path path = this.navigation.getPath();
        if (path != null && !path.isDone()) {
            Vec3 vec3 = path.getNextEntityPos(this);
            if (vec3.y > this.getY() + 0.5) {
                return 0.5f;
            }
        }
        if (this.moveControl.getSpeedModifier() <= 0.6) {
            return 0.2f;
        }
        return 0.3f;
    }

    @Override
    protected void jumpFromGround() {
        double e;
        super.jumpFromGround();
        double d = this.moveControl.getSpeedModifier();
        if (d > 0.0 && (e = this.getDeltaMovement().horizontalDistanceSqr()) < 0.01) {
            this.moveRelative(0.1f, new Vec3(0.0, 0.0, 1.0));
        }
        if (!this.level.isClientSide) {
            this.level.broadcastEntityEvent(this, (byte)1);
        }
    }

    public float getJumpCompletion(float f) {
        if (this.jumpDuration == 0) {
            return 0.0f;
        }
        return ((float)this.jumpTicks + f) / (float)this.jumpDuration;
    }

    public void setSpeedModifier(double d) {
        this.getNavigation().setSpeedModifier(d);
        this.moveControl.setWantedPosition(this.moveControl.getWantedX(), this.moveControl.getWantedY(), this.moveControl.getWantedZ(), d);
    }

    @Override
    public void setJumping(boolean bl) {
        super.setJumping(bl);
        if (bl) {
            this.playSound(this.getJumpSound(), this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f) * 0.8f);
        }
    }

    public void startJumping() {
        this.setJumping(true);
        this.jumpDuration = 10;
        this.jumpTicks = 0;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TYPE_ID, Variant.BROWN.id);
    }

    @Override
    public void customServerAiStep() {
        if (this.jumpDelayTicks > 0) {
            --this.jumpDelayTicks;
        }
        if (this.moreCarrotTicks > 0) {
            this.moreCarrotTicks -= this.random.nextInt(3);
            if (this.moreCarrotTicks < 0) {
                this.moreCarrotTicks = 0;
            }
        }
        if (this.onGround) {
            RabbitJumpControl rabbitJumpControl;
            LivingEntity livingEntity;
            if (!this.wasOnGround) {
                this.setJumping(false);
                this.checkLandingDelay();
            }
            if (this.getVariant() == Variant.EVIL && this.jumpDelayTicks == 0 && (livingEntity = this.getTarget()) != null && this.distanceToSqr(livingEntity) < 16.0) {
                this.facePoint(livingEntity.getX(), livingEntity.getZ());
                this.moveControl.setWantedPosition(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), this.moveControl.getSpeedModifier());
                this.startJumping();
                this.wasOnGround = true;
            }
            if (!(rabbitJumpControl = (RabbitJumpControl)this.jumpControl).wantJump()) {
                if (this.moveControl.hasWanted() && this.jumpDelayTicks == 0) {
                    Path path = this.navigation.getPath();
                    Vec3 vec3 = new Vec3(this.moveControl.getWantedX(), this.moveControl.getWantedY(), this.moveControl.getWantedZ());
                    if (path != null && !path.isDone()) {
                        vec3 = path.getNextEntityPos(this);
                    }
                    this.facePoint(vec3.x, vec3.z);
                    this.startJumping();
                }
            } else if (!rabbitJumpControl.canJump()) {
                this.enableJumpControl();
            }
        }
        this.wasOnGround = this.onGround;
    }

    @Override
    public boolean canSpawnSprintParticle() {
        return false;
    }

    private void facePoint(double d, double e) {
        this.setYRot((float)(Mth.atan2(e - this.getZ(), d - this.getX()) * 57.2957763671875) - 90.0f);
    }

    private void enableJumpControl() {
        ((RabbitJumpControl)this.jumpControl).setCanJump(true);
    }

    private void disableJumpControl() {
        ((RabbitJumpControl)this.jumpControl).setCanJump(false);
    }

    private void setLandingDelay() {
        this.jumpDelayTicks = this.moveControl.getSpeedModifier() < 2.2 ? 10 : 1;
    }

    private void checkLandingDelay() {
        this.setLandingDelay();
        this.disableJumpControl();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.jumpTicks != this.jumpDuration) {
            ++this.jumpTicks;
        } else if (this.jumpDuration != 0) {
            this.jumpTicks = 0;
            this.jumpDuration = 0;
            this.setJumping(false);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 3.0).add(Attributes.MOVEMENT_SPEED, 0.3f);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("RabbitType", this.getVariant().id);
        compoundTag.putInt("MoreCarrotTicks", this.moreCarrotTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.setVariant(Variant.byId(compoundTag.getInt("RabbitType")));
        this.moreCarrotTicks = compoundTag.getInt("MoreCarrotTicks");
    }

    protected SoundEvent getJumpSound() {
        return SoundEvents.RABBIT_JUMP;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.RABBIT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.RABBIT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RABBIT_DEATH;
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if (this.getVariant() == Variant.EVIL) {
            this.playSound(SoundEvents.RABBIT_ATTACK, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
            return entity.hurt(DamageSource.mobAttack(this), 8.0f);
        }
        return entity.hurt(DamageSource.mobAttack(this), 3.0f);
    }

    @Override
    public SoundSource getSoundSource() {
        return this.getVariant() == Variant.EVIL ? SoundSource.HOSTILE : SoundSource.NEUTRAL;
    }

    private static boolean isTemptingItem(ItemStack itemStack) {
        return itemStack.is(Items.CARROT) || itemStack.is(Items.GOLDEN_CARROT) || itemStack.is(Blocks.DANDELION.asItem());
    }

    /*
     * Unable to fully structure code
     */
    @Override
    @Nullable
    public Rabbit getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        block2: {
            block3: {
                rabbit = EntityType.RABBIT.create(serverLevel);
                if (rabbit == null) break block2;
                variant = Rabbit.getRandomRabbitVariant(serverLevel, this.blockPosition());
                if (this.random.nextInt(20) == 0) break block3;
                if (!(ageableMob instanceof Rabbit)) ** GOTO lbl-1000
                rabbit2 = (Rabbit)ageableMob;
                if (this.random.nextBoolean()) {
                    variant = rabbit2.getVariant();
                } else lbl-1000:
                // 2 sources

                {
                    variant = this.getVariant();
                }
            }
            rabbit.setVariant(variant);
        }
        return rabbit;
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return Rabbit.isTemptingItem(itemStack);
    }

    @Override
    public Variant getVariant() {
        return Variant.byId(this.entityData.get(DATA_TYPE_ID));
    }

    @Override
    public void setVariant(Variant variant) {
        if (variant == Variant.EVIL) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(8.0);
            this.goalSelector.addGoal(4, new EvilRabbitAttackGoal(this));
            this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]).setAlertOthers(new Class[0]));
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<Player>((Mob)this, Player.class, true));
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<Wolf>((Mob)this, Wolf.class, true));
            if (!this.hasCustomName()) {
                this.setCustomName(Component.translatable(Util.makeDescriptionId("entity", KILLER_BUNNY)));
            }
        }
        this.entityData.set(DATA_TYPE_ID, variant.id);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        Variant variant = Rabbit.getRandomRabbitVariant(serverLevelAccessor, this.blockPosition());
        if (spawnGroupData instanceof RabbitGroupData) {
            variant = ((RabbitGroupData)spawnGroupData).variant;
        } else {
            spawnGroupData = new RabbitGroupData(variant);
        }
        this.setVariant(variant);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    private static Variant getRandomRabbitVariant(LevelAccessor levelAccessor, BlockPos blockPos) {
        Holder<Biome> holder = levelAccessor.getBiome(blockPos);
        int i = levelAccessor.getRandom().nextInt(100);
        if (holder.is(BiomeTags.SPAWNS_WHITE_RABBITS)) {
            return i < 80 ? Variant.WHITE : Variant.WHITE_SPLOTCHED;
        }
        if (holder.is(BiomeTags.SPAWNS_GOLD_RABBITS)) {
            return Variant.GOLD;
        }
        return i < 50 ? Variant.BROWN : (i < 90 ? Variant.SALT : Variant.BLACK);
    }

    public static boolean checkRabbitSpawnRules(EntityType<Rabbit> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource) {
        return levelAccessor.getBlockState(blockPos.below()).is(BlockTags.RABBITS_SPAWNABLE_ON) && Rabbit.isBrightEnoughToSpawn(levelAccessor, blockPos);
    }

    boolean wantsMoreFood() {
        return this.moreCarrotTicks <= 0;
    }

    @Override
    public void handleEntityEvent(byte b) {
        if (b == 1) {
            this.spawnSprintParticle();
            this.jumpDuration = 10;
            this.jumpTicks = 0;
        } else {
            super.handleEntityEvent(b);
        }
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.6f * this.getEyeHeight(), this.getBbWidth() * 0.4f);
    }

    @Override
    @Nullable
    public /* synthetic */ AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }

    @Override
    public /* synthetic */ Object getVariant() {
        return this.getVariant();
    }

    public static class RabbitJumpControl
    extends JumpControl {
        private final Rabbit rabbit;
        private boolean canJump;

        public RabbitJumpControl(Rabbit rabbit) {
            super(rabbit);
            this.rabbit = rabbit;
        }

        public boolean wantJump() {
            return this.jump;
        }

        public boolean canJump() {
            return this.canJump;
        }

        public void setCanJump(boolean bl) {
            this.canJump = bl;
        }

        @Override
        public void tick() {
            if (this.jump) {
                this.rabbit.startJumping();
                this.jump = false;
            }
        }
    }

    static class RabbitMoveControl
    extends MoveControl {
        private final Rabbit rabbit;
        private double nextJumpSpeed;

        public RabbitMoveControl(Rabbit rabbit) {
            super(rabbit);
            this.rabbit = rabbit;
        }

        @Override
        public void tick() {
            if (this.rabbit.onGround && !this.rabbit.jumping && !((RabbitJumpControl)this.rabbit.jumpControl).wantJump()) {
                this.rabbit.setSpeedModifier(0.0);
            } else if (this.hasWanted()) {
                this.rabbit.setSpeedModifier(this.nextJumpSpeed);
            }
            super.tick();
        }

        @Override
        public void setWantedPosition(double d, double e, double f, double g) {
            if (this.rabbit.isInWater()) {
                g = 1.5;
            }
            super.setWantedPosition(d, e, f, g);
            if (g > 0.0) {
                this.nextJumpSpeed = g;
            }
        }
    }

    static class RabbitPanicGoal
    extends PanicGoal {
        private final Rabbit rabbit;

        public RabbitPanicGoal(Rabbit rabbit, double d) {
            super(rabbit, d);
            this.rabbit = rabbit;
        }

        @Override
        public void tick() {
            super.tick();
            this.rabbit.setSpeedModifier(this.speedModifier);
        }
    }

    static class RabbitAvoidEntityGoal<T extends LivingEntity>
    extends AvoidEntityGoal<T> {
        private final Rabbit rabbit;

        public RabbitAvoidEntityGoal(Rabbit rabbit, Class<T> class_, float f, double d, double e) {
            super(rabbit, class_, f, d, e);
            this.rabbit = rabbit;
        }

        @Override
        public boolean canUse() {
            return this.rabbit.getVariant() != Variant.EVIL && super.canUse();
        }
    }

    static class RaidGardenGoal
    extends MoveToBlockGoal {
        private final Rabbit rabbit;
        private boolean wantsToRaid;
        private boolean canRaid;

        public RaidGardenGoal(Rabbit rabbit) {
            super(rabbit, 0.7f, 16);
            this.rabbit = rabbit;
        }

        @Override
        public boolean canUse() {
            if (this.nextStartTick <= 0) {
                if (!this.rabbit.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                    return false;
                }
                this.canRaid = false;
                this.wantsToRaid = this.rabbit.wantsMoreFood();
            }
            return super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return this.canRaid && super.canContinueToUse();
        }

        @Override
        public void tick() {
            super.tick();
            this.rabbit.getLookControl().setLookAt((double)this.blockPos.getX() + 0.5, this.blockPos.getY() + 1, (double)this.blockPos.getZ() + 0.5, 10.0f, this.rabbit.getMaxHeadXRot());
            if (this.isReachedTarget()) {
                Level level = this.rabbit.level;
                BlockPos blockPos = this.blockPos.above();
                BlockState blockState = level.getBlockState(blockPos);
                Block block = blockState.getBlock();
                if (this.canRaid && block instanceof CarrotBlock) {
                    int i = blockState.getValue(CarrotBlock.AGE);
                    if (i == 0) {
                        level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 2);
                        level.destroyBlock(blockPos, true, this.rabbit);
                    } else {
                        level.setBlock(blockPos, (BlockState)blockState.setValue(CarrotBlock.AGE, i - 1), 2);
                        level.levelEvent(2001, blockPos, Block.getId(blockState));
                    }
                    this.rabbit.moreCarrotTicks = 40;
                }
                this.canRaid = false;
                this.nextStartTick = 10;
            }
        }

        @Override
        protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
            BlockState blockState = levelReader.getBlockState(blockPos);
            if (blockState.is(Blocks.FARMLAND) && this.wantsToRaid && !this.canRaid && (blockState = levelReader.getBlockState(blockPos.above())).getBlock() instanceof CarrotBlock && ((CarrotBlock)blockState.getBlock()).isMaxAge(blockState)) {
                this.canRaid = true;
                return true;
            }
            return false;
        }
    }

    public static enum Variant implements StringRepresentable
    {
        BROWN(0, "brown"),
        WHITE(1, "white"),
        BLACK(2, "black"),
        WHITE_SPLOTCHED(3, "white_splotched"),
        GOLD(4, "gold"),
        SALT(5, "salt"),
        EVIL(99, "evil");

        private static final IntFunction<Variant> BY_ID;
        public static final Codec<Variant> CODEC;
        final int id;
        private final String name;

        private Variant(int j, String string2) {
            this.id = j;
            this.name = string2;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public int id() {
            return this.id;
        }

        public static Variant byId(int i) {
            return BY_ID.apply(i);
        }

        static {
            BY_ID = ByIdMap.sparse(Variant::id, Variant.values(), BROWN);
            CODEC = StringRepresentable.fromEnum(Variant::values);
        }
    }

    static class EvilRabbitAttackGoal
    extends MeleeAttackGoal {
        public EvilRabbitAttackGoal(Rabbit rabbit) {
            super(rabbit, 1.4, true);
        }

        @Override
        protected double getAttackReachSqr(LivingEntity livingEntity) {
            return 4.0f + livingEntity.getBbWidth();
        }
    }

    public static class RabbitGroupData
    extends AgeableMob.AgeableMobGroupData {
        public final Variant variant;

        public RabbitGroupData(Variant variant) {
            super(1.0f);
            this.variant = variant;
        }
    }
}

