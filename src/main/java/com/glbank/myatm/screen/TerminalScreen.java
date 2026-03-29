package com.glbank.myatm.screen;

import com.glbank.myatm.menu.TerminalMenu;
import com.glbank.myatm.network.PacketHandler;
import com.glbank.myatm.network.TerminalRequestPacket;
import com.glbank.myatm.network.TerminalResponsePacket;
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

public class TerminalScreen extends AbstractContainerScreen<TerminalMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("myatm", "textures/gui/terminal.png");
    private static final ResourceLocation TEXTURE_ACTIVE =
            new ResourceLocation("myatm", "textures/gui/terminal_active.png");

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;

    private EditBox amountField;
    private EditBox pinField;
    private Button payButton;

    private String statusLine = "";
    private int statusColor = 0xFFFFFF;
    private boolean waiting = false;
    private boolean showActive = false;

    private final BlockPos blockPos;

    public TerminalScreen(TerminalMenu menu, Inventory playerInventory, Component title) {
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


        amountField = new EditBox(this.font, x + 10, y + 29, 80, 12,
                Component.literal("Amount"));
        amountField.setMaxLength(10);
        amountField.setHint(Component.literal("Amount (DC)"));
        addRenderableWidget(amountField);


        pinField = new EditBox(this.font, x + 10, y + 47, 80, 12,
                Component.literal("PIN"));
        pinField.setMaxLength(8);
        pinField.setHint(Component.literal("PIN"));
        pinField.setFormatter((str, pos) ->
                net.minecraft.util.FormattedCharSequence.forward(
                        "*".repeat(str.length()),
                        net.minecraft.network.chat.Style.EMPTY));
        addRenderableWidget(pinField);


        payButton = Button.builder(Component.literal("Pay"), btn -> sendPayment())
                .pos(x + 96, y + 52)
                .size(70, 18)
                .build();
        addRenderableWidget(payButton);
    }

    private void sendPayment() {
        String amountStr = amountField.getValue().trim();
        String pin = pinField.getValue().trim();

        if (amountStr.isEmpty() || pin.isEmpty()) {
            setStatus(false, "Fill amount and PIN");
            return;
        }
        if (!pin.matches("\\d{4,8}")) {
            setStatus(false, "PIN: 4-8 digits");
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(amountStr);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            setStatus(false, "Invalid amount");
            return;
        }

        waiting = true;
        payButton.active = false;
        setStatus(false, "Processing...");

        PacketHandler.CHANNEL.send(
                PacketDistributor.SERVER.noArg(),
                new TerminalRequestPacket(blockPos, amount, pin)
        );
    }


    public void handleResponse(TerminalResponsePacket pkt) {
        waiting = false;
        if (payButton != null) payButton.active = true;

        if (pkt.success) {
            setStatus(true, "Payment done!");
            showActive = true;
            amountField.setValue("");
            pinField.setValue("");
        } else {
            String msg = pkt.message.isEmpty() ? "Error" : pkt.message;
            setStatus(false, msg);
        }
    }

    private void setStatus(boolean success, String line) {
        this.statusLine = line;
        this.statusColor = success ? 0x55FF55 : 0xFF5555;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        if (amountField.isFocused() || pinField.isFocused()) {
            if (keyCode == 256) {
                this.onClose();
                return true;
            }
            return amountField.keyPressed(keyCode, scanCode, modifiers)
                    || pinField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        ResourceLocation tex = showActive ? TEXTURE_ACTIVE : TEXTURE;
        graphics.blit(tex, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        int x = leftPos;
        int y = topPos;

        if (!statusLine.isEmpty() && statusColor != 0x55FF55) {
            graphics.drawString(font, statusLine, x + 10, y + 84, statusColor, false);
        }

    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
    }
}