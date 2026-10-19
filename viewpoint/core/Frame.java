package viewpoint.core;

import java.util.ArrayList;
import viewpoint.render.SceneData;
import zombie.characters.IsoGameCharacter;
import zombie.core.skinnedmodel.model.ModelInstance;
import zombie.core.skinnedmodel.model.ModelSlotRenderData;
import zombie.core.textures.TextureDraw.GenericDrawer;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.objects.IsoWorldInventoryObject;
import zombie.vehicles.BaseVehicle;

public final class Frame {
   public long number;
   public long takenNanos;
   public final SceneData scene = new SceneData();
   public float camX;
   public float camY;
   public float camZ;
   public float viewYaw;
   public float viewPitch;
   public boolean onCamera;
   public float eyeX;
   public float eyeY;
   public float eyeZ;
   public float eyeLean;
   public final float[] seatTilt = new float[]{0.0F, 0.0F, 0.0F, 1.0F};
   public final CameraSquares cameraSquares = new CameraSquares();
   public final CameraShot cameraShot = new CameraShot();
   public final ArrayList<ModelSlotRenderData> snapshots = new ArrayList<>();
   public final ArrayList<GenericDrawer> drawers = new ArrayList<>();
   public final ArrayList<ModelInstance> corpseInstances = new ArrayList<>();
   public final ArrayList<IsoObject> modelObjects = new ArrayList<>();
   public final ArrayList<IsoWorldInventoryObject> modelItems = new ArrayList<>();
   public IsoGridSquare lootSquare;
   public BaseVehicle lootVehicle;
   public IsoGameCharacter lootCharacter;
}
