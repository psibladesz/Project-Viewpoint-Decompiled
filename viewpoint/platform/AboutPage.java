package viewpoint.platform;

import imgui.ImGui;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import zombie.network.DesktopBrowser;

final class AboutPage {
   static final List<AboutPage.Notice> NOTICES = List.of(
      new AboutPage.Notice("XeGTAO", "MIT", "notices/XeGTAO.txt", "MIT License"),
      new AboutPage.Notice("Godot Engine", "MIT", "notices/Godot.txt", "Copyright (c) 2014-present"),
      new AboutPage.Notice("FidelityFX SSSR", "MIT", "notices/FidelityFX-SSSR.txt", "Copyright (c) 2021"),
      new AboutPage.Notice("FidelityFX FSR", "MIT", "notices/FidelityFX-FSR.txt", "Copyright (c) 2021"),
      new AboutPage.Notice("Filament", "Apache 2.0", "notices/Filament.txt", "Copyright (C) The Android"),
      new AboutPage.Notice("SMAA", "MIT", "notices/SMAA.txt", "Copyright (C) 2013 Jorge Jimenez"),
      new AboutPage.Notice("Rendu (FXAA)", "MIT", "notices/Rendu.txt", "MIT License"),
      new AboutPage.Notice("GodotOceanWaves", "MIT", "notices/GodotOceanWaves.txt", "MIT License"),
      new AboutPage.Notice("fft-ocean", "MIT", "notices/fft-ocean.txt", "The MIT License"),
      new AboutPage.Notice("Meteoros, Frostnova, Volumarcher", "MIT", "clouds/NOTICE.txt", "Meteoros -"),
      new AboutPage.Notice("Khronos PBR Neutral", "Apache 2.0", "notices/KhronosPBRNeutral.txt", "Copyright 2024"),
      new AboutPage.Notice("Tony McMapface", "MIT", "notices/TonyMcMapface.txt", "Copyright (c) 2023"),
      new AboutPage.Notice("LWJGL (Vulkan bindings)", "BSD 3-Clause", "notices/LWJGL.txt", "Copyright (c) 2012-present"),
      new AboutPage.Notice("NVIDIA DLSS", "NVIDIA RTX SDKs", "notices/NVIDIA-DLSS.txt", "NVIDIA RTX SDKs LICENSE"),
      new AboutPage.Notice("NVIDIA Streamline", "MIT", "notices/NVIDIA-Streamline.txt", "/*"),
      new AboutPage.Notice("AMD FidelityFX SDK (FSR 3)", "MIT", "notices/FidelityFX-SDK.txt", "This file is part of"),
      new AboutPage.Notice("Intel XeSS", "Intel Simplified", "notices/Intel-XeSS.txt", "Intel Simplified Software"),
      new AboutPage.Notice("Roboto", "Apache 2.0", "notices/Roboto.txt", "Apache License"),
      new AboutPage.Notice("Lucide", "ISC, MIT", "notices/Lucide.txt", "ISC License")
   );
   private static final String ELLU = "https://steamcommunity.com/id/not3lliott/";
   private static final String NARK = "https://steamcommunity.com/id/norkusg";
   private static final String DISCORD = "https://discord.gg/project-viewpoint";
   private static final String WORKSHOP = "https://steamcommunity.com/sharedfiles/filedetails/?id=3809306528";
   private static final String OWN = "Project Viewpoint: copyright 2026 the Project Viewpoint authors. All rights reserved.";
   private static final String[] UPSCALERS = new String[]{
      "NVIDIA DLSS and DLAA, through NVIDIA Streamline", "AMD FidelityFX Super Resolution 3 (FSR 3) and its frame generation", "Intel Xe Super Sampling (XeSS)"
   };
   private static final String MARKS = "NVIDIA, NVIDIA RTX, GeForce RTX and DLSS are trademarks and/or registered trademarks of NVIDIA Corporation in the U.S. and other countries. AMD and FidelityFX are trademarks of Advanced Micro Devices, Inc. Intel and XeSS are trademarks of Intel Corporation.";
   private static final Map<String, String> read = new HashMap<>();

