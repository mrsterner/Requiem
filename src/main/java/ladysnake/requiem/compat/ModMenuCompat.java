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
package ladysnake.requiem.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigBranch;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.common.RequiemConfig;
import me.shedaniel.clothconfig2.api.AbstractConfigEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.AbstractConfigScreen;
import me.shedaniel.fiber2cloth.api.Fiber2Cloth;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collections;

public final class ModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        try {
            return new ClothConfigScreenFactory(Requiem.MOD_ID, RequiemConfig.configTree(), RequiemConfig::save);
        } catch (Throwable t) {
            return parent -> null;
        }
    }

    public static class ClothConfigScreenFactory implements ConfigScreenFactory<Screen> {

        private final String modId;
        private final ConfigBranch node;
        private final Runnable save;

        public ClothConfigScreenFactory(String modId, ConfigBranch node, Runnable save) {
            this.modId = modId;
            this.node = node;
            this.save = save;
        }

        @Override
        public Screen create(Screen parent) {
            return appendInfo((AbstractConfigScreen) Fiber2Cloth.create(parent, modId, node, "config.requiem.title")
                .setSaveRunnable(save)
                .build()
                .getScreen());
        }

        private AbstractConfigScreen appendInfo(AbstractConfigScreen screen) {
            AbstractConfigEntry<?> entry = this.createInfoEntry();
            entry.setScreen(screen);
            screen.getCategorizedEntries().put(Text.translatable("config.requiem.more"), Collections.singletonList(entry));
            return screen;
        }

        private AbstractConfigEntry<?> createInfoEntry() {
            return ConfigEntryBuilder.create()
                .startTextDescription(Text.translatable(
                    "config.requiem.more_info",
                    makeUrlText(Text.translatable("config.requiem.more_info.datapacks"), getLocalizedDataPackUrl()),
                    makeUrlText(Text.translatable("config.requiem.more_info.gamerules"), getLocalizedGameRuleUrl()),
                    makeUrlText(Text.translatable("config.requiem.more_info.official_doc"), "https://ladysnake.github.io/wiki/requiem/configuration")
                )).setColor(0xFFFFFFFF).build();
        }

        private String getLocalizedGameRuleUrl() {
            //noinspection SwitchStatementWithTooFewBranches
            switch (MinecraftClient.getInstance().getLanguageManager().getLanguage().substring(0, 2)) {
                case "pt": return "https://minecraft-pt.gamepedia.com/Regra_de_jogo";
                default: return "https://minecraft.gamepedia.com/Game_rule";
            }
        }

        private String getLocalizedDataPackUrl() {
            switch (MinecraftClient.getInstance().getLanguageManager().getLanguage().substring(0, 2)) {
                case "fr": return "https://minecraft-fr.gamepedia.com/Pack_de_donn%C3%A9es";
                case "de": return "https://minecraft-de.gamepedia.com/Datenpaket";
                case "ja": return "https://minecraft-ja.gamepedia.com/%E3%83%87%E3%83%BC%E3%82%BF%E3%83%91%E3%83%83%E3%82%AF";
                case "pl": return "https://minecraft-pl.gamepedia.com/Paczki_danych";
                case "pt": return "https://minecraft-pt.gamepedia.com/Pacote_de_dados";
                case "ru": return "https://minecraft-ru.gamepedia.com/%D0%9D%D0%B0%D0%B1%D0%BE%D1%80_%D0%B4%D0%B0%D0%BD%D0%BD%D1%8B%D1%85";
                case "zh": return "https://minecraft-zh.gamepedia.com/%E6%95%B0%E6%8D%AE%E5%8C%85";
                default: return "https://minecraft.gamepedia.com/Data_Pack";
            }
        }

        private MutableText makeUrlText(MutableText text, String url) {
            return text.styled(style -> style
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(url)))
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                .withColor(Formatting.BLUE)
            );
        }
    }
}
