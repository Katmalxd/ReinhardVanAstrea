package com.reinhardaddon;

import com.mojang.logging.LogUtils;
import forge.net.conczin.mca.entity.VillagerEntityMCA;
import forge.net.conczin.mca.entity.VillagerLike;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

/** Toda la lógica de "quién es Reinhard", "qué lleva en las manos" y "quién puede usar el Sunstrike" vive aquí. */
public final class ReinhardAbility {
    private static final Logger LOGGER = LogUtils.getLogger();

    /** Marca guardada en el NBT del aldeano (Forge la persiste en "ForgeData"). */
    public static final String TAG = "reinhard_van_astrea";
    /** NBT del ítem espada: true = desenvainada. El cliente lo lee con el predicado reinhard_addon:drawn. */
    public static final String DRAWN_TAG = "Drawn";

    private ReinhardAbility() {}

    // ------------------------------------------------------------------ identidad

    public static boolean isReinhard(VillagerEntityMCA v) {
        return v.getPersistentData().getBoolean(TAG);
    }

    /** Nombre MCA del aldeano (valor sincronizado de MCA). */
    public static String nameOf(VillagerEntityMCA v) {
        Object o = v.getTrackedValue(VillagerLike.VILLAGER_NAME);
        return o == null ? "" : o.toString();
    }

    /** Auto-conversión: guardia de MCA cuyo nombre contiene "reinhard". */
    public static boolean looksLikeReinhard(VillagerEntityMCA v) {
        return nameOf(v).toLowerCase(Locale.ROOT).contains("reinhard")
                && "guard".equals(v.getProfessionId().getPath());
    }

    // ------------------------------------------------------------------ regla de uso del Sunstrike

    /**
     * Condición de uso: lanzador vivo + aldeano MCA + marcado como Reinhard + ReinhardSword en la MANO PRINCIPAL
     * (enfundada o desenvainada). Se comprueba al lanzar, durante el ataque y en el instante del daño.
     */
    public static boolean isAuthorized(@Nullable LivingEntity caster) {
        return caster instanceof VillagerEntityMCA v
                && v.isAlive()
                && isReinhard(v)
                && v.getMainHandItem().getItem() instanceof ReinhardSwordItem;
    }

    /** Solo daña a hostiles (o a su objetivo actual). Nunca a jugadores, aldeanos, golems ni mascotas. */
    public static boolean isValidVictim(@Nullable LivingEntity caster, LivingEntity t) {
        if (t == caster || !t.isAlive()) return false;
        if (t instanceof Player || t instanceof AbstractVillager || t instanceof IronGolem) return false;
        if (t instanceof TamableAnimal pet && pet.isTame()) return false;
        if (t instanceof Enemy) return true;
        return caster instanceof Mob m && m.getTarget() == t;
    }

    /** Invoca el Sunstrike sobre el objetivo. Devuelve false si no está autorizado. */
    public static boolean tryCast(VillagerEntityMCA caster, LivingEntity target) {
        if (!isAuthorized(caster) || !(caster.level() instanceof ServerLevel level)) return false;

        // Mowzie's espera las coordenadas del BLOQUE DE SUELO (coloca la entidad en x+0.5, y+1.0625, z+0.5).
        BlockPos ground = target.blockPosition().below();
        ReinhardSunstrikeEntity strike = new ReinhardSunstrikeEntity(
                ModEntities.REINHARD_SUNSTRIKE.get(), level, caster, ground.getX(), ground.getY(), ground.getZ());
        strike.onSummon();            // fija la variante aleatoria del dibujo, igual que SunstrikeAbility.start()
        level.addFreshEntity(strike);
        LOGGER.info("[reinhard_addon] Sunstrike lanzado sobre {} en {}", target.getName().getString(), ground);
        return true;
    }

    // ------------------------------------------------------------------ equipo y estado enfundada/desenvainada

    public static boolean isDrawn(ItemStack sword) {
        return sword.hasTag() && sword.getTag().getBoolean(DRAWN_TAG);
    }

    /**
     * Marca + (nombre) + espada enfundada + estadísticas. Es lo que convierte a un guardia MCA en Reinhard.
     * @param rename true = fija el nombre "Reinhard van Astrea" (comandos); false = respeta el nombre que ya tiene.
     */
    public static void makeReinhard(VillagerEntityMCA v, boolean rename) {
        v.getPersistentData().putBoolean(TAG, true);
        if (rename) v.setName("Reinhard van Astrea");                 // VillagerLike#setName (API de MCA)
        v.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.REINHARD_SWORD.get()));
        v.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        v.setDropChance(EquipmentSlot.MAINHAND, 0.0F);                // no suelta la espada al morir
        v.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        v.setPersistenceRequired();
        setBase(v, Attributes.MAX_HEALTH, 200.0D);
        v.setHealth(v.getMaxHealth());
        LOGGER.info("[reinhard_addon] {} convertido en Reinhard", nameOf(v));
    }

    /** Si la mano principal está VACÍA se repone la espada (misma condición que usa MCA para reequipar). */
    public static void ensureSword(VillagerEntityMCA v) {
        if (v.getMainHandItem().isEmpty() && !v.isUsingItem()) {
            v.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.REINHARD_SWORD.get()));
        }
    }

    /**
     * Enfundada: espada con modelo enfundado en la mano principal, otra mano vacía.
     * Desenvainada: modelo desenvainado en la mano principal + vaina en la otra mano (igual que en Tensura Blade).
     */
    public static void setDrawn(VillagerEntityMCA v, boolean drawn) {
        ItemStack main = v.getMainHandItem();
        if (main.getItem() instanceof ReinhardSwordItem && isDrawn(main) != drawn) {
            ItemStack copy = main.copy();
            copy.getOrCreateTag().putBoolean(DRAWN_TAG, drawn);
            v.setItemSlot(EquipmentSlot.MAINHAND, copy);               // stack nuevo -> se sincroniza al cliente
        }
        ItemStack off = v.getOffhandItem();
        boolean hasSheath = off.is(ModItems.REINHARD_SHEATH.get());
        if (drawn && !hasSheath) {
            v.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ModItems.REINHARD_SHEATH.get()));
        } else if (!drawn && hasSheath) {
            v.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
    }

    private static void setBase(Mob mob, Attribute attribute, double value) {
        AttributeInstance inst = mob.getAttribute(attribute);
        if (inst != null) inst.setBaseValue(value);
    }
}
