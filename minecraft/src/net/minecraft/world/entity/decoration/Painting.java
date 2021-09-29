package net.minecraft.world.entity.decoration;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public class Painting extends HangingEntity {
	public Motive motive = Motive.KEBAB;

	public Painting(EntityType<? extends Painting> entityType, Level level) {
		super(entityType, level);
	}

	public Painting(Level level, BlockPos blockPos, Direction direction) {
		super(EntityType.PAINTING, level, blockPos);
		List<Motive> list = Lists.<Motive>newArrayList();
		int i = 0;

		for (Motive motive : Registry.MOTIVE) {
			this.motive = motive;
			this.setDirection(direction);
			if (this.survives()) {
				list.add(motive);
				int j = motive.getWidth() * motive.getHeight();
				if (j > i) {
					i = j;
				}
			}
		}

		if (!list.isEmpty()) {
			Iterator<Motive> iterator = list.iterator();

			while (iterator.hasNext()) {
				Motive motivex = (Motive)iterator.next();
				if (motivex.getWidth() * motivex.getHeight() < i) {
					iterator.remove();
				}
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
		return this.motive.getWidth();
	}

	@Override
	public int getHeight() {
		return this.motive.getHeight();
	}

	@Override
	public void dropItem(@Nullable Entity entity) {
		if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
			this.playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
			if (entity instanceof Player player && player.getAbilities().instabuild) {
				return;
			}

			this.spawnAtLocation(Items.PAINTING);
		}
	}

	@Override
	public void playPlacementSound() {
		this.playSound(SoundEvents.PAINTING_PLACE, 1.0F, 1.0F);
	}

	@Override
	public void moveTo(double d, double e, double f, float g, float h) {
		this.setPos(d, e, f);
	}

	@Override
	public void lerpTo(double d, double e, double f, float g, float h, int i, boolean bl) {
		BlockPos blockPos = this.pos.offset(d - this.getX(), e - this.getY(), f - this.getZ());
		this.setPos((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
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
