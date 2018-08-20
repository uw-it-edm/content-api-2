package edu.uw.edm.contentapi2.service.util;

import com.google.common.base.Preconditions;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.springframework.util.StringUtils;

import java.awt.*;
import java.io.IOException;

public class PdfUtils {

    private static PDFont FONT = PDType1Font.HELVETICA_BOLD;
    private static int DEFAULT_FONT_SIZE = 10;
    private static float STARTING_X = 50;
    private static float STARTING_Y = 400;

    public static PDDocument createUnableToConvertToPdf(String itemId, String downloadUrl, String originalFileName) throws IOException {
        Preconditions.checkArgument(!StringUtils.isEmpty(itemId), "ItemId is required.");
        Preconditions.checkArgument(!StringUtils.isEmpty(downloadUrl), "Download URL is required.");
        final PDDocument document = new PDDocument();
        final PDPage page = new PDPage(new PDRectangle(11f * 72, 8.5f * 72));
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

            if (StringUtils.isEmpty(originalFileName)) {
                addText(contentStream, originalFileName + " could not be converted to .pdf:", STARTING_X, STARTING_Y + 80, DEFAULT_FONT_SIZE + 2);
            } else {
                addText(contentStream, "The file you requested could not be converted to .pdf:", STARTING_X, STARTING_Y + 80, DEFAULT_FONT_SIZE + 2);
            }
            addText(contentStream, "Item ID: " + itemId, STARTING_X, STARTING_Y + 60, DEFAULT_FONT_SIZE + 2);
            addText(contentStream, "Use this link to download the original file:", STARTING_X, STARTING_Y + 20, DEFAULT_FONT_SIZE + 2);
            addText(contentStream, downloadUrl, STARTING_X, STARTING_Y, DEFAULT_FONT_SIZE, Color.blue);
            addUrlBox(page, downloadUrl, STARTING_X, STARTING_Y, DEFAULT_FONT_SIZE);
        }
        return document;
    }

    private static void addText(PDPageContentStream contentStream, String text, float x, float y, int fontSize) throws IOException {
        addText(contentStream, text, x, y, fontSize, null);
    }

    private static void addText(PDPageContentStream contentStream, String text, float x, float y, int fontSize, Color color) throws IOException {
        contentStream.beginText();
        contentStream.setFont(FONT, fontSize);
        if (color != null) {
            contentStream.setNonStrokingColor(color);
        }
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    private static void addUrlBox(PDPage page, String downloadUrl, float x, float y, int fontSize) throws IOException {
        final PDAnnotationLink txtLink = new PDAnnotationLink();
        final PDActionURI action = new PDActionURI();
        action.setURI(downloadUrl);
        txtLink.setAction(action);

        final float textWidth = FONT.getStringWidth(downloadUrl) / 1000 * fontSize;

        final PDRectangle position = new PDRectangle();
        position.setLowerLeftX(x);
        position.setLowerLeftY(y - 5);
        position.setUpperRightX(x + textWidth);
        position.setUpperRightY(y + 20);
        txtLink.setRectangle(position);

        page.getAnnotations().add(txtLink);
    }

}
