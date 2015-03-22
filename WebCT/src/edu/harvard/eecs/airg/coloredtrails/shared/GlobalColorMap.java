/*
	Colored Trails
	
	Copyright (C) 2006, President and Fellows of Harvard College.  All Rights Reserved.
	
	This program is free software; you can redistribute it and/or
	modify it under the terms of the GNU General Public License
	as published by the Free Software Foundation; either version 2
	of the License, or (at your option) any later version.
	
	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.
	
	You should have received a copy of the GNU General Public License
	along with this program; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package edu.harvard.eecs.airg.coloredtrails.shared;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * A static class to provide a list of color names and a mapping of
 * color names to Java Color objects.  This is used so that all client
 * server communication can take place in terms of color strings, but
 * the client can still create the proper color from a given string.
 *
 * The colors in this mapping are primarily acquired from X11's rgb.txt.
 *
 * @author Paul Heymann (ct3@heymann.be)
 */
public class GlobalColorMap {
    private static Hashtable<String, List> colorMap =
            new Hashtable<String, List>();

    /**
     * Add a color to the list of colors which are defined by the
     * global color map.
     *
     * @param name The name of the color to be added.
     * @param r The red component value.
     * @param g The green component value.
     * @param b The blue component value.
     */
    private static void addColor(String name, int r, int g, int b) {
        colorMap.put(name, Arrays.asList(new Integer(r), new Integer(g),
                new Integer(b)));
    }

    /**
     * Get a Color object corresponding to a color of the particular name.
     * @param name The name of the Color object to get.
     * @return The corresponding Color object to the color name "name".
     */
    public static Color getColorByName(String name) {
        List l = colorMap.get(name);
        if (l == null) {
            throw new RuntimeException("Game is using invalid colors!");
        }

        return new Color((Integer) l.get(0), (Integer) l.get(1),
                (Integer) l.get(2));
    }

    /**
     * Get a random color name.
     * @return A random color name string.
     */
    public static String getRandomColorName() {
        Random r = new Random();
        Set s = colorMap.keySet();
        Object[] objectKeys = s.toArray();
        return (String) objectKeys[r.nextInt(s.size())];
    }
     /**
     * Get a list of all colors in this global color map.
     *
     * @return A list of all colors in global color map.
     */
     public static ArrayList<String> getColorListArray() {


         return new ArrayList<String>(colorMap.keySet());
     }

