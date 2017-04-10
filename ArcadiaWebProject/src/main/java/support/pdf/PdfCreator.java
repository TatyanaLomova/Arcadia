package support.pdf;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.TextPosition;
import support.pdf.tableCreator.easytable.*;

import javax.swing.text.Position;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import static support.pdf.tableCreator.easytable.Cell.HorizontalAlignment.*;

public class PdfCreator {
    private Integer pageCounter;
    private final  String  FILE_data;
    private String date;
    private List<List<String>> data2;
    private List<List<String>> headers;

    public PdfCreator(String fileName, List<List<String>> data2, List<List<String>> headers){
        // output file name
        FILE_data = fileName + ".pdf";
        // current date in next format: 2014-09-10T17:11:35.847-07:00
        this.date = OffsetDateTime.now().toString();
        // pdf title and text after table
        this.headers = headers;
        // text, which will be printed in pdf
        this.data2 = data2;
        this.pageCounter = 0;
    }

    // TODO: save file on server
    public void createPDF() throws IOException {

        TextCursor textCursor;
        PDDocument document = null;
        PDPageContentStream contentStream = null;
        PDPage page;

        // creating empty pdf document
        document = new PDDocument();
        page = addNewPage(document);
        textCursor = new TextCursor(page, 30, 40);
        insertImage(document, page, "D:\\Tanya\\Arcadia\\git\\Arcadia\\ArcadiaWebProject\\src\\main\\java\\support\\pdf\\images\\logo.png", textCursor);
        textCursor.setAlignment(TextCursor.Align.CENTER);
        writeText( document, page, "HEder1", 14, textCursor);
        writeText( document, page,  date, 12, textCursor);
        writePageNumber( document, page, pageCounter.toString(), 12, textCursor);

        // Saving the document
        //  document.save("src\\main\\java\\support\\pdf\\reports\\" + FILE_data) ;
        document.save("D:\\Tanya\\Arcadia\\git\\" + FILE_data) ;

        //Closing the document
        document.close() ;

/*
        try {
            // creating empty pdf document
            document = new PDDocument();
            page = addNewPage(document);
            textCursor = new TextCursor(page, 65, 65);
            for (List<String> reportHeader : headers) {
                // Writing name of Report
                if (!reportHeader.get(0).isEmpty())
                writeText( document, page, reportHeader.get(0) + "\n" + date, 16, textCursor);

                // Insert logo
                insertImage(contentStream, document, "D:/Arcadia/logo", textCursor) ;

                createTable(document, page, data2, textCursor);

                // Writing additional information
                if (!reportHeader.get(1).isEmpty())
                writeText(document, page, reportHeader.get(1)+"\n", 12, textCursor) ;
            }
        }
        finally {

            if (document != null) {
                // Saving the document
                //  document.save("src\\main\\java\\support\\pdf\\reports\\" + FILE_data) ;
                document.save("D:/" + FILE_data) ;

                //Closing the document
                document.close() ;
            }
        }
        */
    }

    public PDPage addNewPage(PDDocument document) throws IOException {
        PDPage page = new PDPage();
        document.addPage(page);
        pageCounter++;
        return page;
    }

    private void createTable(PDDocument document, PDPage page, List<List<String>> data, TextCursor textCursor) throws IOException {

        PDFont font = PDType1Font.TIMES_ROMAN;
        int fontSize = 12;
        int i = 0;
        List<String> firstRow = data2.get(0);
        Row.RowBuilder rowBuilder;
        PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);

