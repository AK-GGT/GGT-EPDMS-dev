package de.iai.ilcd.rest;

import de.fzk.iai.ilcd.api.dataset.ILCDTypes;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.dao.LifeCycleModelDao;
import de.iai.ilcd.model.lifecyclemodel.LifeCycleModel;
import de.iai.ilcd.visualization.LCMviz;
import de.iai.ilcd.webgui.controller.admin.export.DataExportController;
import de.iai.ilcd.xml.zip.ZipArchiveBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.nio.file.Paths;


/**
 * REST web service for Life-cycle Models
 */

@Component
@Path("lifecyclemodels")
public class LifeCycleModelResource extends AbstractDataSetResource<LifeCycleModel> {

    static final Logger LOGGER = LogManager.getLogger(LifeCycleModelResource.class);

    @Autowired
    private DataExportController dataExportController;

    public LifeCycleModelResource() {
        super(DataSetType.LIFECYCLEMODEL, ILCDTypes.LIFECYCLEMODEL);
    }


    @GET
    @Path("{uuid}/viz")
    @Produces("image/svg+xml")
    public Response generateSVG(@PathParam("uuid") String UUID) {
        LifeCycleModelDao dao = new LifeCycleModelDao();
        LifeCycleModel lcm = dao.getByUuid(UUID); // "20062015-184a-41b8-8fa6-49e999cbd101"
        LCMviz diagram = new LCMviz(lcm);
        return Response.ok(diagram.drawSVG())
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    @POST
    @Path("{uuid}/viz")
    @Produces("image/svg+xml")
    public Response generateConfigrableSVG(@PathParam("uuid") String UUID,
                                           @FormParam("palette") String palette, // -X POST -d "palette=fancy&rankdir=LR&..."
                                           @FormParam("rankdir") String rankdir,
                                           @FormParam("spline") String spline,
                                           @FormParam("arrowHeadShape") String arrowheadshape,
                                           @FormParam("arrowLineStyle") String arrowlinestyle,
                                           @FormParam("imageWidth") int imagewidth,
                                           @FormParam("nodeSeprate") int nodeseprate,
                                           @FormParam("rankSeprate") int rankSeprate,
                                           @FormParam("canvasPadding") int pad,
                                           @FormParam("descriptionWidth") int descriptionwidth,
                                           @FormParam("fontSizeProcessUUID") int fontsizeprocessUUID,
                                           @FormParam("fontSizeFlowUUID") int fontsizeflowUUID) {

        LifeCycleModelDao dao = new LifeCycleModelDao();
        LifeCycleModel lcm = dao.getByUuid(UUID); // "20062015-184a-41b8-8fa6-49e999cbd101"
        LCMviz diagram = new LCMviz(lcm);

        diagram.setPalette(palette);
        diagram.setRank(rankdir);
        diagram.setSpline(spline);
        diagram.setArrowHeadShape(arrowheadshape);
        diagram.setArrowLineStyle(arrowlinestyle);
        diagram.setImageWidth(imagewidth);
        diagram.setNodeSeperate(nodeseprate);
        diagram.setRankSeperate(rankSeprate);
        diagram.setCanvasPadding(pad);
        diagram.setDescriptionWidth(descriptionwidth);
        diagram.setFontSizeProcessUUID(fontsizeprocessUUID);
        diagram.setFontSizeFlowUUID(fontsizeflowUUID);

        return Response.ok(diagram.drawSVG())
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }


    /**
     * Get dependencies of a lifecycle model in a zip archive.
     *
     * @param uuid    the UUID of the lifecycle model
     * @param version the version of the process, if version not provided (null)
     *                latest is assumed
     * @return A zip archive contains the lifecycle model and it's dependencies.
     */

    @GET
    @Path("{uuid}/zipexport")
    @Produces("application/zip")
    public Response getDependencies(@PathParam("uuid") String uuid, @QueryParam("version") String version) {

        LifeCycleModelDao dao = (LifeCycleModelDao) getFreshDaoInstance();
        LifeCycleModel lcm;

        // if version not provided (null) or parser fails to read it properly, latest
        // version is assumed
        lcm = dao.getByUuidAndVersion(uuid, version);

        java.nio.file.Path dir = Paths.get(ConfigurationService.INSTANCE.getZipFileDirectory());

        // Overwrite the same file to avoid post cleaning
        ZipArchiveBuilder zipArchiveBuilder = new ZipArchiveBuilder(dir.resolve("RESTtmpDEPexport.zip"), dir);

        dataExportController.writeDependencies(lcm, zipArchiveBuilder);
        zipArchiveBuilder.close();

        String filename = lcm.getUuidAsString() + "_dependencies.zip";
        return Response.ok(zipArchiveBuilder.getFile())
                .header("Content-Type", zipArchiveBuilder.MIME)
                .header("Content-Disposition", zipArchiveBuilder.getContentDisposition(filename))
                .build();
    }

    @Override
    protected DataSetDao<LifeCycleModel, ?, ?> getFreshDaoInstance() {
        return new LifeCycleModelDao();
    }

    @Override
    protected Class<LifeCycleModel> getDataSetType() {
        return LifeCycleModel.class;
    }

    @Override
    protected String getXMLTemplatePath() {
        return "/xml/lifecyclemodel.vm";
    }

    @Override
    protected String getHTMLDatasetDetailTemplatePath() {
        return "/html/lifecyclemodel.vm";
    }

    @Override
    protected String getDataSetTypeName() {
        return "lifecyclemodel";
    }

    @Override
    protected boolean userRequiresDatasetDetailRights() {
        return true;
    }

}
