package de.iai.ilcd.rest;

import de.fzk.iai.ilcd.api.app.categories.Categories;
import de.fzk.iai.ilcd.api.app.categories.Category;
import de.fzk.iai.ilcd.api.binding.generated.categories.CategoriesType;
import de.fzk.iai.ilcd.api.binding.generated.categories.CategoryType;
import de.fzk.iai.ilcd.api.binding.helper.CategoriesHelper;
import de.fzk.iai.ilcd.service.client.impl.ServiceDAO;
import de.fzk.iai.ilcd.service.client.impl.vo.CategorySystemList;
import de.fzk.iai.ilcd.service.client.impl.vo.epd.ProcessSubType;
import de.fzk.iai.ilcd.service.model.ICategorySystemList;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.CategoryDefinition;
import de.iai.ilcd.model.common.CategorySystem;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.common.exception.DatastockNotFoundException;
import de.iai.ilcd.model.dao.CategorySystemDao;
import de.iai.ilcd.model.dao.CommonDataStockDao;
import de.iai.ilcd.model.dao.ProcessDao;
import de.iai.ilcd.model.datastock.AbstractDataStock;
import de.iai.ilcd.model.datastock.IDataStockMetaData;
import de.iai.ilcd.service.util.CategoriesXLSConverter;
import de.iai.ilcd.util.CategoryTranslator;
import de.iai.ilcd.util.SodaResourceBundle;
import de.iai.ilcd.webgui.util.CategoryDefinitionsUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.tools.generic.ValueParser;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * This service represents the definitions for category systems that are
 * present in the database. (Not to be confused with the actual category
 * declaration in the datasets!)
 */

@Component
@Path("categorySystems")
public class CategorySystemResource extends AbstractResource {

    public static Logger logger = LogManager.getLogger(CategorySystemResource.class);

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of FileResource
     */
    public CategorySystemResource() {
    }