        try {
            // Define the table structure
            Table.TableBuilder tableBuilder = new Table.TableBuilder();

            for (String header : firstRow) {
                tableBuilder.addColumnOfWidth(header.length());
            }

            tableBuilder.setFontSize(fontSize).setFont(font);

            // adding rows to the table
            for (List<String> row : data2) {
                rowBuilder = new Row.RowBuilder();
                for (String col : row) {
                    rowBuilder.add(Cell.withText(col)
                            .setHorizontalAlignment(CENTER)
                            .withHorizontalBorders());
                }
                tableBuilder.addRow(rowBuilder.setBackgroundColor(i++ % 2 == 0 ? new Color(190, 245, 229) : Color.WHITE).build());
            }

            // Draw! --------------------------------------------
            (new TableDrawer(contentStream, tableBuilder.build(), textCursor.getCurentX(), textCursor.getCurentY())).draw();
        } finally {
            //Closing the content stream
            if (contentStream != null)
                contentStream.close() ;
        }
    }

    private void writePageNumber(PDDocument document, PDPage page, String text, int fontSize, TextCursor textCursor) throws IOException {

        textCursor.setCurentY(textCursor.getStartY() - 750); // 42
        writeText(document, page, text, 12, textCursor);
    }

    // TODO: text position
    private void writeText(PDDocument document, PDPage page, String text, int fontSize, TextCursor textCursor) throws IOException {

        float leading = 1.5f * fontSize;
        PDFont font = PDType1Font.TIMES_ROMAN;
        PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);

        switch (textCursor.getAlignment()){
            case RIGHT:
                textCursor.setCursorToRight(text);
                break;
            case CENTER:
                textCursor.setCursorToCenter(text);
                break;
            case LEFT:
                textCursor.setCursorToLeft();
                break;
        }

        // Split original text to short strings
        List<String> lines = splitToStrings(text, fontSize, font, textCursor.getWidth());

        try {
            // starting content stream
            contentStream.beginText();
            // setting font parameters: type and size
            contentStream.setFont(font, fontSize);
            //Setting the position for the line
            contentStream.newLineAtOffset(textCursor.getCurentX(),textCursor.getCurentY());

            //Writing text in the form of string
            for (String line : lines) {
                if ((textCursor.getCurentY()) < 42) {
                    contentStream.endText();
                    addNewPage(document);
                    // starting content stream
                    contentStream.beginText();
                    // setting font parameters: type and size
                    contentStream.setFont(font, fontSize);
                    //Setting the position for the line
                    contentStream.newLineAtOffset(textCursor.getCurentX(),textCursor.getCurentY());
                }
                contentStream.showText(line);
                contentStream.newLineAtOffset(0, -leading);
                textCursor.setCurentY(textCursor.getCurentY() - leading);
            }
            // end content stream
            contentStream.endText();
        } finally {
            //Closing the content stream
            if (contentStream != null)
                contentStream.close() ;
        }
    }

    private List<String> splitToStrings(String textNL, int fontSize, PDFont font, float width) throws IOException {

        List<String> lines = new ArrayList<String>();
        for (String text : textNL.split("\n")) {
            int lastSpace = -1;
            while (text.length() > 0) {
                int spaceIndex = text.indexOf(' ', lastSpace + 1);
                if (spaceIndex < 0)
                    spaceIndex = text.length();
                String subString = text.substring(0, spaceIndex);
                float size = fontSize * font.getStringWidth(subString) / 1000;
                //System.out.printf("'%s' - %f of %f\n", subString, size, width);
                if (size > width)
                {
                    if (lastSpace < 0)
                        lastSpace = spaceIndex;
                    subString = text.substring(0, lastSpace);
                    lines.add(subString);
                    text = text.substring(lastSpace).trim();
                    //System.out.printf("'%s' is line\n", subString);
                    lastSpace = -1;
                }
                else if (spaceIndex == text.length())
                {
                    lines.add(text);
                    //System.out.printf("'%s' is line\n", text);
                    text = "";
                }
                else
                {
                    lastSpace = spaceIndex;
                }
            }
        }

        return lines;
    }

    // TODO: load file from relative path
    private void insertImage(PDDocument document,PDPage page, String path, TextCursor textCursor) throws IOException {

        PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);

        try {
            //Creating PDImageXObject object
            PDImageXObject pdImage = PDImageXObject.createFromFile(path, document);
            // 1:3
            pdImage.setWidth(90);
            pdImage.setHeight(30);

            //Drawing the image in the PDF document
            contentStream.drawImage(pdImage, textCursor.getCurentX() + 475 , textCursor.getCurentY());
        } finally {
            //Closing the content stream
            if (contentStream != null)
                contentStream.close() ;
        }

    }

    public static void main(String[] args) throws IOException {

        List<List<String>> data = null;
        List<List<String>> headers = null;

        PdfCreator pdfReport = new PdfCreator("Test1", data, headers);
        pdfReport.createPDF();
    }
}
