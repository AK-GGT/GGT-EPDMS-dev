package de.iai.ilcd.webgui.controller.util;

import de.fzk.iai.ilcd.service.model.enums.GlobalReferenceTypeValue;
import de.iai.ilcd.model.common.DataSetVersion;
import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.model.common.Uuid;
import de.iai.ilcd.model.common.exception.FormatException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.DataFormatException;

public class ExcelToReferenceSetParser {

    public static Logger logger = LogManager.getLogger(ExcelToReferenceSetParser.class);

    private int typeColumnNumber;

    private int uuidColumnNumber;

    private int versionColumnNumber;

    private Boolean columnsIdentified = false;

    private int invalidRowsCounter = 0;

    /**
     * Parses a set of references from a suitable Excel file.
     *
     * @param excelFile
     * @return
     * @throws IOException
     * @throws EncryptedDocumentException
     * @throws InvalidFormatException
     * @throws DataFormatException
     */
    public Set<GlobalReference> parseGlobalReferencesFrom(InputStream excelInputStream)
            throws DataFormatException, EncryptedDocumentException, IOException, InvalidFormatException {
        invalidRowsCounter = 0;
        Workbook wb = WorkbookFactory.create(excelInputStream); // throws EncryptedDocumentException, IOException,
        // InvalidFormatException

        Set<GlobalReference> result = new HashSet<GlobalReference>();

        for (int i = 0; i < wb.getNumberOfSheets(); i++) {// We must iterate through sheets, then rows, then cells.
            Sheet sheet = wb.getSheetAt(i);
            determineRelevantColumns(sheet);
            if (columnsIdentified) {
                for (Row row : sheet) {
                    int rowNumber = row.getRowNum();
                    if (rowNumber != 0) {
                        int displayedRowNumber = rowNumber + 1;
                        GlobalReference ref = new GlobalReference();

                        try {
                            ref = parseGlobalReferenceFrom(row);    // the actual parsing happens here.
                        } catch (MissingPropertyException | DataFormatException | FormatException e) {
                            if (logger.isDebugEnabled())
                                logger.debug("Row " + displayedRowNumber + ": " + e.getMessage());
                        }

                        if (isValid(ref)) {
                            result.add(ref);
                        } else {
                            if (!isEmpty(row)) // We care about corrupted entries not weird Excel behaviour.
                                invalidRowsCounter += 1;
                        }
                    }
                }
            }
        }
        if (result.isEmpty())
            throw new DataFormatException(
                    "Excel file could not be parsed. If it contains entries, non of the entries match the minimum requirements: Having 'Dataset Type', 'UUID', 'Version' specified.");
        if (invalidRowsCounter > 0)
            logger.warn(invalidRowsCounter + " rows could not be parsed.");
        return result;
    }

