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
package ladysnake.requiem.common.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;

import java.util.Optional;

public class RemnantTradeOffer extends TradeOffer {
    private final TradeOffer vanillaOffer, demonOffer;
    private final boolean exorcism;
    private boolean tempDisabled;
    boolean demonCustomer;

    public static final Codec<RemnantTradeOffer> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
                // Assume these match the properties of RemnantTradeOffer
                TradeOffer.CODEC.fieldOf("vanilla_offer").forGetter(tradeOffer -> tradeOffer.vanillaOffer),
                TradeOffer.CODEC.fieldOf("demon_offer").forGetter(tradeOffer -> tradeOffer.demonOffer),
                Codec.BOOL.fieldOf("exorcism").forGetter(RemnantTradeOffer::isExorcism)
            )
            .apply(instance, RemnantTradeOffer::new)
    );

    public RemnantTradeOffer(TradeOffer vanillaOffer, TradeOffer demonOffer, boolean exorcism) {
        super(new TradedItem(vanillaOffer.getOriginalFirstBuyItem().getItem()), vanillaOffer.getSecondBuyItem(), vanillaOffer.getSellItem(), vanillaOffer.getUses(), vanillaOffer.getMaxUses(), vanillaOffer.getMerchantExperience(), vanillaOffer.getPriceMultiplier(), vanillaOffer.getDemandBonus());
        this.vanillaOffer = vanillaOffer;
        this.demonOffer = demonOffer;
        this.exorcism = exorcism;
    }

    private TradeOffer getDelegate() {
        return this.demonCustomer ? this.demonOffer : this.vanillaOffer;
    }

    public boolean isExorcism() {
        return this.demonCustomer && this.exorcism;
    }

    @Override
    public ItemStack getOriginalFirstBuyItem() {
        return getDelegate().getOriginalFirstBuyItem();
    }

    @Override
    public ItemStack getDisplayedFirstBuyItem() {
        return getDelegate().getDisplayedFirstBuyItem();
    }

    @Override
    public Optional<TradedItem> getSecondBuyItem() {
        return getDelegate().getSecondBuyItem();
    }

    @Override
    public ItemStack getSellItem() {
        return getDelegate().getSellItem();
    }

    @Override
    public void updateDemandBonus() {
        getDelegate().updateDemandBonus();
    }

    @Override
    public ItemStack copySellItem() {
        return getDelegate().copySellItem();
    }

    @Override
    public int getUses() {
        return getDelegate().getUses();
    }

    @Override
    public void resetUses() {
        getDelegate().resetUses();
    }

    @Override
    public int getMaxUses() {
        return getDelegate().getMaxUses();
    }

    @Override
    public void use() {
        getDelegate().use();
    }

    @Override
    public int getDemandBonus() {
        return getDelegate().getDemandBonus();
    }

    @Override
    public void increaseSpecialPrice(int increment) {
        getDelegate().increaseSpecialPrice(increment);
    }

    @Override
    public void clearSpecialPrice() {
        getDelegate().clearSpecialPrice();
    }

    @Override
    public int getSpecialPrice() {
        return getDelegate().getSpecialPrice();
    }

    @Override
    public void setSpecialPrice(int specialPrice) {
        getDelegate().setSpecialPrice(specialPrice);
    }

    @Override
    public float getPriceMultiplier() {
        return getDelegate().getPriceMultiplier();
    }

    @Override
    public int getMerchantExperience() {
        return getDelegate().getMerchantExperience();
    }

    @Override
    public boolean isDisabled() {
        return tempDisabled || getDelegate().isDisabled();
    }

    @Override
    public void disable() {
        getDelegate().disable();
    }

    @Override
    public boolean hasBeenUsed() {
        return getDelegate().hasBeenUsed();
    }

    @Override
    public boolean shouldRewardPlayerExperience() {
        return getDelegate().shouldRewardPlayerExperience();
    }

    @Override
    public boolean matchesBuyItems(ItemStack first, ItemStack second) {
        return getDelegate().matchesBuyItems(first, second);
    }

    @Override
    public boolean depleteBuyItems(ItemStack firstBuyStack, ItemStack secondBuyStack) {
        return getDelegate().depleteBuyItems(firstBuyStack, secondBuyStack);
    }

    public void setRemnant(boolean demon) {
        demonCustomer = demon;
    }

    public void setTempDisabled(boolean tempDisabled) {
        this.tempDisabled = tempDisabled;
    }
}
