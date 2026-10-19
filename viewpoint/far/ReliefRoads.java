package viewpoint.far;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

final class ReliefRoads {
   static final String STREETS = "streets.xml";
   final float[][] streets;

   ReliefRoads(float[][] var1) {
      this.streets = var1;
   }

   static ReliefRoads read(File var0) throws IOException {
      File var1 = new File(var0, "streets.xml");
      if (!var1.isFile()) {
         return new ReliefRoads(new float[0][]);
      }

      final ArrayList<float[]> var2 = new ArrayList<>();

      try {
         SAXParserFactory var3 = SAXParserFactory.newInstance();
         var3.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
         var3.newSAXParser().parse(var1, new DefaultHandler() {
            private float[] street;
            private int count;

            @Override
            public void startElement(String var1, String var2x, String var3x, Attributes var4) {
               if (var3x.equals("street")) {
                  this.street = new float[17];
                  this.street[0] = Float.parseFloat(var4.getValue("width"));
                  this.count = 1;
               } else if (var3x.equals("point") && this.street != null) {
                  if (this.count + 2 > this.street.length) {
                     this.street = Arrays.copyOf(this.street, this.street.length * 2);
                  }

                  this.street[this.count++] = Float.parseFloat(var4.getValue("x"));
                  this.street[this.count++] = Float.parseFloat(var4.getValue("y"));
               }
            }

            @Override
            public void endElement(String var1, String var2x, String var3x) {
               if (var3x.equals("street") && this.street != null) {
                  var2.add(Arrays.copyOf(this.street, this.count));
                  this.street = null;
               }
            }
         });
      } catch (ParserConfigurationException | SAXException var4) {
         throw new IOException("the streets in " + var1 + " could not be read", var4);
      }

      return new ReliefRoads(var2.toArray(new float[0][]));
   }
}