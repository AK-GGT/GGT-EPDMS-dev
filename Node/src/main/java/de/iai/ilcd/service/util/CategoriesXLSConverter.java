package de.iai.ilcd.service.util;

import de.fzk.iai.ilcd.api.app.categories.Categories;
import de.fzk.iai.ilcd.api.app.categories.Category;
import de.fzk.iai.ilcd.api.app.categories.CategorySystem;
import de.fzk.iai.ilcd.api.binding.generated.categories.CategoryType;
import de.fzk.iai.ilcd.service.client.impl.vo.epd.ProcessSubType;
import de.iai.ilcd.configuration.ConfigurationService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

public class CategoriesXLSConverter implements Serializable {

    private static final long serialVersionUID = 5612516188877667998L;

    public static Logger logger = LogManager.getLogger(CategoriesXLSConverter.class);

    private ResourceBundle resourceBundle;

    public void generateXLS(String catSystemName, Map<String, CategorySystem> categoriesMap, boolean showCounts, Map<String, Map<String, Integer>> counts, String stockName, ResourceBundle resourceBundle, OutputStream os) throws IOException {

        this.resourceBundle = resourceBundle;

        Workbook wb = new XSSFWorkbook();

        String firstKey = (String) categoriesMap.keySet().toArray()[0];

        String name = catSystemName + ("de".equalsIgnoreCase(firstKey) ? "-" : " ")
                + resourceBundle.getString("admin.categories.categorydefinitions.categories");
        Sheet sheetData = wb.createSheet(name);
        Sheet sheetDataPretty = wb.createSheet(name + resourceBundle.getString("admin.categories.categorydefinitions.categories.pretty"));
        Sheet sheetMetaInfo = wb.createSheet("Info");

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormatTimeStamp = new SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss");
        String newDate = dateFormatTimeStamp.format(date);

        sheetMetaInfo.createRow(0);
        sheetMetaInfo.createRow(1);
        sheetMetaInfo.createRow(2);

        sheetMetaInfo.getRow(0).createCell(0, CellType.STRING).setCellValue("generated on");
        sheetMetaInfo.getRow(0).createCell(1, CellType.STRING).setCellValue(newDate);

        sheetMetaInfo.getRow(1).createCell(0, CellType.STRING).setCellValue("Node ID");
        sheetMetaInfo.getRow(1).createCell(1, CellType.STRING).setCellValue(ConfigurationService.INSTANCE.getNodeId() + " (" + ConfigurationService.INSTANCE.getNodeName() + ")");

        sheetMetaInfo.getRow(2).createCell(0, CellType.STRING).setCellValue("soda4LCA");
        sheetMetaInfo.getRow(2).createCell(1, CellType.STRING).setCellValue(ConfigurationService.INSTANCE.getVersionTag());

        if (StringUtils.isNotBlank(stockName)) {
            sheetMetaInfo.getRow(2).createCell(0, CellType.STRING).setCellValue("data stock");
            sheetMetaInfo.getRow(2).createCell(1, CellType.STRING).setCellValue(stockName);
        }

        sheetMetaInfo.autoSizeColumn(0);
        sheetMetaInfo.autoSizeColumn(1);

        int colRegular = 0;
        int colPretty = 0;

        for (String lang : categoriesMap.keySet()) {

            logger.debug("processing language: {}, cols regular/pretty {}/{}", lang, colRegular, colPretty );

            writeHeader(sheetData, colRegular, true, showCounts, (categoriesMap.keySet().size()>1 ? lang : null));
            writeHeader(sheetDataPretty, colPretty, false, showCounts, (categoriesMap.keySet().size()>1 ? lang : null));

            CategorySystem cs = categoriesMap.get(lang);
            Categories processCategories = (Categories) cs.getCategories().get(0);

            List<CategoryType> cats = processCategories.getCategory();

            logger.trace("cat system: {}  cat1: {}", cs.getName(), cats.get(0).getName());

            writeCategories(cats, 0, 0, colRegular, sheetData, counts, false);

            writeCategories(cats, 0, 0, colPretty, sheetDataPretty, counts, true);

            colRegular += 3;
            colPretty += 2;
        }

        autoSizeColumns(sheetMetaInfo, 2);
        autoSizeColumns(sheetData, 3 * categoriesMap.keySet().size());
        autoSizeColumns(sheetDataPretty, 3 * categoriesMap.keySet().size());

        try {
            wb.write(os);
        } catch (Exception e) {
            logger.error("Error generating XLS file", e);
        } finally {
            wb.close();
        }

    }

