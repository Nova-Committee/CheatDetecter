package top.infsky.cheatdetector.config;

import lombok.Getter;
import lombok.Setter;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Getter
@Setter
@Config(name = "Alert")
public class AlertConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    boolean allowAlert = true;

    @ConfigEntry.Gui.Tooltip
    boolean allowAlertFixes = false;

    @ConfigEntry.Gui.Tooltip
    boolean allowAlertVLClear = false;

    @ConfigEntry.Gui.Tooltip
    boolean disableBuffer = false;

    @ConfigEntry.Gui.Tooltip
    String prefix = "§b§lTR§r§l>";
}