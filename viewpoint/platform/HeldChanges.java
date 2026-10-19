package viewpoint.platform;

import imgui.ImGui;
import imgui.ImGuiIO;
import java.util.Map;

final class HeldChanges {
   private static final String POPUP = "Apply changes?";
   private static final int POPUP_FLAGS = 324;
   private static final String UNAPPLIED_TEXT = "The graphics settings have changes that are not applied.";
   private final GraphicsEdits edits = new GraphicsEdits();
   private Runnable leaving;
   private volatile boolean unapplied;

   LoadModel.Values over(LoadModel.Values var1) {
      return this.edits.over(var1);
   }

   void put(Map<String, String> var1) {
      this.edits.put(var1, Onboarding.values());
   }

   boolean any() {
      return this.edits.any();
   }

   boolean unapplied() {
      return this.unapplied;
   }

   void track() {
      this.unapplied = this.edits.any();
   }

   void leave(Runnable var1) {
      if (this.edits.any()) {
         this.leaving = var1;
      } else {
         var1.run();
      }
   }

   void confirm() {
      if (this.leaving != null) {
         if (!ImGui.isPopupOpen("Apply changes?")) {
            ImGui.openPopup("Apply changes?");
         }

         ImGuiIO var1 = ImGui.getIO();
         ImGui.setNextWindowPos(var1.getDisplaySizeX() / 2.0F, var1.getDisplaySizeY() / 2.0F, 8, 0.5F, 0.5F);
         if (ImGui.beginPopupModal("Apply changes?", 324)) {
            ImGui.textUnformatted("The graphics settings have changes that are not applied.");
            ImGui.spacing();
            boolean var2 = Widgets.accentButton("Apply");
            ImGui.sameLine();
            boolean var3 = ImGui.button("Discard");
            if (var2 || var3) {
               this.finish(var2);
               Runnable var4 = this.leaving;
               this.leaving = null;
               ImGui.closeCurrentPopup();
               var4.run();
            }

            ImGui.endPopup();
         }
      }
   }

   void buttons(int var1) {
      boolean var2 = this.edits.any();
      ImGui.beginDisabled(!var2);
      if (Widgets.accentButton("Apply")) {
         this.finish(true);
      }

      ImGui.sameLine();
      if (ImGui.button("Discard")) {
         this.finish(false);
      }

      ImGui.endDisabled();
      ImGui.sameLine();
      ImGui.alignTextToFramePadding();
      ImGui.pushStyleColor(0, Theme.TEXT_DIM);
      ImGui.textWrapped(!var2 ? "No changes held" : (var1 == 1 ? "1 change held" : (var1 > 1 ? var1 + " changes held" : "Changes held")));
      ImGui.popStyleColor();
   }

   private void finish(boolean var1) {
      if (var1) {
         this.edits.apply(Onboarding.values());
      } else {
         this.edits.discard();
      }

      this.unapplied = false;
   }
}
