package dev.imb11.skinshuffle.compat.api;

import dev.imb11.skinshuffle.Platform;
import dev.imb11.skinshuffle.compat.CapesCompat;

import java.util.ArrayList;

public class CompatLoader {
    private static final ArrayList<CompatHandler> HELPERS = new ArrayList<>();

    static {
        //FIXME this is probably broken
//        HELPERS.add(new MinecraftCapesCompat());
        HELPERS.add(new CapesCompat());
    }

    public static void init() {
        for (CompatHandler helper : HELPERS) {
            if (Platform.isModLoaded(helper.getID()))
                helper.execute();
        }
    }
}