    private void determineRelevantColumns(Sheet sheet) {
        Boolean typeColumnNumberUnascertained = true; // We settle for the first matching columns.
        Boolean uuidColumnNumberUnascertained = true;
        Boolean versionColumnNumberUnascertained = true;
        Row row = sheet.getRow(0);
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            String thisColumnsTitle = "";
            try {
                thisColumnsTitle = getStringValueOf(cell);
            } catch (DataFormatException e) {
                // Handled elsewhere.
            }
            if (typeColumnNumberUnascertained && StringUtils.containsIgnoreCase(thisColumnsTitle, "typ")) {
                typeColumnNumber = cell.getColumnIndex();
                typeColumnNumberUnascertained = false;
            }
            if (uuidColumnNumberUnascertained && StringUtils.containsIgnoreCase(thisColumnsTitle, "uuid")) {
                uuidColumnNumber = cell.getColumnIndex();
                uuidColumnNumberUnascertained = false;
            }
            if (versionColumnNumberUnascertained && StringUtils.containsIgnoreCase(thisColumnsTitle, "version")) {
                versionColumnNumber = cell.getColumnIndex();
                versionColumnNumberUnascertained = false;
            }
        }
        if (!typeColumnNumberUnascertained && !uuidColumnNumberUnascertained && !versionColumnNumberUnascertained)
            columnsIdentified = true;
    }

    /**
     * Parsing <code>GlobalReference<code> from a row which is expected to contain
     * UUIDs , GlobalReferenceTypeValues and DataSetVersions.
     *
     * @param row
     * @return
     * @throws DataFormatException
     * @throws MissingPropertyException
     * @throws FormatException
     */
    private GlobalReference parseGlobalReferenceFrom(Row row)
            throws DataFormatException, MissingPropertyException, FormatException {
        if (isEmpty(row))
            return null;
        GlobalReference ref = new GlobalReference();

        String uuidString = new String();
        String versionString = new String();
        String typeString = new String();
        int rowNumber = row.getRowNum() + 1;

        // Trying to parse Excel-Cell-Values.
        try {
            uuidString = getStringValueOf(row.getCell(uuidColumnNumber));
            typeString = getStringValueOf(row.getCell(typeColumnNumber));
        } catch (DataFormatException e) {
            if (logger.isDebugEnabled())
                logger.debug(e.getMessage());
        }
        if (StringUtils.isBlank(uuidString))
            throw new MissingPropertyException("UUID not found.");
        if (StringUtils.isBlank(typeString))
            throw new MissingPropertyException("data set type not found.");

        // version is optional so we need to catch the CellType.BLANK format exception.
        try {
            versionString = getStringValueOf(row.getCell(versionColumnNumber));
        } catch (DataFormatException e) {
            // optional, so it's fine: the most recent version will be fetched.
        }

        // Trying to parse GlobalReference from Cell data..
        // ..UUID
        try {
            ref.setUuid(new Uuid(uuidString));
        } catch (IllegalArgumentException e) {
            throw new FormatException("UUID could not be parsed.");
        }

        // ..Type
        try {
            ref.setType(GlobalReferenceTypeValue.fromValue(typeString));
        } catch (IllegalArgumentException e) {
            if (logger.isDebugEnabled())
                logger.debug("Row " + rowNumber + ": Data set type could not be parsed.");
        }

        // ..Version
        try {
            ref.setVersion(DataSetVersion.parse(versionString));
        } catch (FormatException e) {
            // optional, so it's fine: the most recent version will be fetched.
            if (logger.isDebugEnabled())
                logger.debug("Row " + rowNumber + ": Version could not be parsed.");
        }
        return ref;
    }

    /**
     * Gets the String value of a cell. If the CellType is invalid it returns an
     * empty String and throws a FormatException.
     *
     * @param cell
     * @return
     * @throws FormatException
     */
    private String getStringValueOf(Cell cell) throws DataFormatException {
        StringBuilder value = new StringBuilder("");
        if (cell != null) {
            if (cell.getCellType() == CellType.STRING) {
                value.append(cell.getStringCellValue());
            } else {
                throw new DataFormatException("Invalid CellType: CellType should be CellType.STRING.");
            }
        }
        return value.toString();
    }

    /**
     * We consider a <code>GlobalReference<code> as valid, when it contains some
     * String as UUID.
     *
     * @param ref
     * @return
     */
    private boolean isValid(GlobalReference ref) {
        if (ref == null || ref.getUuid() == null || ref.getUuid().getUuid().isEmpty() || ref.getType() == null)
            return false;
        return true;
    }

    public int getNumberOfFailures() {
        return invalidRowsCounter;
    }

    private Boolean isEmpty(Row row) {
        if (row == null || row.getLastCellNum() <= 0)
            return true;
        return false;
    }
}

/**
 * Dummy exception created as a polyfill after removing
 * <code>org.codehaus.groovy.maven.runtime</code>
 * <p>
 * Code can be trivially refactored to remove it
 *
 * @author MK
 * @since soda4LCA 6.7.3
 */
class MissingPropertyException extends Exception {
    private static final long serialVersionUID = 3125699788985439716L;

    public MissingPropertyException(String msg) {
        super(msg);
    }
}
