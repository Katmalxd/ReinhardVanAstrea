package com.reinhardaddon;

import com.bobmowzie.mowziesmobs.server.entity.effects.EntitySunstrike;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Sunstrike de Mowzie's con área ampliada y color blanco, usable solo por Reinhard.
 *
 * Se REUTILIZA la lógica de tiempos y el renderer de Mowzie's (extendiendo EntitySunstrike) y se
 * sobrescriben solo los puntos públicos: daño/área, tick, culling y distancia de render.
 * Tiempos reales de Mowzie's: tick 35 = impacto (daño), tick 43 = fin del rayo, hasta 1243 = marca en suelo.
 */
public class ReinhardSunstrikeEntity extends EntitySunstrike {
    /** Radio original de Mowzie's = 3.0 bloques. Aquí 3x. */
    public static final double AREA_RADIUS = 9.0D;
    /** Factor de escala visual horizontal (el renderer original usa constantes fijas, así que se escala la PoseStack). */
    public static final float AREA_SCALE = (float) (AREA_RADIUS / 3.0D);
    /** Daño mágico en el impacto. */
    public static final float DAMAGE = 40.0F;
    /** Se elimina poco después de terminar el rayo (43) para no dejar la marca de suelo 62 s. */
    private static final int LIFETIME_TICKS = 50;

    /** Solo servidor, no persistente: si se recarga el chunk a mitad de ataque, la entidad se descarta (a propósito). */
    @Nullable
    private LivingEntity reinhardCaster;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ReinhardSunstrikeEntity(EntityType<ReinhardSunstrikeEntity> type, Level level) {
        super((EntityType) type, level);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ReinhardSunstrikeEntity(EntityType<ReinhardSunstrikeEntity> type, Level level, LivingEntity caster, int x, int y, int z) {
        super((EntityType) type, level, caster, x, y, z);
        this.reinhardCaster = caster;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide) {
            // Defensa en profundidad: aunque alguien la creara por otra vía, sin Reinhard+espada desaparece.
            if (!ReinhardAbility.isAuthorized(this.reinhardCaster) || this.tickCount > LIFETIME_TICKS) {
                this.discard();
                return;
            }
            super.tick();
        } else {
            ParticleWhitening.ACTIVE = true;
            try {
                super.tick();            // tick original de Mowzie's (sus partículas vanilla pasan por ClientLevelMixin)
            } finally {
                ParticleWhitening.ACTIVE = false;
            }
            spawnAreaParticles();
        }
    }

    /** Mowzie's llama a esto con 3.0 en el tick 35 (impacto). Se ignora ese valor y se usa el área ampliada. */
    @Override
    public void damageEntityLivingBaseNearby(double ignoredOriginalRadius) {
        if (this.level().isClientSide) return;
        LivingEntity caster = this.reinhardCaster;
        if (!ReinhardAbility.isAuthorized(caster)) {   // comprobación final justo en el impacto
            this.discard();
            return;
        }
        final double r = AREA_RADIUS;
        // Misma caja que el original: columna desde el suelo hasta el cielo (el rayo cae desde arriba).
        AABB column = new AABB(getX() - r, getY() - 0.5D, getZ() - r,
                               getX() + r, this.level().getMaxBuildHeight() + 20, getZ() + r);
        DamageSource source = this.level().damageSources().indirectMagic(this, caster);

        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, column,
                e -> ReinhardAbility.isValidVictim(caster, e))) {
            double dx = target.getX() - getX();
            double dz = target.getZ() - getZ();
            if (dx * dx + dz * dz >= r * r) continue;      // círculo, no cuadrado
            if (target.hurt(source, DAMAGE)) {
                target.invulnerableTime = 0;
            }
        }
    }

    /** Partículas blancas que rellenan el área ampliada durante el impacto (las de Mowzie's solo cubren ~2 bloques). */
    private void spawnAreaParticles() {
        if (!this.isStriking() || this.getStrikeDrawTime(1.0F) < 1.0F) return;   // solo entre el tick 35 y el 43
        for (int i = 0; i < 40; i++) {
            double ang = this.random.nextDouble() * Math.PI * 2.0D;
            double rad = Math.sqrt(this.random.nextDouble()) * AREA_RADIUS;
            this.level().addParticle(ParticleTypes.END_ROD,
                    getX() + Math.cos(ang) * rad, getY() + 0.1D, getZ() + Math.sin(ang) * rad,
                    0.0D, 0.05D + this.random.nextDouble() * 0.15D, 0.0D);
        }
    }

    /** Con hitbox de 0.1 el rayo desaparecería al sacar el suelo de pantalla: ampliamos la caja de culling. */
    @Override
    public AABB getBoundingBoxForCulling() {
        double r = AREA_RADIUS;
        return new AABB(getX() - r, getY(), getZ() - r, getX() + r, getY() + 256.0D, getZ() + r);
    }

    /** Mowzie's solo renderiza a < 32 bloques; el área es mayor, así que se visualiza desde 64. */
    @Override
    public boolean shouldRenderAtSqrDistance(double distanceSq) {
        return distanceSq < 64.0D * 64.0D;
    }
}
