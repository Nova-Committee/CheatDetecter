package top.infsky.cheatdetector.impl.modules.common;

import lombok.Getter;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.infsky.cheatdetector.impl.Module;
import top.infsky.cheatdetector.utils.TRSelf;
import top.infsky.cheatdetector.config.ModuleConfig;

public class NoRotateSet extends Module {
    @Getter
    @Nullable
    private static Module instance = null;
    public NoRotateSet(@NotNull TRSelf player) {
        super("NoRotateSet", player);
        instance = this;
    }

    @Override
    public boolean _handleMovePlayer(@NotNull ClientboundPlayerPositionPacket packet, @NotNull CallbackInfo ci) {
        if (isDisabled() || ci.isCancelled()) return false;
        if (packet.getId() != player.fabricPlayer.getId()) return false;

        if (hasRotation(packet)) {
            ci.cancel();
            if (hasPosition(packet)) {
                player.fabricPlayer.connection.handleMovePlayer(new ClientboundPlayerPositionPacket(
                        packet.getX(), packet.getY(), packet.getZ(), player.fabricPlayer.getYRot(), player.fabricPlayer.getXRot(), packet.getRelativeArguments(), packet.getId()
                ));
            }
            return true;
        }
        return false;
    }

    public boolean hasRotation(@NotNull ClientboundPlayerPositionPacket packet) {
        return packet.getXRot() != player.fabricPlayer.getXRot() || packet.getYRot() != player.fabricPlayer.getYRot();
    }

    public boolean hasPosition(@NotNull ClientboundPlayerPositionPacket packet) {
        return new Vec3(packet.getX(), packet.getY(), packet.getZ()).distanceTo(player.fabricPlayer.position()) != 0;
    }

    @Override
    public boolean isDisabled() {
        return !ModuleConfig.noRotateSetEnabled;
    }
}