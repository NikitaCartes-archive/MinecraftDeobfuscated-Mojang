/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.Optional;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PowderSnowBlock
extends Block
implements BucketPickup {
    public PowderSnowBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public boolean skipRendering(BlockState blockState, BlockState blockState2, Direction direction) {
        if (blockState2.is(this)) {
            return true;
        }
        return super.skipRendering(blockState, blockState2, direction);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return Shapes.empty();
    }

    @Override
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (!(entity instanceof LivingEntity) || ((LivingEntity)entity).getFeetBlockState().is(Blocks.POWDER_SNOW)) {
            entity.makeStuckInBlock(blockState, new Vec3(0.9f, 0.99f, 0.9f));
        }
        entity.setBodyIsInPowderSnow(true);
        if (!entity.isSpectator() && (entity.xOld != entity.getX() || entity.zOld != entity.getZ()) && level.random.nextBoolean()) {
            PowderSnowBlock.spawnPowderSnowParticles(level, new Vec3(entity.getX(), blockPos.getY(), entity.getZ()));
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        EntityCollisionContext entityCollisionContext;
        Optional<Entity> optional;
        if (collisionContext instanceof EntityCollisionContext && (optional = (entityCollisionContext = (EntityCollisionContext)collisionContext).getEntity()).isPresent() && PowderSnowBlock.canEntityWalkOnPowderSnow(optional.get()) && collisionContext.isAbove(Shapes.block(), blockPos, false) && !collisionContext.isDescending()) {
            return super.getCollisionShape(blockState, blockGetter, blockPos, collisionContext);
        }
        return Shapes.empty();
    }

    @Override
    public VoxelShape getVisualShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return Shapes.empty();
    }

    public static void spawnPowderSnowParticles(Level level, Vec3 vec3) {
        if (!level.isClientSide) {
            return;
        }
        Random random = level.getRandom();
        double d = vec3.y + 1.0;
        for (int i = 0; i < random.nextInt(3); ++i) {
            level.addParticle(ParticleTypes.SNOWFLAKE, vec3.x, d, vec3.z, (-1.0f + random.nextFloat() * 2.0f) / 12.0f, 0.05f, (-1.0f + random.nextFloat() * 2.0f) / 12.0f);
        }
    }

    public static boolean canEntityWalkOnPowderSnow(Entity entity) {
        if (entity.getType().is(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS)) {
            return true;
        }
        if (entity instanceof LivingEntity) {
            return ((LivingEntity)entity).getItemBySlot(EquipmentSlot.FEET).is(Items.LEATHER_BOOTS);
        }
        return false;
    }

    @Override
    public ItemStack pickupBlock(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
        levelAccessor.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 11);
        if (!levelAccessor.isClientSide()) {
            levelAccessor.levelEvent(2001, blockPos, Block.getId(blockState));
        }
        return new ItemStack(Items.POWDER_SNOW_BUCKET);
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.of(SoundEvents.BUCKET_FILL_POWDER_SNOW);
    }
}

