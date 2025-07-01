package cc.abbie.amap.client.minimap.config;

import cc.abbie.amap.AMap;
import folk.sisby.kaleido.api.ReflectiveConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import net.fabricmc.loader.api.FabricLoader;

public class MinimapConfig extends ReflectiveConfig {
    public static final MinimapConfig INSTANCE = createToml(FabricLoader.getInstance().getConfigDir(), AMap.MODID, "minimap", MinimapConfig.class);
    
    public static void init() {}
    
    public final TrackedValue<Boolean> enable = value(true); 
    public final TrackedValue<RenderType> renderType = value(RenderType.SURFACE); 
    public final TrackedValue<Boolean> deathPoint = value(false); 
    public final TrackedValue<Boolean> autoUpdateCheck = value(false);
    
    public final Minimap minimap = new Minimap();
    public final SurfaceMap surfaceMap = new SurfaceMap();
    public final EntitiesRadar entitiesRadar = new EntitiesRadar();
    public final Marker marker = new Marker();
    
    public enum RenderType {
        SURFACE,
        CAVE,
        BIOME;
    }

    public static class Minimap extends Section {
        public final TrackedValue<Shape> shape = value(Shape.SQUARE);
        public final TrackedValue<Texture> texture = value(Texture.ORIGINAL);
        public final TrackedValue<Position> position = value(Position.UPPER_RIGHT);
        public final TrackedValue<Scale> scale = value(Scale.AUTO);
        public final TrackedValue<Opacity> opacity = value(Opacity.P100);
        public final TrackedValue<Scale> largeScale = value(Scale.AUTO);
        public final TrackedValue<Opacity> largeOpacity = value(Opacity.P100);
        public final TrackedValue<Boolean> largeLabel = value(false);
        public final TrackedValue<Boolean> filtering = value(true);
        public final TrackedValue<CoordinatesType> showCoordinates = value(CoordinatesType.TYPE2);
        public final TrackedValue<Boolean> showBiome = value(true);
        public final TrackedValue<Boolean> showMenuKey = value(true);
        public final TrackedValue<Scale> fontScale = value(Scale.AUTO);
        public final TrackedValue<Zoom> defaultZoom = value(Zoom.X1_0);
        public final TrackedValue<MaskType> mapMaskType = value(MaskType.DEPTH);
        public final TrackedValue<Amount> updateFrequency = value(Amount.MIDDLE);
        public final TrackedValue<Boolean> threading = value(false);
        public final TrackedValue<Amount> threadPriority = value(Amount.LOW);
        public final TrackedValue<Boolean> preloadedChunks = value(false);
        
        public enum Shape {
            SQUARE,
            ROUND;
        }
        
        public enum Texture {
            ORIGINAL,
            ANTIQUE;
        }
        
        public enum Position {
            UPPER_RIGHT,
            LOWER_RIGHT,
            UPPER_LEFT,
            LOWER_LEFT;
        }
        
        public enum Scale {
            AUTO,
            SMALL,
            NORMAL,
            LARGE,
            LARGER,
            GUI_SCALE;
        }
        
        public enum MaskType {
            DEPTH,
            STENCIL;
        }
        
        public enum Amount {
            VERY_LOW,
            LOW,
            MIDDLE,
            HIGH,
            VERY_HIGH;
        }
        
        public enum CoordinatesType {
            DISABLED,
            TYPE1,
            TYPE2;
        }
        
        public enum Zoom {
            X0_5,
            X1_0,
            X1_5,
            X2_0,
            X4_0,
            X8_0;
        }
        
        public enum Opacity {
            P25,
            P50,
            P75,
            P100;
        }
    }
    
    public static class SurfaceMap extends Section {
        public final TrackedValue<Lighting> lighting = value(Lighting.DYNAMIC);
        public final TrackedValue<LightingType> lightingType = value(LightingType.TYPE1);
        public final TrackedValue<Boolean> terrainUndulate = value(true);
        public final TrackedValue<Boolean> terrainDepth = value(true);
        public final TrackedValue<Boolean> transparency = value(true);
        public final TrackedValue<Boolean> environmentColor = value(true);
        public final TrackedValue<Boolean> omitHeightCalc = value(true);
        public final TrackedValue<Boolean> hideSnow = value(false);
        public final TrackedValue<Boolean> showChunkGrid = value(false);
        public final TrackedValue<Boolean> showSlimeChunk = value(false);
        
        public enum Lighting {
            DISABLED,
            DAY_TIME,
            NIGHT_TIME,
            DYNAMIC;
        }
        
        public enum LightingType {
            TYPE1,
            TYPE2;
        }
    }
    
    public static class EntitiesRadar extends Section {
        public final TrackedValue<Boolean> enable = value(false);
        public final TrackedValue<Boolean> player = value(true);
        public final TrackedValue<Boolean> animal = value(true);
        public final TrackedValue<Boolean> monster = value(true);
        public final TrackedValue<Boolean> slime = value(true);
        public final TrackedValue<Boolean> squid = value(true);
        public final TrackedValue<Boolean> other = value(true);
        public final TrackedValue<Boolean> lightning = value(true);
        public final TrackedValue<Boolean> showDirection = value(true);
    }
    
    public static class Marker extends Section {
        public final TrackedValue<Boolean> enable = value(true);
        public final TrackedValue<Boolean> icon = value(true);
        public final TrackedValue<Boolean> label = value(true);
        public final TrackedValue<Boolean> distance = value(true);
    }
    
}
