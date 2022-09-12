/* ******************************************************************** */
/*                                                                      */
/*  PdfToolbox                                                          */
/*                                                                      */
/*  Collection of tool around the PDF library                           */
/* ******************************************************************** */
package org.camunda.cherry.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.blend.BlendMode;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;

import java.awt.*;
import java.io.IOException;

public class PdfToolbox {

    /**
     * Add a text in background on a page
     *
     * @param doc  pdfDocument
     * @param page page to add the text
     * @param text test to add
     * @throws IOException can't add the text
     */
	 
	private static final WORKERLOGO = "data:image/svg+xml,%3C?xml version='1.0' encoding='UTF-8' standalone='no'?%3E%3Csvg   xmlns:dc='http://purl.org/dc/elements/1.1/'   xmlns:cc='http://creativecommons.org/ns%23'   xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns%23'   xmlns:svg='http://www.w3.org/2000/svg'   xmlns='http://www.w3.org/2000/svg'   version='1.1'   id='Layer_1'   x='0px'   y='0px'   viewBox='0 0 512 512'   style='enable-background:new 0 0 512 512;'   xml:space='preserve'%3E%3Cmetadata   id='metadata57'%3E%3Crdf:RDF%3E%3Ccc:Work       rdf:about=''%3E%3Cdc:format%3Eimage/svg+xml%3C/dc:format%3E%3Cdc:type         rdf:resource='http://purl.org/dc/dcmitype/StillImage' /%3E%3Cdc:title%3E%3C/dc:title%3E%3C/cc:Work%3E%3C/rdf:RDF%3E%3C/metadata%3E%3Cdefs   id='defs55' /%3E%3Cpath   style='fill:%23AFB9D2;'   d='M475.931,101.828l-29.928,29.928l-41.806-11.201L392.995,78.75l29.954-29.954  c13.522-13.522,0.517-36.068-18.031-31.413c-14.16,3.554-27.575,10.838-38.686,21.871c-21.718,21.565-29.012,53.035-21.784,81.278  c0.836,3.267,0.049,6.716-2.337,9.101L66.594,405.15c13.467,0.488,26.784,5.714,37.045,15.959  c10.254,10.261,15.47,23.59,15.957,37.054l275.348-275.349c2.406-2.406,5.894-3.184,9.183-2.315  c28.056,7.421,59.152,0.238,81.154-21.764c11.136-11.136,18.487-24.606,22.07-38.834C512.031,101.33,489.474,88.286,475.931,101.828  z'   id='path2' /%3E%3Cg   id='g8'%3E %3Cpath   style='fill:%23959CB5;'   d='M67.962,512c-14.149,0-28.292-5.377-39.063-16.15c-21.528-21.536-21.528-56.583,0-78.119   c21.552-21.544,56.6-21.528,78.119,0c21.536,21.536,21.536,56.583,0,78.119C96.253,506.614,82.103,512,67.962,512z M67.962,424.008   c-8.401,0-16.801,3.202-23.198,9.591c-12.784,12.784-12.784,33.603,0,46.386c12.793,12.774,33.593,12.793,46.386,0   c12.784-12.793,12.784-33.593,0-46.386C84.754,427.21,76.362,424.008,67.962,424.008z'   id='path4' /%3E %3Cpath   style='fill:%23959CB5;'   d='M373.116,151.632c-4.575-4.575-11.987-4.575-16.564,0l-72.765,72.769   c-3.659,3.659-9.593,3.659-13.252,0l-11.596-11.596l-59.634,59.634l11.597,11.597c3.659,3.659,3.659,9.592,0,13.252l-86.893,86.895   c-4.575,4.575-4.575,11.987,0,16.564c2.288,2.288,5.28,3.432,8.281,3.432c3.001,0,5.994-1.144,8.281-3.432l86.891-86.895   c3.659-3.659,9.593-3.659,13.252,0l11.595,11.595l59.634-59.634l-11.596-11.596c-3.659-3.659-3.659-9.592,0-13.252l72.767-72.769   C377.691,163.619,377.691,156.207,373.116,151.632z'   id='path6' /%3E%3C/g%3E%3Cpath   style='fill:%23FFDC64;'   d='M478.548,458.919L110.923,91.293l-33.131,33.129l304.541,304.541l24.231,50.734l25.603,25.603  c3.659,3.659,9.592,3.659,13.251,0l33.129-33.129C482.208,468.51,482.207,462.578,478.548,458.919z'   id='path10' /%3E%3Cpath   style='fill:%23FFC850;'   d='M446.223,491.244l-25.603-25.603l-24.231-50.734L125.123,143.64c-3.659-3.659-3.659-9.593,0-13.252  l12.448-12.448l-26.647-26.647l-33.131,33.129l304.541,304.541l24.231,50.734l25.603,25.603c3.659,3.659,9.592,3.659,13.251,0  l14.056-14.056C455.815,494.904,449.882,494.904,446.223,491.244z'   id='path12' /%3E%3Crect   x='81.751'   y='34.519'   transform='matrix(-0.7071 0.7071 -0.7071 -0.7071 229.0837 30.3718)'   style='fill:%23959CB5;'   width='53.001'   height='56.222'   id='rect14' /%3E%3Cpath   style='fill:%23AFB9D2;'   d='M75.382,55.752c-3.659-3.659-9.593-3.659-13.252,0L22.375,95.508  c-29.276,29.276-21.257,84.758,8.019,114.033l8.227-19.352c2.074-4.88,1.322-10.244-0.958-15.031  c-4.471-9.385-2.343-21.127,6.384-28.265c7.282-5.957,17.272-6.317,25.442-2.244c3.78,1.883,8.278,1.442,11.239-1.568l40.656-41.325  L75.382,55.752z'   id='path16' /%3E%3Cpath   style='fill:%23959CB5;'   d='M58.169,124.844c-22.082,0-39.982,17.9-39.982,39.982c0,13.626,6.844,25.626,17.252,32.845  l3.18-7.481c2.074-4.881,1.322-10.244-0.958-15.031c-4.471-9.383-2.343-21.127,6.384-28.265c7.282-5.957,17.272-6.317,25.443-2.244  c3.78,1.883,8.278,1.442,11.239-1.568l6.035-6.134C79.501,129.499,69.391,124.844,58.169,124.844z'   id='path18' /%3E%3Cpath   style='fill:%23AFB9D2;'   d='M154.893,69.004l13.252-13.252c3.659-3.659,3.659-9.593,0-13.252L128.389,2.744  c-3.659-3.659-9.593-3.659-13.252,0l-13.251,13.252c-3.659,3.659-3.659,9.593,0,13.252l39.755,39.755  C145.3,72.663,151.234,72.663,154.893,69.004z'   id='path20' /%3E%3Cg   id='g22'%3E%3C/g%3E%3Cg   id='g24'%3E%3C/g%3E%3Cg   id='g26'%3E%3C/g%3E%3Cg   id='g28'%3E%3C/g%3E%3Cg   id='g30'%3E%3C/g%3E%3Cg   id='g32'%3E%3C/g%3E%3Cg   id='g34'%3E%3C/g%3E%3Cg   id='g36'%3E%3C/g%3E%3Cg   id='g38'%3E%3C/g%3E%3Cg   id='g40'%3E%3C/g%3E%3Cg   id='g42'%3E%3C/g%3E%3Cg   id='g44'%3E%3C/g%3E%3Cg   id='g46'%3E%3C/g%3E%3Cg   id='g48'%3E%3C/g%3E%3Cg   id='g50'%3E%3C/g%3E%3Cg   id='g108'   transform='matrix(8.4591514,0,0,8.4591514,165.06614,33.21287)'%3E%3Cpath     d='m 16.7328,13.29858 c -0.1476,1.8864 -1.6524,2.7918 -1.9584,2.9574 -1.629,0.8892 -4.3704,0.5796 -5.6952001,-1.2852 -0.7308,-1.026 -1.098,-2.6586 -0.387,-3.789 l 0.009,-0.018 c 0.2556,-0.4158 0.8478,-1.1178 1.9404001,-1.1412 h 0.0396 c 0.3906,0 0.8406,0.1188 1.2384,0.2232 0.279,0.0738 0.5202,0.1368 0.7002,0.1494 0.1134,0.0072 0.2754,-0.0216 0.4806,-0.0576 0.6138,-0.108 1.638,-0.288 2.5344,0.3456 1.2204,0.865801 1.1052,2.5452 1.098,2.6154 z'     id='path2-4'     style='fill:%23aa0000;fill-opacity:1' /%3E%3Cpath     d='m 9.2718,10.287181 c -0.351,0.2484 -0.5832,0.5562 -0.7236,0.783 l -0.009,0.0144 c -0.3636001,0.5796 -0.4644,1.2528 -0.3960001,1.9116 -0.0072,0.0036 -0.0144,0.0072 -0.0198,0.0126 -1.3338001,1.089 -2.8404,0.8082 -3.4254003,0.6372 l -0.0342,-0.009 c -1.8630002,-0.4644 -3.70259996,-2.4768 -3.3642,-4.6206 0.1872,-1.1808001 1.08,-2.538 2.4264,-2.8476 0.2322,-0.054 1.4454,-0.279 2.421,0.5382 0.2988,0.252 0.5202,0.6192001 0.7146,0.9432 0.1386,0.234 0.2592,0.4338 0.3906001,0.5652 0.0846,0.0846 0.2322,0.1674 0.4194,0.2736 0.549,0.3078001 1.3770001,0.7740001 1.5930002,1.7784 0.0018,0.0072 0.0036,0.0144 0.0072,0.0198 z'     id='path4-7'     style='fill:%23aa0000;fill-opacity:1' /%3E%3Cpath     d='m 12.9294,10.18638 c -0.1062,0.018 -0.1998,0.0306 -0.2664,0.0306 -0.0108,0 -0.0216,-0.0018 -0.0306,-0.0018 -0.0504,-0.0036 -0.1098,-0.0108 -0.1728,-0.0234 0.5526,-1.4112 0.7812,-3.6684 -1.1394,-6.5268 -0.0036,-0.0072 -0.009,-0.0126 -0.0162,-0.018 0.1116,-0.4428 0.1494,-0.7524 0.1584,-0.828 0.0054,-0.0504 -0.0306,-0.0936 -0.0792,-0.099 -0.0504,-0.0072 -0.0936,0.0306 -0.099,0.0792 -0.0216,0.1926 -0.2376,1.9296001 -1.4490001,3.4722004 -0.7578001,0.9648001 -1.9512001,1.6830001 -2.3634002,1.8882 -0.0378,-0.0252 -0.0702,-0.0504 -0.0918,-0.072 -0.0594,-0.0594 -0.117,-0.1368 -0.1764,-0.2286 0.0342,-0.018 0.0702,-0.0378 0.108,-0.0576 0.4302,-0.2304 1.1502,-0.6138001 2.0286002,-1.5336001 1.0386001,-1.0872 1.2366001,-2.7522001 0.9450001,-3.2058 -0.1404,-0.2196 -0.3654001,-0.2412 -0.5454001,-0.2574 -0.2358,-0.0198 -0.378,-0.0342 -0.4032,-0.3546 -0.0234,-0.279 0.108,-0.5256 0.369,-0.6912 0.3510001,-0.225 0.9792001,-0.297 1.5876001,0.0486 0.3798,0.2142 0.3762,0.5796 0.3744,0.9324 -0.0018,0.2358 -0.0036,0.459 0.1134,0.6192 l 0.0504,0.0684 c 0.288,0.3906001 0.9612,1.3014001 1.3644,2.8908 C 13.626,8.01918 13.2192,9.56898 12.9294,10.18638 Z'     id='path6-8'     style='fill:%23502d16;fill-opacity:1' /%3E%3Cpath     d='m 7.6338,2.8045802 c -0.0144,0.0072 -0.0288,0.0108 -0.0432,0.0108 -0.0324,0 -0.063,-0.018 -0.0792,-0.0468 -0.0018,-0.0018 -0.1278,-0.2268 -0.3546,-0.4338 -0.0378,-0.0324 -0.0396,-0.09 -0.0072,-0.126 0.0342,-0.0378 0.09,-0.0396 0.1278,-0.0072 0.2502,0.2268 0.3852,0.4698 0.3906,0.4806 0.025201,0.0432 0.009,0.0972 -0.0342,0.1224 z'     id='path8' /%3E%3Cpath     d='m 7.8156,4.51998 c -0.1872,0.2214 -0.2844,0.261 -0.432,0.3204 l -0.0468,0.0198 c -0.0108,0.0054 -0.0234,0.0072 -0.0342,0.0072 -0.036,0 -0.0684,-0.0216 -0.0828,-0.0558 -0.0198,-0.045 0.0018,-0.099 0.0486,-0.117 l 0.0468,-0.0198 c 0.135,-0.0558 0.2034,-0.0846 0.3636,-0.27 0.0324,-0.0396 0.09,-0.0432 0.1278,-0.0108 0.0378,0.0324 0.0414,0.0882 0.009,0.126 z'     id='path10-6' /%3E%3Cpath     d='m 10.134,3.1591802 c -0.0468,-0.0738 -0.108,-0.1134 -0.1764,-0.135 -0.2394,0.3222 -1.0818001,0.81 -1.9278,0.81 -0.225,0 -0.4518,-0.0342 -0.666,-0.1152 -0.576,-0.2196 -0.9702,-0.5256 -1.3518001,-0.8208 -0.3024,-0.2358 -0.5886,-0.4572 -0.9378,-0.6084001 -0.0468,-0.0216 -0.0666,-0.0738 -0.0468,-0.1188 0.0198,-0.0468 0.072,-0.0666 0.1188,-0.0468 0.369,0.162 0.6642,0.3888001 0.9756,0.6318 0.3708,0.2880001 0.756,0.5850001 1.3050001,0.7938 0.5598,0.2124 1.1880001,0.0738 1.6650001,-0.1404 -0.2052,-0.6336 -1.5318002,-2.56139997 -3.7188,-2.0700002 -0.3384,0.0756 -0.6336,0.1458 -0.8928,0.207 -1.098,0.2574 -1.5678,0.3672001 -2.0556002,0.2376 0.045,0.2268001 0.1638,0.4194 0.3528,0.5742001 0.378,0.3042 0.927,0.3798 1.1880001,0.3834 C 3.8322,2.59038 3.69,2.49318 3.5154,2.43198 3.4686,2.41578 3.4434,2.36538 3.4596,2.31858 c 0.0162,-0.0486 0.0684,-0.072 0.1152,-0.0558 0.567,0.1998 0.828,0.7074 1.2492001,1.7424002 0.441,1.0836 1.9836001,2.0934 3.2346,1.7190001 1.2744,-0.3816 1.2528001,-1.7244 1.251,-1.7388 0,-0.036 0.0216,-0.0702 0.054,-0.0846 0.0846,-0.0378 0.5184,-0.2358 0.8243999,-0.5238 0.0054,-0.0054 0.0108,-0.009 0.0162,-0.009 v -0.0018 C 10.1898,3.27798 10.1646,3.20778 10.134,3.1591802 M 7.1496005,2.20878 c 0.0342,-0.0378 0.09,-0.0396 0.1278,-0.0072 0.2502,0.2268 0.3852,0.4698 0.3906,0.4806 0.0252,0.0432 0.009,0.0972 -0.0342,0.1224 -0.0144,0.0072 -0.0288,0.0108 -0.0432,0.0108 -0.0324,0 -0.063,-0.018 -0.0792,-0.0468 -0.0018,-0.0018 -0.1278,-0.2268 -0.3546,-0.4338 -0.037801,-0.0324 -0.0396,-0.09 -0.0072,-0.126 m -1.5156002,1.2258 c -0.0036,0 -0.0558,0.009 -0.1584,0.009 -0.081,0 -0.1908,-0.0054 -0.3312,-0.0234 -0.0504,-0.0054 -0.0846,-0.0504 -0.0774,-0.1008 0.0054,-0.0486 0.0504,-0.0828 0.1008,-0.0774 0.2916,0.0378 0.4338,0.0162 0.4356,0.0162 0.0486,-0.009 0.0954,0.0234 0.1026,0.0738 0.009,0.0486 -0.0234,0.0936 -0.072,0.1026 M 7.8156,4.51998 c -0.1872,0.2214 -0.2844,0.261 -0.432,0.3204 l -0.0468,0.0198 c -0.0108,0.0054 -0.0234,0.0072 -0.0342,0.0072 -0.036,0 -0.0684,-0.0216 -0.0828,-0.0558 -0.0198,-0.045 0.0018,-0.099 0.0486,-0.117 l 0.0468,-0.0198 c 0.135,-0.0558 0.2034,-0.0846 0.3636,-0.27 0.0324,-0.0396 0.09,-0.0432 0.1278,-0.0108 0.0378,0.0324 0.0414,0.0882 0.009,0.126 z'     id='path12-0'     style='fill:%23008000;fill-opacity:1' /%3E%3Cpath     d='m 5.7060003,3.33198 c 0.009,0.0486 -0.0234,0.0936 -0.072,0.1026 -0.0036,0 -0.0558,0.009 -0.1584,0.009 -0.081,0 -0.1908,-0.0054 -0.3312,-0.0234 -0.0504,-0.0054 -0.0846,-0.0504 -0.0774,-0.1008 0.0054,-0.0486 0.0504,-0.0828 0.1008,-0.0774 0.2916,0.0378 0.4338,0.0162 0.4356,0.0162 0.0486,-0.009 0.0954,0.0234 0.1026,0.0738 z'     id='path14' /%3E%3C/g%3E%3C/svg%3E";
	
