package fr.atesab.x13;

import java.util.List;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.IBlockReader;

public class XrayMode implements SideRenderer {
	@FunctionalInterface
	public interface SideViewer {
		boolean shouldRenderSide(boolean blockInList, IBlockState state, IBlockReader reader, BlockPos pos,
				EnumFacing face);
	}

	@FunctionalInterface
	public interface BlockViewer {
		boolean shouldRenderBlock(boolean blockInList, IBlockState state, IBlockReader reader, BlockPos pos);
	}

	public enum ViewMode {
		/**
		 * Default mode, like in Xray and Redstone mode
		 */
		EXCLUSIVE((il, v1, v2, v3, v4) -> il, (il, v1, v2, v3) -> il),
		/**
		 * Inclusive mode, like in Cave Mode
		 */
		INCLUSIVE((il, v1, reader, pos, face) -> !il && reader.getBlockState(pos.offset(face)).isAir(),
				(il, v1, reader, pos) -> {
					if (il) {
						return false;
					}

					// return true if any adjacent block is air
					BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos(pos);
					return reader.getBlockState(mPos.move(1, 0, 0)).isAir() ||
							reader.getBlockState(mPos.move(-2, 0, 0)).isAir() ||
							reader.getBlockState(mPos.move(1, 1, 0)).isAir() ||
							reader.getBlockState(mPos.move(0, -2, 0)).isAir() ||
							reader.getBlockState(mPos.move(0, 1, 1)).isAir() ||
							reader.getBlockState(mPos.move(0, 0, -2)).isAir();
				});

		private final BlockViewer blockViewer;
		private final SideViewer sideViewer;

		ViewMode(SideViewer sideViewer, BlockViewer blockViewer) {
			this.sideViewer = sideViewer;
			this.blockViewer = blockViewer;
		}

		public SideViewer getSideViewer() {
			return sideViewer;
		}

		public BlockViewer getBlockViewer() {
			return blockViewer;
		}
	}

	private static final List<XrayMode> MODES = Lists.newArrayList();
	private List<Block> blocks;
	private final String defaultBlocks;
	private boolean enabled;
	private KeyBinding key;
	private final String name;
	private final int color;
	private ViewMode viewMode;
	private static int colorCursor = -1;
	private static final int[] COLORS = { 0xff00ffff, 0xff00ff00, 0xffff0000, 0xffffff00, 0xffff00ff,
			0xff7aff00, 0xffff7a00, 0xff00ff7a, 0xffff007a, 0xff7a00ff, 0xff7a7aff, 0xff7aff7a, 0xffff7a7a };
	public static final String CUSTOM_PREFIX = "Custom_";

	static int nextColor() {
		return COLORS[colorCursor = (colorCursor + 1) % COLORS.length];
	}

	public XrayMode(String name, int keyCode, ViewMode viewMode) {
		this.name = name;
		this.color = nextColor();
		this.enabled = false;
		this.blocks = Lists.newArrayList();
		this.key = new KeyBinding(name, keyCode, "key.categories.xray");
		this.viewMode = viewMode;
		this.defaultBlocks = "";
		MODES.add(this);
	}

	public XrayMode(String name, int keyCode, ViewMode viewMode, Block... defaultBlocks) {
		this.name = name;
		this.color = nextColor();
		this.enabled = false;
		this.blocks = Lists.newArrayList(defaultBlocks);
		this.key = new KeyBinding("x13.mod." + name, keyCode, "key.categories.xray");
		this.viewMode = viewMode;
		this.defaultBlocks = XrayMain.getBlockNamesToString(blocks);
		MODES.add(this);
	}

	public List<Block> getBlocks() {
		return blocks;
	}

	public String getDefaultBlocks() {
		return defaultBlocks;
	}

	public int getColor() {
		return color;
	}

	public KeyBinding getKey() {
		return key;
	}

	public String getName() {
		return name;
	}

	public String getNameTranslate() {
		return name.startsWith(CUSTOM_PREFIX) ? name : I18n.format("x13.mod." + name);
	}

	public ViewMode getViewMode() {
		return viewMode;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setBlocks(List<Block> blocks) {
		this.blocks = blocks;
	}

	public void setViewMode(ViewMode viewMode) {
		this.viewMode = viewMode;
	}

	public boolean toggleKey() {
		if (key.isPressed()) {
			toggle();
			return true;
		}
		return false;
	}

	public void setConfig(String[] data) {
		blocks.clear();
		for (String d : data) {
			Block b = IRegistry.BLOCK.get(new ResourceLocation(d));
			if (b != null && !b.equals(Blocks.AIR))
				blocks.add(b);
		}
	}

	@Override
	public void shouldSideBeRendered(IBlockState state, IBlockReader reader, BlockPos pos, EnumFacing face,
			CallbackInfoReturnable<Boolean> ci) {
		if (isEnabled())
			ci.setReturnValue(
					viewMode.getSideViewer().shouldRenderSide(blocks.contains(state.getBlock()), state, reader, pos, face));
	}

	public boolean shouldBlockBeRendered(IBlockState state) {
		return !isEnabled() || ((viewMode == ViewMode.EXCLUSIVE) == blocks.contains(state.getBlock()));
	}

	public boolean shouldBlockBeRendered(IBlockState state, IBlockReader reader, BlockPos pos) {
		return !isEnabled() || viewMode.getBlockViewer().shouldRenderBlock(blocks.contains(state.getBlock()), state, reader, pos);
	}

	public void toggle() {
		toggle(!enabled);
	}

	public void toggle(boolean enable) {
		toggle(enable, true);
	}

	public void toggle(boolean enable, boolean reloadRenderers) {
		MODES.forEach(m -> m.toggle0(false, false));
		toggle0(enable, reloadRenderers);
		XrayMain.getMod().internalFullbright();
		if (reloadRenderers)
			Minecraft.getInstance().worldRenderer.loadRenderers();
	}

	private void toggle0(boolean enable, boolean reloadRenderers) {
		enabled = enable;
	}
}
