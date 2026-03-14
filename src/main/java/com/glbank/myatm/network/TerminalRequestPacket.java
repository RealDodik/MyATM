package com.glbank.myatm.network;

import com.glbank.myatm.blockentity.TerminalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TerminalRequestPacket {

    private final BlockPos pos;
    private final int amount;
    private final String pin;

    public TerminalRequestPacket(BlockPos pos, int amount, String pin) {
        this.pos = pos;
        this.amount = amount;
        this.pin = pin;
    }

    public static void encode(TerminalRequestPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeInt(pkt.amount);
        buf.writeUtf(pkt.pin, 16);
    }

    public static TerminalRequestPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        int amount = buf.readInt();
        String pin = buf.readUtf(16);
        return new TerminalRequestPacket(pos, amount, pin);
    }

    public static void handle(TerminalRequestPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            BlockEntity be = player.level().getBlockEntity(pkt.pos);
            if (be instanceof TerminalBlockEntity terminal) {
                terminal.processPayment(player, pkt.amount, pkt.pin);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
