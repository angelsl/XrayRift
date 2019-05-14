package fr.atesab.x13.mixin;

import fr.atesab.x13.XrayMain;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(value = IBlockState.class, priority = 500)
public interface MixinIBlockState extends IBlockState {
    //@Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
    default EnumBlockRenderType getRenderType() {
        if (!XrayMain.getMod().shouldBlockBeRendered(this)) {
            return EnumBlockRenderType.INVISIBLE;
        }

        return getBlock().getRenderType(this);
    }
}
