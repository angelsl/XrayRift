package fr.atesab.x13.mixin;

import fr.atesab.x13.XrayMain;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = IBlockState.class, priority = 500)
public interface MixinIBlockState extends IBlockState {
    default EnumBlockRenderType getRenderType() {
        if (!XrayMain.getMod().shouldBlockBeRendered(this)) {
            return EnumBlockRenderType.INVISIBLE;
        }

        return getBlock().getRenderType(this);
    }
}
