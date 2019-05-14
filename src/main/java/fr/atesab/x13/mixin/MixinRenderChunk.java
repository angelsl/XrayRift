package fr.atesab.x13.mixin;

import fr.atesab.x13.XrayMain;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderChunk.class)
public class MixinRenderChunk {
    @Redirect(method = "rebuildChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/state/IBlockState;isOpaqueCube(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean onIsOpaqueCube(IBlockState _this, IBlockReader worldIn, BlockPos pos) {
        if (!XrayMain.getMod().shouldBlockBeRendered(_this)) {
            return false;
        }

        return _this.isOpaqueCube(worldIn, pos);
    }
}
