package top.infsky.cheatdetector.anticheat.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.HitResult;

public record UseItemOn(Item item, BlockPos blockPos, Direction direction, HitResult.Type type) {
}
