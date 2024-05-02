package top.infsky.cheatdetector.impl.modules.hypixel;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import oshi.util.tuples.Pair;
import top.infsky.cheatdetector.config.Advanced3Config;
import top.infsky.cheatdetector.config.ModuleConfig;
import top.infsky.cheatdetector.impl.Module;
import top.infsky.cheatdetector.impl.utils.AimSimulator;
import top.infsky.cheatdetector.impl.utils.ClickSimulator;
import top.infsky.cheatdetector.impl.utils.world.LevelUtils;
import top.infsky.cheatdetector.impl.utils.world.PlayerRotation;
import top.infsky.cheatdetector.mixins.MouseHandlerInvoker;
import top.infsky.cheatdetector.utils.TRPlayer;
import top.infsky.cheatdetector.utils.TRSelf;

import java.util.*;

public class Killaura extends Module {
    @Nullable
    private Pair<AbstractClientPlayer, Set<Task>> lastTarget = null;
    private int hasWaitAfterSwitch = Integer.MAX_VALUE;
    private final ClickSimulator clickSimulator;
    public Killaura(@NotNull TRSelf player) {
        super("Killaura", player);
        clickSimulator = new ClickSimulator(Advanced3Config.killauraMinCPS, Advanced3Config.killauraMaxCPS);
    }

    @Override
    public void _onTick() {
        if (isDisabled()) return;

        clickSimulator.setCPS(Advanced3Config.killauraMinCPS, Advanced3Config.killauraMaxCPS);
        boolean attack = clickSimulator.tickShouldClick();

        Map<Double, Pair<AbstractClientPlayer, Set<Task>>> targets = new TreeMap<>();
        for (AbstractClientPlayer worldPlayer : LevelUtils.getClientLevel().players()) {
            if (worldPlayer.is(player.fabricPlayer)) continue;
            double distance = worldPlayer.distanceTo(player.fabricPlayer);

            if (Advanced3Config.killauraAttack && distance < Advanced3Config.killauraAttackReach) {
                targets.put(distance, new Pair<>(worldPlayer, Set.of(Task.AIM, Task.ATTACK)));
            } else if (Advanced3Config.killauraPreAim && distance < Advanced3Config.killauraPreAimReach) {
                targets.put(distance, new Pair<>(worldPlayer, Set.of(Task.AIM)));
            }
        }

        for (Pair<AbstractClientPlayer, Set<Task>> target : targets.values()) {
            // 必定按照距离从小到大排序，也就是第一个目标一定是距离最近的玩家
            AbstractClientPlayer worldPlayer = target.getA();

            if (Advanced3Config.killauraSwitch && hasWaitAfterSwitch < Advanced3Config.killauraSwitchDelay
                    && targets.containsValue(lastTarget)) continue;
            else {
                lastTarget = target;
                hasWaitAfterSwitch = 0;
            }

            Set<Task> tasks = target.getB();

            for (Task task : tasks) {
                switch (task) {
                    case AIM -> rotate(worldPlayer);
                    case ATTACK -> {
                        if (attack) attack(worldPlayer);
                    }
                }
            }

            if (!Advanced3Config.killauraSwitch) break;
        }
    }

    private void rotate(AbstractClientPlayer target) {
        if (Advanced3Config.killauraNoRotation) return;

        Pair<Double, Double> rot;
        if (Advanced3Config.killauraLegitAim) {
            rot = AimSimulator.getLegitAim(target, player);
        } else {
            rot = new Pair<>(
                    PlayerRotation.getYaw(target.getEyePosition()),
                    PlayerRotation.getPitch(target.getEyePosition())
            );
        }

        if (Advanced3Config.killauraLookView)
            PlayerRotation.rotate(rot.getA(), rot.getB());
        else
            PlayerRotation.silentRotate(rot.getA(), rot.getB(), player.currentOnGround);
    }

    private void attack(AbstractClientPlayer target) {
        if (Advanced3Config.killauraRayCast && Advanced3Config.killauraLookView) {
            if (TRPlayer.CLIENT.crosshairPickEntity != target) return;
        }

        Connection connection = Objects.requireNonNull(TRPlayer.CLIENT.getConnection()).getConnection();

        player.fabricPlayer.swing(InteractionHand.MAIN_HAND);
        connection.send(ServerboundInteractPacket.createAttackPacket(target, false));
        player.fabricPlayer.attack(target);
    }

    @Override
    public boolean isDisabled() {
        return !ModuleConfig.killauraEnabled || !ModuleConfig.aaaHypixelModeEnabled;
    }

    @Override
    public boolean _handleMovePlayer(@NotNull ServerboundMovePlayerPacket packet, @NotNull Connection connection, PacketSendListener listener, @NotNull CallbackInfo ci) {
        if (isDisabled()) return false;
        if (Advanced3Config.killauraLookView) return false;
        return PlayerRotation.cancelRotationPacket(packet, connection, listener, ci);
    }

    public enum Task {
        AIM,
        ATTACK
    }
}