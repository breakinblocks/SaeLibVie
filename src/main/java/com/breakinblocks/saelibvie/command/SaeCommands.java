package com.breakinblocks.saelibvie.command;

import com.breakinblocks.saelibvie.SaeLibVie;
import com.breakinblocks.saelibvie.item.ModNames;
import com.breakinblocks.saelibvie.nbtedit.NbtEditInfo;
import com.breakinblocks.saelibvie.nbtedit.NbtEditSessions;
import com.breakinblocks.saelibvie.net.EditNbtPayload;
import com.breakinblocks.saelibvie.net.NetUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class SaeCommands {
    private SaeCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal(SaeLibVie.MOD_ID)
                .then(Commands.literal("nbtedit")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("block")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> editBlock(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, "pos")))))
                        .then(Commands.literal("entity")
                                .then(Commands.argument("entity", EntityArgument.entity())
                                        .executes(ctx -> editEntity(ctx.getSource(), EntityArgument.getEntity(ctx, "entity")))))
                        .then(Commands.literal("player")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> editPlayer(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                        .then(Commands.literal("item")
                                .executes(ctx -> editItem(ctx.getSource())))));
    }

    private static void send(ServerPlayer player, CompoundTag info, CompoundTag data) {
        NbtEditSessions.put(player.getUUID(), info);
        NetUtil.sendTo(player, new EditNbtPayload(info, data));
    }

    private static String modOf(ResourceLocation id) {
        return ModNames.getModName(id.getNamespace()).orElse(id.getNamespace());
    }

    private static int editBlock(CommandSourceStack source, BlockPos pos) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = source.getLevel();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        BlockState state = level.getBlockState(pos);
        if (blockEntity == null) {
            source.sendFailure(Component.literal("Not a block entity: " + state.getBlock().getName().getString()).withStyle(ChatFormatting.RED));
            return 0;
        }
        CompoundTag data = blockEntity.saveWithFullMetadata(level.registryAccess());
        data.remove("x");
        data.remove("y");
        data.remove("z");
        String id = data.contains("id", Tag.TAG_STRING) ? data.getString("id") : String.valueOf(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType()));
        data.remove("id");
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        Component title = blockEntity instanceof Nameable nameable ? nameable.getDisplayName() : Component.literal(blockEntity.getClass().getSimpleName());
        NbtEditInfo info = NbtEditInfo.of("block")
                .put("pos", NbtUtils.writeBlockPos(pos))
                .put("id", StringTag.valueOf(id))
                .title(title)
                .line("Class", blockEntity.getClass().getName())
                .line("ID", id)
                .line("Block", blockId)
                .line("Block Class", state.getBlock().getClass().getName())
                .line("Position", "[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]")
                .line("Mod", modOf(blockId))
                .line("Ticking", state.getBlock() instanceof EntityBlock entityBlock
                        && entityBlock.getTicker(level, state, blockEntity.getType()) != null);
        send(player, info.build(), data);
        return 1;
    }

    private static int editEntity(CommandSourceStack source, Entity entity) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (entity instanceof ServerPlayer) {
            return 0;
        }
        ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        CompoundTag data = new CompoundTag();
        entity.save(data);
        NbtEditInfo info = NbtEditInfo.of("entity")
                .put("id", IntTag.valueOf(entity.getId()))
                .title(entity.getDisplayName())
                .line("Class", entity.getClass().getName())
                .line("ID", typeId)
                .line("Mod", modOf(typeId));
        send(player, info.build(), data);
        return 1;
    }

    private static int editPlayer(CommandSourceStack source, ServerPlayer target) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        CompoundTag data = new CompoundTag();
        target.saveWithoutId(data);
        data.remove("id");
        NbtEditInfo info = NbtEditInfo.of("player")
                .put("id", NbtUtils.createUUID(target.getUUID()))
                .title(target.getDisplayName())
                .line("Name", target.getGameProfile().getName())
                .line("Display Name", target.getDisplayName().getString())
                .line("UUID", target.getUUID());
        send(player, info.build(), data);
        return 1;
    }

    private static int editItem(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            return 0;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        Tag saved = stack.save(player.registryAccess());
        CompoundTag data = saved instanceof CompoundTag compound ? compound : new CompoundTag();
        NbtEditInfo info = NbtEditInfo.of("item")
                .title(stack.getHoverName())
                .line("Class", stack.getItem().getClass().getName())
                .line("ID", itemId)
                .line("Mod", modOf(itemId));
        send(player, info.build(), data);
        return 1;
    }
}
