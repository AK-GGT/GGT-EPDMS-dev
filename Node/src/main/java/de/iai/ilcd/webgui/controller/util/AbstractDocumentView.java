package de.iai.ilcd.webgui.controller.util;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import de.iai.ilcd.configuration.ConfigurationService;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

@ManagedBean
@ViewScoped
public abstract class AbstractDocumentView implements Serializable {

    private static final long serialVersionUID = 5612516188877667998L;
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final float LINE_HEIGHT_IN_POINTS = 12.75f;
    public static Logger logger = LogManager.getLogger(AbstractDocumentView.class);
    HSSFWorkbook wb;
    private int descriptionRowNo = 0;
    private String soda4LCAVersion;
    private String dateString;

    /**
     * In case of POI-Upgrade: You probably need to replace the enums.
     * <p>
     * HSSFCellStyle.VERTICAL_BOTTOM -> VerticalAlignment.Bottom
     * <p>
     * HSSFColor.GREY_25_PERCENT.index -> HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex()
     * <p>
     * HSSFCellStyle.SOLID_FOREGROUND -> FillPatternType.SOLID_FOREGROUND
     *
     * @param document
     */
    public void postProcessXLS(Object document) {
        soda4LCAVersion = ConfigurationService.INSTANCE.getVersionTag();

        wb = (HSSFWorkbook) document;
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormatTimeStamp = new SimpleDateFormat("dd.MM.yyyy 'at' HH.mm.ss");
        dateString = dateFormatTimeStamp.format(date);

        wb.setSheetName(0, dateString);
        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFRow header = sheet.getRow(0);

        HSSFCellStyle cellStyle = wb.createCellStyle();
        cellStyle.setVerticalAlignment(VerticalAlignment.TOP);

        HSSFCellStyle headerCellStyle = wb.createCellStyle();
        headerCellStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        String pattern = "(<div.*?>)|(</div>)";
        formatDocument(sheet, cellStyle, pattern);

        for (int i = 0; i < header.getPhysicalNumberOfCells(); i++) {
            HSSFCell cell = header.getCell(i);
            cell.setCellStyle(headerCellStyle);
            sheet.autoSizeColumn(i);
        }

    }

    /**
     * @param sheet
     * @param cellStyle
     * @param pattern
     */
    protected void formatDocument(HSSFSheet sheet, HSSFCellStyle cellStyle, String pattern) {
        HSSFCellStyle cs = wb.createCellStyle();
        cs.setVerticalAlignment(VerticalAlignment.TOP);

        for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {
            HSSFRow row = sheet.getRow(i);
            if (row == null)
                return;
            for (int j = 0; j < row.getPhysicalNumberOfCells(); j++) {
                HSSFCell cell = row.getCell(j);
                cell.setCellValue(cell.getStringCellValue().replaceAll(pattern, " "));
                formatCellContain(cellStyle, i, row, cell);

                // If there's line breaks, we need to adjust height manually..
                String sValue = cell.getStringCellValue();
                if (sValue.contains(LINE_SEPARATOR)) {
                    int lineCount = StringUtils.countMatches(sValue, LINE_SEPARATOR) + 1; // line breaks + first line
                    row.setHeightInPoints(lineCount * LINE_HEIGHT_IN_POINTS);
                }

                cell.setCellStyle(cs);
            }
        }
    }

    /**
     * Inserts a new row (containing metadata to be inserted) to description sheet.
     *
     * @param cellKey   The metadata name
     * @param cellValue The metadata value
     * @param sheet     The sheet no, usually sheet no 1
     */
    private void insertRow(String cellKey, String cellValue, HSSFSheet sheet) {
        HSSFRow firstRow = sheet.createRow(descriptionRowNo);
        HSSFCell firstRowCell0 = firstRow.createCell(0);
        HSSFCell firstRowCell1 = firstRow.createCell(1);
        firstRowCell0.setCellValue(cellKey);
        firstRowCell1.setCellValue(cellValue);
        sheet.addMergedRegion(new CellRangeAddress(descriptionRowNo, descriptionRowNo, 1, 4));
        descriptionRowNo += 1;
    }

    /**
     * Creates an additional sheet containing all metadata of exported file (includes by default current date and time, as well as soda4CLA version).
     *
     * @param sheetName The name the description sheet will have
     * @param rows      The additional metadata the description sheet will contain; map key is description name, map value is description value
     */
    protected void createDescriptionSheet(String sheetName, Map<String, String> rows) {
        HSSFSheet sheet = wb.createSheet(sheetName);
        if (dateString == null) {
            createNewDate();
        }
        insertRow("Date:", dateString, sheet);
        insertRow("soda4LCA Version:", soda4LCAVersion, sheet);
        Iterator<Map.Entry<String, String>> iterator = rows.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> row = (Map.Entry<String, String>) iterator.next();
            insertRow((String) row.getKey(), (String) row.getValue(), sheet);
        }
    }


    /**
     * @param cellStyle
     * @param i
     * @param row
     * @param cell
     */
    protected void formatCellContain(HSSFCellStyle cellStyle, int i, HSSFRow row, HSSFCell cell) {
        String cellValue = cell.getStringCellValue();
        if (cellValue.contains("UUID:")) {
            int uuidIndex = cellValue.indexOf("UUID:");
            String partWithUUID = cellValue.substring(uuidIndex);
            cell.setCellValue(cellValue.substring(0, uuidIndex));
            row.getCell(0).setCellValue(partWithUUID.replace("UUID:", ""));
            if (i != 0)
                cell.setCellStyle(cellStyle);
        }
    }


    public void postProcessCSV(Object document) {

    }

    public void preProcessPDF(Object document) throws IOException, BadElementException, DocumentException {
        Document pdf = (Document) document;
        pdf.open();
        pdf.setPageSize(PageSize.A4);
    }

    /**
     * Gets the current date.
     *
     * @return current date
     */
    public String getDate() {
        if (dateString == null) {
            createNewDate();
        }
        return dateString;
    }

    /**
     * Creates a new date with current date and time information.
     */
    private void createNewDate() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormatTimeStamp = new SimpleDateFormat("dd.MM.yyyy 'at' HH.mm.ss");
        dateString = dateFormatTimeStamp.format(date);
    }

}
