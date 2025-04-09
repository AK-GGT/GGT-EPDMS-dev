package de.iai.ilcd.visualization;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.model.lifecyclemodel.DownstreamProcess;
import de.iai.ilcd.model.lifecyclemodel.LifeCycleModel;
import de.iai.ilcd.model.lifecyclemodel.OutputExchange;
import de.iai.ilcd.model.lifecyclemodel.ProcessInstance;
import de.iai.ilcd.model.process.Exchange;
import de.iai.ilcd.persistence.PersistenceUtil;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Font;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.util.HtmlUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Map;

import static guru.nidi.graphviz.model.Factory.*;

/**
 * @author MK
 * @since soda4LCA 5.9.0
 */

public class LCMviz extends GraphAttributes {

    private static final Logger LOGGER = LogManager.getLogger(LCMviz.class);

    private MutableGraph graph;
    private LifeCycleModel lcm;

    public LCMviz(LifeCycleModel lcm) {
        this.lcm = lcm;
    }

    public String drawSVG() {
        this.graph = drawGraph();
        return Graphviz.fromGraph(this.graph).width(this.imageWidth).render(Format.SVG).toString();
    }

    private MutableGraph drawGraph() {
        MutableGraph g = mutGraph("LifeCycleModel - " + lcm.getUuidAsString()).setDirected(true);
        g.graphAttrs().add(Color.WHEAT.gradient(Color.CADETBLUE2).background().angle(90)); // Color.rgb("888888")
//		g.graphAttrs().add(Rank.dir(RankDir.LEFT_TO_RIGHT)); //rankdir: "LR"
        g.graphAttrs().add("rankdir", this.rankDIR.name());
        g.graphAttrs().add("nodesep", this.nodeSeperate);
        g.graphAttrs().add("ranksep", this.rankSeperate);
        g.graphAttrs().add("pad", this.canvasPadding);
//		g.graphAttrs().add("landscape","true");		
//		g.graphAttrs().add("rank", "max");
        g.graphAttrs().add("splines", this.spline.name());

        for (ProcessInstance sourceInstance : lcm.getProcesses()) {

            String src_id = String.valueOf(sourceInstance.getDataSetInternalID()),
                    src_uuid = sourceInstance.getReferenceToProcess().getRefObjectId(),
                    src_desc = sourceInstance.getReferenceToProcess().getShortDescription().getDefaultValue();

            for (OutputExchange exchange : sourceInstance.getConnections()) {

                for (DownstreamProcess downstreamProcess : exchange.getDownstreamProcesses()) {

                    ProcessInstance targetInstance = lcm
                            .getProcessInstanceByID(downstreamProcess.getInternaldownstreamprocess_id());

                    String dest_id = String.valueOf(targetInstance.getDataSetInternalID()),
                            dest_uuid = targetInstance.getReferenceToProcess().getRefObjectId(),
                            dest_desc = targetInstance.getReferenceToProcess().getShortDescription().getDefaultValue();


                    // Some characters like '>' breaks HTML rendering
                    src_desc = HtmlUtils.htmlEscape(src_desc);
                    dest_desc = HtmlUtils.htmlEscape(dest_desc);

                    // Warp description field to reduce it's width
                    src_desc = wrapText(src_desc, this.descriptionWidth, "<br/>");
                    dest_desc = wrapText(dest_desc, this.descriptionWidth, "<br/>");

                    // Manipulate font settings to reduce UUID's size
                    src_uuid = decorate(src_uuid, "font", String.format("point-size='%d'", this.fontSizeProcessUUID));
                    dest_uuid = decorate(dest_uuid, "font", String.format("point-size='%d'", this.fontSizeProcessUUID));

                    // Generate node's label as an HTML table
                    String src_label = htmlTable(src_desc, src_uuid, sourceInstance.getParameters());
                    String dest_label = htmlTable(dest_desc, dest_uuid, targetInstance.getParameters());

                    // MutableNode x = mutNode(src_id).add(Records.mOf(rec(src_id, src_desc), rec(src_id, Label.html(src_uuid))));
                    MutableNode x = mutNode(src_id).add(Label.html(src_label)).add(Shape.PLAIN);
                    // x.add("fillcolor", pickColor(src_uuid)).add("style", "filled");

                    x.add("tooltip", "version: " + sourceInstance.getReferenceToProcess().getVersionAsString()); // appears on mouse hover in SVGs
                    x.add("URL", generateProcessURL(sourceInstance));
                    // MutableNode y = mutNode(dest_id).add(Records.mOf(rec(dest_id, dest_desc), rec(dest_id, dest_uuid)));
                    MutableNode y = mutNode(dest_id).add(Label.html(dest_label)).add(Shape.PLAIN);

                    y.add("tooltip", "version: " + targetInstance.getReferenceToProcess().getVersionAsString()); // appears on mouse hover in SVGs
                    y.add("URL", generateProcessURL(targetInstance)); // clickable node

                    // y.add("fillcolor", pickColor(dest_uuid)).add("style", "filled");

                    // x.add("fixedsize", "true");
                    // x.add("width", "5");
                    // x.add("height", "5");
                    //
                    // y.add("fixedsize", "true");
                    // y.add("width", "5");
                    // y.add("height", "5");

                    String arrowtail = exchange.getFlow(), arrowhead = downstreamProcess.getFlow();


                    arrowtail = arrowtail + calcFlowQuantity(sourceInstance, exchange.getFlow());
                    arrowhead = arrowhead + calcFlowQuantity(targetInstance, downstreamProcess.getFlow());
                    MutableNode node = x.addLink(to(y)
//							.with(Arrow.EMPTY) // arrow head shape
                            .with("arrowhead", this.arrowHeadShape.name()) // arrow head shape
//							.with(Style.DASHED) // arrow dashed
                            .with("style", this.arrowLineStyle.name())
                            //.with("URL", "http://okworx.com") // clickable edge TODO: point to flow or exchange
                            .with(Label.of(arrowhead).head())
                            .with(Label.of(arrowtail).tail())
                            .with(Font.size(this.fontSizeFlowUUID))); // font size of labels near both ends of arrow (head & tail)

                    g.add(node);

                }
            }
        }

        this.palette.reset();
        return g;
    }