    protected static void addWatermarkText(PDDocument doc, PDPage page, WriterOption writerOption, String text) throws IOException {
        PDPageContentStream contentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true);

        /*
         * (0,0) is as the bottom left (classical
         */
        float fontHeight = 30;
        float width = page.getMediaBox().getWidth();
        float height = page.getMediaBox().getHeight();
        float stringWidth = writerOption.font.getStringWidth(text) / 1000 * fontHeight;

        float x = (width / 2) - (stringWidth / 2);

        float y = height - 25;
        switch (writerOption.textPosition) {
            case TOP:
                y = height - 25;
                break;
            case CENTER:
                y = height / 2;
                break;
            case BOTTOM:
                y = 5;
                break;
        }

        contentStream.setFont(writerOption.font, fontHeight);

        PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
        gs.setNonStrokingAlphaConstant(2f);
        gs.setStrokingAlphaConstant(0.2f);
        gs.setBlendMode(BlendMode.MULTIPLY);
        gs.setLineWidth(3f);
        contentStream.setGraphicsStateParameters(gs);

        contentStream.setNonStrokingColor(writerOption.color);
        contentStream.setStrokingColor(writerOption.color);

        contentStream.beginText();

        if (writerOption.degree != 0 && writerOption.textPosition == TEXT_POSITION.CENTER) {
            /*
             * Center of the page is (width/2, height / 2)
             * If we start the at this position, then the text will not be center.
             * So we have to move back stringWidth pixel. Transform that in x,y according the degree
             * x =>
             *    - start at the middle of the page (width/2)
             *    - minus vector, projection on the X axis for this vector
             *     Vector is
             *      calculated the half length of the string (stringWidth/2)
             *     Projection on the X axis
             *      use the cosinus( rotation )
             *
             */
            int xText = (int) (((double) width) / 2.0 - Math.cos(Math.toRadians(writerOption.degree)) * ((double) stringWidth) / 2.0);
            int yText = (int) (((double) height) / 2.0 - Math.sin(Math.toRadians(writerOption.degree)) * ((double) stringWidth) / 2.0);

            Matrix matrix = Matrix.getRotateInstance(Math.toRadians(writerOption.degree), xText, yText);


            contentStream.setTextMatrix(matrix);

        } else {
            contentStream.newLineAtOffset(x, y);
        }
        contentStream.showText(text);
        contentStream.endText();
        contentStream.close();
    }

    public enum TEXT_POSITION {TOP, CENTER, BOTTOM}

    public static class WriterOption {
        PDFont font = PDType1Font.HELVETICA_BOLD;
        Color color = Color.gray;
        TEXT_POSITION textPosition = TEXT_POSITION.TOP;
        int degree = 0;

        public static WriterOption getInstance() {
            return new WriterOption();
        }

        public WriterOption setFont(PDFont font) {
            this.font = font;
            return this;
        }

        public WriterOption setColor(Color color) {
            this.color = color;
            return this;
        }

        public WriterOption setTextPosition(TEXT_POSITION textPosition) {
            this.textPosition = textPosition;
            return this;
        }

        public WriterOption setRotation(int degree) {
            this.degree = degree;
            return this;
        }
    }
	
	public String getLogo() {
		return WORKERLOGO;
	}

}