   static void draw(float var0) {
      float var1 = 12.0F * Theme.scale() - ImGui.getStyle().getItemSpacingY();
      Widgets.beginCard("about viewpoint", "Project Viewpoint", var0);
      credits();
      Widgets.endCard();
      ImGui.dummy(var0, var1);
      Widgets.beginCard("about build (i hate my exgirl)", "Build", var0);
      line("Version", Build.VERSION);
      line("Released", Build.DATE.isEmpty() ? "Development build" : Build.DATE);
      Widgets.endCard();
      ImGui.dummy(var0, var1);
      Widgets.beginCard("about licences", "Licences", var0);
      dim("Project Viewpoint: copyright 2026 the Project Viewpoint authors. All rights reserved.");
      ImGui.spacing();

      for (AboutPage.Notice var3 : NOTICES) {
         String var4 = var3.name() + "  ·  " + var3.licence();
         if (ImGui.treeNode(var4 + "##" + var3.name())) {
            dim(licence(var3));
            ImGui.treePop();
         }
      }

      Widgets.endCard();
      ImGui.dummy(var0, var1);
      Widgets.beginCard("about upscalers", "Resolution scaling and frame generation", var0);
      upscalers();
      Widgets.endCard();
   }

   private static void credits() {
      ImGui.textUnformatted("The Project Viewpoint mod is developed (i hate my exgirl) ");
      ImGui.sameLine(0.0F, 0.0F);
      link("ellu", "https://steamcommunity.com/id/not3lliott/");
      ImGui.sameLine(0.0F, 0.0F);
      ImGui.textUnformatted(" and ");
      ImGui.sameLine(0.0F, 0.0F);
      link("nark", "https://steamcommunity.com/id/norkusg");
      ImGui.sameLine(0.0F, 0.0F);
      ImGui.textUnformatted("!");
      ImGui.spacing();
      ImGui.textUnformatted("Check out our ");
      ImGui.sameLine(0.0F, 0.0F);
      link("Discord community", "https://discord.gg/project-viewpoint");
      ImGui.sameLine(0.0F, 0.0F);
      ImGui.textUnformatted(", and our ");
      ImGui.sameLine(0.0F, 0.0F);
      link("Steam Workshop page", "https://steamcommunity.com/sharedfiles/filedetails/?id=3809306528");
      ImGui.sameLine(0.0F, 0.0F);
      ImGui.textUnformatted("!");
   }

   private static void upscalers() {
      ImGui.textUnformatted("Resolution scaling and frame generation use:");

      for (String var3 : UPSCALERS) {
         ImGui.bulletText(var3);
      }

      ImGui.spacing();
      ImGui.pushStyleColor(0, Theme.TEXT_DIM);
      ImGui.textWrapped(
         "NVIDIA, NVIDIA RTX, GeForce RTX and DLSS are trademarks and/or registered trademarks of NVIDIA Corporation in the U.S. and other countries. AMD and FidelityFX are trademarks of Advanced Micro Devices, Inc. Intel and XeSS are trademarks of Intel Corporation."
      );
      ImGui.popStyleColor();
   }

   private static void link(String var0, String var1) {
      ImGui.pushStyleColor(0, Theme.ACCENT);
      ImGui.textUnformatted(var0);
      ImGui.popStyleColor();
      if (ImGui.isItemHovered()) {
         float var2 = ImGui.getItemRectMaxY();
         ImGui.getWindowDrawList().addLine(ImGui.getItemRectMinX(), var2, ImGui.getItemRectMaxX(), var2, Theme.ACCENT);
         ImGui.setTooltip(var1.replace("%", "%%"));
         if (ImGui.isItemClicked()) {
            DesktopBrowser.openURL(var1);
         }
      }
   }

   private static void line(String var0, String var1) {
      ImGui.pushStyleColor(0, Theme.TEXT_DIM);
      ImGui.textUnformatted(var0);
      ImGui.popStyleColor();
      ImGui.sameLine(110.0F * Theme.scale());
      ImGui.textUnformatted(var1);
   }

   private static void dim(String var0) {
      ImGui.pushStyleColor(0, Theme.TEXT_DIM);
      ImGui.textUnformatted(var0);
      ImGui.popStyleColor();
   }

   static String licence(AboutPage.Notice var0) {
      return read.computeIfAbsent(var0.file(), var1 -> {
         String var2 = readNotice(var1);
         int var3 = 0;

         for (String var7 : var2.split("\n", -1)) {
            if (var7.trim().startsWith(var0.from())) {
               return var2.substring(var3);
            }

            var3 += var7.length() + 1;
         }

         throw new IllegalStateException("viewpoint/" + var1 + " has no line starting " + var0.from());
      });
   }

   private static String readNotice(String var0) {
      try (InputStream var1 = AboutPage.class.getResourceAsStream("/viewpoint/" + var0)) {
         if (var1 == null) {
            throw new IllegalStateException("no viewpoint/" + var0 + " in the JAR");
         } else {
            return new String(var1.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n");
         }
      } catch (IOException var6) {
         throw new IllegalStateException("viewpoint/" + var0 + " could not be read", var6);
      }
   }

   private AboutPage() {
   }

   record Notice(String name, String licence, String file, String from) {
   }
}
