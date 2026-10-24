package com.reinhardaddon;

import forge.net.conczin.mca.entity.VillagerEntityMCA;
import java.util.Comparator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Decide CUÁNDO Reinhard desenvaina/enfunda y CUÁNDO lanza el Sunstrike. Solo servidor, solo aldeanos MCA. */
@Mod.EventBusSubscriber(modid = ReinhardAddon.MODID)   // bus FORGE por defecto
public final class ReinhardAbilityHandler {
    private static final String NEXT_CAST_KEY   = "reinhard_next_cast";
    private static final String LAST_COMBAT_KEY = "reinhard_last_combat";
    private static final int  CHECK_EVERY_TICKS = 5;
    private static final int  COOLDOWN_TICKS    = 100;   // 5 s entre rayos
    private static final int  SHEATHE_AFTER     = 200;   // 10 s sin enemigos -> enfunda (igual que en Tensura Blade)
    private static final double RANGE           = 24.0D;

    private ReinhardAbilityHandler() {}

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        // Filtros baratos primero: este evento se dispara para TODA entidad viva en cada tick.
        if (!(event.getEntity() instanceof VillagerEntityMCA villager)) return;
        Level level = villager.level();
        if (level.isClientSide || villager.tickCount % CHECK_EVERY_TICKS != 0) return;

        if (!ReinhardAbility.isReinhard(villager)) {
            // Auto-conversión: guardia MCA llamado "Reinhard" (se revisa cada ~2 s para no gastar CPU).
            if (villager.tickCount % 40 == 0 && ReinhardAbility.looksLikeReinhard(villager)) {
                ReinhardAbility.makeReinhard(villager, false);
            }
            return;
        }

        ReinhardAbility.ensureSword(villager);
        CompoundTag data = villager.getPersistentData();
        long now = level.getGameTime();

        LivingEntity target = findTarget(villager);

        // Estado visual: hay enemigo -> desenvaina; 10 s sin enemigos -> enfunda.
        if (target != null) {
            data.putLong(LAST_COMBAT_KEY, now);
            ReinhardAbility.setDrawn(villager, true);
        } else if (now - data.getLong(LAST_COMBAT_KEY) > SHEATHE_AFTER) {
            ReinhardAbility.setDrawn(villager, false);
        }

        // Sunstrike: solo con objetivo, sin cooldown y con la espada equipada (lo exige isAuthorized dentro de tryCast).
        if (target != null && now >= data.getLong(NEXT_CAST_KEY) && ReinhardAbility.tryCast(villager, target)) {
            data.putLong(NEXT_CAST_KEY, now + COOLDOWN_TICKS);
            villager.swing(InteractionHand.MAIN_HAND);
        }
    }

    private static LivingEntity findTarget(VillagerEntityMCA v) {
        // 1) objetivo de Mob.setTarget  2) memoria ATTACK_TARGET del Brain (los Villager usan Brain)  3) hostil más cercano visible
        LivingEntity t = v.getTarget();
        if (t == null || !t.isAlive()) {
            t = v.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        }
        if (t == null || !t.isAlive()) {
            t = v.level().getEntitiesOfClass(LivingEntity.class, v.getBoundingBox().inflate(RANGE),
                            e -> e instanceof Enemy && e.isAlive() && v.hasLineOfSight(e))
                    .stream().min(Comparator.comparingDouble(v::distanceToSqr)).orElse(null);
        }
        if (t == null || v.distanceToSqr(t) > RANGE * RANGE) return null;
        return ReinhardAbility.isValidVictim(v, t) ? t : null;
    }
}
