package viewpoint.render;

import java.util.ArrayList;

public final class SpotDraws {
   public static final int CAR_MAP = 11;
   public boolean flashOn;
   public float flashStrength;
   public float flashRange;
   public float flashCos;
   public boolean flashAtItem;
   public float flashItemX;
   public float flashItemY;
   public float flashItemZ;
   public final float[] lamps = new float[768];
   public int lampCount;
   public final float[] carMaps = new float[55];
   public int carMapCount;
   public final ArrayList<LampReadbacks.Ask> lampAsks = new ArrayList<>();
}
