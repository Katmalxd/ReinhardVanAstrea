package com.reinhardaddon;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import forge.net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 *   /reinhard summon                crea un guardia Reinhard donde estás
 *   /reinhard convert <entidad>     convierte un aldeano MCA existente
 *   /reinhard status  <entidad>     diagnóstico: por qué lanza (o no) el Sunstrike
 * Además, cualquier GUARDIA de MCA cuyo nombre contenga "Reinhard" se convierte solo.
 */
public final class ReinhardCommands {
    private ReinhardCommands() {}

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("reinhard")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("convert")
                        .then(Commands.argument("target", EntityArgument.entity()).executes(ReinhardCommands::convert)))
                .then(Commands.literal("status")
                        .then(Commands.argument("target", EntityArgument.entity()).executes(ReinhardCommands::status)))
                .then(Commands.literal("summon").executes(ReinhardCommands::summon)));
    }

    private static int convert(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity e = EntityArgument.getEntity(ctx, "target");
        if (!(e instanceof VillagerEntityMCA v)) {
            ctx.getSource().sendFailure(Component.translatable("commands.reinhard_addon.not_mca"));
            return 0;
        }
        ReinhardAbility.makeReinhard(v, true);
        ctx.getSource().sendSuccess(() -> Component.translatable("commands.reinhard_addon.converted", v.getDisplayName()), true);
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity e = EntityArgument.getEntity(ctx, "target");
        if (!(e instanceof VillagerEntityMCA v)) {
            ctx.getSource().sendFailure(Component.translatable("commands.reinhard_addon.not_mca"));
            return 0;
        }
        String report = "nombre=" + ReinhardAbility.nameOf(v)
                + " | profesión=" + v.getProfessionId()
                + " | esReinhard=" + ReinhardAbility.isReinhard(v)
                + " | manoPrincipal=" + v.getMainHandItem().getItem()
                + " | otraMano=" + v.getOffhandItem().getItem()
                + " | desenvainada=" + ReinhardAbility.isDrawn(v.getMainHandItem())
                + " | autorizado=" + ReinhardAbility.isAuthorized(v)
                + " | objetivo=" + (v.getTarget() == null ? "ninguno" : v.getTarget().getName().getString());
        ctx.getSource().sendSuccess(() -> Component.literal(report), false);
        return 1;
    }

    private static int summon(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerLevel level = src.getLevel();
        // Se busca por id para no depender de campos internos de MCA.
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("mca", "male_villager"));
        Entity entity = type == null ? null : type.create(level);
        if (!(entity instanceof VillagerEntityMCA v)) {
            src.sendFailure(Component.translatable("commands.reinhard_addon.not_mca"));
            return 0;
        }
        Vec3 pos = src.getPosition();
        v.moveTo(pos.x, pos.y, pos.z, src.getRotation().y, 0.0F);
        // finalizeSpawn: MCA genera aquí su genética/apariencia/cerebro.
        ((Mob) v).finalizeSpawn(level, level.getCurrentDifficultyAt(v.blockPosition()), MobSpawnType.COMMAND, null, null);
        // Debe ser GUARDIA: así usa la IA de combate de MCA.
        VillagerProfession guard = ForgeRegistries.VILLAGER_PROFESSIONS.getValue(new ResourceLocation("mca", "guard"));
        if (guard != null) v.setProfession(guard);
        ReinhardAbility.makeReinhard(v, true);
        level.addFreshEntityWithPassengers(v);
        src.sendSuccess(() -> Component.translatable("commands.reinhard_addon.summoned"), true);
        return 1;
    }
}