    private int writeCategories(List<CategoryType> cats, int level, int rownum, int colnum, Sheet sheet, Map<String, Map<String, Integer>> counts, boolean pretty) {

        logger.trace("writing categories for level {}, column {}, pretty={}", level, colnum, pretty);

        for (CategoryType catType : cats) {

            rownum++;

            Category cat = (Category) catType;

            Integer count = 0;

            if (sheet.getRow(rownum) == null)
                sheet.createRow(rownum);

            try {
                int i = colnum;
                if (!pretty) {
                    sheet.getRow(rownum).createCell(i, CellType.NUMERIC).setCellValue(level + 1);
                    i++;
                }

                String prettyPrefix = StringUtils.repeat(" ", level * 2);

                sheet.getRow(rownum).createCell(i, CellType.STRING).setCellValue((pretty ? prettyPrefix : "") + cat.getId());
                i++;
                sheet.getRow(rownum).createCell(i, CellType.STRING).setCellValue((pretty ? prettyPrefix : "") + cat.getName());

                if (counts != null) {
                    i++;
                    count = counts.get("ALL").get(cat.getId());
                    if (count != null) {
                        sheet.getRow(rownum).createCell(i, CellType.NUMERIC).setCellValue(count);
                    }

                    for (ProcessSubType subType : ProcessSubType.values()) {
                        count = counts.get(subType.getValue()).get(cat.getId());
                        i++;
                        if (count != null) {
                            sheet.getRow(rownum).createCell(i, CellType.NUMERIC).setCellValue(count);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error generating XLS", e);
            }

            if (logger.isTraceEnabled())
                logger.trace("col " + colnum + "  row " + rownum + "   lvl " + level + " " + cat.getId() + " " + cat.getName() + "    subcats:" + (!cat.getCategory().isEmpty()) + " " + (count != null ? count.toString() : ""));

            if (!cat.getCategory().isEmpty()) {
                rownum = writeCategories(cat.getCategory(), level + 1, rownum, colnum, sheet, counts, pretty);
            }
        }
        return rownum;
    }

    private void writeHeader(Sheet sheet, int column, boolean showLevel, boolean count, String lang) {

        Font font = sheet.getWorkbook().createFont();
        font.setBold(true);
        CellStyle boldStyle = sheet.getWorkbook().createCellStyle();
        boldStyle.setFont(font);

        Row headerRow = (sheet.getRow(0) == null ? sheet.createRow(0) : sheet.getRow(0));

        int i = column;

        if (showLevel) {
            headerRow.createCell(i, CellType.STRING).setCellValue(this.resourceBundle.getString("admin.categories.categorydefinitions.categories.level"));
            headerRow.getCell(i).setCellStyle(boldStyle);
            i++;
        }
        headerRow.createCell(i, CellType.STRING).setCellValue(this.resourceBundle.getString("admin.categories.categorydefinitions.categories.id"));
        headerRow.getCell(i).setCellStyle(boldStyle);
        i++;
        String langSuffix = (lang == null ? "" : " (" + lang + ")");
        headerRow.createCell(i, CellType.STRING).setCellValue(this.resourceBundle.getString("admin.categories.categorydefinitions.categories.name") + langSuffix);
        headerRow.getCell(i).setCellStyle(boldStyle);
        i++;

        if (count) {
            headerRow.createCell(i, CellType.STRING).setCellValue(this.resourceBundle.getString("admin.categories.categorydefinitions.categories.count"));
            headerRow.getCell(i).setCellStyle(boldStyle);
            i++;

            for (ProcessSubType subType : ProcessSubType.values()) {
                headerRow.createCell(i, CellType.STRING).setCellValue(subType.getValue().replace(" dataset", ""));
                headerRow.getCell(i).setCellStyle(boldStyle);
                i++;
            }
        }
    }

    private void autoSizeColumns(Sheet sheet, int columns) {
        for (int i = 0; i < columns; i++)
            sheet.autoSizeColumn(i);
    }

}