    /**
     * Provide a list of names of Category Systems that are present
     *
     * @return the categories either as an XML or XLS document
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_OCTET_STREAM})
    public Response getDefinitions() {

        CategorySystemDao csDao = new CategorySystemDao();

        final ICategorySystemList csList = new CategorySystemList();

        for (CategorySystem cs : csDao.getAll()) {
            de.fzk.iai.ilcd.service.client.impl.vo.types.common.CategorySystem apiCs = new de.fzk.iai.ilcd.service.client.impl.vo.types.common.CategorySystem();
            apiCs.setName(cs.getName());
            csList.getCategorySystems().add(apiCs);
        }

        StreamingOutput stream = out -> {

            ServiceDAO dao = new ServiceDAO();

            try {
                dao.marshal(csList, out);
            } catch (JAXBException e) {
                throw new IOException(e);
            }
        };
        return Response.ok(stream).type(MediaType.APPLICATION_XML).build();

    }

    /**
     * Retrieve the definition for a specific Category System by name
     *
     * @param name     the name
     * @param language the language
     * @param format   either AbstractResource.FORMAT_XML or AbstractResource.FORMAT_XLS
     * @return the definition
     */
    @GET
    @Path("{name}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_OCTET_STREAM})
    public Response getDefinition(final @PathParam("name") String name, @QueryParam("lang") String language,
                                  @DefaultValue(AbstractResource.FORMAT_XML) @QueryParam(AbstractResource.PARAM_FORMAT) final String format,
                                  @QueryParam("countStock") String countStock, @DefaultValue("false") @QueryParam("allLanguages") Boolean allLanguages) {

        if (logger.isDebugEnabled()) {
            logger.debug("getting definitions for category system " + name);
            logger.debug("requested language is " + language);
            logger.debug("requested format is " + format);
            logger.debug("default language is " + ConfigurationService.INSTANCE.getDefaultLanguage());
        }

        if (language == null)
            language = ConfigurationService.INSTANCE.getDefaultLanguage();

        if (logger.isDebugEnabled())
            logger.debug("resulting language is " + language);

        List<String> targetLanguages = new ArrayList<>();
        if (allLanguages) {
            targetLanguages.add(ConfigurationService.INSTANCE.getDefaultLanguage());
            targetLanguages.addAll(ConfigurationService.INSTANCE.getTranslateClassificationCSVlanguages());
        } else {
            targetLanguages.add(language);
        }

        if (logger.isDebugEnabled())
            logger.debug("target language(s): " + targetLanguages);

        if (StringUtils.isBlank(name))
            return Response.status(Status.NOT_FOUND).build();

        // countStock and allLanguages don't work together
        if (countStock != null && allLanguages)
            return Response.status(422).build();

        CategorySystemDao csDao = new CategorySystemDao();

        CategorySystem cs = csDao.getByName(name);

        if (cs == null)
            return Response.status(Status.NOT_FOUND).build();

        CategoryDefinition cd = cs.getCurrentCategoryDefinition();

        if (cd == null)
            return Response.status(Status.NOT_FOUND).build();

        CategoryDefinitionsUtil cdu = new CategoryDefinitionsUtil();

        Map<String, de.fzk.iai.ilcd.api.app.categories.CategorySystem> catsMap = new HashMap<>();

        try {
            for (String lang : targetLanguages) {
                de.fzk.iai.ilcd.api.app.categories.CategorySystem apiCs = cdu.convertDefinitionToCategorySystem(cd);
                catsMap.put(lang, apiCs);
            }
        } catch (UnsupportedEncodingException | JAXBException e) {
            logger.error("Error converting category definitions", e);
            return Response.serverError().build();
        }

        Map<String, Map<String, Integer>> counts = null;
        try {
            if (countStock != null) {
                counts = retrieveCounts(countStock, catsMap.get(language));
            }
        } catch (DatastockNotFoundException e) {
            logger.error(e.getMessage());
            return Response.status(Status.NOT_FOUND).build();
        }

        // if category translations are requested for all languages, loop through languages for which translations are provided
        if (ConfigurationService.INSTANCE.isTranslateClassification()) {
            for (String lang : catsMap.keySet()) {
                if (!StringUtils.equalsIgnoreCase(lang, ConfigurationService.INSTANCE.getDefaultLanguage()))
                    translate(catsMap.get(lang), lang);
            }
        }

        if (AbstractResource.FORMAT_XML.equals(format)) {
            Date lastModified = csDao.getLastModified(name);
            return getDefinitionXML(catsMap.get(language), lastModified);
        } else if (AbstractResource.FORMAT_XLS.equals(format))
            return getDefinitionXLS(name, catsMap, language, countStock, counts);

        return Response.noContent().build();
    }

    @GET
    @Path("{name}/lastModified")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getDefinition(final @PathParam("name") String name, @DefaultValue("dd.MM.YYYY") @QueryParam("format") final String format) {
        if (StringUtils.isBlank(name))
            return Response.status(Status.NOT_FOUND).build();

        CategorySystemDao csDao = new CategorySystemDao();

        Date d = csDao.getLastModified(name);

        if (d == null)
            return Response.status(Status.NOT_FOUND).build();

        LocalDateTime l = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        try {
            String formattedResult = l.format(DateTimeFormatter.ofPattern(format));
            return Response.ok(formattedResult).build();
        } catch (IllegalArgumentException e) {
            return Response.status(400).build();
        }
    }

    private Map<String, Map<String, Integer>> retrieveCounts(String datastock, de.fzk.iai.ilcd.api.app.categories.CategorySystem cs) throws DatastockNotFoundException {
        Map<String, Map<String, Integer>> result = new HashMap<>();

        Map<String, Integer> countsAll = new HashMap<>();
        Map<String, Integer> countsGen = new HashMap<>();
        Map<String, Integer> countsAvg = new HashMap<>();
        Map<String, Integer> countsRep = new HashMap<>();
        Map<String, Integer> countsSpc = new HashMap<>();
        Map<String, Integer> countsTem = new HashMap<>();

        result.put("ALL", countsAll);
        result.put(ProcessSubType.GENERIC_DATASET.getValue(), countsGen);
        result.put(ProcessSubType.AVERAGE_DATASET.getValue(), countsAvg);
        result.put(ProcessSubType.REPRESENTATIVE_DATASET.getValue(), countsRep);
        result.put(ProcessSubType.SPECIFIC_DATASET.getValue(), countsSpc);
        result.put(ProcessSubType.TEMPLATE_DATASET.getValue(), countsTem);

        Categories processCategories = (Categories) cs.getCategories().get(0);

        List<CategoryType> cats = processCategories.getCategory();

        CommonDataStockDao dsDao = new CommonDataStockDao();

        AbstractDataStock stock = dsDao.getDataStockByIdentifier(datastock);

        if (stock == null)
            throw new DatastockNotFoundException("Datastock " + datastock + " does not exist");

        ProcessDao pDao = new ProcessDao();

        for (CategoryType cat : cats) {
            retrieveCounts(result, cs.getName(), cat, new ArrayList<>(), pDao, new IDataStockMetaData[]{stock});
        }

        return result;
    }

    private void retrieveCounts(Map<String, Map<String, Integer>> counts, String categorySystem, CategoryType category, List<String> categories, ProcessDao pDao, IDataStockMetaData[] stocks) {
        categories.add(category.getName());
        Integer count = (int) pDao.getNumberByClass(categorySystem, categories, true, null, stocks, null, true, true);
        counts.get("ALL").put(category.getId(), count);

        for (ProcessSubType subType : ProcessSubType.values()) {
            Integer typeCount = (int) pDao.getNumberByClass(categorySystem, categories, true, makeParams(subType), stocks, null, true, true);
            counts.get(subType.getValue()).put(category.getId(), typeCount);
        }

        if (!category.getCategory().isEmpty()) {
            for (CategoryType cat : category.getCategory()) {
                retrieveCounts(counts, categorySystem, cat, new ArrayList<>(categories), pDao, stocks);
            }
        }
    }

    private ValueParser makeParams(ProcessSubType subType) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("subType", convertSubTypeToDbString(subType));
        return new ValueParser(paramMap);
    }

