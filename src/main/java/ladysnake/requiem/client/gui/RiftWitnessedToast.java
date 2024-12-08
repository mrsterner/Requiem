/*
 * Requiem
 * Copyright (C) 2017-2024 Ladysnake
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
package ladysnake.requiem.client.gui;

import ladysnake.requiem.common.block.RequiemBlocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class RiftWitnessedToast implements Toast {
    private static final Identifier TEXTURE = Identifier.ofVanilla("toast/advancement");
    private static final MutableText RIFT_WITNESSED_TEXT = Text.translatable("requiem:toast.rift_witnessed");
    private static final ItemStack RIFT_BLOCK = new ItemStack(RequiemBlocks.RIFT_RUNE);
    private final Text riftName;

    public RiftWitnessedToast(Text riftName) {
        this.riftName = riftName;
    }

    @Override
    public Visibility draw(DrawContext graphics, ToastManager manager, long startTime) {
        graphics.drawTexture(TEXTURE, 0, 0, 0, 0, this.getWidth(), this.getHeight());
        List<OrderedText> lines = manager.getClient().textRenderer.wrapLines(this.riftName, 125);
        int headerColor = 0xff88ff;
        switch (lines.size()) {
            case 0 -> graphics.drawText(manager.getClient().textRenderer, RIFT_WITNESSED_TEXT, 30, 11, headerColor | 0xFF000000, false);
            case 1 -> {
                graphics.drawText(manager.getClient().textRenderer, RIFT_WITNESSED_TEXT, 30, 7, headerColor | 0xFF000000, false);
                graphics.drawText(manager.getClient().textRenderer, lines.get(0), 30, 18, -1, false);
            }
            default -> {
                if (startTime < 1500L) {
                    int k = MathHelper.floor(MathHelper.clamp((float)(1500L - startTime) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 0x4000000;
                    graphics.drawText(manager.getClient().textRenderer, RIFT_WITNESSED_TEXT, 30, 11, headerColor | k, false);
                } else {
                    int k = MathHelper.floor(MathHelper.clamp((float)(startTime - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 0x4000000;
                    int y = this.getHeight() / 2 - lines.size() * 9 / 2;

                    for(OrderedText orderedText : lines) {
                        graphics.drawText(manager.getClient().textRenderer, orderedText, 30, y, 0xffffff | k, false);
                        y += 9;
                    }
                }
            }
        }

        graphics.drawItem(RIFT_BLOCK, 8, 8);
        return startTime >= 5000L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }
}
