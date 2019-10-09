package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class CompassItem extends Item {
	public CompassItem(Item.Properties properties) {
		super(properties);
		this.addProperty(new ResourceLocation("angle"), new ItemPropertyFunction() {
			@Environment(EnvType.CLIENT)
			private double rotation;
			@Environment(EnvType.CLIENT)
			private double rota;
			@Environment(EnvType.CLIENT)
			private long lastUpdateTick;

			@Environment(EnvType.CLIENT)
			@Override
			public float call(ItemStack itemStack, @Nullable Level level, @Nullable LivingEntity livingEntity) {
				if (livingEntity == null && !itemStack.isFramed()) {
					return 0.0F;
				} else {
					boolean bl = livingEntity != null;
					Entity entity = (Entity)(bl ? livingEntity : itemStack.getFrame());
					if (level == null) {
						level = entity.level;
					}

					double f;
					if (level.dimension.isNaturalDimension()) {
						double d = bl ? (double)entity.yRot : this.getFrameRotation((ItemFrame)entity);
						d = Mth.positiveModulo(d / 360.0, 1.0);
						double e = this.getSpawnToAngle(level, entity) / (float) (Math.PI * 2);
						f = 0.5 - (d - 0.25 - e);
					} else {
						f = Math.random();
					}

					if (bl) {
						f = this.wobble(level, f);
					}

					return Mth.positiveModulo((float)f, 1.0F);
				}
			}

			@Environment(EnvType.CLIENT)
			private double wobble(Level level, double d) {
				if (level.getGameTime() != this.lastUpdateTick) {
					this.lastUpdateTick = level.getGameTime();
					double e = d - this.rotation;
					e = Mth.positiveModulo(e + 0.5, 1.0) - 0.5;
					this.rota += e * 0.1;
					this.rota *= 0.8;
					this.rotation = Mth.positiveModulo(this.rotation + this.rota, 1.0);
				}

				return this.rotation;
			}

			@Environment(EnvType.CLIENT)
			private double getFrameRotation(ItemFrame itemFrame) {
				return (double)Mth.wrapDegrees(180 + itemFrame.getDirection().get2DDataValue() * 90);
			}

			@Environment(EnvType.CLIENT)
			private double getSpawnToAngle(LevelAccessor levelAccessor, Entity entity) {
				BlockPos blockPos = levelAccessor.getSharedSpawnPos();
				return Math.atan2((double)blockPos.getZ() - entity.getZ(), (double)blockPos.getX() - entity.getX());
			}
		});
	}
}
