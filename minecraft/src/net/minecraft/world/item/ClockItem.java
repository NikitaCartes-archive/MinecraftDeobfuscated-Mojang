package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class ClockItem extends Item {
	public ClockItem(Item.Properties properties) {
		super(properties);
		this.addProperty(new ResourceLocation("time"), new ItemPropertyFunction() {
			@Environment(EnvType.CLIENT)
			private double rotation;
			@Environment(EnvType.CLIENT)
			private double rota;
			@Environment(EnvType.CLIENT)
			private long lastUpdateTick;

			@Environment(EnvType.CLIENT)
			@Override
			public float call(ItemStack itemStack, @Nullable Level level, @Nullable LivingEntity livingEntity) {
				boolean bl = livingEntity != null;
				Entity entity = (Entity)(bl ? livingEntity : itemStack.getFrame());
				if (level == null && entity != null) {
					level = entity.level;
				}

				if (level == null) {
					return 0.0F;
				} else {
					double d;
					if (level.dimension.isNaturalDimension()) {
						d = (double)level.getTimeOfDay(1.0F);
					} else {
						d = Math.random();
					}

					d = this.wobble(level, d);
					return (float)d;
				}
			}

			@Environment(EnvType.CLIENT)
			private double wobble(Level level, double d) {
				if (level.getGameTime() != this.lastUpdateTick) {
					this.lastUpdateTick = level.getGameTime();
					double e = d - this.rotation;
					e = Mth.positiveModulo(e + 0.5, 1.0) - 0.5;
					this.rota += e * 0.1;
					this.rota *= 0.9;
					this.rotation = Mth.positiveModulo(this.rotation + this.rota, 1.0);
				}

				return this.rotation;
			}
		});
	}
}
