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


}
