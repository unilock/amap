package cc.abbie.amap.client;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

import folk.sisby.surveyor.terrain.LayerSummary;
import folk.sisby.surveyor.util.RegionPos;
import folk.sisby.surveyor.util.RegistryPalette;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChunkRenderer {

    public static Mode mode = Mode.NORMAL;
    public static Map<ChunkPos, ResourceLocation> textures = new HashMap<>();
    public static Set<ChunkPos> dirtyChunks = new HashSet<>();

    public static void bakeChunk(ChunkPos chunkPos) {
        // needed for shading
        // done first to cancel as early as possible (map gets are expensive?)
        ChunkPos northChunkPos = new ChunkPos(chunkPos.x, chunkPos.z - 1);
        ChunkPos northRegionPos = new ChunkPos(RegionPos.chunkToRegion(northChunkPos.x), RegionPos.chunkToRegion(northChunkPos.z));
        ChunkPos northRegionRelativePos = new ChunkPos(RegionPos.regionRelative(northChunkPos.x), RegionPos.regionRelative(northChunkPos.z));

        LayerSummary.Raw[][] northTerr = MapStorage.INSTANCE.regions.get(northRegionPos);
        if (northTerr == null) return;

        RegistryPalette<Block>.ValueView northBlockPalette = MapStorage.INSTANCE.blockPalettes.get(northChunkPos);
        if (northBlockPalette == null) return;

        RegistryPalette<Biome>.ValueView northBiomePalette = MapStorage.INSTANCE.biomePalettes.get(northChunkPos);
        if (northBiomePalette == null) return;

        if (dirtyChunks.contains(chunkPos)) {
            dirtyChunks.remove(chunkPos);
        } else {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        if (level == null) return;
        RegistryAccess registryAccess = level.registryAccess();
        Registry<Biome> biomeRegistry = registryAccess.registryOrThrow(Registries.BIOME);

        ChunkPos regionPos = new ChunkPos(RegionPos.chunkToRegion(chunkPos.x), RegionPos.chunkToRegion(chunkPos.z));
        ChunkPos regionRelativePos = new ChunkPos(RegionPos.regionRelative(chunkPos.x), RegionPos.regionRelative(chunkPos.z));
        LayerSummary.Raw[][] terr = MapStorage.INSTANCE.regions.get(regionPos);
        RegistryPalette<Block>.ValueView blockPalette = MapStorage.INSTANCE.blockPalettes.get(chunkPos);
        RegistryPalette<Biome>.ValueView biomePalette = MapStorage.INSTANCE.biomePalettes.get(chunkPos);

        if (terr == null || blockPalette == null || biomePalette == null)
            return;

        LayerSummary.Raw summ = terr[regionRelativePos.x][regionRelativePos.z];
        LayerSummary.Raw northSumm = northTerr[northRegionRelativePos.x][northRegionRelativePos.z];
        if (summ == null || northSumm == null) return;

        int[] blocks = summ.blocks();
        int[] biomes = summ.biomes();
        int[] northBlocks = northSumm.blocks();
        int[] northBiomes = northSumm.biomes();
        if (blocks == null || biomes == null || northBlocks == null || northBiomes == null) return;

        ResourceLocation textureLocation = textures.get(regionPos);
        DynamicTexture texture = (DynamicTexture) client.getTextureManager().getTexture(textureLocation);
        NativeImage pixels = texture.getPixels();
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                int idx = 16 * x + y;
                if (!summ.exists().get(idx)) continue;

                Block block = blockPalette.byId(blocks[idx]);
                Biome biome = biomePalette.byId(biomes[idx]);
                if (block == null || biome == null) continue;

                int color = switch (mode) {
                    case NORMAL -> {
                        MapColor.Brightness brightness;
                        int waterDepth = summ.waterDepths()[idx];
                        MapColor mapColor;
                        if (waterDepth > 0) {
//                            color = biome.getWaterColor() | 0xff000000;
                            double f = (double) waterDepth * 0.1 + (double) (x + y & 1) * 0.2;
                            if (f < 0.5) {
                                brightness = MapColor.Brightness.HIGH;
                            } else if (f > 0.9) {
                                brightness = MapColor.Brightness.LOW;
                            } else {
                                brightness = MapColor.Brightness.NORMAL;
                            }
                            mapColor = MapColor.WATER;
                        } else {
                            int depth = summ.depths()[idx];
                            int northDepth;
                            if (y > 0) {
                                int northIdx = 16 * x + y - 1;
                                if (!summ.exists().get(northIdx)) {
                                    northDepth = Integer.MAX_VALUE;
                                } else {
                                    northDepth = summ.depths()[northIdx] - summ.waterDepths()[northIdx];
                                }
                            } else {
                                int northIdx = 16 * x + 15;
                                if (!northSumm.exists().get(northIdx)) {
                                    northDepth = Integer.MAX_VALUE;
                                } else {
                                    northDepth = northSumm.depths()[northIdx] - northSumm.waterDepths()[northIdx];
                                }
                            }
                            if (depth == northDepth) {
                                brightness = MapColor.Brightness.NORMAL;
                            } else if (depth < northDepth) {
                                brightness = MapColor.Brightness.HIGH;
                            } else {
                                brightness = MapColor.Brightness.LOW;
                            }
                            mapColor = block.defaultMapColor();
                        }
                        yield mapColor.calculateRGBColor(brightness);
                    }
                    case BIOME -> FastColor.ABGR32.fromArgb32(getBiomeColorArgb(biomeRegistry.wrapAsHolder(biome)));
                };
                pixels.setPixelRGBA(16 * regionRelativePos.x + x, 16 * regionRelativePos.z + y, color | 0xff000000);
            }
        }
        texture.upload();
    }

    private static void bakeRegion(ChunkPos regionPos) {
        if (!textures.containsKey(regionPos)) {
            DynamicTexture texture = new DynamicTexture(512, 512, false);
            // needed otherwise we may get a texture filled with garbage!
            texture.getPixels().fillRect(0, 0, 512, 512, 0);
            ResourceLocation textureLocation = Minecraft.getInstance().getTextureManager().register("amap/region", texture);
            textures.put(regionPos, textureLocation);
        }

        for (int x = 0; x < 32; x++) {
            for (int z = 0; z < 32; z++) {
                bakeChunk(new ChunkPos(32 * regionPos.x + x, 32 * regionPos.z + z));
            }
        }
    }

    public static void renderRegion(GuiGraphics gui, ChunkPos regionPos) {
        bakeRegion(regionPos);
        if (textures.containsKey(regionPos)) {
            gui.blit(textures.get(regionPos), 0, 0, 0, 0, 512, 512, 512, 512);
        }
    }

    private static int getBiomeColorArgb(Holder<Biome> biomeHolder) {
        Biome biome = biomeHolder.value();
        if (biomeHolder.is(BiomeTags.IS_RIVER) || biomeHolder.is(BiomeTags.IS_OCEAN) || biomeHolder.is(BiomeTags.IS_DEEP_OCEAN)) {
            return biome.getWaterColor();
        } else if (biomeHolder.is(BiomeTags.IS_NETHER)) {
            return biome.getFogColor();
        } else {
            return biome.getGrassColor(0, 0);
        }
    }

    public static void clear() {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        for (ResourceLocation id : textures.values()) {
            textureManager.getTexture(id).close();
        }
        textures.clear();
        dirtyChunks.clear();
    }

    public enum Mode {
        NORMAL,
        BIOME
    }

}
