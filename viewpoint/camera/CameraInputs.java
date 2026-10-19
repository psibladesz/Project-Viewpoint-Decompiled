package viewpoint.camera;

final class CameraInputs {
   static final int NONE = 0;
   static final int MELEE = 1;
   static final int FIREARM = 2;
   static final int LONG_GUN = 3;
   static final int CLIMB = 1;
   static final int FALL = 2;
   static final int GROUND = 3;
   static final int GRABBED = 4;
   static final int SIT = 5;
   static final int DEAD = 6;
   float speed;
   boolean moving;
   boolean running;
   boolean sprinting;
   boolean sneaking;
   boolean attacking;
   int aim;
   int special;
   String state = "";
   float fallTime;
   boolean seated;
   float vehicleSpeed;
   float vehicleYaw;
   int threat;
   float tight;
   float panic;
   float tired;
   float headBob;
   float yawRate;
   float facing;
   float health;
}
