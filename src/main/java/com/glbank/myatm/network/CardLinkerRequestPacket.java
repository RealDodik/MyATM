package com.glbank.myatm.network;

import com.glbank.myatm.blockentity.CardLinkerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CardLinkerRequestPacket {

    private final BlockPos pos;
    private final String login;
    private final String password;
    private final String pin;

    public CardLinkerRequestPacket(BlockPos pos, String login, String password, String pin) {
        this.pos = pos;
        this.login = login;
        this.password = password;
        this.pin = pin;
    }

    public static void encode(CardLinkerRequestPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeUtf(pkt.login, 64);
        buf.writeUtf(pkt.password, 128);
        buf.writeUtf(pkt.pin, 16);
    }

    public static CardLinkerRequestPacket decode(FriendlyByteBuf buf) {
        return new CardLinkerRequestPacket(buf.readBlockPos(), buf.readUtf(64),
                buf.readUtf(128), buf.readUtf(16));
    }

    public static void handle(CardLinkerRequestPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            BlockEntity be = player.level().getBlockEntity(pkt.pos);
            if (be instanceof CardLinkerBlockEntity cl) {
                cl.processRequest(player, pkt.login, pkt.password, pkt.pin);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
