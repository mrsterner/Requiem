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
package ladysnake.requiem.common.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.common.entity.PlayerShellEntity;
import ladysnake.requiem.common.remnant.PlayerSplitter;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;

import static net.minecraft.command.argument.EntityArgumentType.*;
import static net.minecraft.command.argument.GameProfileArgumentType.gameProfile;
import static net.minecraft.command.argument.GameProfileArgumentType.getProfileArgument;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class RequiemShellCommand {
    public static final String SHELL_SUBCOMMAND = "shell";

    public static LiteralArgumentBuilder<ServerCommandSource> shellSubcommand() {
        return literal(SHELL_SUBCOMMAND)
            .then(literal("create")
                .executes(context -> RequiemCommand.runOne(context.getSource().getPlayer(), player -> createShell(context.getSource().getPosition(), player)))
                // pandemonium shell create
                .then(argument("player", player())
                    // pandemonium shell create <player>
                    .executes(context -> RequiemCommand.runOne(getPlayer(context, "player"), player -> createShell(context.getSource().getPosition(), player)))
                )
            )
            .then(literal("split")
                // pandemonium shell split
                .executes(context -> RequiemCommand.runOne(context.getSource().getPlayer(),(s) -> {
                    try {
                        RequiemShellCommand.split(s);
                    } catch (CommandSyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }))
                .then(argument("players", players())
                    // pandemonium shell split <players>
                    .executes(context -> RequiemCommand.runMany(getPlayers(context, "players"), (s) -> {
                        try {
                            RequiemShellCommand.split(s);
                        } catch (CommandSyntaxException e) {
                            throw new RuntimeException(e);
                        }
                    }))
                )
            )
            .then(literal("merge").then(argument("shell", entity())
                .executes(context -> {
                    Entity shell = getEntity(context, "shell");
                    return RequiemCommand.runOne(context.getSource().getPlayer(), player -> {
                        try {
                            merge(player, shell);
                        } catch (CommandSyntaxException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }).then(argument("player", player()).executes(context -> {
                    Entity shell = getEntity(context, "shell");
                    return RequiemCommand.runOne(getPlayer(context, "player"), player -> {
                        try {
                            merge(player, shell);
                        } catch (CommandSyntaxException e) {
                            throw new RuntimeException(e);
                        }
                    });
                })))
            )
            .then(literal("identity").then(literal("set")
                    .then(argument("shells", entities()).then(argument("profile", gameProfile())
                        .executes(context -> {
                            Collection<GameProfile> profiles = getProfileArgument(context, "profile");
                            if (profiles.size() > 1) throw TOO_MANY_PLAYERS_EXCEPTION.create();
                            GameProfile profile = profiles.iterator().next();
                            return RequiemCommand.runMany(getEntities(context, "shells"), s -> {
                                try {
                                    setIdentity(s, profile);
                                } catch (CommandSyntaxException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        })
                    ))
                )
            );
    }

    private static void merge(ServerPlayerEntity player, Entity entity) throws CommandSyntaxException {
        if (!(entity instanceof PlayerShellEntity shell)) {
            Message message = Text.translatable("requiem:commands.shell.fail.not_shell");
            throw new CommandSyntaxException(new SimpleCommandExceptionType(message), message);
        }
        RemnantComponent.get(player).merge(shell);
    }

    private static void setIdentity(Entity shell, GameProfile profile) throws CommandSyntaxException {
        if (!(shell instanceof PlayerShellEntity)) {
            Message message = Text.translatable("requiem:commands.shell.fail.not_shell");
            throw new CommandSyntaxException(new SimpleCommandExceptionType(message), message);
        }
        ((PlayerShellEntity) shell).setDisplayProfile(profile);
    }

    private static void createShell(Vec3d position, ServerPlayerEntity player) {
        PlayerShellEntity shell = PlayerSplitter.createShell(player);
        shell.setPosition(position.x, position.y, position.z);
        player.getWorld().spawnEntity(shell);
    }

    private static void split(ServerPlayerEntity player) throws CommandSyntaxException {
        RemnantComponent remnantComponent = RemnantComponent.get(player);
        if (!remnantComponent.getRemnantType().isDemon()) {
            Message message = Text.translatable("requiem:commands.shell.split.fail.mortal", player.getDisplayName());
            throw new CommandSyntaxException(new SimpleCommandExceptionType(message), message);
        }
        if (remnantComponent.isVagrant()) {
            Message message = Text.translatable("requiem:commands.shell.split.fail.vagrant", player.getDisplayName());
            throw new CommandSyntaxException(new SimpleCommandExceptionType(message), message);
        }
        remnantComponent.splitPlayer(true);
    }
}
