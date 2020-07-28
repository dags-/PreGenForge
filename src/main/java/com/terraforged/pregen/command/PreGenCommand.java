package com.terraforged.pregen.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.terraforged.pregen.Log;
import com.terraforged.pregen.PreGen;
import com.terraforged.pregen.pregen.PreGenConfig;
import com.terraforged.pregen.pregen.PreGenRegion;
import com.terraforged.pregen.pregen.PreGenTask;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.command.arguments.Vec2Argument;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.server.ServerWorld;

public class PreGenCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        registerUtils(dispatcher);
        registerStart(dispatcher);
        registerExpand(dispatcher);
    }

    private static void registerStart(CommandDispatcher<CommandSource> dispatcher) {
        // pregen start <radius>
        dispatcher.register(Commands.literal("pregen")
                .then(Commands.literal("start")
                        .requires(CommandUtils.PERMISSION)
                        .then(Commands.argument("radius", IntegerArgumentType.integer())
                                .executes(PreGenCommand::start))));
        // pregen start <dim> <radius>
        dispatcher.register(Commands.literal("pregen")
                .then(Commands.literal("start")
                        .requires(CommandUtils.PERMISSION)
                        .then(Commands.argument("dimension", DimensionArgument.getDimension())
                                .then(Commands.argument("radius", IntegerArgumentType.integer())
                                        .executes(PreGenCommand::start)))));
        // pregen start <x> <z> <radius>
        dispatcher.register(Commands.literal("pregen")
                .then(Commands.literal("start")
                        .requires(CommandUtils.PERMISSION)
                        .then(Commands.argument("center", Vec2Argument.vec2())
                                .then(Commands.argument("radius", IntegerArgumentType.integer())
                                        .executes(PreGenCommand::start)))));
        // pregen start <dim> <x> <z> <radius>
        dispatcher.register(Commands.literal("pregen")
                .then(Commands.literal("start")
                        .requires(CommandUtils.PERMISSION)
                        .then(Commands.argument("dimension", DimensionArgument.getDimension())
                                .then(Commands.argument("center", Vec2Argument.vec2())
                                        .then(Commands.argument("radius", IntegerArgumentType.integer())
                                                .executes(PreGenCommand::start))))));
    }

    private static void registerExpand(CommandDispatcher<CommandSource> dispatcher) {
        // pregen expand <inner> <outer>
        dispatcher.register(Commands.literal("pregen")
                .then(Commands.literal("expand")
                        .requires(CommandUtils.PERMISSION)
                        .then(Commands.argument("innerRadius", IntegerArgumentType.integer())
                                .then(Commands.argument("outerRadius", IntegerArgumentType.integer())
                                        .executes(PreGenCommand::expand)))));
        // pregen expand <dim> <inner> <outer>
        dispatcher.register(Commands.literal("pregen")
                .then(Commands.literal("expand")
                        .requires(CommandUtils.PERMISSION)
                        .then(Commands.argument("dimension", DimensionArgument.getDimension())
                                .then(Commands.argument("innerRadius", IntegerArgumentType.integer())
                                        .then(Commands.argument("outerRadius", IntegerArgumentType.integer())
                                                .executes(PreGenCommand::expand))))));
        // pregen expand <x> <z> <inner> <outer>
        dispatcher.register(Commands.literal("pregen")
                .then(Commands.literal("expand")
                        .requires(CommandUtils.PERMISSION)
                        .then(Commands.argument("center", Vec2Argument.vec2())
                                .then(Commands.argument("innerRadius", IntegerArgumentType.integer())
                                        .then(Commands.argument("outerRadius", IntegerArgumentType.integer())
                                                .executes(PreGenCommand::expand))))));
        // pregen expand <x> <z> <inner> <outer>
        dispatcher.register(Commands.literal("pregen")
                .then(Commands.literal("expand")
                        .requires(CommandUtils.PERMISSION)
                        .then(Commands.argument("dimension", DimensionArgument.getDimension())
                                .then(Commands.argument("center", Vec2Argument.vec2())
                                        .then(Commands.argument("innerRadius", IntegerArgumentType.integer())
                                                .then(Commands.argument("outerRadius", IntegerArgumentType.integer())
                                                        .executes(PreGenCommand::expand)))))));
    }

    private static void registerUtils(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("pregen")
                .requires(CommandUtils.PERMISSION)
                // toggles progress notifications for players
                .then(Commands.literal("notify")
                        .then(Commands.argument("state", BoolArgumentType.bool())
                                .executes(PreGenCommand::notify)))
                // pauses a pregen task
                .then(Commands.literal("pause")
                        .then(Commands.argument("dimension", DimensionArgument.getDimension())
                                .executes(PreGenCommand::pause))
                        .executes(PreGenCommand::pause))
                // resumes a pregen task
                .then(Commands.literal("resume")
                        .then(Commands.argument("dimension", DimensionArgument.getDimension())
                                .executes(PreGenCommand::resume))
                        .executes(PreGenCommand::resume))
                // cancels & delete a pregen task
                .then(Commands.literal("cancel")
                        .then(Commands.argument("dimension", DimensionArgument.getDimension())
                                .executes(PreGenCommand::cancel))
                        .executes(PreGenCommand::cancel))
                // pregens a specific region
                .then(Commands.literal("region")
                        .then(Commands.argument("position", Vec2Argument.vec2())
                                .then(Commands.argument("dimension", DimensionArgument.getDimension())
                                        .executes(PreGenCommand::startRegion))
                                .executes(PreGenCommand::startRegion)))
                // sets the world game and day time
                .then(Commands.literal("time")
                        .then(Commands.argument("ticks", LongArgumentType.longArg(-1))
                                .then(Commands.argument("dimension", DimensionArgument.getDimension())
                                        .executes(PreGenCommand::time))
                                .executes(PreGenCommand::time))));
    }

    private static int resume(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerWorld worldServer = CommandUtils.getWorld(context);
        PreGen.getInstance().startTask(worldServer);
        CommandUtils.send(context.getSource(), "Pregenerator resumed");
        return Command.SINGLE_SUCCESS;
    }

    private static int pause(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerWorld worldServer = CommandUtils.getWorld(context);
        PreGen.getInstance().pauseTask(worldServer);
        CommandUtils.send(context.getSource(), "Pregenerator paused");
        return Command.SINGLE_SUCCESS;
    }

    private static int cancel(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerWorld worldServer = CommandUtils.getWorld(context);
        PreGen.getInstance().cancelTask(worldServer);
        CommandUtils.send(context.getSource(), "Pregenerator cancelled");
        return Command.SINGLE_SUCCESS;
    }

    private static int notify(CommandContext<CommandSource> context) {
        boolean state = BoolArgumentType.getBool(context, "state");
        String response = PreGen.getInstance().setPlayerNotifications(state);
        CommandUtils.sendToAny(context.getSource(), response);
        return Command.SINGLE_SUCCESS;
    }

    private static int time(CommandContext<CommandSource> context) throws CommandSyntaxException {
        long ticks = LongArgumentType.getLong(context, "ticks");
        ServerWorld worldServer = CommandUtils.getWorld(context);
        worldServer.setGameTime(ticks);
        worldServer.setDayTime(ticks);
        CommandUtils.send(context.getSource(), "Set world & game time: %s", ticks);
        return Command.SINGLE_SUCCESS;
    }

    private static int start(CommandContext<CommandSource> context) throws CommandSyntaxException {
        Log.print(context.getInput());
        ServerWorld world = CommandUtils.getWorld(context);
        Vec2f center = CommandUtils.getCenter(context, world);
        int radius = IntegerArgumentType.getInteger(context, "radius");
        int regionX = PreGenRegion.blockToRegion((int) center.x);
        int regionZ = PreGenRegion.blockToRegion((int) center.y);
        int regionRadius = PreGenRegion.chunkToRegion(radius);
        PreGenConfig config = new PreGenConfig(regionX, regionZ, regionRadius);
        PreGenTask worker = PreGen.getInstance().createTask(world, config);
        worker.start();
        CommandUtils.send(context.getSource(), "Pregenerator started");
        return Command.SINGLE_SUCCESS;
    }

    private static int expand(CommandContext<CommandSource> context) throws CommandSyntaxException {
        Log.print(context.getInput());

        ServerWorld world = CommandUtils.getWorld(context);
        Vec2f center = CommandUtils.getCenter(context, world);
        int innerRadius = IntegerArgumentType.getInteger(context, "innerRadius");
        int outerRadius = IntegerArgumentType.getInteger(context, "outerRadius");

        int regionX = PreGenRegion.blockToRegion((int) center.x);
        int regionZ = PreGenRegion.blockToRegion((int) center.y);
        int regionInnerRadius = PreGenRegion.chunkToRegion(innerRadius);
        int regionOuterRadius = PreGenRegion.chunkToRegion(outerRadius);
        int regionStart = (1 + regionInnerRadius * 2) * (1 + regionInnerRadius * 2);

        PreGenConfig config = new PreGenConfig(regionX, regionZ, Math.max(regionInnerRadius + 1, regionOuterRadius));
        config.setRegionIndex(regionStart);

        PreGenTask worker = PreGen.getInstance().createTask(world, config);
        worker.start();

        CommandUtils.send(context.getSource(), "Pregenerator started");
        return Command.SINGLE_SUCCESS;
    }

    private static int startRegion(CommandContext<CommandSource> context) throws CommandSyntaxException {
        Log.print(context.getInput());

        Vec2f position = Vec2Argument.getVec2f(context, "position");
        int regionX = (int) position.x;
        int regionZ = (int) position.y;

        PreGenConfig config = new PreGenConfig(regionX, regionZ, 1);
        ServerWorld world = CommandUtils.getWorld(context);
        PreGenTask worker = PreGen.getInstance().createTask(world, config);
        worker.start();

        CommandUtils.send(context.getSource(), "Pregenerator started");
        return Command.SINGLE_SUCCESS;
    }
}
