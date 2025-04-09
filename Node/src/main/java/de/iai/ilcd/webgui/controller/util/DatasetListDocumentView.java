package de.iai.ilcd.webgui.controller.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;

@ManagedBean
@ViewScoped
public class DatasetListDocumentView extends AbstractDocumentView implements Serializable {

    private static final long serialVersionUID = 5612516188877667998L;

    public static Logger logger = LogManager.getLogger(DatasetListDocumentView.class);

}
