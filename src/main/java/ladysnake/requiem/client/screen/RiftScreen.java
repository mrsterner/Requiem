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
package ladysnake.requiem.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.block.ObeliskDescriptor;
import ladysnake.requiem.common.network.RequiemNetworking;
import ladysnake.requiem.common.screen.RiftScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RiftScreen extends HandledScreen<RiftScreenHandler> {
    private static final Identifier RIFT_ICONS = Requiem.id("textures/gui/soul_rift.png");
    private static final Vector3f VECTOR_ZERO = new Vector3f(0, 0, 0);

    private @Nullable Matrix4f projectionViewMatrix;
    private @Nullable ObeliskDescriptor currentMouseOver;
    private int selectionIndex = 0;
    private int overlappingSelections = 1;

    public RiftScreen(RiftScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    public boolean shouldPause() {
        return true;
    }

    @Override
    protected void drawBackground(DrawContext graphics, float delta, int mouseX, int mouseY) {
        // Actually nothing
    }

    @Override
    protected void drawForeground(DrawContext graphics, int mouseX, int mouseY) {
        if (this.projectionViewMatrix != null) {
            int obeliskV = 0;
            int sourceObeliskV = 16;
            int selectedObeliskV = 32;
            int textureHeight = 48;
            int textureWidth = 16;
            int iconSize = 16;
            int iconHalfSize = iconSize / 2;
            int centerX = this.width / 2;
            int centerY = this.height / 2;
            this.currentMouseOver = null;
            List<ObeliskDescriptor> selected = new ArrayList<>();
            RenderSystem.setShaderTexture(0, RIFT_ICONS);

            for (ObeliskDescriptor obelisk : this.handler.getObelisks()) {
                Vector3f projected = worldToScreenSpace(this.projectionViewMatrix, obelisk.center());
                int x, y;

                if (projected.z() > 0
                    && projected.x() >= 0 && projected.x() <= width
                    && projected.y() >= 0 && projected.y() <= height) {
                    // Obelisk is in front of us, just display the icon on the screen
                    x = Math.round(projected.x());
                    y = Math.round(projected.y());
                } else {
                    // Obelisk is outside the screen's boundaries
                    if (projected.z() < 0) {
                        // when the point gets behind us, it becomes mirrored, so we have to un-mirror it
                        projected.set(width - projected.x(), height - projected.y(), projected.z());
                    }

                    // Project point to border of the screen
                    // https://stackoverflow.com/questions/1585525/how-to-find-the-intersection-point-between-a-line-and-a-rectangle
                    double slope = (projected.y() - centerY) / (projected.x() - centerX);
                    double heightAtCenterX = slope * (width * 0.5);
                    double lengthAtCenterY = (height * 0.5) / slope;

                    if (height * -0.5 <= heightAtCenterX && heightAtCenterX <= height * 0.5) {
                        if (projected.x() > centerX) {
                            // Right edge
                            x = width;
                        } else {
                            // Left edge
                            x = 0;
                            // Left from center means negative slope, which means inverted Y shift
                            heightAtCenterX = -heightAtCenterX;
                        }
                        y = (int) Math.round(centerY + heightAtCenterX);
                    } else {
                        if (projected.y() > centerY) {
                            // Bottom edge (bottom edge is bigger Y)
                            y = height;
                        } else {
                            // Top edge
                            y = 0;
                            // Above center means negative slope, which means inverted X shift
                            lengthAtCenterY = -lengthAtCenterY;
                        }
                        x = (int) Math.round(centerX + lengthAtCenterY);
                    }
                }

                x = MathHelper.clamp(x, 0, width);
                y = MathHelper.clamp(y, 0, height);

                int v;
                if (obelisk.equals(this.getScreenHandler().getSource())) {
                    v = sourceObeliskV;
                } else if (x > (centerX - iconHalfSize) && x < (centerX + iconHalfSize) && y > (centerY - iconHalfSize) && y < (centerY + iconHalfSize)) {
                    v = selectedObeliskV;
                    selected.add(obelisk);
                } else {
                    v = obeliskV;
                }

                graphics.drawTexture(RIFT_ICONS, x - iconHalfSize, y - iconHalfSize, 0, v, iconSize, iconSize, textureWidth, textureHeight);
            }

            if (!selected.isEmpty()) {
                this.overlappingSelections = selected.size();
                int selectedIndex = this.getSelectionIndex();
                List<Text> lines = new ArrayList<>(selected.size());

                for (int i = 0; i < selected.size(); i++) {
                    ObeliskDescriptor obelisk = selected.get(i);
                    Formatting formatting;

                    if (i == selectedIndex) {
                        this.currentMouseOver = obelisk;
                        formatting = Formatting.LIGHT_PURPLE;
                    } else {
                        formatting = Formatting.GRAY;
                    }

                    lines.add(Text.of(obelisk.resolveName()).copy().formatted(formatting));
                }

                graphics.drawTooltip(this.textRenderer, lines, centerX, centerY);
            } else {
                this.overlappingSelections = 1;
            }
        }

        graphics.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 0xA0A0A0, false);
    }

    private int getSelectionIndex() {
        return MathHelper.clamp(this.selectionIndex, 0, overlappingSelections - 1);
    }

    @Override
    public void render(DrawContext graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
    }

    private Vector3f worldToScreenSpace(Matrix4f projectionViewMatrix, Vec3d worldPos) {
        Vec3d cameraPos = Objects.requireNonNull(client).gameRenderer.getCamera().getPos();
        Vector4f clipSpacePos = worldToClipSpace(projectionViewMatrix, worldPos, cameraPos, 0);

        // If W is 0, we cannot perform the division, so we retry with a little nudge
        if (clipSpacePos.w() == 0) {
            clipSpacePos = worldToClipSpace(projectionViewMatrix, worldPos, cameraPos, 0.00001F);
        }

        float sign = Math.signum(clipSpacePos.w());
        try {
            clipSpacePos.div(clipSpacePos.w());
        } catch (ArithmeticException e) {
            // Should be pretty rare, but may hypothetically happen ?
            return VECTOR_ZERO;
        }

        return new Vector3f(
            ((clipSpacePos.x() + 1f) / 2f) * this.width,
            ((-clipSpacePos.y() + 1f) / 2f) * this.height,   // screen coordinates origin are at top-left
            Math.abs(clipSpacePos.z()) * sign // we use the depth to know if a point is behind us, or behind another point
        );
    }

    private Vector4f worldToClipSpace(Matrix4f projectionViewMatrix, Vec3d worldPos, Vec3d cameraPos, float nudge) {
        Vector4f clipSpacePos = new Vector4f(
            (float) (worldPos.getX() + 0.5F - cameraPos.getX() + nudge),
            (float) (worldPos.getY() + 0.5F - cameraPos.getY() + nudge),
            (float) (worldPos.getZ() + 0.5F - cameraPos.getZ() + nudge),
            1.0F
        );
        clipSpacePos.mul(projectionViewMatrix);
        return clipSpacePos;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.currentMouseOver != null) {
            RequiemNetworking.sendRiftUseMessage(this.currentMouseOver);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

/* TODO

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        this.selectionIndex = Math.floorMod((int) (this.getSelectionIndex() - amount), this.overlappingSelections);
        return true;
    }

 */

    @Override
    protected void init() {
        super.init();
        this.x = 0;
        this.y = 0;
        this.backgroundWidth = this.width;
        this.backgroundHeight = this.height;
        // Center the title
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }

    public void updateMatrices(MatrixStack modelViewStack, Matrix4f projectionMatrix) {
        this.projectionViewMatrix = new Matrix4f(projectionMatrix);
        this.projectionViewMatrix.mul(modelViewStack.peek().getPositionMatrix());
    }
}
