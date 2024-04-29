package top.infsky.cheatdetector.anticheat.modules;

import lombok.Getter;
import lombok.val;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.infsky.cheatdetector.anticheat.Module;
import top.infsky.cheatdetector.anticheat.TRPlayer;
import top.infsky.cheatdetector.anticheat.TRSelf;
import top.infsky.cheatdetector.anticheat.utils.ListUtils;
import top.infsky.cheatdetector.config.ModuleConfig;

import java.util.*;

public class AntiVanish extends Module {
    @Getter
    @Nullable
    private static Module instance = null;
    private String lastMsg;
    public AntiVanish(@NotNull TRSelf player) {
        super("AntiVanish", player);
        instance = this;
    }

    @Override
    public boolean _handlePlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket packet, CallbackInfo ci) {
        if (isDisabled()) return false;
        if (TRPlayer.CLIENT.getConnection() == null) return false;

        final EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions = packet.actions();

        if (actions.contains(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY)) {
            final List<UUID> packetUUIDs = new ArrayList<>();
            final List<UUID> onlineUUIDs = new ArrayList<>();

            for (val player : packet.entries()) {
                packetUUIDs.add(player.profileId());
            }
            for (val player : TRPlayer.CLIENT.getConnection().getListedOnlinePlayers()) {
                onlineUUIDs.add(player.getProfile().getId());
            }

            customMsg(packetUUIDs + "\n" + onlineUUIDs);
            if (packetUUIDs.size() != onlineUUIDs.size()) {
                val uuidDifference = ListUtils.getDifference(packetUUIDs, onlineUUIDs);
                val stringDifference = new ArrayList<String>(uuidDifference.size());
                uuidDifference.forEach(uuid -> {
                    val playerInfo = TRPlayer.CLIENT.getConnection().getPlayerInfo(uuid);
                    if (playerInfo != null) stringDifference.add(playerInfo.getProfile().getName());
                });

                val msg = Component.translatable("cheatdetector.chat.alert.foundVanish").getString() + ListUtils.getSpilt(stringDifference, ".");
                if (!msg.equals(lastMsg)) {
                    customMsg(msg);
                    lastMsg = msg;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isDisabled() {
        return !ModuleConfig.antiVanishEnabled;
    }
}
