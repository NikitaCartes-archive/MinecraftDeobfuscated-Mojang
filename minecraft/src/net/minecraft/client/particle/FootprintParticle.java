package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class FootprintParticle extends Particle {
	private final TextureAtlasSprite sprite;
	private final float rot;

	protected FootprintParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, SpriteSet spriteSet) {
		super(clientLevel, d, e, f);
		this.xd = 0.0;
		this.yd = 0.0;
		this.zd = 0.0;
		this.rot = (float)g;
		this.lifetime = 200;
		this.gravity = 0.0F;
		this.hasPhysics = false;
		this.sprite = spriteSet.get(this.random);
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
		float g = ((float)this.age + f) / (float)this.lifetime;
		g *= g;
		float h = 2.0F - g * 2.0F;
		h *= 0.2F;
		float i = 0.125F;
		Vec3 vec3 = camera.getPosition();
		float j = (float)(this.x - vec3.x);
		float k = (float)(this.y - vec3.y);
		float l = (float)(this.z - vec3.z);
		int m = this.getLightColor(f);
		float n = this.sprite.getU0();
		float o = this.sprite.getU1();
		float p = this.sprite.getV0();
		float q = this.sprite.getV1();
		Matrix4f matrix4f = new Matrix4f().translation(j, k, l);
		matrix4f.rotate((float) (Math.PI / 180.0) * this.rot, 0.0F, 1.0F, 0.0F);
		vertexConsumer.vertex(matrix4f, -0.125F, 0.0F, 0.125F).uv(n, q).color(this.rCol, this.gCol, this.bCol, h).uv2(m).endVertex();
		vertexConsumer.vertex(matrix4f, 0.125F, 0.0F, 0.125F).uv(o, q).color(this.rCol, this.gCol, this.bCol, h).uv2(m).endVertex();
		vertexConsumer.vertex(matrix4f, 0.125F, 0.0F, -0.125F).uv(o, p).color(this.rCol, this.gCol, this.bCol, h).uv2(m).endVertex();
		vertexConsumer.vertex(matrix4f, -0.125F, 0.0F, -0.125F).uv(n, p).color(this.rCol, this.gCol, this.bCol, h).uv2(m).endVertex();
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Provider(SpriteSet spriteSet) {
			this.sprites = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			return new FootprintParticle(clientLevel, d, e, f, g, h, i, this.sprites);
		}
	}
}
