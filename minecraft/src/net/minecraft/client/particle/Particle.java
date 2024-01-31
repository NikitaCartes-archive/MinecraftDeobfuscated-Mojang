package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public abstract class Particle {
	private static final AABB INITIAL_AABB = new AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
	private static final double MAXIMUM_COLLISION_VELOCITY_SQUARED = Mth.square(100.0);
	protected final ClientLevel level;
	protected double xo;
	protected double yo;
	protected double zo;
	protected double x;
	protected double y;
	protected double z;
	protected double xd;
	protected double yd;
	protected double zd;
	private AABB bb = INITIAL_AABB;
	protected boolean onGround;
	protected boolean hasPhysics = true;
	private boolean stoppedByCollision;
	protected boolean removed;
	protected float bbWidth = 0.6F;
	protected float bbHeight = 1.8F;
	protected final RandomSource random = RandomSource.create();
	protected int age;
	protected int lifetime;
	protected float gravity;
	protected float rCol = 1.0F;
	protected float gCol = 1.0F;
	protected float bCol = 1.0F;
	protected float alpha = 1.0F;
	protected float roll;
	protected float oRoll;
	protected float friction = 0.98F;
	protected boolean speedUpWhenYMotionIsBlocked = false;

	protected Particle(ClientLevel clientLevel, double d, double e, double f) {
		this.level = clientLevel;
		this.setSize(0.2F, 0.2F);
		this.setPos(d, e, f);
		this.xo = d;
		this.yo = e;
		this.zo = f;
		this.lifetime = (int)(4.0F / (this.random.nextFloat() * 0.9F + 0.1F));
	}

	public Particle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
		this(clientLevel, d, e, f);
		this.xd = g + (Math.random() * 2.0 - 1.0) * 0.4F;
		this.yd = h + (Math.random() * 2.0 - 1.0) * 0.4F;
		this.zd = i + (Math.random() * 2.0 - 1.0) * 0.4F;
		double j = (Math.random() + Math.random() + 1.0) * 0.15F;
		double k = Math.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);
		this.xd = this.xd / k * j * 0.4F;
		this.yd = this.yd / k * j * 0.4F + 0.1F;
		this.zd = this.zd / k * j * 0.4F;
	}

	public Particle setPower(float f) {
		this.xd *= (double)f;
		this.yd = (this.yd - 0.1F) * (double)f + 0.1F;
		this.zd *= (double)f;
		return this;
	}

	public void setParticleSpeed(double d, double e, double f) {
		this.xd = d;
		this.yd = e;
		this.zd = f;
	}

	public Particle scale(float f) {
		this.setSize(0.2F * f, 0.2F * f);
		return this;
	}

	public void setColor(float f, float g, float h) {
		this.rCol = f;
		this.gCol = g;
		this.bCol = h;
	}

	protected void setAlpha(float f) {
		this.alpha = f;
	}

	public void setLifetime(int i) {
		this.lifetime = i;
	}

	public int getLifetime() {
		return this.lifetime;
	}

	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.age++ >= this.lifetime) {
			this.remove();
		} else {
			this.yd = this.yd - 0.04 * (double)this.gravity;
			this.move(this.xd, this.yd, this.zd);
			if (this.speedUpWhenYMotionIsBlocked && this.y == this.yo) {
				this.xd *= 1.1;
				this.zd *= 1.1;
			}

			this.xd = this.xd * (double)this.friction;
			this.yd = this.yd * (double)this.friction;
			this.zd = this.zd * (double)this.friction;
			if (this.onGround) {
				this.xd *= 0.7F;
				this.zd *= 0.7F;
			}
		}
	}

	public abstract void render(VertexConsumer vertexConsumer, Camera camera, float f);

	public abstract ParticleRenderType getRenderType();

	public String toString() {
		return this.getClass().getSimpleName()
			+ ", Pos ("
			+ this.x
			+ ","
			+ this.y
			+ ","
			+ this.z
			+ "), RGBA ("
			+ this.rCol
			+ ","
			+ this.gCol
			+ ","
			+ this.bCol
			+ ","
			+ this.alpha
			+ "), Age "
			+ this.age;
	}

	public void remove() {
		this.removed = true;
	}

	protected void setSize(float f, float g) {
		if (f != this.bbWidth || g != this.bbHeight) {
			this.bbWidth = f;
			this.bbHeight = g;
			AABB aABB = this.getBoundingBox();
			double d = (aABB.minX + aABB.maxX - (double)f) / 2.0;
			double e = (aABB.minZ + aABB.maxZ - (double)f) / 2.0;
			this.setBoundingBox(new AABB(d, aABB.minY, e, d + (double)this.bbWidth, aABB.minY + (double)this.bbHeight, e + (double)this.bbWidth));
		}
	}

	public void setPos(double d, double e, double f) {
		this.x = d;
		this.y = e;
		this.z = f;
		float g = this.bbWidth / 2.0F;
		float h = this.bbHeight;
		this.setBoundingBox(new AABB(d - (double)g, e, f - (double)g, d + (double)g, e + (double)h, f + (double)g));
	}

	public void move(double d, double e, double f) {
		if (!this.stoppedByCollision) {
			double g = d;
			double h = e;
			double i = f;
			if (this.hasPhysics && (d != 0.0 || e != 0.0 || f != 0.0) && d * d + e * e + f * f < MAXIMUM_COLLISION_VELOCITY_SQUARED) {
				Vec3 vec3 = Entity.collideBoundingBox(null, new Vec3(d, e, f), this.getBoundingBox(), this.level, List.of());
				d = vec3.x;
				e = vec3.y;
				f = vec3.z;
			}

			if (d != 0.0 || e != 0.0 || f != 0.0) {
				this.setBoundingBox(this.getBoundingBox().move(d, e, f));
				this.setLocationFromBoundingbox();
			}

			if (Math.abs(h) >= 1.0E-5F && Math.abs(e) < 1.0E-5F) {
				this.stoppedByCollision = true;
			}

			this.onGround = h != e && h < 0.0;
			if (g != d) {
				this.xd = 0.0;
			}

			if (i != f) {
				this.zd = 0.0;
			}
		}
	}

	protected void setLocationFromBoundingbox() {
		AABB aABB = this.getBoundingBox();
		this.x = (aABB.minX + aABB.maxX) / 2.0;
		this.y = aABB.minY;
		this.z = (aABB.minZ + aABB.maxZ) / 2.0;
	}

	protected int getLightColor(float f) {
		BlockPos blockPos = BlockPos.containing(this.x, this.y, this.z);
		return this.level.hasChunkAt(blockPos) ? LevelRenderer.getLightColor(this.level, blockPos) : 0;
	}

	public boolean isAlive() {
		return !this.removed;
	}

	public AABB getBoundingBox() {
		return this.bb;
	}

	public void setBoundingBox(AABB aABB) {
		this.bb = aABB;
	}

	public Optional<ParticleGroup> getParticleGroup() {
		return Optional.empty();
	}

	@Environment(EnvType.CLIENT)
	public static record LifetimeAlpha(float startAlpha, float endAlpha, float startAtNormalizedAge, float endAtNormalizedAge) {
		public static final Particle.LifetimeAlpha ALWAYS_OPAQUE = new Particle.LifetimeAlpha(1.0F, 1.0F, 0.0F, 1.0F);

		public boolean isOpaque() {
			return this.startAlpha >= 1.0F && this.endAlpha >= 1.0F;
		}

		public float currentAlphaForAge(int i, int j, float f) {
			if (Mth.equal(this.startAlpha, this.endAlpha)) {
				return this.startAlpha;
			} else {
				float g = Mth.inverseLerp(((float)i + f) / (float)j, this.startAtNormalizedAge, this.endAtNormalizedAge);
				return Mth.clampedLerp(this.startAlpha, this.endAlpha, g);
			}
		}
	}
}
