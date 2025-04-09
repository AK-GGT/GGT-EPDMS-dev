package edu.kit.soda4lca.test.ui.main;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides some functions to work with a excel file
 *
 * @author sarai
 */
public class WorkbookTestFunctions extends FileTestFunctions {

    protected final static Logger log = LogManager.getLogger(WorkbookTestFunctions.class);

    /**
     * Creates a workbook of given excel file.
     *
     * @param fileStream The file stream of given excel file
     * @return A workbook of given excel file
     * @throws IOException
     */
    public static HSSFWorkbook createWorkbook(FileInputStream fileStream) throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook(fileStream);
        return workbook;
    }

    /**
     * Creates a new Workbook sheet to work with entries of given excel file.
     *
     * @param fileStream The file stream of given excel file
     * @param index      The sheet number
     * @return A workbook sheet of given excel file
     * @throws IOException
     */
    public static HSSFSheet createSheet(FileInputStream fileStream, int index) throws IOException {
        HSSFWorkbook workbook = createWorkbook(fileStream);
        HSSFSheet sheet = workbook.getSheetAt(index);
        return sheet;
    }

    /**
     * Counts the number of entries in workbook sheet
     *
     * @param sheet The sheet to work with
     * @return Number of entries in workbook sheet
     */
    public static int countEntries(HSSFSheet sheet) {
//		int i = 0;
//		do  {
//			i++;
//		} while (sheet.getRow(i).getCell(1) == null || sheet.getRow(i).getCell(1).getCellType() == Cell.CELL_TYPE_BLANK || sheet.getRow(i).getCell(1).getStringCellValue().isEmpty());
//		//we do not want to count heading as entries
//		i++;
//		log.debug("i is: " + i);
        return sheet.getPhysicalNumberOfRows() - 1;
    }

    /**
     * Creates a file and Workbook sheet to count the number of entries in excel file and compare result with given number of page entries
     *
     * @param fileName    The name of download file
     * @param _class      The class that calls this method
     * @param pageEntries The given number of page entries
     * @throws IOException
     */
    public static void countAndCompareEntries(String fileName, Class<?> _class, int pageEntries) throws IOException {
        //create Workbook sheet to work on excel entries
        String pathName = getPath(fileName, _class);
        File file = new File(pathName);
        FileInputStream fileStream = new FileInputStream(file);
        HSSFSheet sheet = createSheet(fileStream, 0);
        int sheetEntries = countEntries(sheet);
        if (log.isTraceEnabled()) {
            log.trace("number of page entries is: " + pageEntries);
            log.trace("number of sheet entries is: " + sheetEntries);
        }
        file.delete();
        if (sheetEntries != pageEntries) {
            org.testng.Assert.fail("number of entries in xls file doesn't match number of entries in data set");
        }
    }

    /**
     * Checks a result of custom report executions as csv file.
     * Compares therefore the table headings with expected headings
     * as well as number of table entries and number of table columns.
     *
     * @param fileName        The name of resulting file
     * @param _class          The class of to reviewed file
     * @param entriesCount    The number of expected entries
     * @param expectedHeader  The list of expected headers
     * @param expectedColumns The number of expectes columns in resulting table
     * @throws IOException
     */
    public static void compareCSVEntries(String fileName, Class<?> _class, int entriesCount, List<String> expectedHeader, int expectedColumns)
            throws IOException {
        String pathName = getPath(fileName, _class);
        Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(new FileReader(pathName));
        boolean isHeader = true;
        int count = 0;

        List<String> actualHeader = new ArrayList<String>();

        for (CSVRecord record : records) {
            int columnsCount = 0;
            if (isHeader) {
                log.trace("header");
            } else {
                log.trace("content");
            }
            for (int i = 0; i < record.size(); i++) {
                if (isHeader) {
                    if (record.get(i).contains(";")) {
                        String[] splitString = record.get(i).split(";");
                        actualHeader.addAll(Arrays.asList(splitString));
                    } else {
                        actualHeader.add(record.get(i));
                    }
                } else {
                    if (record.get(i).contains(";")) {
                        String[] splitString = record.get(i).split(";");
                        columnsCount += splitString.length;
                    } else
                        columnsCount++;
                }
            }
            if (!isHeader && columnsCount != expectedColumns) {
                org.testng.Assert.fail("The number of actual columns (" + columnsCount
                        + ") does not match number of expected columns (" + expectedColumns + ") in row no. " + count + ".");
            }
            log.trace("new line.");
            if (!isHeader) {
                count++;
            }
            isHeader = false;
        }

        if (expectedHeader.size() != actualHeader.size()) {
            org.testng.Assert.fail("The number of expected headers (" + expectedHeader.size() + ") does not match"
                    + " number of actual headers (" + actualHeader.size() + ".)");
        }

        for (int i = 0; i < actualHeader.size(); i++) {
            if (!actualHeader.get(i).equals(expectedHeader.get(i))) {
                org.testng.Assert.fail("The " + i + "th expected header (" + expectedHeader.get(i) + ") does not match " +
                        i + "th actual header (" + actualHeader.get(i) + ").");
            }
        }

        if (count != entriesCount) {
            org.testng.Assert.fail("number of entries in cvs file (" + count
                    + ") doesn't match expected number of entries in request result (" + entriesCount + ").");
        }
    }
}
