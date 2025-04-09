package de.iai.ilcd.webgui.controller.util;

import de.iai.ilcd.webgui.controller.admin.ValidationHandler;
import de.iai.ilcd.webgui.util.ValidationContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ManagedBean
@ViewScoped
public class ValidationResultDocumentView extends AbstractDocumentView implements Serializable {

    private static final long serialVersionUID = 5612516188877667998L;

    public static Logger logger = LogManager.getLogger(ValidationResultDocumentView.class);

    @ManagedProperty(value = "#{validationHandler}")
    private ValidationHandler validationHandler;

    @Override
    public void postProcessXLS(Object document) {
        super.postProcessXLS(document);

//		HSSFWorkbook wb = (HSSFWorkbook) document;
//		Date date = Calendar.getInstance().getTime();
//		SimpleDateFormat dateFormatTimeStamp = new SimpleDateFormat( "dd.MM.yyyy 'at' HH:mm:ss" );
        StringBuilder sbName = new StringBuilder("Validation log for ");
        if (validationHandler.getValidationContext().equals(ValidationContext.DATASETS))
            sbName.append(validationHandler.getNumberOfDatasets()).append(" datasets");
        else if (validationHandler.getValidationContext().equals(ValidationContext.STOCK))
            sbName.append(validationHandler.getStockName());
        else if (validationHandler.getValidationContext().equals(ValidationContext.IMPORT))
            sbName.append("uploaded datasets");
        String name = sbName.toString();

//		wb.setSheetName( 0, name );
//		HSSFSheet sheet = wb.getSheetAt(0);
//
//		String newDate = dateFormatTimeStamp.format(date);

        List<String> documentViewHeader = new ArrayList<String>();
        documentViewHeader = this.validationHandler.getDocumentViewHeader();

        Map<String, String> description = new HashMap<String, String>();

        description.put("Profile:", documentViewHeader.get(1));
        description.put("Aspects:", documentViewHeader.get(0));

        super.createDescriptionSheet(name, description);


//		makeDocumentDescription(newDate, sheet, documentViewHeader);


//		mergeEmptyCells(sheet);

    }


    /**
     * @param newDate
     * @param sheet
     * @param documentViewHeader
     */
//	private void makeDocumentDescription(String newDate, HSSFSheet sheet, List<String> documentViewHeader) {
//		insertRow("Date: ", newDate, sheet, 0);
//		insertRow("Profile: ", documentViewHeader.get(1), sheet, 1);
//		insertRow("Aspects: ", documentViewHeader.get(0), sheet, 2);
//		insertRow("soda4LCA Version: ", documentViewHeader.get(2), sheet, 3);
//	}


    /**
     * @param sheet
     */
//	private void mergeEmptyCells(HSSFSheet sheet) {
//		sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 4));
//		sheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 4));	
//		sheet.addMergedRegion(new CellRangeAddress(2, 2, 1, 4));	
//		sheet.addMergedRegion(new CellRangeAddress(3, 3, 1, 4));	
//		sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 4));
//	}


    /**
     * @param newDate
     * @param sheet
     */
//	private void insertRow(String cellKey, String cellValue, HSSFSheet sheet, int rowNumber) {
//		HSSFRow firstRow = sheet.createRow(rowNumber);
//		HSSFCell fisrtRowCell0 = firstRow.createCell(0);
//		HSSFCell fisrtRowCell1 = firstRow.createCell(1);
//		fisrtRowCell0.setCellValue(cellKey);
//		fisrtRowCell1.setCellValue(cellValue);
//	}
    public ValidationHandler getValidationHandler() {
        return validationHandler;
    }


    public void setValidationHandler(ValidationHandler validationHandler) {
        this.validationHandler = validationHandler;
    }

}