    private String convertSubTypeToDbString(ProcessSubType subType) {
        return subType.getValue().toUpperCase().replaceAll(" ", "_");
    }

    private Response getDefinitionXML(final de.fzk.iai.ilcd.api.app.categories.CategorySystem cs, Date lastModified) {
        StreamingOutput stream = out -> {

            CategoriesHelper ch = new CategoriesHelper();

            try {
                ch.marshal(cs, out);
            } catch (JAXBException e) {
                throw new IOException(e);
            }
        };
        return Response.ok(stream).type(MediaType.APPLICATION_XML).lastModified(lastModified).build();
    }

    private Response getDefinitionXLS(final String catSystemName, final Map<String, de.fzk.iai.ilcd.api.app.categories.CategorySystem> catsMap, String language, final String stockName, final Map<String, Map<String, Integer>> counts) {

        final CategoriesXLSConverter xc = new CategoriesXLSConverter();

        ResourceBundle resourceBundle = new SodaResourceBundle(language);

        StreamingOutput stream = out -> xc.generateXLS(catSystemName, catsMap, (counts != null), counts, stockName, resourceBundle, out);
        String fileNameSuffix = resourceBundle.getString("admin.categories.categorydefinitions.categories");
        String fileName = catSystemName + "_" + fileNameSuffix + ".xlsx";

        return Response.ok(stream).header("Content-Disposition", "attachment; filename=" + fileName).type(MediaType.APPLICATION_OCTET_STREAM)
                .build();
    }

    private void translate(de.fzk.iai.ilcd.api.app.categories.CategorySystem cs, String language) {
        CategoryTranslator t = new CategoryTranslator(DataSetType.PROCESS, cs.getName());

        for (CategoriesType cats : cs.getCategories()) {
            for (CategoryType cat : cats.getCategory()) {
                translate((Category) cat, t, language);
            }
        }
    }

    private void translate(de.fzk.iai.ilcd.api.app.categories.Category category, CategoryTranslator t, String language) {
        category.setName(t.translateTo(category.getId(), language));
        for (CategoryType subCat : category.getCategory()) {
            subCat.setName(t.translateTo(subCat.getId(), language));
            translate((Category) subCat, t, language);
        }
    }
}
