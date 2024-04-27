package top.infsky.cheatdetector.anticheat;

import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.infsky.cheatdetector.config.AlertConfig;
import top.infsky.cheatdetector.config.FixesConfig;
import top.infsky.cheatdetector.utils.LogUtils;

public abstract class Fix extends Check {
    @Nullable
    public static Fix instance = null;
    public TRSelf player;
    public Fix(String fixName, @NotNull TRSelf player) {
        super(fixName, player);
        this.player = player;
        instance = this;
    }

    @Override
    public void flag() {
        if (!FixesConfig.packetFixEnabled) return;
        if (isDisabled()) return;
        violations++;
        if (!AlertConfig.disableBuffer)
            if (violations % getAlertBuffer() != 0) return;
        LogUtils.alert(player.fabricPlayer.getName().getString(), checkName, String.format("(VL:%s)", violations));
    }

    @Override
    public void flag(String extraMsg) {
        if (!FixesConfig.packetFixEnabled) return;
        if (isDisabled()) return;
        violations++;
        if (!AlertConfig.disableBuffer)
            if (violations % getAlertBuffer() != 0) return;
        LogUtils.alert(player.fabricPlayer.getName().getString(), checkName, String.format("(VL:%s) %s%s", violations, ChatFormatting.GRAY, extraMsg));
    }
}
