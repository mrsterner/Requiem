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
package ladysnake.requiem.core.util;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;

public class OrderedInventory extends SimpleInventory {
    public OrderedInventory(int size) {
        super(size);
    }

    @Override
    public void readNbtList(NbtList tags, RegistryWrapper.WrapperLookup registries) {
        for (int slot = 0; slot < this.size(); ++slot) {
            this.setStack(slot, ItemStack.EMPTY);
        }

        for (int i = 0; i < tags.size(); ++i) {
            NbtCompound compoundTag = tags.getCompound(i);
            int slot = compoundTag.getByte("Slot") & 255;
            if (slot >= 0 && slot < this.size()) {
                this.setStack(slot, ItemStack.fromNbt(registries, compoundTag).get());
            }
        }
    }

    @Override
    public NbtList toNbtList(RegistryWrapper.WrapperLookup registries) {
        NbtList listTag = new NbtList();

        for (int slot = 0; slot < this.size(); ++slot) {
            ItemStack itemStack = this.getStack(slot);
            if (!itemStack.isEmpty()) {
                NbtCompound compoundTag = new NbtCompound();
                compoundTag.putByte("Slot", (byte) slot);
                itemStack.encode(registries);
                listTag.add(compoundTag);
            }
        }

        return listTag;
    }
}