    static {
        // these color defs are from X11 rgb.txt
        addColor("snow", 255, 250, 250);
        addColor("ghost white", 248, 248, 255);
        addColor("GhostWhite", 248, 248, 255);
        addColor("white smoke", 245, 245, 245);
        addColor("WhiteSmoke", 245, 245, 245);
        addColor("gainsboro", 220, 220, 220);
        addColor("floral white", 255, 250, 240);
        addColor("FloralWhite", 255, 250, 240);
        addColor("old lace", 253, 245, 230);
        addColor("OldLace", 253, 245, 230);
        addColor("linen", 250, 240, 230);
        addColor("antique white", 250, 235, 215);
        addColor("AntiqueWhite", 250, 235, 215);
        addColor("papaya whip", 255, 239, 213);
        addColor("PapayaWhip", 255, 239, 213);
        addColor("blanched almond", 255, 235, 205);
        addColor("BlanchedAlmond", 255, 235, 205);
        addColor("bisque", 255, 228, 196);
        addColor("peach puff", 255, 218, 185);
        addColor("PeachPuff", 255, 218, 185);
        addColor("navajo white", 255, 222, 173);
        addColor("NavajoWhite", 255, 222, 173);
        addColor("moccasin", 255, 228, 181);
        addColor("cornsilk", 255, 248, 220);
        addColor("ivory", 255, 255, 240);
        addColor("lemon chiffon", 255, 250, 205);
        addColor("LemonChiffon", 255, 250, 205);
        addColor("seashell", 255, 245, 238);
        addColor("honeydew", 240, 255, 240);
        addColor("mint cream", 245, 255, 250);
        addColor("MintCream", 245, 255, 250);
        addColor("azure", 240, 255, 255);
        addColor("alice blue", 240, 248, 255);
        addColor("AliceBlue", 240, 248, 255);
        addColor("lavender", 230, 230, 250);
        addColor("lavender blush", 255, 240, 245);
        addColor("LavenderBlush", 255, 240, 245);
        addColor("misty rose", 255, 228, 225);
        addColor("MistyRose", 255, 228, 225);
        addColor("white", 255, 255, 255);
        addColor("dim gray", 105, 105, 105);
        addColor("DimGray", 105, 105, 105);
        addColor("dim grey", 105, 105, 105);
        addColor("DimGrey", 105, 105, 105);
        addColor("slate gray", 112, 128, 144);
        addColor("SlateGray", 112, 128, 144);
        addColor("slate grey", 112, 128, 144);
        addColor("SlateGrey", 112, 128, 144);
        addColor("light slate gray", 119, 136, 153);
        addColor("LightSlateGray", 119, 136, 153);
        addColor("light slate grey", 119, 136, 153);
        addColor("LightSlateGrey", 119, 136, 153);
        addColor("gray", 190, 190, 190);
        addColor("grey", 190, 190, 190);
        addColor("light grey", 211, 211, 211);
        addColor("LightGrey", 211, 211, 211);
        addColor("light gray", 211, 211, 211);
        addColor("LightGray", 211, 211, 211);
        addColor("cornflower blue", 100, 149, 237);
        addColor("CornflowerBlue", 100, 149, 237);
        addColor("slate blue", 106, 90, 205);
        addColor("SlateBlue", 106, 90, 205);
        addColor("medium slate blue", 123, 104, 238);
        addColor("MediumSlateBlue", 123, 104, 238);
        addColor("light slate blue", 132, 112, 255);
        addColor("LightSlateBlue", 132, 112, 255);
        addColor("sky blue", 135, 206, 235);
        addColor("SkyBlue", 135, 206, 235);
        addColor("light sky blue", 135, 206, 250);
        addColor("LightSkyBlue", 135, 206, 250);
        addColor("light steel blue", 176, 196, 222);
        addColor("LightSteelBlue", 176, 196, 222);
        addColor("light blue", 173, 216, 230);
        addColor("LightBlue", 173, 216, 230);
        addColor("powder blue", 176, 224, 230);
        addColor("PowderBlue", 176, 224, 230);
        addColor("pale turquoise", 175, 238, 238);
        addColor("PaleTurquoise", 175, 238, 238);
        addColor("light cyan", 224, 255, 255);
        addColor("LightCyan", 224, 255, 255);
        addColor("medium aquamarine", 102, 205, 170);
        addColor("MediumAquamarine", 102, 205, 170);
        addColor("aquamarine", 127, 255, 212);
        addColor("dark sea green", 143, 188, 143);
        addColor("DarkSeaGreen", 143, 188, 143);
        addColor("pale green", 152, 251, 152);
        addColor("PaleGreen", 152, 251, 152);
        addColor("lawn green", 124, 252, 0);
        addColor("LawnGreen", 124, 252, 0);
        addColor("chartreuse", 127, 255, 0);
        addColor("green yellow", 173, 255, 47);
        addColor("GreenYellow", 173, 255, 47);
        addColor("yellow green", 154, 205, 50);
        addColor("YellowGreen", 154, 205, 50);
        addColor("olive drab", 107, 142, 35);
        addColor("OliveDrab", 107, 142, 35);
        addColor("dark khaki", 189, 183, 107);
        addColor("DarkKhaki", 189, 183, 107);
        addColor("khaki", 240, 230, 140);
        addColor("pale goldenrod", 238, 232, 170);
        addColor("PaleGoldenrod", 238, 232, 170);
        addColor("light goldenrod yellow", 250, 250, 210);
        addColor("LightGoldenrodYellow", 250, 250, 210);
        addColor("light yellow", 255, 255, 224);
        addColor("LightYellow", 255, 255, 224);
        addColor("yellow", 255, 255, 0);
        addColor("gold", 255, 215, 0);
        addColor("light goldenrod", 238, 221, 130);
        addColor("LightGoldenrod", 238, 221, 130);
        addColor("goldenrod", 218, 165, 32);
        addColor("dark goldenrod", 184, 134, 11);
        addColor("DarkGoldenrod", 184, 134, 11);
        addColor("rosy brown", 188, 143, 143);
        addColor("RosyBrown", 188, 143, 143);
        addColor("indian red", 205, 92, 92);
        addColor("IndianRed", 205, 92, 92);
        addColor("saddle brown", 139, 69, 19);
        addColor("SaddleBrown", 139, 69, 19);
        addColor("sienna", 160, 82, 45);
        addColor("peru", 205, 133, 63);
        addColor("burlywood", 222, 184, 135);
        addColor("beige", 245, 245, 220);
        addColor("wheat", 245, 222, 179);
        addColor("sandy brown", 244, 164, 96);
        addColor("SandyBrown", 244, 164, 96);
        addColor("tan", 210, 180, 140);
        addColor("chocolate", 210, 105, 30);
        addColor("firebrick", 178, 34, 34);
        addColor("brown", 165, 42, 42);
        addColor("dark salmon", 233, 150, 122);
        addColor("DarkSalmon", 233, 150, 122);
        addColor("salmon", 250, 128, 114);
        addColor("light salmon", 255, 160, 122);
        addColor("LightSalmon", 255, 160, 122);
        addColor("orange", 255, 165, 0);
        addColor("dark orange", 255, 140, 0);
        addColor("DarkOrange", 255, 140, 0);
        addColor("coral", 255, 127, 80);
        addColor("light coral", 240, 128, 128);
        addColor("LightCoral", 240, 128, 128);
        addColor("tomato", 255, 99, 71);
        addColor("orange red", 255, 69, 0);
        addColor("OrangeRed", 255, 69, 0);
        addColor("red", 255, 0, 0);
        addColor("hot pink", 255, 105, 180);
        addColor("HotPink", 255, 105, 180);
        addColor("deep pink", 255, 20, 147);
        addColor("DeepPink", 255, 20, 147);
        addColor("pink", 255, 192, 203);
        addColor("light pink", 255, 182, 193);
        addColor("LightPink", 255, 182, 193);
        addColor("pale violet red", 219, 112, 147);
        addColor("PaleVioletRed", 219, 112, 147);
        addColor("maroon", 176, 48, 96);
        addColor("medium violet red", 199, 21, 133);
        addColor("MediumVioletRed", 199, 21, 133);
        addColor("violet red", 208, 32, 144);
        addColor("VioletRed", 208, 32, 144);
        addColor("magenta", 255, 0, 255);
        addColor("violet", 238, 130, 238);
        addColor("plum", 221, 160, 221);
        addColor("orchid", 218, 112, 214);
        addColor("medium orchid", 186, 85, 211);
        addColor("MediumOrchid", 186, 85, 211);
        addColor("dark orchid", 153, 50, 204);
        addColor("DarkOrchid", 153, 50, 204);
        addColor("dark violet", 148, 0, 211);
        addColor("DarkViolet", 148, 0, 211);
        addColor("blue violet", 138, 43, 226);
        addColor("BlueViolet", 138, 43, 226);
        addColor("purple", 160, 32, 240);
        addColor("medium purple", 147, 112, 219);
        addColor("MediumPurple", 147, 112, 219);
        addColor("thistle", 216, 191, 216);
        addColor("snow1", 255, 250, 250);
        addColor("snow2", 238, 233, 233);
        addColor("snow3", 205, 201, 201);
        addColor("snow4", 139, 137, 137);
        addColor("seashell1", 255, 245, 238);
        addColor("seashell2", 238, 229, 222);
        addColor("seashell3", 205, 197, 191);
        addColor("seashell4", 139, 134, 130);
        addColor("AntiqueWhite1", 255, 239, 219);
        addColor("AntiqueWhite2", 238, 223, 204);
        addColor("AntiqueWhite3", 205, 192, 176);
        addColor("AntiqueWhite4", 139, 131, 120);
        addColor("bisque1", 255, 228, 196);
        addColor("bisque2", 238, 213, 183);
        addColor("bisque3", 205, 183, 158);
        addColor("bisque4", 139, 125, 107);
        addColor("PeachPuff1", 255, 218, 185);
        addColor("PeachPuff2", 238, 203, 173);
        addColor("PeachPuff3", 205, 175, 149);
        addColor("PeachPuff4", 139, 119, 101);
        addColor("NavajoWhite1", 255, 222, 173);
        addColor("NavajoWhite2", 238, 207, 161);
        addColor("NavajoWhite3", 205, 179, 139);
        addColor("NavajoWhite4", 139, 121, 94);
        addColor("LemonChiffon1", 255, 250, 205);
        addColor("LemonChiffon2", 238, 233, 191);
        addColor("LemonChiffon3", 205, 201, 165);
        addColor("LemonChiffon4", 139, 137, 112);
        addColor("cornsilk1", 255, 248, 220);
        addColor("cornsilk2", 238, 232, 205);
        addColor("cornsilk3", 205, 200, 177);
        addColor("cornsilk4", 139, 136, 120);
        addColor("ivory1", 255, 255, 240);
        addColor("ivory2", 238, 238, 224);
        addColor("ivory3", 205, 205, 193);
        addColor("ivory4", 139, 139, 131);
        addColor("honeydew1", 240, 255, 240);
        addColor("honeydew2", 224, 238, 224);
        addColor("honeydew3", 193, 205, 193);
        addColor("honeydew4", 131, 139, 131);
        addColor("LavenderBlush1", 255, 240, 245);
        addColor("LavenderBlush2", 238, 224, 229);
        addColor("LavenderBlush3", 205, 193, 197);
        addColor("LavenderBlush4", 139, 131, 134);
        addColor("MistyRose1", 255, 228, 225);
        addColor("MistyRose2", 238, 213, 210);
        addColor("MistyRose3", 205, 183, 181);
        addColor("MistyRose4", 139, 125, 123);
        addColor("azure1", 240, 255, 255);
        addColor("azure2", 224, 238, 238);
        addColor("azure3", 193, 205, 205);
        addColor("azure4", 131, 139, 139);
        addColor("SlateBlue1", 131, 111, 255);
        addColor("SlateBlue2", 122, 103, 238);
        addColor("SlateBlue3", 105, 89, 205);
        addColor("SkyBlue1", 135, 206, 255);
        addColor("SkyBlue2", 126, 192, 238);
        addColor("SkyBlue3", 108, 166, 205);
        addColor("LightSkyBlue1", 176, 226, 255);
        addColor("LightSkyBlue2", 164, 211, 238);
        addColor("LightSkyBlue3", 141, 182, 205);
        addColor("SlateGray1", 198, 226, 255);
        addColor("SlateGray2", 185, 211, 238);
        addColor("SlateGray3", 159, 182, 205);
        addColor("SlateGray4", 108, 123, 139);
        addColor("LightSteelBlue1", 202, 225, 255);
        addColor("LightSteelBlue2", 188, 210, 238);
        addColor("LightSteelBlue3", 162, 181, 205);
        addColor("LightSteelBlue4", 110, 123, 139);
        addColor("LightBlue1", 191, 239, 255);
        addColor("LightBlue2", 178, 223, 238);
        addColor("LightBlue3", 154, 192, 205);
        addColor("LightBlue4", 104, 131, 139);
        addColor("LightCyan1", 224, 255, 255);
        addColor("LightCyan2", 209, 238, 238);
        addColor("LightCyan3", 180, 205, 205);
        addColor("LightCyan4", 122, 139, 139);
        addColor("PaleTurquoise1", 187, 255, 255);
        addColor("PaleTurquoise2", 174, 238, 238);
        addColor("PaleTurquoise3", 150, 205, 205);
        addColor("PaleTurquoise4", 102, 139, 139);
        addColor("CadetBlue1", 152, 245, 255);
        addColor("CadetBlue2", 142, 229, 238);
        addColor("CadetBlue3", 122, 197, 205);
        addColor("DarkSlateGray1", 151, 255, 255);
        addColor("DarkSlateGray2", 141, 238, 238);
        addColor("DarkSlateGray3", 121, 205, 205);
        addColor("aquamarine1", 127, 255, 212);
        addColor("aquamarine2", 118, 238, 198);
        addColor("aquamarine3", 102, 205, 170);
        addColor("DarkSeaGreen1", 193, 255, 193);
        addColor("DarkSeaGreen2", 180, 238, 180);
        addColor("DarkSeaGreen3", 155, 205, 155);
        addColor("DarkSeaGreen4", 105, 139, 105);
        addColor("PaleGreen1", 154, 255, 154);
        addColor("PaleGreen2", 144, 238, 144);
        addColor("PaleGreen3", 124, 205, 124);
        addColor("chartreuse1", 127, 255, 0);
        addColor("chartreuse2", 118, 238, 0);
        addColor("chartreuse3", 102, 205, 0);
        addColor("OliveDrab1", 192, 255, 62);
        addColor("OliveDrab2", 179, 238, 58);
        addColor("OliveDrab3", 154, 205, 50);
        addColor("OliveDrab4", 105, 139, 34);
        addColor("DarkOliveGreen1", 202, 255, 112);
        addColor("DarkOliveGreen2", 188, 238, 104);
        addColor("DarkOliveGreen3", 162, 205, 90);
        addColor("DarkOliveGreen4", 110, 139, 61);
        addColor("khaki1", 255, 246, 143);
        addColor("khaki2", 238, 230, 133);
        addColor("khaki3", 205, 198, 115);
        addColor("khaki4", 139, 134, 78);
        addColor("LightGoldenrod1", 255, 236, 139);
        addColor("LightGoldenrod2", 238, 220, 130);
        addColor("LightGoldenrod3", 205, 190, 112);
        addColor("LightGoldenrod4", 139, 129, 76);
        addColor("LightYellow1", 255, 255, 224);
        addColor("LightYellow2", 238, 238, 209);
        addColor("LightYellow3", 205, 205, 180);
        addColor("LightYellow4", 139, 139, 122);
        addColor("yellow1", 255, 255, 0);
        addColor("yellow2", 238, 238, 0);
        addColor("yellow3", 205, 205, 0);
        addColor("yellow4", 139, 139, 0);
        addColor("gold1", 255, 215, 0);
        addColor("gold2", 238, 201, 0);
        addColor("gold3", 205, 173, 0);
        addColor("gold4", 139, 117, 0);
        addColor("goldenrod1", 255, 193, 37);
        addColor("goldenrod2", 238, 180, 34);
        addColor("goldenrod3", 205, 155, 29);
        addColor("goldenrod4", 139, 105, 20);
        addColor("DarkGoldenrod1", 255, 185, 15);
        addColor("DarkGoldenrod2", 238, 173, 14);
        addColor("DarkGoldenrod3", 205, 149, 12);
        addColor("DarkGoldenrod4", 139, 101, 8);
        addColor("RosyBrown1", 255, 193, 193);
        addColor("RosyBrown2", 238, 180, 180);
        addColor("RosyBrown3", 205, 155, 155);
        addColor("RosyBrown4", 139, 105, 105);
        addColor("IndianRed1", 255, 106, 106);
        addColor("IndianRed2", 238, 99, 99);
        addColor("IndianRed3", 205, 85, 85);
        addColor("IndianRed4", 139, 58, 58);
        addColor("sienna1", 255, 130, 71);
        addColor("sienna2", 238, 121, 66);
        addColor("sienna3", 205, 104, 57);
        addColor("sienna4", 139, 71, 38);
        addColor("burlywood1", 255, 211, 155);
        addColor("burlywood2", 238, 197, 145);
        addColor("burlywood3", 205, 170, 125);
        addColor("burlywood4", 139, 115, 85);
        addColor("wheat1", 255, 231, 186);
        addColor("wheat2", 238, 216, 174);
        addColor("wheat3", 205, 186, 150);
        addColor("wheat4", 139, 126, 102);
        addColor("tan1", 255, 165, 79);
        addColor("tan2", 238, 154, 73);
        addColor("tan3", 205, 133, 63);
        addColor("tan4", 139, 90, 43);
        addColor("chocolate1", 255, 127, 36);
        addColor("chocolate2", 238, 118, 33);
        addColor("chocolate3", 205, 102, 29);
        addColor("chocolate4", 139, 69, 19);
        addColor("firebrick1", 255, 48, 48);
        addColor("firebrick2", 238, 44, 44);
        addColor("firebrick3", 205, 38, 38);
        addColor("firebrick4", 139, 26, 26);
        addColor("brown1", 255, 64, 64);
        addColor("brown2", 238, 59, 59);
        addColor("brown3", 205, 51, 51);
        addColor("brown4", 139, 35, 35);
        addColor("salmon1", 255, 140, 105);
        addColor("salmon2", 238, 130, 98);
        addColor("salmon3", 205, 112, 84);
        addColor("salmon4", 139, 76, 57);
        addColor("LightSalmon1", 255, 160, 122);
        addColor("LightSalmon2", 238, 149, 114);
        addColor("LightSalmon3", 205, 129, 98);
        addColor("LightSalmon4", 139, 87, 66);
        addColor("orange1", 255, 165, 0);
        addColor("orange2", 238, 154, 0);
        addColor("orange3", 205, 133, 0);
        addColor("orange4", 139, 90, 0);
        addColor("DarkOrange1", 255, 127, 0);
        addColor("DarkOrange2", 238, 118, 0);
        addColor("DarkOrange3", 205, 102, 0);
        addColor("DarkOrange4", 139, 69, 0);
        addColor("coral1", 255, 114, 86);
        addColor("coral2", 238, 106, 80);
        addColor("coral3", 205, 91, 69);
        addColor("coral4", 139, 62, 47);
        addColor("tomato1", 255, 99, 71);
        addColor("tomato2", 238, 92, 66);
        addColor("tomato3", 205, 79, 57);
        addColor("tomato4", 139, 54, 38);
        addColor("OrangeRed1", 255, 69, 0);
        addColor("OrangeRed2", 238, 64, 0);
        addColor("OrangeRed3", 205, 55, 0);
        addColor("OrangeRed4", 139, 37, 0);
        addColor("red1", 255, 0, 0);
        addColor("red2", 238, 0, 0);
        addColor("red3", 205, 0, 0);
        addColor("red4", 139, 0, 0);
        addColor("DeepPink1", 255, 20, 147);
        addColor("DeepPink2", 238, 18, 137);
        addColor("DeepPink3", 205, 16, 118);
        addColor("DeepPink4", 139, 10, 80);
        addColor("HotPink1", 255, 110, 180);
        addColor("HotPink2", 238, 106, 167);
        addColor("HotPink3", 205, 96, 144);
        addColor("HotPink4", 139, 58, 98);
        addColor("pink1", 255, 181, 197);
        addColor("pink2", 238, 169, 184);
        addColor("pink3", 205, 145, 158);
        addColor("pink4", 139, 99, 108);
        addColor("LightPink1", 255, 174, 185);
        addColor("LightPink2", 238, 162, 173);
        addColor("LightPink3", 205, 140, 149);
        addColor("LightPink4", 139, 95, 101);
        addColor("PaleVioletRed1", 255, 130, 171);
        addColor("PaleVioletRed2", 238, 121, 159);
        addColor("PaleVioletRed3", 205, 104, 137);
        addColor("PaleVioletRed4", 139, 71, 93);
        addColor("maroon1", 255, 52, 179);
        addColor("maroon2", 238, 48, 167);
        addColor("maroon3", 205, 41, 144);
        addColor("maroon4", 139, 28, 98);
        addColor("VioletRed1", 255, 62, 150);
        addColor("VioletRed2", 238, 58, 140);
        addColor("VioletRed3", 205, 50, 120);
        addColor("VioletRed4", 139, 34, 82);
        addColor("magenta1", 255, 0, 255);
        addColor("magenta2", 238, 0, 238);
        addColor("magenta3", 205, 0, 205);
        addColor("magenta4", 139, 0, 139);
        addColor("orchid1", 255, 131, 250);
        addColor("orchid2", 238, 122, 233);
        addColor("orchid3", 205, 105, 201);
        addColor("orchid4", 139, 71, 137);
        addColor("plum1", 255, 187, 255);
        addColor("plum2", 238, 174, 238);
        addColor("plum3", 205, 150, 205);
        addColor("plum4", 139, 102, 139);
        addColor("MediumOrchid1", 224, 102, 255);
        addColor("MediumOrchid2", 209, 95, 238);
        addColor("MediumOrchid3", 180, 82, 205);
        addColor("MediumOrchid4", 122, 55, 139);
        addColor("DarkOrchid1", 191, 62, 255);
        addColor("DarkOrchid2", 178, 58, 238);
        addColor("DarkOrchid3", 154, 50, 205);
        addColor("DarkOrchid4", 104, 34, 139);
        addColor("purple1", 155, 48, 255);
        addColor("purple2", 145, 44, 238);
        addColor("purple3", 125, 38, 205);
        addColor("MediumPurple1", 171, 130, 255);
        addColor("MediumPurple2", 159, 121, 238);
        addColor("MediumPurple3", 137, 104, 205);
        addColor("thistle1", 255, 225, 255);
        addColor("thistle2", 238, 210, 238);
        addColor("thistle3", 205, 181, 205);
        addColor("thistle4", 139, 123, 139);
        addColor("gray40", 102, 102, 102);
        addColor("grey40", 102, 102, 102);
        addColor("gray41", 105, 105, 105);
        addColor("grey41", 105, 105, 105);
        addColor("gray42", 107, 107, 107);
        addColor("grey42", 107, 107, 107);
        addColor("gray43", 110, 110, 110);
        addColor("grey43", 110, 110, 110);
        addColor("gray44", 112, 112, 112);
        addColor("grey44", 112, 112, 112);
        addColor("gray45", 115, 115, 115);
        addColor("grey45", 115, 115, 115);
        addColor("gray46", 117, 117, 117);
        addColor("grey46", 117, 117, 117);
        addColor("gray47", 120, 120, 120);
        addColor("grey47", 120, 120, 120);
        addColor("gray48", 122, 122, 122);
        addColor("grey48", 122, 122, 122);
        addColor("gray49", 125, 125, 125);
        addColor("grey49", 125, 125, 125);
        addColor("gray50", 127, 127, 127);
        addColor("grey50", 127, 127, 127);
        addColor("gray51", 130, 130, 130);
        addColor("grey51", 130, 130, 130);
        addColor("gray52", 133, 133, 133);
        addColor("grey52", 133, 133, 133);
        addColor("gray53", 135, 135, 135);
        addColor("grey53", 135, 135, 135);
        addColor("gray54", 138, 138, 138);
        addColor("grey54", 138, 138, 138);
        addColor("gray55", 140, 140, 140);
        addColor("grey55", 140, 140, 140);
        addColor("gray56", 143, 143, 143);
        addColor("grey56", 143, 143, 143);
        addColor("gray57", 145, 145, 145);
        addColor("grey57", 145, 145, 145);
        addColor("gray58", 148, 148, 148);
        addColor("grey58", 148, 148, 148);
        addColor("gray59", 150, 150, 150);
        addColor("grey59", 150, 150, 150);
        addColor("gray60", 153, 153, 153);
        addColor("grey60", 153, 153, 153);
        addColor("gray61", 156, 156, 156);
        addColor("grey61", 156, 156, 156);
        addColor("gray62", 158, 158, 158);
        addColor("grey62", 158, 158, 158);
        addColor("gray63", 161, 161, 161);
        addColor("grey63", 161, 161, 161);
        addColor("gray64", 163, 163, 163);
        addColor("grey64", 163, 163, 163);
        addColor("gray65", 166, 166, 166);
        addColor("grey65", 166, 166, 166);
        addColor("gray66", 168, 168, 168);
        addColor("grey66", 168, 168, 168);
        addColor("gray67", 171, 171, 171);
        addColor("grey67", 171, 171, 171);
        addColor("gray68", 173, 173, 173);
        addColor("grey68", 173, 173, 173);
        addColor("gray69", 176, 176, 176);
        addColor("grey69", 176, 176, 176);
        addColor("gray70", 179, 179, 179);
        addColor("grey70", 179, 179, 179);
        addColor("gray71", 181, 181, 181);
        addColor("grey71", 181, 181, 181);
        addColor("gray72", 184, 184, 184);
        addColor("grey72", 184, 184, 184);
        addColor("gray73", 186, 186, 186);
        addColor("grey73", 186, 186, 186);
        addColor("gray74", 189, 189, 189);
        addColor("grey74", 189, 189, 189);
        addColor("gray75", 191, 191, 191);
        addColor("grey75", 191, 191, 191);
        addColor("gray76", 194, 194, 194);
        addColor("grey76", 194, 194, 194);
        addColor("gray77", 196, 196, 196);
        addColor("grey77", 196, 196, 196);
        addColor("gray78", 199, 199, 199);
        addColor("grey78", 199, 199, 199);
        addColor("gray79", 201, 201, 201);
        addColor("grey79", 201, 201, 201);
        addColor("gray80", 204, 204, 204);
        addColor("grey80", 204, 204, 204);
        addColor("gray81", 207, 207, 207);
        addColor("grey81", 207, 207, 207);
        addColor("gray82", 209, 209, 209);
        addColor("grey82", 209, 209, 209);
        addColor("gray83", 212, 212, 212);
        addColor("grey83", 212, 212, 212);
        addColor("gray84", 214, 214, 214);
        addColor("grey84", 214, 214, 214);
        addColor("gray85", 217, 217, 217);
        addColor("grey85", 217, 217, 217);
        addColor("gray86", 219, 219, 219);
        addColor("grey86", 219, 219, 219);
        addColor("gray87", 222, 222, 222);
        addColor("grey87", 222, 222, 222);
        addColor("gray88", 224, 224, 224);
        addColor("grey88", 224, 224, 224);
        addColor("gray89", 227, 227, 227);
        addColor("grey89", 227, 227, 227);
        addColor("gray90", 229, 229, 229);
        addColor("grey90", 229, 229, 229);
        addColor("gray91", 232, 232, 232);
        addColor("grey91", 232, 232, 232);
        addColor("gray92", 235, 235, 235);
        addColor("grey92", 235, 235, 235);
        addColor("gray93", 237, 237, 237);
        addColor("grey93", 237, 237, 237);
        addColor("gray94", 240, 240, 240);
        addColor("grey94", 240, 240, 240);
        addColor("gray95", 242, 242, 242);
        addColor("grey95", 242, 242, 242);
        addColor("gray96", 245, 245, 245);
        addColor("grey96", 245, 245, 245);
        addColor("gray97", 247, 247, 247);
        addColor("grey97", 247, 247, 247);
        addColor("gray98", 250, 250, 250);
        addColor("grey98", 250, 250, 250);
        addColor("gray99", 252, 252, 252);
        addColor("grey99", 252, 252, 252);
        addColor("gray100", 255, 255, 255);
        addColor("grey100", 255, 255, 255);
        addColor("dark grey", 169, 169, 169);
        addColor("DarkGrey", 169, 169, 169);
        addColor("dark gray", 169, 169, 169);
        addColor("DarkGray", 169, 169, 169);
        addColor("dark blue", 0, 0, 139);
        addColor("DarkBlue", 0, 0, 139);
        addColor("dark cyan", 0, 139, 139);
        addColor("DarkCyan", 0, 139, 139);
        addColor("dark magenta", 139, 0, 139);
        addColor("DarkMagenta", 139, 0, 139);
        addColor("dark red", 139, 0, 0);
        addColor("DarkRed", 139, 0, 0);
        addColor("light green", 144, 238, 144);
        addColor("LightGreen", 144, 238, 144);

        addColor("AgentOrange", 255, 127, 127);
        addColor("ObjectOlive", 191, 255, 127);
        addColor("TealSkyish", 127, 255, 255);
        addColor("YurplePurple", 191, 127, 255);
		
        addColor("RGBRed", 255, 0, 0);
        addColor("RGBGreen", 0, 255, 0);
        addColor("RGBBlue", 0, 0, 255);
                
        addColor("CTRed", 255, 127, 127);
        addColor("CTGreen", 191, 255, 127);
        addColor("CTBlue", 127, 255, 255);
        addColor("CTPurple", 191, 127, 255);
        addColor("CTYellow", 255, 246, 45);
        addColor("CTOrange", 255, 150, 34);
        addColor("CTDkGreen", 127, 191, 127);
    }
}