    /**
     * Fetch an Exchange from database to get relevant flow property name (Unit) and value of the flow
     * which gets multipled by the Multiplicative Factor of the ProcessInstance
     * <p>
     * Flow value * Multiplicative Factor (Flow Unit)
     *
     * @param pi       the ProcessInstance to get uuid, version and multiplicative factor
     * @param flowUUID as a string
     * @return Flow value * Multiplicative Factor (Flow Unit) in scientific notation
     */
    private String calcFlowQuantity(ProcessInstance pi, String flowUUID) {
        EntityManager em = PersistenceUtil.getEntityManager();

        Exchange e;
        try {
            Query q = em.createQuery(
                    "SELECT e FROM Process p JOIN p.exchanges e WHERE e.flow.uuid.uuid = :fuuid AND p.uuid.uuid = :puuid AND p.version = :ver");
            q.setParameter("puuid", pi.getReferenceToProcess().getRefObjectId());
            q.setParameter("ver", pi.getReferenceToProcess().getVersion());
            q.setParameter("fuuid", flowUUID);
            q.setMaxResults(1);
            e = (Exchange) q.getSingleResult();

        } catch (Exception x) {
            LOGGER.error(x.getMessage());
            return null;
        }
        return String.format(" [%2.3e %s]", e.getResultingAmount() * pi.getMultiplicationFactor(), e.getUnit());
    }


    /**
     * Breaking a section of text into lines so that it will fit into the
     * available width of a node
     *
     * @param text  you want to break into pieces
     * @param width number of character to look for a space after to append the <code>sep</code> string
     * @param sep   expected to be either <b>\n</b> or <b>&ltbr/&gt</b>
     * @return given text broken into multiple chunks, separated by given <code>sep</code>
     */
    private String wrapText(String text, int width, String sep) {
        int w = 0;
        String[] tokens = text.split(" ");
        for (int i = 0; i < tokens.length; i++) {
            w += tokens[i].length();
            if (w > width) {
                tokens[i] += sep;
                w = 0;
            }
        }
        return String.join(" ", tokens);

    }


