/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.animal;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
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
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarrotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class Rabbit
extends Animal {
    private static final EntityDataAccessor<Integer> DATA_TYPE_ID = SynchedEntityData.defineId(Rabbit.class, EntityDataSerializers.INT);
    private static final ResourceLocation KILLER_BUNNY = new ResourceLocation("killer_bunny");
    private int jumpTicks;
    private int jumpDuration;
    private boolean wasOnGround;
    private int jumpDelayTicks;
    private int moreCarrotTicks;

    public Rabbit(EntityType<? extends Rabbit> entityType, Level level) {
        super((EntityType<? extends Animal>)entityType, level);
        this.jumpControl = new RabbitJumpControl(this);
        this.moveControl = new RabbitMoveControl(this);
        this.setSpeedModifier(0.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RabbitPanicGoal(this, 2.2));
        this.goalSelector.addGoal(2, new BreedGoal(this, 0.8));
        this.goalSelector.addGoal(3, new TemptGoal((PathfinderMob)this, 1.0, Ingredient.of(Items.CARROT, Items.GOLDEN_CARROT, Blocks.DANDELION), false));
        this.goalSelector.addGoal(4, new RabbitAvoidEntityGoal<Player>(this, Player.class, 8.0f, 2.2, 2.2));
        this.goalSelector.addGoal(4, new RabbitAvoidEntityGoal<Wolf>(this, Wolf.class, 10.0f, 2.2, 2.2));
        this.goalSelector.addGoal(4, new RabbitAvoidEntityGoal<Monster>(this, Monster.class, 4.0f, 2.2, 2.2));
        this.goalSelector.addGoal(5, new RaidGardenGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 10.0f));
    }

    @Override
    protected float getJumpPower() {
        if (this.horizontalCollision || this.moveControl.hasWanted() && this.moveControl.getWantedY() > this.y + 0.5) {
            return 0.5f;
        }
        Path path = this.navigation.getPath();
        if (path != null && path.getIndex() < path.getSize()) {
            Vec3 vec3 = path.currentPos(this);
            if (vec3.y > this.y + 0.5) {
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
        if (d > 0.0 && (e = Rabbit.getHorizontalDistanceSqr(this.getDeltaMovement())) < 0.01) {
            this.moveRelative(0.1f, new Vec3(0.0, 0.0, 1.0));
        }
        if (!this.level.isClientSide) {
            this.level.broadcastEntityEvent(this, (byte)1);
        }
    }

    @Environment(value=EnvType.CLIENT)
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
        this.entityData.define(DATA_TYPE_ID, 0);
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
            if (this.getRabbitType() == 99 && this.jumpDelayTicks == 0 && (livingEntity = this.getTarget()) != null && this.distanceToSqr(livingEntity) < 16.0) {
                this.facePoint(livingEntity.x, livingEntity.z);
                this.moveControl.setWantedPosition(livingEntity.x, livingEntity.y, livingEntity.z, this.moveControl.getSpeedModifier());
                this.startJumping();
                this.wasOnGround = true;
            }
            if (!(rabbitJumpControl = (RabbitJumpControl)this.jumpControl).wantJump()) {
                if (this.moveControl.hasWanted() && this.jumpDelayTicks == 0) {
                    Path path = this.navigation.getPath();
                    Vec3 vec3 = new Vec3(this.moveControl.getWantedX(), this.moveControl.getWantedY(), this.moveControl.getWantedZ());
                    if (path != null && path.getIndex() < path.getSize()) {
                        vec3 = path.currentPos(this);
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
    public void updateSprintingState() {
    }

    private void facePoint(double d, double e) {
        this.yRot = (float)(Mth.atan2(e - this.z, d - this.x) * 57.2957763671875) - 90.0f;
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

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(3.0);
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3f);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("RabbitType", this.getRabbitType());
        compoundTag.putInt("MoreCarrotTicks", this.moreCarrotTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.setRabbitType(compoundTag.getInt("RabbitType"));
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
        if (this.getRabbitType() == 99) {
            this.playSound(SoundEvents.RABBIT_ATTACK, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
            return entity.hurt(DamageSource.mobAttack(this), 8.0f);
        }
        return entity.hurt(DamageSource.mobAttack(this), 3.0f);
    }

    @Override
    public SoundSource getSoundSource() {
        return this.getRabbitType() == 99 ? SoundSource.HOSTILE : SoundSource.NEUTRAL;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        if (this.isInvulnerableTo(damageSource)) {
            return false;
        }
        return super.hurt(damageSource, f);
    }

    private boolean isTemptingItem(Item item) {
        return item == Items.CARROT || item == Items.GOLDEN_CARROT || item == Blocks.DANDELION.asItem();
    }

    @Override
    public Rabbit getBreedOffspring(AgableMob agableMob) {
        Rabbit rabbit = EntityType.RABBIT.create(this.level);
        int i = this.getRandomRabbitType(this.level);
        if (this.random.nextInt(20) != 0) {
            i = agableMob instanceof Rabbit && this.random.nextBoolean() ? ((Rabbit)agableMob).getRabbitType() : this.getRabbitType();
        }
        rabbit.setRabbitType(i);
        return rabbit;
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return this.isTemptingItem(itemStack.getItem());
    }

    public int getRabbitType() {
        return this.entityData.get(DATA_TYPE_ID);
    }

    public void setRabbitType(int i) {
        if (i == 99) {
            this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(8.0);
            this.goalSelector.addGoal(4, new EvilRabbitAttackGoal(this));
            this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]).setAlertOthers(new Class[0]));
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<Player>((Mob)this, Player.class, true));
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<Wolf>((Mob)this, Wolf.class, true));
            if (!this.hasCustomName()) {
                this.setCustomName(new TranslatableComponent(Util.makeDescriptionId("entity", KILLER_BUNNY), new Object[0]));
            }
        }
        this.entityData.set(DATA_TYPE_ID, i);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(LevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        spawnGroupData = super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
        int i = this.getRandomRabbitType(levelAccessor);
        boolean bl = false;
        if (spawnGroupData instanceof RabbitGroupData) {
            i = ((RabbitGroupData)spawnGroupData).rabbitType;
            bl = true;
        } else {
            spawnGroupData = new RabbitGroupData(i);
        }
        this.setRabbitType(i);
        if (bl) {
            this.setAge(-24000);
        }
        return spawnGroupData;
    }

    private int getRandomRabbitType(LevelAccessor levelAccessor) {
        Biome biome = levelAccessor.getBiome(new BlockPos(this));
        int i = this.random.nextInt(100);
        if (biome.getPrecipitation() == Biome.Precipitation.SNOW) {
            return i < 80 ? 1 : 3;
        }
        if (biome.getBiomeCategory() == Biome.BiomeCategory.DESERT) {
            return 4;
        }
        return i < 50 ? 0 : (i < 90 ? 5 : 2);
    }

    public static boolean checkRabbitSpawnRules(EntityType<Rabbit> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
        Block block = levelAccessor.getBlockState(blockPos.below()).getBlock();
        return (block == Blocks.GRASS_BLOCK || block == Blocks.SNOW || block == Blocks.SAND) && levelAccessor.getRawBrightness(blockPos, 0) > 8;
    }

    private boolean wantsMoreFood() {
        return this.moreCarrotTicks == 0;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public void handleEntityEvent(byte b) {
        if (b == 1) {
            this.doSprintParticleEffect();
            this.jumpDuration = 10;
            this.jumpTicks = 0;
        } else {
            super.handleEntityEvent(b);
        }
    }

    @Override
    public /* synthetic */ AgableMob getBreedOffspring(AgableMob agableMob) {
        return this.getBreedOffspring(agableMob);
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
                this.wantsToRaid = true;
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
                    Integer integer = blockState.getValue(CarrotBlock.AGE);
                    if (integer == 0) {
                        level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 2);
                        level.destroyBlock(blockPos, true);
                    } else {
                        level.setBlock(blockPos, (BlockState)blockState.setValue(CarrotBlock.AGE, integer - 1), 2);
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
            BlockState blockState;
            Block block = levelReader.getBlockState(blockPos).getBlock();
            if (block == Blocks.FARMLAND && this.wantsToRaid && !this.canRaid && (block = (blockState = levelReader.getBlockState(blockPos = blockPos.above())).getBlock()) instanceof CarrotBlock && ((CarrotBlock)block).isMaxAge(blockState)) {
                this.canRaid = true;
                return true;
            }
            return false;
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
            return this.rabbit.getRabbitType() != 99 && super.canUse();
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

    public class RabbitJumpControl
    extends JumpControl {
        private final Rabbit rabbit;
        private boolean canJump;

        public RabbitJumpControl(Rabbit rabbit2) {
            super(rabbit2);
            this.rabbit = rabbit2;
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

    public static class RabbitGroupData
    implements SpawnGroupData {
        public final int rabbitType;

        public RabbitGroupData(int i) {
            this.rabbitType = i;
        }
    }
}

