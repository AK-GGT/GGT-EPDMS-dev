package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.webgui.controller.AbstractHandler;
import de.iai.ilcd.webgui.controller.util.ExcelToReferenceSetParser;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.primefaces.event.FileUploadEvent;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.util.Set;
import java.util.zip.DataFormatException;

/**
 * Handles parsing of imported Excel file and gets available DataSets matching
 * any parsed references.
 * <p>
 * The Excel file should contain UUIDs, DataSetVersions, GlobalReferenceTypeValues.
 */
@ManagedBean
@ViewScoped
public class ExcelSelectionHandler extends AbstractHandler {

    /**
     * For Serialization..
     */
    private static final long serialVersionUID = 8264085192324757971L;

    private final ExcelToReferenceSetParser parser = new ExcelToReferenceSetParser();

    private String fileName = "";

    private int numberOfSuccesses = 0;

    private Set<GlobalReference> parsedReferences;

    public void handleUpload(FileUploadEvent event)
            throws EncryptedDocumentException, InvalidFormatException, DataFormatException, IOException {
        numberOfSuccesses = 0;
        fileName = event.getFile().getFileName();
        parsedReferences = parser.parseGlobalReferencesFrom(event.getFile().getInputStream());
        numberOfSuccesses = parsedReferences.size();
    }

    public String getFileName() {
        return fileName;
    }

    public Set<GlobalReference> getParsedReferences() {
        return this.parsedReferences;
    }

    public int getNumberOfSuccesses() {
        return numberOfSuccesses;
    }
}
