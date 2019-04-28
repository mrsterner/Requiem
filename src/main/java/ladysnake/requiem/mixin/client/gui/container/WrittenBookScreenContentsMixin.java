/*
 * Requiem
 * Copyright (C) 2019 Ladysnake
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
 */
package ladysnake.requiem.mixin.client.gui.container;

import net.minecraft.client.gui.WrittenBookScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WrittenBookScreen.Contents.class)
public interface WrittenBookScreenContentsMixin {
/* TODO enable when mixin supports injection into interface static methods
    @Inject(method = "create", at = @At("TAIL"), cancellable = true)
    static void handleOpiDaemonium(ItemStack book, CallbackInfoReturnable<WrittenBookScreen.Contents> cir) {
        Item item = book.getItem();
        if (item instanceof OpusDemoniumItem) {
            cir.setReturnValue(new WrittenBookScreen.WritableBookContents(book));
        } else if (item instanceof WrittenOpusItem) {
            cir.setReturnValue(new WrittenOpusContents((WrittenOpusItem) item));
        }
    }
*/
}
