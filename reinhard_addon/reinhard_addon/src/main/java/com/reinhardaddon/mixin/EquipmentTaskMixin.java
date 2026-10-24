package com.reinhardaddon.mixin;

import com.reinhardaddon.ReinhardAbility;
import forge.net.conczin.mca.entity.VillagerEntityMCA;
import forge.net.conczin.mca.entity.ai.brain.tasks.EquipmentTask;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * CAUSA RAÍZ de "el guardia no tiene la espada": EquipmentTask.start() de MCA SOBRESCRIBE incondicionalmente
 * la mano dominante (mejor espada del inventario, o vacía) y la otra mano (objeto del EquipmentSet, o vacía)
 * cada vez que el guardia cambia de actividad. Aquí se desactiva SOLO para Reinhard, para que conserve la
 * espada y la vaina que le pone este addon. El resto de aldeanos de MCA no se ven afectados.
 *
 * remap=false: apunta a una clase de MCA (nombres propios, no ofuscados). require=0: si una futura versión de
 * MCA cambia la firma, el mixin simplemente no se aplica (ReinhardAbility.ensureSword sigue como respaldo)
 * en lugar de impedir que arranque el juego.
 */
@Mixin(value = EquipmentTask.class, remap = false)
public abstract class EquipmentTaskMixin {

    @Inject(
            method = "start(Lnet/minecraft/server/level/ServerLevel;Lforge/net/conczin/mca/entity/VillagerEntityMCA;J)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0)
    private void reinhard_addon$keepOwnEquipment(ServerLevel level, VillagerEntityMCA villager, long gameTime, CallbackInfo ci) {
        if (ReinhardAbility.isReinhard(villager)) {
            ci.cancel();
        }
    }
}
