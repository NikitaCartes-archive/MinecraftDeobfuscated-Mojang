package net.minecraft.world.entity;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Brightness;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;

public abstract class Display extends Entity {
	static final Logger LOGGER = LogUtils.getLogger();
	private static final long NO_LAST_UPDATE = -1000L;
	public static final int NO_BRIGHTNESS_OVERRIDE = -1;
	private static final EntityDataAccessor<Long> DATA_INTERPOLATION_START_TICKS_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.LONG);
	private static final EntityDataAccessor<Integer> DATA_INTERPOLATION_DURATION_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Vector3f> DATA_TRANSLATION_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.VECTOR3);
	private static final EntityDataAccessor<Vector3f> DATA_SCALE_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.VECTOR3);
	private static final EntityDataAccessor<Quaternionf> DATA_LEFT_ROTATION_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.QUATERNION);
	private static final EntityDataAccessor<Quaternionf> DATA_RIGHT_ROTATION_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.QUATERNION);
	private static final EntityDataAccessor<Byte> DATA_BILLBOARD_RENDER_CONSTRAINTS_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Integer> DATA_BRIGHTNESS_OVERRIDE_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> DATA_VIEW_RANGE_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DATA_SHADOW_RADIUS_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DATA_SHADOW_STRENGTH_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DATA_WIDTH_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DATA_HEIGHT_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> DATA_GLOW_COLOR_OVERRIDE_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.INT);
	private static final float INITIAL_SHADOW_RADIUS = 0.0F;
	private static final float INITIAL_SHADOW_STRENGTH = 1.0F;
	private static final int NO_GLOW_COLOR_OVERRIDE = 0;
	public static final String TAG_INTERPOLATION_DURATION = "interpolation_duration";
	public static final String TAG_INTERPOLATION_START = "interpolation_start";
	public static final String TAG_TRANSFORMATION = "transformation";
	public static final String TAG_BILLBOARD = "billboard";
	public static final String TAG_BRIGHTNESS = "brightness";
	public static final String TAG_VIEW_RANGE = "view_range";
	public static final String TAG_SHADOW_RADIUS = "shadow_radius";
	public static final String TAG_SHADOW_STRENGTH = "shadow_strength";
	public static final String TAG_WIDTH = "width";
	public static final String TAG_HEIGHT = "height";
	public static final String TAG_GLOW_COLOR_OVERRIDE = "glow_color_override";
	private final Display.GenericInterpolator<Transformation> transformation = new Display.GenericInterpolator<Transformation>(Transformation.identity()) {
		protected Transformation interpolate(float f, Transformation transformation, Transformation transformation2) {
			return transformation.slerp(transformation2, f);
		}
	};
	private final Display.FloatInterpolator shadowRadius = new Display.FloatInterpolator(0.0F);
	private final Display.FloatInterpolator shadowStrength = new Display.FloatInterpolator(1.0F);
	private final Quaternionf orientation = new Quaternionf();
	protected final Display.InterpolatorSet interpolators = new Display.InterpolatorSet();
	private AABB cullingBoundingBox;

	public Display(EntityType<?> entityType, Level level) {
		super(entityType, level);
		this.noPhysics = true;
		this.noCulling = true;
		this.cullingBoundingBox = this.getBoundingBox();
		this.interpolators
			.addEntry(
				Set.of(DATA_TRANSLATION_ID, DATA_LEFT_ROTATION_ID, DATA_SCALE_ID, DATA_RIGHT_ROTATION_ID),
				synchedEntityData -> this.transformation.updateValue(createTransformation(synchedEntityData))
			);
		this.interpolators.addEntry(DATA_SHADOW_STRENGTH_ID, this.shadowStrength);
		this.interpolators.addEntry(DATA_SHADOW_RADIUS_ID, this.shadowRadius);
	}

	@Override
	public void onSyncedDataUpdated(List<SynchedEntityData.DataValue<?>> list) {
		super.onSyncedDataUpdated(list);
		boolean bl = false;

		for (SynchedEntityData.DataValue<?> dataValue : list) {
			bl |= this.interpolators.shouldTriggerUpdate(dataValue.id());
		}

		if (bl) {
			this.interpolators.updateValues(this.entityData);
		}
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		super.onSyncedDataUpdated(entityDataAccessor);
		if (DATA_HEIGHT_ID.equals(entityDataAccessor) || DATA_WIDTH_ID.equals(entityDataAccessor)) {
			this.updateCulling();
		}
	}

	private static Transformation createTransformation(SynchedEntityData synchedEntityData) {
		Vector3f vector3f = synchedEntityData.get(DATA_TRANSLATION_ID);
		Quaternionf quaternionf = synchedEntityData.get(DATA_LEFT_ROTATION_ID);
		Vector3f vector3f2 = synchedEntityData.get(DATA_SCALE_ID);
		Quaternionf quaternionf2 = synchedEntityData.get(DATA_RIGHT_ROTATION_ID);
		return new Transformation(vector3f, quaternionf, vector3f2, quaternionf2);
	}

	@Override
	public void tick() {
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(DATA_INTERPOLATION_START_TICKS_ID, -1000L);
		this.entityData.define(DATA_INTERPOLATION_DURATION_ID, 0);
		this.entityData.define(DATA_TRANSLATION_ID, new Vector3f());
		this.entityData.define(DATA_SCALE_ID, new Vector3f(1.0F, 1.0F, 1.0F));
		this.entityData.define(DATA_RIGHT_ROTATION_ID, new Quaternionf());
		this.entityData.define(DATA_LEFT_ROTATION_ID, new Quaternionf());
		this.entityData.define(DATA_BILLBOARD_RENDER_CONSTRAINTS_ID, Display.BillboardConstraints.FIXED.getId());
		this.entityData.define(DATA_BRIGHTNESS_OVERRIDE_ID, -1);
		this.entityData.define(DATA_VIEW_RANGE_ID, 1.0F);
		this.entityData.define(DATA_SHADOW_RADIUS_ID, 0.0F);
		this.entityData.define(DATA_SHADOW_STRENGTH_ID, 1.0F);
		this.entityData.define(DATA_WIDTH_ID, 0.0F);
		this.entityData.define(DATA_HEIGHT_ID, 0.0F);
		this.entityData.define(DATA_GLOW_COLOR_OVERRIDE_ID, 0);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compoundTag) {
		if (compoundTag.contains("transformation")) {
			Transformation.EXTENDED_CODEC
				.decode(NbtOps.INSTANCE, compoundTag.get("transformation"))
				.resultOrPartial(Util.prefix("Display entity", LOGGER::error))
				.ifPresent(pair -> this.setTransformation((Transformation)pair.getFirst()));
		}

		if (compoundTag.contains("interpolation_duration", 99)) {
			int i = compoundTag.getInt("interpolation_duration");
			this.setInterpolationDuration(i);
		}

		if (compoundTag.contains("interpolation_start", 99)) {
			long l = compoundTag.getLong("interpolation_start");
			if (l < 0L) {
				this.setInterpolationStartTick(this.level.getGameTime());
			} else {
				this.setInterpolationStartTick(l);
			}
		}

		if (compoundTag.contains("billboard", 8)) {
			Display.BillboardConstraints.CODEC
				.decode(NbtOps.INSTANCE, compoundTag.get("billboard"))
				.resultOrPartial(Util.prefix("Display entity", LOGGER::error))
				.ifPresent(pair -> this.setBillboardConstraints((Display.BillboardConstraints)pair.getFirst()));
		}

		if (compoundTag.contains("view_range", 99)) {
			this.setViewRange(compoundTag.getFloat("view_range"));
		}

		if (compoundTag.contains("shadow_radius", 99)) {
			this.setShadowRadius(compoundTag.getFloat("shadow_radius"));
		}

		if (compoundTag.contains("shadow_strength", 99)) {
			this.setShadowStrength(compoundTag.getFloat("shadow_strength"));
		}

		if (compoundTag.contains("width", 99)) {
			this.setWidth(compoundTag.getFloat("width"));
		}

		if (compoundTag.contains("height", 99)) {
			this.setHeight(compoundTag.getFloat("height"));
		}

		if (compoundTag.contains("glow_color_override", 99)) {
			this.setGlowColorOverride(compoundTag.getInt("glow_color_override"));
		}

		if (compoundTag.contains("brightness", 10)) {
			Brightness.CODEC
				.decode(NbtOps.INSTANCE, compoundTag.get("brightness"))
				.resultOrPartial(Util.prefix("Display entity", LOGGER::error))
				.ifPresent(pair -> this.setBrightnessOverride((Brightness)pair.getFirst()));
		} else {
			this.setBrightnessOverride(null);
		}
	}

	private void setTransformation(Transformation transformation) {
		this.entityData.set(DATA_TRANSLATION_ID, transformation.getTranslation());
		this.entityData.set(DATA_LEFT_ROTATION_ID, transformation.getLeftRotation());
		this.entityData.set(DATA_SCALE_ID, transformation.getScale());
		this.entityData.set(DATA_RIGHT_ROTATION_ID, transformation.getRightRotation());
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compoundTag) {
		Transformation.EXTENDED_CODEC
			.encodeStart(NbtOps.INSTANCE, createTransformation(this.entityData))
			.result()
			.ifPresent(tag -> compoundTag.put("transformation", tag));
		Display.BillboardConstraints.CODEC.encodeStart(NbtOps.INSTANCE, this.getBillboardConstraints()).result().ifPresent(tag -> compoundTag.put("billboard", tag));
		compoundTag.putInt("interpolation_duration", this.getInterpolationDuration());
		compoundTag.putFloat("view_range", this.getViewRange());
		compoundTag.putFloat("shadow_radius", this.getShadowRadius());
		compoundTag.putFloat("shadow_strength", this.getShadowStrength());
		compoundTag.putFloat("width", this.getWidth());
		compoundTag.putFloat("height", this.getHeight());
		compoundTag.putLong("interpolation_start", this.getInterpolationStartTick());
		compoundTag.putInt("glow_color_override", this.getGlowColorOverride());
		Brightness brightness = this.getBrightnessOverride();
		if (brightness != null) {
			Brightness.CODEC.encodeStart(NbtOps.INSTANCE, brightness).result().ifPresent(tag -> compoundTag.put("brightness", tag));
		}
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return new ClientboundAddEntityPacket(this);
	}

	@Override
	public AABB getBoundingBoxForCulling() {
		return this.cullingBoundingBox;
	}

	@Override
	public PushReaction getPistonPushReaction() {
		return PushReaction.IGNORE;
	}

	public Quaternionf orientation() {
		return this.orientation;
	}

	public Transformation transformation(float f) {
		return this.transformation.get(f);
	}

	private void setInterpolationDuration(int i) {
		this.entityData.set(DATA_INTERPOLATION_DURATION_ID, i);
	}

	private int getInterpolationDuration() {
		return this.entityData.get(DATA_INTERPOLATION_DURATION_ID);
	}

	private void setInterpolationStartTick(long l) {
		this.entityData.set(DATA_INTERPOLATION_START_TICKS_ID, l);
	}

	private long getInterpolationStartTick() {
		return this.entityData.get(DATA_INTERPOLATION_START_TICKS_ID);
	}

	private void setBillboardConstraints(Display.BillboardConstraints billboardConstraints) {
		this.entityData.set(DATA_BILLBOARD_RENDER_CONSTRAINTS_ID, billboardConstraints.getId());
	}

	public Display.BillboardConstraints getBillboardConstraints() {
		return (Display.BillboardConstraints)Display.BillboardConstraints.BY_ID.apply(this.entityData.get(DATA_BILLBOARD_RENDER_CONSTRAINTS_ID));
	}

	private void setBrightnessOverride(@Nullable Brightness brightness) {
		this.entityData.set(DATA_BRIGHTNESS_OVERRIDE_ID, brightness != null ? brightness.pack() : -1);
	}

	@Nullable
	private Brightness getBrightnessOverride() {
		int i = this.entityData.get(DATA_BRIGHTNESS_OVERRIDE_ID);
		return i != -1 ? Brightness.unpack(i) : null;
	}

	public int getPackedBrightnessOverride() {
		return this.entityData.get(DATA_BRIGHTNESS_OVERRIDE_ID);
	}

	private void setViewRange(float f) {
		this.entityData.set(DATA_VIEW_RANGE_ID, f);
	}

	private float getViewRange() {
		return this.entityData.get(DATA_VIEW_RANGE_ID);
	}

	private void setShadowRadius(float f) {
		this.entityData.set(DATA_SHADOW_RADIUS_ID, f);
	}

	private float getShadowRadius() {
		return this.entityData.get(DATA_SHADOW_RADIUS_ID);
	}

	public float getShadowRadius(float f) {
		return this.shadowRadius.get(f);
	}

	private void setShadowStrength(float f) {
		this.entityData.set(DATA_SHADOW_STRENGTH_ID, f);
	}

	private float getShadowStrength() {
		return this.entityData.get(DATA_SHADOW_STRENGTH_ID);
	}

	public float getShadowStrength(float f) {
		return this.shadowStrength.get(f);
	}

	private void setWidth(float f) {
		this.entityData.set(DATA_WIDTH_ID, f);
	}

	private float getWidth() {
		return this.entityData.get(DATA_WIDTH_ID);
	}

	private void setHeight(float f) {
		this.entityData.set(DATA_HEIGHT_ID, f);
	}

	private int getGlowColorOverride() {
		return this.entityData.get(DATA_GLOW_COLOR_OVERRIDE_ID);
	}

	private void setGlowColorOverride(int i) {
		this.entityData.set(DATA_GLOW_COLOR_OVERRIDE_ID, i);
	}

	public float calculateInterpolationProgress(long l, float f) {
		int i = this.getInterpolationDuration();
		if (i <= 0) {
			return 1.0F;
		} else {
			long m = this.getInterpolationStartTick();
			float g = (float)(l - m);
			float h = g + f;
			return Mth.clamp(Mth.inverseLerp(h, 0.0F, (float)i), 0.0F, 1.0F);
		}
	}

	private float getHeight() {
		return this.entityData.get(DATA_HEIGHT_ID);
	}

	@Override
	public void setPos(double d, double e, double f) {
		super.setPos(d, e, f);
		this.updateCulling();
	}

	private void updateCulling() {
		float f = this.getWidth();
		float g = this.getHeight();
		if (f != 0.0F && g != 0.0F) {
			this.noCulling = false;
			float h = f / 2.0F;
			double d = this.getX();
			double e = this.getY();
			double i = this.getZ();
			this.cullingBoundingBox = new AABB(d - (double)h, e, i - (double)h, d + (double)h, e + (double)g, i + (double)h);
		} else {
			this.noCulling = true;
		}
	}

	@Override
	public void setXRot(float f) {
		super.setXRot(f);
		this.xRotO = f;
		this.updateOrientation();
	}

	@Override
	public void setYRot(float f) {
		super.setYRot(f);
		this.yRotO = f;
		this.updateOrientation();
	}

	private void updateOrientation() {
		this.orientation.rotationYXZ((float) (-Math.PI / 180.0) * this.getYRot(), (float) (Math.PI / 180.0) * this.getXRot(), 0.0F);
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double d) {
		return d < Mth.square((double)this.getViewRange() * 64.0 * getViewScale());
	}

	@Override
	public int getTeamColor() {
		int i = this.getGlowColorOverride();
		return i != 0 ? i : super.getTeamColor();
	}

	public static enum BillboardConstraints implements StringRepresentable {
		FIXED((byte)0, "fixed"),
		VERTICAL((byte)1, "vertical"),
		HORIZONTAL((byte)2, "horizontal"),
		CENTER((byte)3, "center");

		public static final Codec<Display.BillboardConstraints> CODEC = StringRepresentable.fromEnum(Display.BillboardConstraints::values);
		public static final IntFunction<Display.BillboardConstraints> BY_ID = ByIdMap.continuous(
			Display.BillboardConstraints::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO
		);
		private final byte id;
		private final String name;

		private BillboardConstraints(byte b, String string2) {
			this.name = string2;
			this.id = b;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		byte getId() {
			return this.id;
		}
	}

	public static class BlockDisplay extends Display {
		public static final String TAG_BLOCK_STATE = "block_state";
		private static final EntityDataAccessor<BlockState> DATA_BLOCK_STATE_ID = SynchedEntityData.defineId(
			Display.BlockDisplay.class, EntityDataSerializers.BLOCK_STATE
		);

		public BlockDisplay(EntityType<?> entityType, Level level) {
			super(entityType, level);
		}

		@Override
		protected void defineSynchedData() {
			super.defineSynchedData();
			this.entityData.define(DATA_BLOCK_STATE_ID, Blocks.AIR.defaultBlockState());
		}

		public BlockState getBlockState() {
			return this.entityData.get(DATA_BLOCK_STATE_ID);
		}

		public void setBlockState(BlockState blockState) {
			this.entityData.set(DATA_BLOCK_STATE_ID, blockState);
		}

		@Override
		protected void readAdditionalSaveData(CompoundTag compoundTag) {
			super.readAdditionalSaveData(compoundTag);
			this.setBlockState(NbtUtils.readBlockState(this.level.holderLookup(Registries.BLOCK), compoundTag.getCompound("block_state")));
		}

		@Override
		protected void addAdditionalSaveData(CompoundTag compoundTag) {
			super.addAdditionalSaveData(compoundTag);
			compoundTag.put("block_state", NbtUtils.writeBlockState(this.getBlockState()));
		}
	}

	static class ColorInterpolator extends Display.IntInterpolator {
		protected ColorInterpolator(int i) {
			super(i);
		}

		@Override
		protected int interpolate(float f, int i, int j) {
			return FastColor.ARGB32.lerp(f, i, j);
		}
	}

	static class FloatInterpolator extends Display.Interpolator<Float> {
		protected FloatInterpolator(float f) {
			super(f);
		}

		protected float interpolate(float f, float g, float h) {
			return Mth.lerp(f, g, h);
		}

		public float get(float f) {
			return !((double)f >= 1.0) && this.lastValue != null ? this.interpolate(f, this.lastValue, this.currentValue) : this.currentValue;
		}
	}

	abstract static class GenericInterpolator<T> extends Display.Interpolator<T> {
		protected GenericInterpolator(T object) {
			super(object);
		}

		protected abstract T interpolate(float f, T object, T object2);

		public T get(float f) {
			return !((double)f >= 1.0) && this.lastValue != null ? this.interpolate(f, this.lastValue, this.currentValue) : this.currentValue;
		}
	}

	static class IntInterpolator extends Display.Interpolator<Integer> {
		protected IntInterpolator(int i) {
			super(i);
		}

		protected int interpolate(float f, int i, int j) {
			return Mth.lerp(f, i, j);
		}

		public int get(float f) {
			return !((double)f >= 1.0) && this.lastValue != null ? this.interpolate(f, this.lastValue, this.currentValue) : this.currentValue;
		}
	}

	abstract static class Interpolator<T> {
		@Nullable
		protected T lastValue;
		protected T currentValue;

		protected Interpolator(T object) {
			this.currentValue = object;
		}

		public void updateValue(T object) {
			this.lastValue = this.currentValue;
			this.currentValue = object;
		}
	}

	static class InterpolatorSet {
		private final IntSet interpolatedData = new IntOpenHashSet();
		private final List<Consumer<SynchedEntityData>> updaters = new ArrayList();

		protected <T> void addEntry(EntityDataAccessor<T> entityDataAccessor, Display.Interpolator<T> interpolator) {
			this.interpolatedData.add(entityDataAccessor.getId());
			this.updaters.add((Consumer)synchedEntityData -> interpolator.updateValue(synchedEntityData.get(entityDataAccessor)));
		}

		protected void addEntry(Set<EntityDataAccessor<?>> set, Consumer<SynchedEntityData> consumer) {
			for (EntityDataAccessor<?> entityDataAccessor : set) {
				this.interpolatedData.add(entityDataAccessor.getId());
			}

			this.updaters.add(consumer);
		}

		public boolean shouldTriggerUpdate(int i) {
			return this.interpolatedData.contains(i);
		}

		public void updateValues(SynchedEntityData synchedEntityData) {
			for (Consumer<SynchedEntityData> consumer : this.updaters) {
				consumer.accept(synchedEntityData);
			}
		}
	}

	public static class ItemDisplay extends Display {
		private static final String TAG_ITEM = "item";
		private static final String TAG_ITEM_DISPLAY = "item_display";
		private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK_ID = SynchedEntityData.defineId(
			Display.ItemDisplay.class, EntityDataSerializers.ITEM_STACK
		);
		private static final EntityDataAccessor<Byte> DATA_ITEM_DISPLAY_ID = SynchedEntityData.defineId(Display.ItemDisplay.class, EntityDataSerializers.BYTE);
		private final SlotAccess slot = new SlotAccess() {
			@Override
			public ItemStack get() {
				return ItemDisplay.this.getItemStack();
			}

			@Override
			public boolean set(ItemStack itemStack) {
				ItemDisplay.this.setItemStack(itemStack);
				return true;
			}
		};

		public ItemDisplay(EntityType<?> entityType, Level level) {
			super(entityType, level);
		}

		@Override
		protected void defineSynchedData() {
			super.defineSynchedData();
			this.entityData.define(DATA_ITEM_STACK_ID, ItemStack.EMPTY);
			this.entityData.define(DATA_ITEM_DISPLAY_ID, ItemDisplayContext.FIXED.getId());
		}

		public ItemStack getItemStack() {
			return this.entityData.get(DATA_ITEM_STACK_ID);
		}

		void setItemStack(ItemStack itemStack) {
			this.entityData.set(DATA_ITEM_STACK_ID, itemStack);
		}

		private void setItemTransform(ItemDisplayContext itemDisplayContext) {
			this.entityData.set(DATA_ITEM_DISPLAY_ID, itemDisplayContext.getId());
		}

		public ItemDisplayContext getItemTransform() {
			return (ItemDisplayContext)ItemDisplayContext.BY_ID.apply(this.entityData.get(DATA_ITEM_DISPLAY_ID));
		}

		@Override
		protected void readAdditionalSaveData(CompoundTag compoundTag) {
			super.readAdditionalSaveData(compoundTag);
			this.setItemStack(ItemStack.of(compoundTag.getCompound("item")));
			if (compoundTag.contains("item_display", 8)) {
				ItemDisplayContext.CODEC
					.decode(NbtOps.INSTANCE, compoundTag.get("item_display"))
					.resultOrPartial(Util.prefix("Display entity", Display.LOGGER::error))
					.ifPresent(pair -> this.setItemTransform((ItemDisplayContext)pair.getFirst()));
			}
		}

		@Override
		protected void addAdditionalSaveData(CompoundTag compoundTag) {
			super.addAdditionalSaveData(compoundTag);
			compoundTag.put("item", this.getItemStack().save(new CompoundTag()));
			ItemDisplayContext.CODEC.encodeStart(NbtOps.INSTANCE, this.getItemTransform()).result().ifPresent(tag -> compoundTag.put("item_display", tag));
		}

		@Override
		public SlotAccess getSlot(int i) {
			return i == 0 ? this.slot : SlotAccess.NULL;
		}
	}

	public static class TextDisplay extends Display {
		public static final String TAG_TEXT = "text";
		private static final String TAG_LINE_WIDTH = "line_width";
		private static final String TAG_TEXT_OPACITY = "text_opacity";
		private static final String TAG_BACKGROUND_COLOR = "background";
		private static final String TAG_SHADOW = "shadow";
		private static final String TAG_SEE_THROUGH = "see_through";
		private static final String TAG_USE_DEFAULT_BACKGROUND = "default_background";
		private static final String TAG_ALIGNMENT = "alignment";
		public static final byte FLAG_SHADOW = 1;
		public static final byte FLAG_SEE_THROUGH = 2;
		public static final byte FLAG_USE_DEFAULT_BACKGROUND = 4;
		public static final byte FLAG_ALIGN_LEFT = 8;
		public static final byte FLAG_ALIGN_RIGHT = 16;
		private static final byte INITIAL_TEXT_OPACITY = -1;
		public static final int INITIAL_BACKGROUND = 1073741824;
		private static final EntityDataAccessor<Component> DATA_TEXT_ID = SynchedEntityData.defineId(Display.TextDisplay.class, EntityDataSerializers.COMPONENT);
		private static final EntityDataAccessor<Integer> DATA_LINE_WIDTH_ID = SynchedEntityData.defineId(Display.TextDisplay.class, EntityDataSerializers.INT);
		private static final EntityDataAccessor<Integer> DATA_BACKGROUND_COLOR_ID = SynchedEntityData.defineId(Display.TextDisplay.class, EntityDataSerializers.INT);
		private static final EntityDataAccessor<Byte> DATA_TEXT_OPACITY_ID = SynchedEntityData.defineId(Display.TextDisplay.class, EntityDataSerializers.BYTE);
		private static final EntityDataAccessor<Byte> DATA_STYLE_FLAGS_ID = SynchedEntityData.defineId(Display.TextDisplay.class, EntityDataSerializers.BYTE);
		private final Display.IntInterpolator textOpacity = new Display.IntInterpolator(-1);
		private final Display.IntInterpolator backgroundColor = new Display.ColorInterpolator(1073741824);
		@Nullable
		private Display.TextDisplay.CachedInfo clientDisplayCache;

		public TextDisplay(EntityType<?> entityType, Level level) {
			super(entityType, level);
			this.interpolators.addEntry(DATA_BACKGROUND_COLOR_ID, this.backgroundColor);
			this.interpolators
				.addEntry(
					Set.of(DATA_TEXT_OPACITY_ID), synchedEntityData -> this.textOpacity.updateValue(Integer.valueOf(synchedEntityData.get(DATA_TEXT_OPACITY_ID) & 255))
				);
		}

		@Override
		protected void defineSynchedData() {
			super.defineSynchedData();
			this.entityData.define(DATA_TEXT_ID, Component.empty());
			this.entityData.define(DATA_LINE_WIDTH_ID, 200);
			this.entityData.define(DATA_BACKGROUND_COLOR_ID, 1073741824);
			this.entityData.define(DATA_TEXT_OPACITY_ID, (byte)-1);
			this.entityData.define(DATA_STYLE_FLAGS_ID, (byte)0);
		}

		@Override
		public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
			super.onSyncedDataUpdated(entityDataAccessor);
			this.clientDisplayCache = null;
		}

		public Component getText() {
			return this.entityData.get(DATA_TEXT_ID);
		}

		private void setText(Component component) {
			this.entityData.set(DATA_TEXT_ID, component);
		}

		public int getLineWidth() {
			return this.entityData.get(DATA_LINE_WIDTH_ID);
		}

		private void setLineWidth(int i) {
			this.entityData.set(DATA_LINE_WIDTH_ID, i);
		}

		public byte getTextOpacity(float f) {
			return (byte)this.textOpacity.get(f);
		}

		private byte getTextOpacity() {
			return this.entityData.get(DATA_TEXT_OPACITY_ID);
		}

		private void setTextOpacity(byte b) {
			this.entityData.set(DATA_TEXT_OPACITY_ID, b);
		}

		public int getBackgroundColor(float f) {
			return this.backgroundColor.get(f);
		}

		private int getBackgroundColor() {
			return this.entityData.get(DATA_BACKGROUND_COLOR_ID);
		}

		private void setBackgroundColor(int i) {
			this.entityData.set(DATA_BACKGROUND_COLOR_ID, i);
		}

		public byte getFlags() {
			return this.entityData.get(DATA_STYLE_FLAGS_ID);
		}

		private void setFlags(byte b) {
			this.entityData.set(DATA_STYLE_FLAGS_ID, b);
		}

		private static byte loadFlag(byte b, CompoundTag compoundTag, String string, byte c) {
			return compoundTag.getBoolean(string) ? (byte)(b | c) : b;
		}

		@Override
		protected void readAdditionalSaveData(CompoundTag compoundTag) {
			super.readAdditionalSaveData(compoundTag);
			if (compoundTag.contains("line_width", 99)) {
				this.setLineWidth(compoundTag.getInt("line_width"));
			}

			if (compoundTag.contains("text_opacity", 99)) {
				this.setTextOpacity(compoundTag.getByte("text_opacity"));
			}

			if (compoundTag.contains("background", 99)) {
				this.setBackgroundColor(compoundTag.getInt("background"));
			}

			byte b = loadFlag((byte)0, compoundTag, "shadow", (byte)1);
			b = loadFlag(b, compoundTag, "see_through", (byte)2);
			b = loadFlag(b, compoundTag, "default_background", (byte)4);
			Optional<Display.TextDisplay.Align> optional = Display.TextDisplay.Align.CODEC
				.decode(NbtOps.INSTANCE, compoundTag.get("alignment"))
				.resultOrPartial(Util.prefix("Display entity", Display.LOGGER::error))
				.map(Pair::getFirst);
			if (optional.isPresent()) {
				b = switch ((Display.TextDisplay.Align)optional.get()) {
					case CENTER -> b;
					case LEFT -> (byte)(b | 8);
					case RIGHT -> (byte)(b | 16);
				};
			}

			this.setFlags(b);
			if (compoundTag.contains("text", 8)) {
				String string = compoundTag.getString("text");

				try {
					Component component = Component.Serializer.fromJson(string);
					if (component != null) {
						CommandSourceStack commandSourceStack = this.createCommandSourceStack().withPermission(2);
						Component component2 = ComponentUtils.updateForEntity(commandSourceStack, component, this, 0);
						this.setText(component2);
					} else {
						this.setText(Component.empty());
					}
				} catch (Exception var8) {
					Display.LOGGER.warn("Failed to parse display entity text {}", string, var8);
				}
			}
		}

		private static void storeFlag(byte b, CompoundTag compoundTag, String string, byte c) {
			compoundTag.putBoolean(string, (b & c) != 0);
		}

		@Override
		protected void addAdditionalSaveData(CompoundTag compoundTag) {
			super.addAdditionalSaveData(compoundTag);
			compoundTag.putString("text", Component.Serializer.toJson(this.getText()));
			compoundTag.putInt("line_width", this.getLineWidth());
			compoundTag.putInt("background", this.getBackgroundColor());
			compoundTag.putByte("text_opacity", this.getTextOpacity());
			byte b = this.getFlags();
			storeFlag(b, compoundTag, "shadow", (byte)1);
			storeFlag(b, compoundTag, "see_through", (byte)2);
			storeFlag(b, compoundTag, "default_background", (byte)4);
			Display.TextDisplay.Align.CODEC.encodeStart(NbtOps.INSTANCE, getAlign(b)).result().ifPresent(tag -> compoundTag.put("alignment", tag));
		}

		public Display.TextDisplay.CachedInfo cacheDisplay(Display.TextDisplay.LineSplitter lineSplitter) {
			if (this.clientDisplayCache == null) {
				int i = this.getLineWidth();
				this.clientDisplayCache = lineSplitter.split(this.getText(), i);
			}

			return this.clientDisplayCache;
		}

		public static Display.TextDisplay.Align getAlign(byte b) {
			if ((b & 8) != 0) {
				return Display.TextDisplay.Align.LEFT;
			} else {
				return (b & 16) != 0 ? Display.TextDisplay.Align.RIGHT : Display.TextDisplay.Align.CENTER;
			}
		}

		public static enum Align implements StringRepresentable {
			CENTER("center"),
			LEFT("left"),
			RIGHT("right");

			public static final Codec<Display.TextDisplay.Align> CODEC = StringRepresentable.fromEnum(Display.TextDisplay.Align::values);
			private final String name;

			private Align(String string2) {
				this.name = string2;
			}

			@Override
			public String getSerializedName() {
				return this.name;
			}
		}

		public static record CachedInfo(List<Display.TextDisplay.CachedLine> lines, int width) {
		}

		public static record CachedLine(FormattedCharSequence contents, int width) {
		}

		@FunctionalInterface
		public interface LineSplitter {
			Display.TextDisplay.CachedInfo split(Component component, int i);
		}
	}
}
