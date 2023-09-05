package net.minecraft.world.entity;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.List;
import java.util.Optional;
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
	public static final int NO_BRIGHTNESS_OVERRIDE = -1;
	private static final EntityDataAccessor<Integer> DATA_TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS_ID = SynchedEntityData.defineId(
		Display.class, EntityDataSerializers.INT
	);
	private static final EntityDataAccessor<Integer> DATA_TRANSFORMATION_INTERPOLATION_DURATION_ID = SynchedEntityData.defineId(
		Display.class, EntityDataSerializers.INT
	);
	private static final EntityDataAccessor<Integer> DATA_POS_ROT_INTERPOLATION_DURATION_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.INT);
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
	private static final IntSet RENDER_STATE_IDS = IntSet.of(
		DATA_TRANSLATION_ID.getId(),
		DATA_SCALE_ID.getId(),
		DATA_LEFT_ROTATION_ID.getId(),
		DATA_RIGHT_ROTATION_ID.getId(),
		DATA_BILLBOARD_RENDER_CONSTRAINTS_ID.getId(),
		DATA_BRIGHTNESS_OVERRIDE_ID.getId(),
		DATA_SHADOW_RADIUS_ID.getId(),
		DATA_SHADOW_STRENGTH_ID.getId()
	);
	private static final float INITIAL_SHADOW_RADIUS = 0.0F;
	private static final float INITIAL_SHADOW_STRENGTH = 1.0F;
	private static final int NO_GLOW_COLOR_OVERRIDE = -1;
	public static final String TAG_POS_ROT_INTERPOLATION_DURATION = "teleport_duration";
	public static final String TAG_TRANSFORMATION_INTERPOLATION_DURATION = "interpolation_duration";
	public static final String TAG_TRANSFORMATION_START_INTERPOLATION = "start_interpolation";
	public static final String TAG_TRANSFORMATION = "transformation";
	public static final String TAG_BILLBOARD = "billboard";
	public static final String TAG_BRIGHTNESS = "brightness";
	public static final String TAG_VIEW_RANGE = "view_range";
	public static final String TAG_SHADOW_RADIUS = "shadow_radius";
	public static final String TAG_SHADOW_STRENGTH = "shadow_strength";
	public static final String TAG_WIDTH = "width";
	public static final String TAG_HEIGHT = "height";
	public static final String TAG_GLOW_COLOR_OVERRIDE = "glow_color_override";
	private long interpolationStartClientTick = -2147483648L;
	private int interpolationDuration;
	private float lastProgress;
	private AABB cullingBoundingBox;
	protected boolean updateRenderState;
	private boolean updateStartTick;
	private boolean updateInterpolationDuration;
	@Nullable
	private Display.RenderState renderState;
	@Nullable
	private Display.PosRotInterpolationTarget posRotInterpolationTarget;

	public Display(EntityType<?> entityType, Level level) {
		super(entityType, level);
		this.noPhysics = true;
		this.noCulling = true;
		this.cullingBoundingBox = this.getBoundingBox();
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		super.onSyncedDataUpdated(entityDataAccessor);
		if (DATA_HEIGHT_ID.equals(entityDataAccessor) || DATA_WIDTH_ID.equals(entityDataAccessor)) {
			this.updateCulling();
		}

		if (DATA_TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS_ID.equals(entityDataAccessor)) {
			this.updateStartTick = true;
		}

		if (DATA_TRANSFORMATION_INTERPOLATION_DURATION_ID.equals(entityDataAccessor)) {
			this.updateInterpolationDuration = true;
		}

		if (RENDER_STATE_IDS.contains(entityDataAccessor.getId())) {
			this.updateRenderState = true;
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
		Entity entity = this.getVehicle();
		if (entity != null && entity.isRemoved()) {
			this.stopRiding();
		}

		if (this.level().isClientSide) {
			if (this.updateStartTick) {
				this.updateStartTick = false;
				int i = this.getTransformationInterpolationDelay();
				this.interpolationStartClientTick = (long)(this.tickCount + i);
			}

			if (this.updateInterpolationDuration) {
				this.updateInterpolationDuration = false;
				this.interpolationDuration = this.getTransformationInterpolationDuration();
			}

			if (this.updateRenderState) {
				this.updateRenderState = false;
				boolean bl = this.interpolationDuration != 0;
				if (bl && this.renderState != null) {
					this.renderState = this.createInterpolatedRenderState(this.renderState, this.lastProgress);
				} else {
					this.renderState = this.createFreshRenderState();
				}

				this.updateRenderSubState(bl, this.lastProgress);
			}

			if (this.posRotInterpolationTarget != null) {
				if (this.posRotInterpolationTarget.steps == 0) {
					this.posRotInterpolationTarget.applyTargetPosAndRot(this);
					this.setOldPosAndRot();
					this.posRotInterpolationTarget = null;
				} else {
					this.posRotInterpolationTarget.applyLerpStep(this);
					this.posRotInterpolationTarget.steps--;
					if (this.posRotInterpolationTarget.steps == 0) {
						this.posRotInterpolationTarget = null;
					}
				}
			}
		}
	}

	protected abstract void updateRenderSubState(boolean bl, float f);

	@Override
	protected void defineSynchedData() {
		this.entityData.define(DATA_POS_ROT_INTERPOLATION_DURATION_ID, 0);
		this.entityData.define(DATA_TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS_ID, 0);
		this.entityData.define(DATA_TRANSFORMATION_INTERPOLATION_DURATION_ID, 0);
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
		this.entityData.define(DATA_GLOW_COLOR_OVERRIDE_ID, -1);
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
			this.setTransformationInterpolationDuration(i);
		}

		if (compoundTag.contains("start_interpolation", 99)) {
			int i = compoundTag.getInt("start_interpolation");
			this.setTransformationInterpolationDelay(i);
		}

		if (compoundTag.contains("teleport_duration", 99)) {
			int i = compoundTag.getInt("teleport_duration");
			this.setPosRotInterpolationDuration(Mth.clamp(i, 0, 59));
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
		compoundTag.putInt("interpolation_duration", this.getTransformationInterpolationDuration());
		compoundTag.putInt("teleport_duration", this.getPosRotInterpolationDuration());
		compoundTag.putFloat("view_range", this.getViewRange());
		compoundTag.putFloat("shadow_radius", this.getShadowRadius());
		compoundTag.putFloat("shadow_strength", this.getShadowStrength());
		compoundTag.putFloat("width", this.getWidth());
		compoundTag.putFloat("height", this.getHeight());
		compoundTag.putInt("glow_color_override", this.getGlowColorOverride());
		Brightness brightness = this.getBrightnessOverride();
		if (brightness != null) {
			Brightness.CODEC.encodeStart(NbtOps.INSTANCE, brightness).result().ifPresent(tag -> compoundTag.put("brightness", tag));
		}
	}

	@Override
	public void lerpTo(double d, double e, double f, float g, float h, int i) {
		int j = this.getPosRotInterpolationDuration();
		this.posRotInterpolationTarget = new Display.PosRotInterpolationTarget(j, d, e, f, (double)g, (double)h);
	}

	@Override
	public double lerpTargetX() {
		return this.posRotInterpolationTarget != null ? this.posRotInterpolationTarget.targetX : this.getX();
	}

	@Override
	public double lerpTargetY() {
		return this.posRotInterpolationTarget != null ? this.posRotInterpolationTarget.targetY : this.getY();
	}

	@Override
	public double lerpTargetZ() {
		return this.posRotInterpolationTarget != null ? this.posRotInterpolationTarget.targetZ : this.getZ();
	}

	@Override
	public float lerpTargetXRot() {
		return this.posRotInterpolationTarget != null ? (float)this.posRotInterpolationTarget.targetXRot : this.getXRot();
	}

	@Override
	public float lerpTargetYRot() {
		return this.posRotInterpolationTarget != null ? (float)this.posRotInterpolationTarget.targetYRot : this.getYRot();
	}

	@Override
	public AABB getBoundingBoxForCulling() {
		return this.cullingBoundingBox;
	}

	@Override
	public PushReaction getPistonPushReaction() {
		return PushReaction.IGNORE;
	}

	@Override
	public boolean isIgnoringBlockTriggers() {
		return true;
	}

	@Nullable
	public Display.RenderState renderState() {
		return this.renderState;
	}

	private void setTransformationInterpolationDuration(int i) {
		this.entityData.set(DATA_TRANSFORMATION_INTERPOLATION_DURATION_ID, i);
	}

	private int getTransformationInterpolationDuration() {
		return this.entityData.get(DATA_TRANSFORMATION_INTERPOLATION_DURATION_ID);
	}

	private void setTransformationInterpolationDelay(int i) {
		this.entityData.set(DATA_TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS_ID, i, true);
	}

	private int getTransformationInterpolationDelay() {
		return this.entityData.get(DATA_TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS_ID);
	}

	private void setPosRotInterpolationDuration(int i) {
		this.entityData.set(DATA_POS_ROT_INTERPOLATION_DURATION_ID, i);
	}

	private int getPosRotInterpolationDuration() {
		return this.entityData.get(DATA_POS_ROT_INTERPOLATION_DURATION_ID);
	}

	private void setBillboardConstraints(Display.BillboardConstraints billboardConstraints) {
		this.entityData.set(DATA_BILLBOARD_RENDER_CONSTRAINTS_ID, billboardConstraints.getId());
	}

	private Display.BillboardConstraints getBillboardConstraints() {
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

	private int getPackedBrightnessOverride() {
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

	private void setShadowStrength(float f) {
		this.entityData.set(DATA_SHADOW_STRENGTH_ID, f);
	}

	private float getShadowStrength() {
		return this.entityData.get(DATA_SHADOW_STRENGTH_ID);
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

	public float calculateInterpolationProgress(float f) {
		int i = this.interpolationDuration;
		if (i <= 0) {
			return 1.0F;
		} else {
			float g = (float)((long)this.tickCount - this.interpolationStartClientTick);
			float h = g + f;
			float j = Mth.clamp(Mth.inverseLerp(h, 0.0F, (float)i), 0.0F, 1.0F);
			this.lastProgress = j;
			return j;
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
	public boolean shouldRenderAtSqrDistance(double d) {
		return d < Mth.square((double)this.getViewRange() * 64.0 * getViewScale());
	}

	@Override
	public int getTeamColor() {
		int i = this.getGlowColorOverride();
		return i != -1 ? i : super.getTeamColor();
	}

	private Display.RenderState createFreshRenderState() {
		return new Display.RenderState(
			Display.GenericInterpolator.constant(createTransformation(this.entityData)),
			this.getBillboardConstraints(),
			this.getPackedBrightnessOverride(),
			Display.FloatInterpolator.constant(this.getShadowRadius()),
			Display.FloatInterpolator.constant(this.getShadowStrength()),
			this.getGlowColorOverride()
		);
	}

	private Display.RenderState createInterpolatedRenderState(Display.RenderState renderState, float f) {
		Transformation transformation = renderState.transformation.get(f);
		float g = renderState.shadowRadius.get(f);
		float h = renderState.shadowStrength.get(f);
		return new Display.RenderState(
			new Display.TransformationInterpolator(transformation, createTransformation(this.entityData)),
			this.getBillboardConstraints(),
			this.getPackedBrightnessOverride(),
			new Display.LinearFloatInterpolator(g, this.getShadowRadius()),
			new Display.LinearFloatInterpolator(h, this.getShadowStrength()),
			this.getGlowColorOverride()
		);
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
		@Nullable
		private Display.BlockDisplay.BlockRenderState blockRenderState;

		public BlockDisplay(EntityType<?> entityType, Level level) {
			super(entityType, level);
		}

		@Override
		protected void defineSynchedData() {
			super.defineSynchedData();
			this.entityData.define(DATA_BLOCK_STATE_ID, Blocks.AIR.defaultBlockState());
		}

		@Override
		public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
			super.onSyncedDataUpdated(entityDataAccessor);
			if (entityDataAccessor.equals(DATA_BLOCK_STATE_ID)) {
				this.updateRenderState = true;
			}
		}

		private BlockState getBlockState() {
			return this.entityData.get(DATA_BLOCK_STATE_ID);
		}

		private void setBlockState(BlockState blockState) {
			this.entityData.set(DATA_BLOCK_STATE_ID, blockState);
		}

		@Override
		protected void readAdditionalSaveData(CompoundTag compoundTag) {
			super.readAdditionalSaveData(compoundTag);
			this.setBlockState(NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), compoundTag.getCompound("block_state")));
		}

		@Override
		protected void addAdditionalSaveData(CompoundTag compoundTag) {
			super.addAdditionalSaveData(compoundTag);
			compoundTag.put("block_state", NbtUtils.writeBlockState(this.getBlockState()));
		}

		@Nullable
		public Display.BlockDisplay.BlockRenderState blockRenderState() {
			return this.blockRenderState;
		}

		@Override
		protected void updateRenderSubState(boolean bl, float f) {
			this.blockRenderState = new Display.BlockDisplay.BlockRenderState(this.getBlockState());
		}

		public static record BlockRenderState(BlockState blockState) {
		}
	}

	static record ColorInterpolator(int previous, int current) implements Display.IntInterpolator {
		@Override
		public int get(float f) {
			return FastColor.ARGB32.lerp(f, this.previous, this.current);
		}
	}

	@FunctionalInterface
	public interface FloatInterpolator {
		static Display.FloatInterpolator constant(float f) {
			return g -> f;
		}

		float get(float f);
	}

	@FunctionalInterface
	public interface GenericInterpolator<T> {
		static <T> Display.GenericInterpolator<T> constant(T object) {
			return f -> object;
		}

		T get(float f);
	}

	@FunctionalInterface
	public interface IntInterpolator {
		static Display.IntInterpolator constant(int i) {
			return f -> i;
		}

		int get(float f);
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
		@Nullable
		private Display.ItemDisplay.ItemRenderState itemRenderState;

		public ItemDisplay(EntityType<?> entityType, Level level) {
			super(entityType, level);
		}

		@Override
		protected void defineSynchedData() {
			super.defineSynchedData();
			this.entityData.define(DATA_ITEM_STACK_ID, ItemStack.EMPTY);
			this.entityData.define(DATA_ITEM_DISPLAY_ID, ItemDisplayContext.NONE.getId());
		}

		@Override
		public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
			super.onSyncedDataUpdated(entityDataAccessor);
			if (DATA_ITEM_STACK_ID.equals(entityDataAccessor) || DATA_ITEM_DISPLAY_ID.equals(entityDataAccessor)) {
				this.updateRenderState = true;
			}
		}

		ItemStack getItemStack() {
			return this.entityData.get(DATA_ITEM_STACK_ID);
		}

		void setItemStack(ItemStack itemStack) {
			this.entityData.set(DATA_ITEM_STACK_ID, itemStack);
		}

		private void setItemTransform(ItemDisplayContext itemDisplayContext) {
			this.entityData.set(DATA_ITEM_DISPLAY_ID, itemDisplayContext.getId());
		}

		private ItemDisplayContext getItemTransform() {
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

		@Nullable
		public Display.ItemDisplay.ItemRenderState itemRenderState() {
			return this.itemRenderState;
		}

		@Override
		protected void updateRenderSubState(boolean bl, float f) {
			this.itemRenderState = new Display.ItemDisplay.ItemRenderState(this.getItemStack(), this.getItemTransform());
		}

		public static record ItemRenderState(ItemStack itemStack, ItemDisplayContext itemTransform) {
		}
	}

	static record LinearFloatInterpolator(float previous, float current) implements Display.FloatInterpolator {
		@Override
		public float get(float f) {
			return Mth.lerp(f, this.previous, this.current);
		}
	}

	static record LinearIntInterpolator(int previous, int current) implements Display.IntInterpolator {
		@Override
		public int get(float f) {
			return Mth.lerpInt(f, this.previous, this.current);
		}
	}

	static class PosRotInterpolationTarget {
		int steps;
		final double targetX;
		final double targetY;
		final double targetZ;
		final double targetYRot;
		final double targetXRot;

		PosRotInterpolationTarget(int i, double d, double e, double f, double g, double h) {
			this.steps = i;
			this.targetX = d;
			this.targetY = e;
			this.targetZ = f;
			this.targetYRot = g;
			this.targetXRot = h;
		}

		void applyTargetPosAndRot(Entity entity) {
			entity.setPos(this.targetX, this.targetY, this.targetZ);
			entity.setRot((float)this.targetYRot, (float)this.targetXRot);
		}

		void applyLerpStep(Entity entity) {
			entity.lerpPositionAndRotationStep(this.steps, this.targetX, this.targetY, this.targetZ, this.targetYRot, this.targetXRot);
		}
	}

	public static record RenderState(
		Display.GenericInterpolator<Transformation> transformation,
		Display.BillboardConstraints billboardConstraints,
		int brightnessOverride,
		Display.FloatInterpolator shadowRadius,
		Display.FloatInterpolator shadowStrength,
		int glowColorOverride
	) {
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
		private static final IntSet TEXT_RENDER_STATE_IDS = IntSet.of(
			DATA_TEXT_ID.getId(), DATA_LINE_WIDTH_ID.getId(), DATA_BACKGROUND_COLOR_ID.getId(), DATA_TEXT_OPACITY_ID.getId(), DATA_STYLE_FLAGS_ID.getId()
		);
		@Nullable
		private Display.TextDisplay.CachedInfo clientDisplayCache;
		@Nullable
		private Display.TextDisplay.TextRenderState textRenderState;

		public TextDisplay(EntityType<?> entityType, Level level) {
			super(entityType, level);
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
			if (TEXT_RENDER_STATE_IDS.contains(entityDataAccessor.getId())) {
				this.updateRenderState = true;
			}
		}

		private Component getText() {
			return this.entityData.get(DATA_TEXT_ID);
		}

		private void setText(Component component) {
			this.entityData.set(DATA_TEXT_ID, component);
		}

		private int getLineWidth() {
			return this.entityData.get(DATA_LINE_WIDTH_ID);
		}

		private void setLineWidth(int i) {
			this.entityData.set(DATA_LINE_WIDTH_ID, i);
		}

		private byte getTextOpacity() {
			return this.entityData.get(DATA_TEXT_OPACITY_ID);
		}

		private void setTextOpacity(byte b) {
			this.entityData.set(DATA_TEXT_OPACITY_ID, b);
		}

		private int getBackgroundColor() {
			return this.entityData.get(DATA_BACKGROUND_COLOR_ID);
		}

		private void setBackgroundColor(int i) {
			this.entityData.set(DATA_BACKGROUND_COLOR_ID, i);
		}

		private byte getFlags() {
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

		@Override
		protected void updateRenderSubState(boolean bl, float f) {
			if (bl && this.textRenderState != null) {
				this.textRenderState = this.createInterpolatedTextRenderState(this.textRenderState, f);
			} else {
				this.textRenderState = this.createFreshTextRenderState();
			}

			this.clientDisplayCache = null;
		}

		@Nullable
		public Display.TextDisplay.TextRenderState textRenderState() {
			return this.textRenderState;
		}

		private Display.TextDisplay.TextRenderState createFreshTextRenderState() {
			return new Display.TextDisplay.TextRenderState(
				this.getText(),
				this.getLineWidth(),
				Display.IntInterpolator.constant(this.getTextOpacity()),
				Display.IntInterpolator.constant(this.getBackgroundColor()),
				this.getFlags()
			);
		}

		private Display.TextDisplay.TextRenderState createInterpolatedTextRenderState(Display.TextDisplay.TextRenderState textRenderState, float f) {
			int i = textRenderState.backgroundColor.get(f);
			int j = textRenderState.textOpacity.get(f);
			return new Display.TextDisplay.TextRenderState(
				this.getText(),
				this.getLineWidth(),
				new Display.LinearIntInterpolator(j, this.getTextOpacity()),
				new Display.ColorInterpolator(i, this.getBackgroundColor()),
				this.getFlags()
			);
		}

		public Display.TextDisplay.CachedInfo cacheDisplay(Display.TextDisplay.LineSplitter lineSplitter) {
			if (this.clientDisplayCache == null) {
				if (this.textRenderState != null) {
					this.clientDisplayCache = lineSplitter.split(this.textRenderState.text(), this.textRenderState.lineWidth());
				} else {
					this.clientDisplayCache = new Display.TextDisplay.CachedInfo(List.of(), 0);
				}
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

		public static record TextRenderState(Component text, int lineWidth, Display.IntInterpolator textOpacity, Display.IntInterpolator backgroundColor, byte flags) {
		}
	}

	static record TransformationInterpolator(Transformation previous, Transformation current) implements Display.GenericInterpolator<Transformation> {
		public Transformation get(float f) {
			return (double)f >= 1.0 ? this.current : this.previous.slerp(this.current, f);
		}
	}
}
