/*
 * Requiem
 * Copyright (C) 2017-2023 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 *
 * Linking this mod statically or dynamically with other
 * modules is making a combined work based on this mod.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * In addition, as a special exception, the copyright holders of
 * this mod give you permission to combine this mod
 * with free software programs or libraries that are released under the GNU LGPL
 * and with code included in the standard release of Minecraft under All Rights Reserved (or
 * modified versions of such code, with unchanged license).
 * You may copy and distribute such a system following the terms of the GNU GPL for this mod
 * and the licenses of the other code concerned.
 *
 * Note that people who make modified versions of this mod are not obligated to grant
 * this special exception for their modified versions; it is their choice whether to do so.
 * The GNU General Public License gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */
package ladysnake.requiem.mixin.client.inventory;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.entity.InventoryLimiter;
import ladysnake.requiem.api.v1.entity.InventoryShape;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.client.RequiemClient;
import ladysnake.requiem.common.network.RequiemNetworking;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import ladysnake.requiem.core.inventory.PossessionInventoryScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.ladysnake.locki.DefaultInventoryNodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> {
    @Unique
    private static final Identifier INVENTORY_SLOTS = Requiem.id("textures/gui/inventory_slots.png");

    private PlayerEntity requiem$player;
    @Unique
    private TexturedButtonWidget supercrafterButton;

    public InventoryScreenMixin(PlayerScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Inject(method = {"init", "handledScreenTick"}, at = @At("HEAD"), cancellable = true)
    private void trySwapInventoryInit(CallbackInfo ci) {
        if (InventoryLimiter.instance().getInventoryShape(requiem$player) == InventoryShape.ALT_LARGE) {
            assert this.client != null && this.client.player != null;
            this.client.setScreen(new PossessionInventoryScreen(this.client.player));
            ci.cancel();
        }
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addSupercrafterButton(CallbackInfo ci) {
        MobEntity possessedEntity = PossessionComponent.getHost(this.requiem$player);
        if (possessedEntity != null && possessedEntity.getType().isIn(RequiemEntityTypeTags.SUPERCRAFTERS)) {
            this.supercrafterButton = this.addDrawableChild(new TexturedButtonWidget(
                this.x + 131,
                this.height / 2 - 22,
                20,
                18,
                0,
                0,
                19,
                RequiemClient.CRAFTING_BUTTON_TEXTURE,
                (buttonWidget) -> RequiemNetworking.sendSupercrafterMessage())
            );
        }
    }

    @Dynamic("Lambda method, implementation of PressAction for the crafting book button")
    @Inject(method = { "m_ekzothcc", "method_19891" }, at = @At("RETURN"), remap = false, require = 1, allow = 1)
    private void repositionCraftingButton(ButtonWidget button, CallbackInfo ci) {
        if (this.supercrafterButton != null) {
            this.supercrafterButton.setPosition(this.x + 131, this.height / 2 - 22);
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void constructor(PlayerEntity player, CallbackInfo ci) {
        this.requiem$player = player;
    }

    @ModifyArg(method = "drawForeground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)I"))
    private Text swapScreenName(Text name) {
        MobEntity possessedEntity = PossessionComponent.getHost(this.requiem$player);
        if (possessedEntity != null) {
            return possessedEntity.getName();
        }
        return name;
    }

    @Inject(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawEntity(Lnet/minecraft/client/gui/GuiGraphics;IIIFFLnet/minecraft/entity/LivingEntity;)V"))
    private void scissorEntity(GuiGraphics graphics, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        InventoryLimiter.instance().getInventoryShape(requiem$player).setupEntityCrop(this.x, this.y);
    }

    @Inject(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawEntity(Lnet/minecraft/client/gui/GuiGraphics;IIIFFLnet/minecraft/entity/LivingEntity;)V", shift = At.Shift.AFTER))
    private void disableScissor(GuiGraphics graphics, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        InventoryLimiter.instance().getInventoryShape(requiem$player).tearDownEntityCrop();
    }

    // looks like the target is not getting remapped, so we have to resort to good ol ordinal
    @ModifyArg(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"))
    private Identifier swapBackground(Identifier background) {
        return InventoryLimiter.instance().getInventoryShape(requiem$player).swapBackground(background);
    }

    @Inject(
        method = "drawBackground",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V",
            shift = At.Shift.AFTER
        )
    )
    private void drawSlots(GuiGraphics graphics, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        assert this.client != null;

        InventoryShape inventoryShape = InventoryLimiter.instance().getInventoryShape(requiem$player);
        if (inventoryShape.isAltShape()) {
            int x = this.x;
            int y = this.y;
            if (!InventoryLimiter.instance().isLocked(this.requiem$player, DefaultInventoryNodes.ARMOR)) {
                graphics.drawTexture(INVENTORY_SLOTS, x + 7, y + 7, 7, 7, 18, 72);
            }
            if (!InventoryLimiter.instance().isLocked(this.requiem$player, DefaultInventoryNodes.HANDS)) {
                if (inventoryShape == InventoryShape.ALT_SMALL) {
                    graphics.drawTexture(INVENTORY_SLOTS, x + 76, y + 61, 76, 61, 18, 18);
                } else {
                    graphics.drawTexture(INVENTORY_SLOTS, x + 46, y + 61, 46, 61, 48, 18);
                }
            }
            if (!InventoryLimiter.instance().isLocked(this.requiem$player, DefaultInventoryNodes.CRAFTING)) {
                graphics.drawTexture(INVENTORY_SLOTS, x + 97, y + 17, 97, 17, 75, 36);
            }
            if (!InventoryLimiter.instance().isLocked(this.requiem$player, DefaultInventoryNodes.MAIN_INVENTORY)) {
                graphics.drawTexture(INVENTORY_SLOTS, x + 7, y + 83, 7, 83, 162, 76);
            } else if (!InventoryLimiter.instance().isLocked(this.requiem$player, DefaultInventoryNodes.HOTBAR)) {
                graphics.drawTexture(INVENTORY_SLOTS, x + 7, y + 141, 7, 141, 162, 18);
            }
        }
    }

    @ModifyArg(
        method = "drawBackground",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawEntity(Lnet/minecraft/client/gui/GuiGraphics;IIIFFLnet/minecraft/entity/LivingEntity;)V"),
        index = 1
    )
    private int shiftPossessedEntityX(int x) {
        return (int) InventoryLimiter.instance().getInventoryShape(requiem$player).shiftEntityX(x);
    }

    @ModifyArg(
        method = "drawBackground",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawEntity(Lnet/minecraft/client/gui/GuiGraphics;IIIFFLnet/minecraft/entity/LivingEntity;)V"),
        index = 2
    )
    private int shiftPossessedEntityY(int y) {
        return (int) InventoryLimiter.instance().getInventoryShape(requiem$player).shiftEntityY(y);
    }

    @ModifyArg(
        method = "drawBackground",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawEntity(Lnet/minecraft/client/gui/GuiGraphics;IIIFFLnet/minecraft/entity/LivingEntity;)V"),
        index = 4
    )
    private float shiftPossessedEntityLookX(float x) {
        return InventoryLimiter.instance().getInventoryShape(requiem$player).shiftEntityX(x);
    }

    @ModifyArg(
        method = "drawBackground",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawEntity(Lnet/minecraft/client/gui/GuiGraphics;IIIFFLnet/minecraft/entity/LivingEntity;)V"),
        index = 5
    )
    private float shiftPossessedEntityLookY(float y) {
        return InventoryLimiter.instance().getInventoryShape(requiem$player).shiftEntityY(y);
    }
}
