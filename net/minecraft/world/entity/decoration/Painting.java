/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.decoration;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddPaintingPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class Painting
extends HangingEntity {
    public Motive motive;

    public Painting(EntityType<? extends Painting> entityType, Level level) {
        super((EntityType<? extends HangingEntity>)entityType, level);
    }

    public Painting(Level level, BlockPos blockPos, Direction direction) {
        super(EntityType.PAINTING, level, blockPos);
        Motive motive;
        ArrayList<Motive> list = Lists.newArrayList();
        int i = 0;
        Iterator iterator = Registry.MOTIVE.iterator();
        while (iterator.hasNext()) {
            this.motive = motive = (Motive)iterator.next();
            this.setDirection(direction);
            if (!this.survives()) continue;
            list.add(motive);
            int j = motive.getWidth() * motive.getHeight();
            if (j <= i) continue;
            i = j;
        }
        if (!list.isEmpty()) {
            Iterator iterator2 = list.iterator();
            while (iterator2.hasNext()) {
                motive = (Motive)iterator2.next();
                if (motive.getWidth() * motive.getHeight() >= i) continue;
                iterator2.remove();
            }
            this.motive = (Motive)list.get(this.random.nextInt(list.size()));
        }
        this.setDirection(direction);
    }

    public Painting(Level level, BlockPos blockPos, Direction direction, Motive motive) {
        this(level, blockPos, direction);
        this.motive = motive;
        this.setDirection(direction);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putString("Motive", Registry.MOTIVE.getKey(this.motive).toString());
        compoundTag.putByte("Facing", (byte)this.direction.get2DDataValue());
        super.addAdditionalSaveData(compoundTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        this.motive = Registry.MOTIVE.get(ResourceLocation.tryParse(compoundTag.getString("Motive")));
        this.direction = Direction.from2DDataValue(compoundTag.getByte("Facing"));
        super.readAdditionalSaveData(compoundTag);
        this.setDirection(this.direction);
    }

    @Override
    public int getWidth() {
        if (this.motive == null) {
            return 1;
        }
        return this.motive.getWidth();
    }

    @Override
    public int getHeight() {
        if (this.motive == null) {
            return 1;
        }
        return this.motive.getHeight();
    }

    @Override
    public void dropItem(@Nullable Entity entity) {
        if (!this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            return;
        }
        this.playSound(SoundEvents.PAINTING_BREAK, 1.0f, 1.0f);
        if (entity instanceof Player) {
            Player player = (Player)entity;
            if (player.getAbilities().instabuild) {
                return;
            }
        }
        this.spawnAtLocation(Items.PAINTING);
    }

    @Override
    public void playPlacementSound() {
        this.playSound(SoundEvents.PAINTING_PLACE, 1.0f, 1.0f);
    }

    @Override
    public void moveTo(double d, double e, double f, float g, float h) {
        this.setPos(d, e, f);
    }

    @Override
    public void lerpTo(double d, double e, double f, float g, float h, int i, boolean bl) {
        BlockPos blockPos = this.pos.offset(d - this.getX(), e - this.getY(), f - this.getZ());
        this.setPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddPaintingPacket(this);
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.PAINTING);
    }
}

