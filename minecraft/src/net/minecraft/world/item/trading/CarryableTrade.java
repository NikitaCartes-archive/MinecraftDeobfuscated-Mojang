package net.minecraft.world.item.trading;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;

public interface CarryableTrade {
	Codec<CarryableTrade> CODEC = new Codec<CarryableTrade>() {
		@Override
		public <T> DataResult<Pair<CarryableTrade, T>> decode(DynamicOps<T> dynamicOps, T object) {
			DataResult<Pair<CarryableTrade, T>> dataResult = CarryableTrade.Block.CODEC.decode(dynamicOps, object).map(pair -> pair.mapFirst(Function.identity()));
			DataResult<Pair<CarryableTrade, T>> dataResult2 = CarryableTrade.Entity.CODEC.decode(dynamicOps, object).map(pair -> pair.mapFirst(Function.identity()));
			return dataResult.result().isPresent() ? dataResult : dataResult2;
		}

		public <T> DataResult<T> encode(CarryableTrade carryableTrade, DynamicOps<T> dynamicOps, T object) {
			return CarryableTrade.encode(carryableTrade, dynamicOps);
		}
	};

	static CarryableTrade.Block block(net.minecraft.world.level.block.Block block) {
		return new CarryableTrade.Block(block);
	}

	static CarryableTrade.Entity entity(EntityType<?> entityType) {
		return new CarryableTrade.Entity(entityType);
	}

	void giveToPlayer(ServerPlayer serverPlayer);

	boolean matches(CarryableTrade carryableTrade);

	default ItemStack asItemStack() {
		return ItemStack.EMPTY;
	}

	Codec<? extends CarryableTrade> getCodec();

	static <T, C extends CarryableTrade> DataResult<T> encode(C carryableTrade, DynamicOps<T> dynamicOps) {
		Codec<C> codec = (Codec<C>)carryableTrade.getCodec();
		return codec.encodeStart(dynamicOps, carryableTrade);
	}

	public static record Block(net.minecraft.world.level.block.Block block) implements CarryableTrade {
		public static final Codec<CarryableTrade.Block> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(Registry.BLOCK.byNameCodec().fieldOf("block").forGetter(CarryableTrade.Block::block)).apply(instance, CarryableTrade.Block::new)
		);

		@Override
		public void giveToPlayer(ServerPlayer serverPlayer) {
			serverPlayer.setCarriedBlock(this.block.defaultBlockState());
		}

		@Override
		public boolean matches(CarryableTrade carryableTrade) {
			return carryableTrade instanceof CarryableTrade.Block block ? block.block() == this.block : false;
		}

		@Override
		public ItemStack asItemStack() {
			return new ItemStack(this.block);
		}

		@Override
		public Codec<? extends CarryableTrade> getCodec() {
			return CODEC;
		}
	}

	public static record Entity(EntityType<?> entity) implements CarryableTrade {
		public static final Codec<CarryableTrade.Entity> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(Registry.ENTITY_TYPE.byNameCodec().fieldOf("entity").forGetter(CarryableTrade.Entity::entity))
					.apply(instance, CarryableTrade.Entity::new)
		);

		@Override
		public void giveToPlayer(ServerPlayer serverPlayer) {
			ServerLevel serverLevel = serverPlayer.getLevel();
			net.minecraft.world.entity.Entity entity = this.entity
				.spawn(serverLevel, null, serverPlayer, serverPlayer.blockPosition(), MobSpawnType.SPAWNER, false, false);
			if (entity != null) {
				serverPlayer.startCarryingEntity(entity);
			}
		}

		@Override
		public boolean matches(CarryableTrade carryableTrade) {
			return carryableTrade instanceof CarryableTrade.Entity entity ? entity.entity() == this.entity : false;
		}

		@Override
		public Codec<? extends CarryableTrade> getCodec() {
			return CODEC;
		}
	}
}
