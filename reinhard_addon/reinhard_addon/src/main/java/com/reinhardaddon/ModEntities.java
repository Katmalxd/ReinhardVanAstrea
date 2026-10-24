package com.reinhardaddon;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ReinhardAddon.MODID);

    /** Mismos parámetros que Mowzie's: MISC y hitbox 0.1x0.1. Añadimos noSummon(): no se puede usar /summon. */
    public static final RegistryObject<EntityType<ReinhardSunstrikeEntity>> REINHARD_SUNSTRIKE =
            ENTITIES.register("reinhard_sunstrike", () ->
                    EntityType.Builder.<ReinhardSunstrikeEntity>of(ReinhardSunstrikeEntity::new, MobCategory.MISC)
                            .sized(0.1F, 0.1F)
                            .fireImmune()
                            .noSummon()
                            .build(ReinhardAddon.MODID + ":reinhard_sunstrike"));

    private ModEntities() {}
}
