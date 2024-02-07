package net.minecraft.world.entity.decoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Painting extends HangingEntity implements VariantHolder<Holder<PaintingVariant>> {
	private static final EntityDataAccessor<Holder<PaintingVariant>> DATA_PAINTING_VARIANT_ID = SynchedEntityData.defineId(
		Painting.class, EntityDataSerializers.PAINTING_VARIANT
	);
	private static final ResourceKey<PaintingVariant> DEFAULT_VARIANT = PaintingVariants.KEBAB;
	public static final String VARIANT_TAG = "variant";

	private static Holder<PaintingVariant> getDefaultVariant() {
		return BuiltInRegistries.PAINTING_VARIANT.getHolderOrThrow(DEFAULT_VARIANT);
	}

	public Painting(EntityType<? extends Painting> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(DATA_PAINTING_VARIANT_ID, getDefaultVariant());
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		if (DATA_PAINTING_VARIANT_ID.equals(entityDataAccessor)) {
			this.recalculateBoundingBox();
		}
	}

	public void setVariant(Holder<PaintingVariant> holder) {
		this.entityData.set(DATA_PAINTING_VARIANT_ID, holder);
	}

	public Holder<PaintingVariant> getVariant() {
		return this.entityData.get(DATA_PAINTING_VARIANT_ID);
	}

	public static Optional<Painting> create(Level level, BlockPos blockPos, Direction direction) {
		Painting painting = new Painting(level, blockPos);
		List<Holder<PaintingVariant>> list = new ArrayList();
		BuiltInRegistries.PAINTING_VARIANT.getTagOrEmpty(PaintingVariantTags.PLACEABLE).forEach(list::add);
		if (list.isEmpty()) {
			return Optional.empty();
		} else {
			painting.setDirection(direction);
			list.removeIf(holder -> {
				painting.setVariant(holder);
				return !painting.survives();
			});
			if (list.isEmpty()) {
				return Optional.empty();
			} else {
				int i = list.stream().mapToInt(Painting::variantArea).max().orElse(0);
				list.removeIf(holder -> variantArea(holder) < i);
				Optional<Holder<PaintingVariant>> optional = Util.getRandomSafe(list, painting.random);
				if (optional.isEmpty()) {
					return Optional.empty();
				} else {
					painting.setVariant((Holder<PaintingVariant>)optional.get());
					painting.setDirection(direction);
					return Optional.of(painting);
				}
			}
		}
	}

	private static int variantArea(Holder<PaintingVariant> holder) {
		return holder.value().getWidth() * holder.value().getHeight();
	}

	private Painting(Level level, BlockPos blockPos) {
		super(EntityType.PAINTING, level, blockPos);
	}

	public Painting(Level level, BlockPos blockPos, Direction direction, Holder<PaintingVariant> holder) {
		this(level, blockPos);
		this.setVariant(holder);
		this.setDirection(direction);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		storeVariant(compoundTag, this.getVariant());
		compoundTag.putByte("facing", (byte)this.direction.get2DDataValue());
		super.addAdditionalSaveData(compoundTag);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		Holder<PaintingVariant> holder = (Holder<PaintingVariant>)loadVariant(compoundTag).orElseGet(Painting::getDefaultVariant);
		this.setVariant(holder);
		this.direction = Direction.from2DDataValue(compoundTag.getByte("facing"));
		super.readAdditionalSaveData(compoundTag);
		this.setDirection(this.direction);
	}

	public static void storeVariant(CompoundTag compoundTag, Holder<PaintingVariant> holder) {
		compoundTag.putString("variant", ((ResourceKey)holder.unwrapKey().orElse(DEFAULT_VARIANT)).location().toString());
	}

	public static Optional<Holder<PaintingVariant>> loadVariant(CompoundTag compoundTag) {
		return Optional.ofNullable(ResourceLocation.tryParse(compoundTag.getString("variant")))
			.map(resourceLocation -> ResourceKey.create(Registries.PAINTING_VARIANT, resourceLocation))
			.flatMap(BuiltInRegistries.PAINTING_VARIANT::getHolder);
	}

	@Override
	public int getWidth() {
		return this.getVariant().value().getWidth();
	}

	@Override
	public int getHeight() {
		return this.getVariant().value().getHeight();
	}

	@Override
	public void dropItem(@Nullable Entity entity) {
		if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
			this.playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
			if (entity instanceof Player player && player.hasInfiniteMaterials()) {
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
	public void lerpTo(double d, double e, double f, float g, float h, int i) {
		this.setPos(d, e, f);
	}

	@Override
	public Vec3 trackingPosition() {
		return Vec3.atLowerCornerOf(this.pos);
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return new ClientboundAddEntityPacket(this, this.direction.get3DDataValue(), this.getPos());
	}

	@Override
	public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
		super.recreateFromPacket(clientboundAddEntityPacket);
		this.setDirection(Direction.from3DDataValue(clientboundAddEntityPacket.getData()));
	}

	@Override
	public ItemStack getPickResult() {
		return new ItemStack(Items.PAINTING);
	}
}