    /**
     * Wraps a given text with a opening and closing HTML tag
     *
     * @param payload    text itself you want to wrap
     * @param tag        only HTML-Like Labels that
     *                   not to be mistaken with regular HTML tags
     * @param attributes (optional) you can add to the opening tag
     * @return payload wrapped in a Graphviz friendly HTML syntax.
     * @see [https://graphviz.org/doc/info/shapes.html#html]
     */
    private String decorate(String payload, String tag, String... attributes) {
        String attrs = String.join(" ", attributes);
        return String.format("<%s %s>%s</%s>", tag, attrs, payload, tag);
    }


    /**
     * Generates a HTML table in following format:
     *
     *
     * <table border='1'>
     * <tr>
     * <td colspan='3'>Description</td>
     * </tr>
     * <tr>
     * <td colspan='3'>UUID</td>
     * </tr>
     * <tr>
     * <td rowspan='3'>parameters</td>
     * <td>key1</td>
     * <td>value1</td>
     * </tr>
     * <tr>
     * <td>key2</td>
     * <td>value2</td>
     * <tr>
     * <td>key3</td>
     * <td>value3</td>
     * </tr>
     * </table>
     *
     * @param description top field content
     * @param uuid        middle field content
     * @param parameters  provide null if you don't have one
     * @return payload wrapped in a Graphviz friendly HTML table.
     */
    private String htmlTable(String description, String uuid, Map<String, Double> parameters) {


//		+-------------+-------------------+--------+
//		| description |                   |        |
//		| uuid        |                   |        |
//		+-------------+-------------------+--------+
//		|             |  key1             | value1 |
//		| parameters  |  key2             | value2 |
//		|             |  key3             | value3 |
//		+-------------+-------------------+--------+


//		+-------------+-------------------+--------+
//		|              description                 |
//		|                  uuid                    |
//		+-------------+-------------------+--------+
//		|             |  key1             | value1 |
//		| parameters  |  key2             | value2 |
//		|             |  key3             | value3 |
//		+-------------+-------------------+--------+

        String payload = String.format(
                "<tr>"
                        + "<td COLSPAN='3'>%s</td>"
                        + "</tr>"
                        + "<hr/>"
                        + "<tr>"
                        + "<td COLSPAN='3'>%s</td>"
                        + "</tr>", description, uuid);
        String appendix = "";
        if (parameters != null && parameters.size() > 0) {
            String row = String.format("<td rowspan='%d'><i>parameters</i></td>", parameters.size());
            for (String k : parameters.keySet()) {
                row += decorate(decorate(k, "b"), "td"); // <td><b>key</b></td>
                row += decorate(decorate(String.valueOf(parameters.get(k)), "u"), "td"); // <td><u>value</u></td>

//				 <tr>
//					[<td rowspan='n'>parameters</td>]
//				 	<td>key</td>
//				 	<td>value</td>
//				 <tr>
                row = decorate(row, "tr");
                appendix += row;
                row = "";
            }
        }
        payload += appendix;

//		return String.format(
//				"<table border='1' cellborder='0' bgcolor='%s'>"
//					+ "<tr><td>%s</td></tr>"
//					+ "<hr/>"
//					+ "<tr><td>%s</td></tr>"
//				+ "</table>",
//				pickColor(uuid), description, uuid);

        return decorate(payload, "table", "border='1'", "cellborder='1'", "bgcolor='" + this.palette.pick(uuid) + "'");
    }


    private String generateProcessURL(ProcessInstance p) {
        // TODO: maybe ask URLGeneratorBean instead?
        GlobalReference ref = p.getReferenceToProcess();
        return HtmlUtils.htmlEscape(String.format(ConfigurationService.INSTANCE.getContextPath() + "/datasetdetail/process.xhtml?uuid=%s&version=%s", ref.getRefObjectId(), ref.getVersion()));
    }

}
