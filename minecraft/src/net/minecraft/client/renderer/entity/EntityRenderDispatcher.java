package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Map;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Culler;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.Direction;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.TraderLlama;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.fishing.FishingHook;
import net.minecraft.world.entity.global.LightningBolt;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.PigZombie;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.MinecartSpawner;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class EntityRenderDispatcher {
	private final Map<Class<? extends Entity>, EntityRenderer<? extends Entity>> renderers = Maps.<Class<? extends Entity>, EntityRenderer<? extends Entity>>newHashMap();
	private final Map<String, PlayerRenderer> playerRenderers = Maps.<String, PlayerRenderer>newHashMap();
	private final PlayerRenderer defaultPlayerRenderer;
	private Font font;
	private double xOff;
	private double yOff;
	private double zOff;
	public final TextureManager textureManager;
	public Level level;
	public Camera camera;
	public Entity crosshairPickEntity;
	public float playerRotY;
	public float playerRotX;
	public Options options;
	private boolean solidRender;
	private boolean shouldRenderShadow = true;
	private boolean renderHitBoxes;

	private <T extends Entity> void register(Class<T> class_, EntityRenderer<? super T> entityRenderer) {
		this.renderers.put(class_, entityRenderer);
	}

	public EntityRenderDispatcher(TextureManager textureManager, ItemRenderer itemRenderer, ReloadableResourceManager reloadableResourceManager) {
		this.textureManager = textureManager;
		this.register(CaveSpider.class, new CaveSpiderRenderer(this));
		this.register(Spider.class, new SpiderRenderer(this));
		this.register(Pig.class, new PigRenderer(this));
		this.register(Sheep.class, new SheepRenderer(this));
		this.register(Cow.class, new CowRenderer(this));
		this.register(MushroomCow.class, new MushroomCowRenderer(this));
		this.register(Wolf.class, new WolfRenderer(this));
		this.register(Chicken.class, new ChickenRenderer(this));
		this.register(Ocelot.class, new OcelotRenderer(this));
		this.register(Rabbit.class, new RabbitRenderer(this));
		this.register(Parrot.class, new ParrotRenderer(this));
		this.register(Turtle.class, new TurtleRenderer(this));
		this.register(Silverfish.class, new SilverfishRenderer(this));
		this.register(Endermite.class, new EndermiteRenderer(this));
		this.register(Creeper.class, new CreeperRenderer(this));
		this.register(EnderMan.class, new EndermanRenderer(this));
		this.register(SnowGolem.class, new SnowGolemRenderer(this));
		this.register(Skeleton.class, new SkeletonRenderer(this));
		this.register(WitherSkeleton.class, new WitherSkeletonRenderer(this));
		this.register(Stray.class, new StrayRenderer(this));
		this.register(Witch.class, new WitchRenderer(this));
		this.register(Blaze.class, new BlazeRenderer(this));
		this.register(PigZombie.class, new PigZombieRenderer(this));
		this.register(Zombie.class, new ZombieRenderer(this));
		this.register(ZombieVillager.class, new ZombieVillagerRenderer(this, reloadableResourceManager));
		this.register(Husk.class, new HuskRenderer(this));
		this.register(Drowned.class, new DrownedRenderer(this));
		this.register(Slime.class, new SlimeRenderer(this));
		this.register(MagmaCube.class, new LavaSlimeRenderer(this));
		this.register(Giant.class, new GiantMobRenderer(this, 6.0F));
		this.register(Ghast.class, new GhastRenderer(this));
		this.register(Squid.class, new SquidRenderer(this));
		this.register(Villager.class, new VillagerRenderer(this, reloadableResourceManager));
		this.register(WanderingTrader.class, new WanderingTraderRenderer(this));
		this.register(IronGolem.class, new IronGolemRenderer(this));
		this.register(Bat.class, new BatRenderer(this));
		this.register(Guardian.class, new GuardianRenderer(this));
		this.register(ElderGuardian.class, new ElderGuardianRenderer(this));
		this.register(Shulker.class, new ShulkerRenderer(this));
		this.register(PolarBear.class, new PolarBearRenderer(this));
		this.register(Evoker.class, new EvokerRenderer(this));
		this.register(Vindicator.class, new VindicatorRenderer(this));
		this.register(Pillager.class, new PillagerRenderer(this));
		this.register(Ravager.class, new RavagerRenderer(this));
		this.register(Vex.class, new VexRenderer(this));
		this.register(Illusioner.class, new IllusionerRenderer(this));
		this.register(Phantom.class, new PhantomRenderer(this));
		this.register(Pufferfish.class, new PufferfishRenderer(this));
		this.register(Salmon.class, new SalmonRenderer(this));
		this.register(Cod.class, new CodRenderer(this));
		this.register(TropicalFish.class, new TropicalFishRenderer(this));
		this.register(Dolphin.class, new DolphinRenderer(this));
		this.register(Panda.class, new PandaRenderer(this));
		this.register(Cat.class, new CatRenderer(this));
		this.register(Fox.class, new FoxRenderer(this));
		this.register(Bee.class, new BeeRenderer(this));
		this.register(EnderDragon.class, new EnderDragonRenderer(this));
		this.register(EndCrystal.class, new EndCrystalRenderer(this));
		this.register(WitherBoss.class, new WitherBossRenderer(this));
		this.register(Entity.class, new DefaultRenderer(this));
		this.register(Painting.class, new PaintingRenderer(this));
		this.register(ItemFrame.class, new ItemFrameRenderer(this, itemRenderer));
		this.register(LeashFenceKnotEntity.class, new LeashKnotRenderer(this));
		this.register(Arrow.class, new TippableArrowRenderer(this));
		this.register(SpectralArrow.class, new SpectralArrowRenderer(this));
		this.register(ThrownTrident.class, new ThrownTridentRenderer(this));
		this.register(Snowball.class, new ThrownItemRenderer(this, itemRenderer));
		this.register(ThrownEnderpearl.class, new ThrownItemRenderer(this, itemRenderer));
		this.register(EyeOfEnder.class, new ThrownItemRenderer(this, itemRenderer));
		this.register(ThrownEgg.class, new ThrownItemRenderer(this, itemRenderer));
		this.register(ThrownPotion.class, new ThrownItemRenderer(this, itemRenderer));
		this.register(ThrownExperienceBottle.class, new ThrownItemRenderer(this, itemRenderer));
		this.register(FireworkRocketEntity.class, new FireworkEntityRenderer(this, itemRenderer));
		this.register(LargeFireball.class, new ThrownItemRenderer(this, itemRenderer, 3.0F));
		this.register(SmallFireball.class, new ThrownItemRenderer(this, itemRenderer, 0.75F));
		this.register(DragonFireball.class, new DragonFireballRenderer(this));
		this.register(WitherSkull.class, new WitherSkullRenderer(this));
		this.register(ShulkerBullet.class, new ShulkerBulletRenderer(this));
		this.register(ItemEntity.class, new ItemEntityRenderer(this, itemRenderer));
		this.register(ExperienceOrb.class, new ExperienceOrbRenderer(this));
		this.register(PrimedTnt.class, new TntRenderer(this));
		this.register(FallingBlockEntity.class, new FallingBlockRenderer(this));
		this.register(ArmorStand.class, new ArmorStandRenderer(this));
		this.register(EvokerFangs.class, new EvokerFangsRenderer(this));
		this.register(MinecartTNT.class, new TntMinecartRenderer(this));
		this.register(MinecartSpawner.class, new MinecartRenderer(this));
		this.register(AbstractMinecart.class, new MinecartRenderer(this));
		this.register(Boat.class, new BoatRenderer(this));
		this.register(FishingHook.class, new FishingHookRenderer(this));
		this.register(AreaEffectCloud.class, new AreaEffectCloudRenderer(this));
		this.register(Horse.class, new HorseRenderer(this));
		this.register(SkeletonHorse.class, new UndeadHorseRenderer(this));
		this.register(ZombieHorse.class, new UndeadHorseRenderer(this));
		this.register(Mule.class, new ChestedHorseRenderer(this, 0.92F));
		this.register(Donkey.class, new ChestedHorseRenderer(this, 0.87F));
		this.register(Llama.class, new LlamaRenderer(this));
		this.register(TraderLlama.class, new LlamaRenderer(this));
		this.register(LlamaSpit.class, new LlamaSpitRenderer(this));
		this.register(LightningBolt.class, new LightningBoltRenderer(this));
		this.defaultPlayerRenderer = new PlayerRenderer(this);
		this.playerRenderers.put("default", this.defaultPlayerRenderer);
		this.playerRenderers.put("slim", new PlayerRenderer(this, true));
	}

	public void setPosition(double d, double e, double f) {
		this.xOff = d;
		this.yOff = e;
		this.zOff = f;
	}

	public <T extends Entity, U extends EntityRenderer<T>> U getRenderer(Class<? extends Entity> class_) {
		EntityRenderer<? extends Entity> entityRenderer = (EntityRenderer<? extends Entity>)this.renderers.get(class_);
		if (entityRenderer == null && class_ != Entity.class) {
			entityRenderer = this.getRenderer(class_.getSuperclass());
			this.renderers.put(class_, entityRenderer);
		}

		return (U)entityRenderer;
	}

	@Nullable
	public <T extends Entity, U extends EntityRenderer<T>> U getRenderer(T entity) {
		if (entity instanceof AbstractClientPlayer) {
			String string = ((AbstractClientPlayer)entity).getModelName();
			PlayerRenderer playerRenderer = (PlayerRenderer)this.playerRenderers.get(string);
			return (U)(playerRenderer != null ? playerRenderer : this.defaultPlayerRenderer);
		} else {
			return this.getRenderer(entity.getClass());
		}
	}

	public void prepare(Level level, Font font, Camera camera, Entity entity, Options options) {
		this.level = level;
		this.options = options;
		this.camera = camera;
		this.crosshairPickEntity = entity;
		this.font = font;
		if (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).isSleeping()) {
			Direction direction = ((LivingEntity)camera.getEntity()).getBedOrientation();
			if (direction != null) {
				this.playerRotY = direction.getOpposite().toYRot();
				this.playerRotX = 0.0F;
			}
		} else {
			this.playerRotY = camera.getYRot();
			this.playerRotX = camera.getXRot();
		}
	}

	public void setPlayerRotY(float f) {
		this.playerRotY = f;
	}

	public boolean shouldRenderShadow() {
		return this.shouldRenderShadow;
	}

	public void setRenderShadow(boolean bl) {
		this.shouldRenderShadow = bl;
	}

	public void setRenderHitBoxes(boolean bl) {
		this.renderHitBoxes = bl;
	}

	public boolean shouldRenderHitBoxes() {
		return this.renderHitBoxes;
	}

	public boolean hasSecondPass(Entity entity) {
		return this.getRenderer(entity).hasSecondPass();
	}

	public boolean shouldRender(Entity entity, Culler culler, double d, double e, double f) {
		EntityRenderer<Entity> entityRenderer = this.getRenderer(entity);
		return entityRenderer != null && entityRenderer.shouldRender(entity, culler, d, e, f);
	}

	public void render(Entity entity, float f, boolean bl) {
		if (entity.tickCount == 0) {
			entity.xOld = entity.x;
			entity.yOld = entity.y;
			entity.zOld = entity.z;
		}

		double d = Mth.lerp((double)f, entity.xOld, entity.x);
		double e = Mth.lerp((double)f, entity.yOld, entity.y);
		double g = Mth.lerp((double)f, entity.zOld, entity.z);
		float h = Mth.lerp(f, entity.yRotO, entity.yRot);
		int i = entity.getLightColor();
		if (entity.isOnFire()) {
			i = 15728880;
		}

		int j = i % 65536;
		int k = i / 65536;
		RenderSystem.glMultiTexCoord2f(33985, (float)j, (float)k);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.render(entity, d - this.xOff, e - this.yOff, g - this.zOff, h, f, bl);
	}

	public void render(Entity entity, double d, double e, double f, float g, float h, boolean bl) {
		EntityRenderer<Entity> entityRenderer = null;

		try {
			entityRenderer = this.getRenderer(entity);
			if (entityRenderer != null && this.textureManager != null) {
				try {
					entityRenderer.setSolidRender(this.solidRender);
					entityRenderer.render(entity, d, e, f, g, h);
				} catch (Throwable var17) {
					throw new ReportedException(CrashReport.forThrowable(var17, "Rendering entity in world"));
				}

				try {
					if (!this.solidRender) {
						entityRenderer.postRender(entity, d, e, f, g, h);
					}
				} catch (Throwable var18) {
					throw new ReportedException(CrashReport.forThrowable(var18, "Post-rendering entity in world"));
				}

				if (this.renderHitBoxes && !entity.isInvisible() && !bl && !Minecraft.getInstance().showOnlyReducedInfo()) {
					try {
						this.renderHitbox(entity, d, e, f, g, h);
					} catch (Throwable var16) {
						throw new ReportedException(CrashReport.forThrowable(var16, "Rendering entity hitbox in world"));
					}
				}
			}
		} catch (Throwable var19) {
			CrashReport crashReport = CrashReport.forThrowable(var19, "Rendering entity in world");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Entity being rendered");
			entity.fillCrashReportCategory(crashReportCategory);
			CrashReportCategory crashReportCategory2 = crashReport.addCategory("Renderer details");
			crashReportCategory2.setDetail("Assigned renderer", entityRenderer);
			crashReportCategory2.setDetail("Location", CrashReportCategory.formatLocation(d, e, f));
			crashReportCategory2.setDetail("Rotation", g);
			crashReportCategory2.setDetail("Delta", h);
			throw new ReportedException(crashReport);
		}
	}

	public void renderSecondPass(Entity entity, float f) {
		if (entity.tickCount == 0) {
			entity.xOld = entity.x;
			entity.yOld = entity.y;
			entity.zOld = entity.z;
		}

		double d = Mth.lerp((double)f, entity.xOld, entity.x);
		double e = Mth.lerp((double)f, entity.yOld, entity.y);
		double g = Mth.lerp((double)f, entity.zOld, entity.z);
		float h = Mth.lerp(f, entity.yRotO, entity.yRot);
		int i = entity.getLightColor();
		if (entity.isOnFire()) {
			i = 15728880;
		}

		int j = i % 65536;
		int k = i / 65536;
		RenderSystem.glMultiTexCoord2f(33985, (float)j, (float)k);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		EntityRenderer<Entity> entityRenderer = this.getRenderer(entity);
		if (entityRenderer != null && this.textureManager != null) {
			entityRenderer.renderSecondPass(entity, d - this.xOff, e - this.yOff, g - this.zOff, h, f);
		}
	}

	private void renderHitbox(Entity entity, double d, double e, double f, float g, float h) {
		RenderSystem.depthMask(false);
		RenderSystem.disableTexture();
		RenderSystem.disableLighting();
		RenderSystem.disableCull();
		RenderSystem.disableBlend();
		float i = entity.getBbWidth() / 2.0F;
		AABB aABB = entity.getBoundingBox();
		LevelRenderer.renderLineBox(
			aABB.minX - entity.x + d,
			aABB.minY - entity.y + e,
			aABB.minZ - entity.z + f,
			aABB.maxX - entity.x + d,
			aABB.maxY - entity.y + e,
			aABB.maxZ - entity.z + f,
			1.0F,
			1.0F,
			1.0F,
			1.0F
		);
		if (entity instanceof EnderDragon) {
			for (EnderDragonPart enderDragonPart : ((EnderDragon)entity).getSubEntities()) {
				double j = (enderDragonPart.x - enderDragonPart.xo) * (double)h;
				double k = (enderDragonPart.y - enderDragonPart.yo) * (double)h;
				double l = (enderDragonPart.z - enderDragonPart.zo) * (double)h;
				AABB aABB2 = enderDragonPart.getBoundingBox();
				LevelRenderer.renderLineBox(
					aABB2.minX - this.xOff + j,
					aABB2.minY - this.yOff + k,
					aABB2.minZ - this.zOff + l,
					aABB2.maxX - this.xOff + j,
					aABB2.maxY - this.yOff + k,
					aABB2.maxZ - this.zOff + l,
					0.25F,
					1.0F,
					0.0F,
					1.0F
				);
			}
		}

		if (entity instanceof LivingEntity) {
			float m = 0.01F;
			LevelRenderer.renderLineBox(
				d - (double)i,
				e + (double)entity.getEyeHeight() - 0.01F,
				f - (double)i,
				d + (double)i,
				e + (double)entity.getEyeHeight() + 0.01F,
				f + (double)i,
				1.0F,
				0.0F,
				0.0F,
				1.0F
			);
		}

		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		Vec3 vec3 = entity.getViewVector(h);
		bufferBuilder.begin(3, DefaultVertexFormat.POSITION_COLOR);
		bufferBuilder.vertex(d, e + (double)entity.getEyeHeight(), f).color(0, 0, 255, 255).endVertex();
		bufferBuilder.vertex(d + vec3.x * 2.0, e + (double)entity.getEyeHeight() + vec3.y * 2.0, f + vec3.z * 2.0).color(0, 0, 255, 255).endVertex();
		tesselator.end();
		RenderSystem.enableTexture();
		RenderSystem.enableLighting();
		RenderSystem.enableCull();
		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
	}

	public void setLevel(@Nullable Level level) {
		this.level = level;
		if (level == null) {
			this.camera = null;
		}
	}

	public double distanceToSqr(double d, double e, double f) {
		return this.camera.getPosition().distanceToSqr(d, e, f);
	}

	public Font getFont() {
		return this.font;
	}

	public void setSolidRendering(boolean bl) {
		this.solidRender = bl;
	}
}
