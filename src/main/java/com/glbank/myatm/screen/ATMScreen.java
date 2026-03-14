package com.glbank.myatm.screen;

import com.glbank.myatm.menu.ATMMenu;
import com.glbank.myatm.network.ATMRequestPacket;
import com.glbank.myatm.network.ATMResponsePacket;
import com.glbank.myatm.network.PacketHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.network.PacketDistributor;

public class ATMScreen extends AbstractContainerScreen<ATMMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("myatm", "textures/gui/atm.png");
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;

    private EditBox loginField;
    private EditBox passwordField;
    private EditBox pinField;
    private Button enterButton;

    private String statusLine1 = "";
    private String statusLine2 = "";
    private int statusColor = 0xFFFFFF;
    private boolean waiting = false;

    private final BlockPos blockPos;

    public ATMScreen(ATMMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.blockPos = menu.blockEntity.getBlockPos();
    }

    @Override
    protected void init() {
        super.init();
        int x = this.leftPos;
        int y = this.topPos;

        loginField = new EditBox(this.font, x + 10, y + 20, 100, 12,
                Component.literal("Login"));
        loginField.setMaxLength(32);
        loginField.setHint(Component.literal("Login"));
        addRenderableWidget(loginField);

        passwordField = new EditBox(this.font, x + 10, y + 38, 100, 12,
                Component.literal("Password"));
        passwordField.setMaxLength(64);
        passwordField.setHint(Component.literal("Password"));
        passwordField.setFormatter((str, pos) ->
                net.minecraft.util.FormattedCharSequence.forward(
                        "*".repeat(str.length()),
                        net.minecraft.network.chat.Style.EMPTY));
        addRenderableWidget(passwordField);

        pinField = new EditBox(this.font, x + 10, y + 56, 80, 12,
                Component.literal("PIN"));
        pinField.setMaxLength(8);
        pinField.setHint(Component.literal("PIN"));
        pinField.setFormatter((str, pos) ->
                net.minecraft.util.FormattedCharSequence.forward(
                        "*".repeat(str.length()),
                        net.minecraft.network.chat.Style.EMPTY));
        addRenderableWidget(pinField);

        enterButton = Button.builder(Component.literal("Enter"), btn -> sendRequest())
                .pos(x + 96, y + 53)
                .size(70, 18)
                .build();
        addRenderableWidget(enterButton);
    }

    private String pendingLogin = "";

    private void sendRequest() {
        String login = loginField.getValue().trim();
        String password = passwordField.getValue();
        String pin = pinField.getValue().trim();

        if (login.isEmpty() || password.isEmpty() || pin.isEmpty()) {
            setStatus(false, "Fill all fields!", "");
            return;
        }
        if (!pin.matches("\\d{4,8}")) {
            setStatus(false, "PIN must be 4-8 digits", "");
            return;
        }

        waiting = true;
        pendingLogin = login;
        enterButton.active = false;
        setStatus(false, "Connecting...", "");

        PacketHandler.CHANNEL.send(
                PacketDistributor.SERVER.noArg(),
                new ATMRequestPacket(blockPos, login, password, pin)
        );
    }

    public void handleResponse(ATMResponsePacket pkt) {
        waiting = false;
        if (enterButton != null) enterButton.active = true;

        if (pkt.success) {
            net.minecraft.world.item.ItemStack cardStack = menu.getSlot(0).getItem();
            if (!cardStack.isEmpty() && cardStack.getItem() instanceof com.glbank.myatm.item.CardItem) {
                cardStack.setHoverName(net.minecraft.network.chat.Component.literal(pendingLogin));
                net.minecraft.nbt.CompoundTag tag = cardStack.getOrCreateTag();
                tag.putString("CardNumber", pkt.cardNumber);
                tag.putString("CVV", pkt.cvv);
            }

            String raw = pkt.cardNumber;
            String formatted = raw.length() == 16
                    ? raw.substring(0, 4) + " " + raw.substring(4, 8)
                    + " " + raw.substring(8, 12) + " " + raw.substring(12, 16)
                    : raw;
            setStatus(true, "Card: " + formatted, "CVV: " + pkt.cvv);
            loginField.setValue("");
            passwordField.setValue("");
            pinField.setValue("");
        } else {
            String msg = pkt.message.isEmpty() ? "Error" : pkt.message;
            setStatus(false, msg, "");
        }
    }

    private void setStatus(boolean success, String line1, String line2) {
        this.statusLine1 = line1;
        this.statusLine2 = line2;
        this.statusColor = success ? 0x55FF55 : 0xFF5555;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (loginField.isFocused() || passwordField.isFocused() || pinField.isFocused()) {
            if (keyCode == 256) {
                this.onClose();
                return true;
            }
            return loginField.keyPressed(keyCode, scanCode, modifiers)
                    || passwordField.keyPressed(keyCode, scanCode, modifiers)
                    || pinField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        int x = leftPos;
        int y = topPos;

        if (!statusLine1.isEmpty()) {
            graphics.drawString(font, statusLine1, x + 10, y + 76, statusColor, false);
        }
        if (!statusLine2.isEmpty()) {
            graphics.drawString(font, statusLine2, x + 10, y + 87, statusColor, false);
        }

        if (waiting) {
            int dots = (int) ((System.currentTimeMillis() / 400) % 4);
            graphics.drawString(font, ".".repeat(dots), x + 80, y + 76, 0xFFAA00, false);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
    }
}