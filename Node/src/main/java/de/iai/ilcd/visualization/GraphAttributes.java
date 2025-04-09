package de.iai.ilcd.visualization;

import guru.nidi.graphviz.attribute.Color;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author MK
 * @since soda4LCA 5.9.0
 */
public abstract class GraphAttributes {

    public Palette palette = Palette.DEFAULT;
    public Spline spline = Spline.ortho;
    public RankDIR rankDIR = RankDIR.LR;
    public ArrowHeadShape arrowHeadShape = ArrowHeadShape.empty;
    public ArrowLineStyle arrowLineStyle = ArrowLineStyle.dashed;
    public int imageWidth = 2048,
            nodeSeperate = 2,
            rankSeperate = 4,
            canvasPadding = 2,
            descriptionWidth = 200,
            fontSizeProcessUUID = 10,
            fontSizeFlowUUID = 9;

    public void setImageWidth(int imageWidth) {
        if (imageWidth != 0)
            this.imageWidth = imageWidth;
    }

    public void setNodeSeperate(int nodeSeperate) {
        if (nodeSeperate != 0)
            this.nodeSeperate = nodeSeperate;
    }

    public void setRankSeperate(int rankSeperate) {
        if (rankSeperate != 0)
            this.rankSeperate = rankSeperate;
    }

    public void setCanvasPadding(int canvasPadding) {
        if (canvasPadding != 0)
            this.canvasPadding = canvasPadding;
    }

    public void setFontSizeProcessUUID(int fontSizeProcessUUID) {
        if (fontSizeProcessUUID != 0)
            this.fontSizeProcessUUID = fontSizeProcessUUID;
    }

    public void setDescriptionWidth(int descriptionWidth) {
        if (descriptionWidth != 0)
            this.descriptionWidth = descriptionWidth;
    }

    public void setFontSizeFlowUUID(int fontSizeFlowUUID) {
        if (fontSizeFlowUUID != 0)
            this.fontSizeFlowUUID = fontSizeFlowUUID;
    }

    public void setPalette(@Nullable String palette) {
        this.palette = safeValueOf(Palette.class, palette);
    }

    public void setSpline(@Nullable String spline) {
        this.spline = safeValueOf(Spline.class, spline);
    }

    public void setRank(@Nullable String rank) {
        this.rankDIR = safeValueOf(RankDIR.class, rank);
    }

    public void setArrowHeadShape(@Nullable String arrowHeadShape) {
        this.arrowHeadShape = safeValueOf(ArrowHeadShape.class, arrowHeadShape);
    }

    public void setArrowLineStyle(@Nullable String arrowLineStyle) {
        this.arrowLineStyle = safeValueOf(ArrowLineStyle.class, arrowLineStyle);
    }

    /**
     * Better Enum.valueOf(), handles case sensitivity and nulls.
     * <p>
     * Falls back to the first constant in the given enum if provide value is
     * null or not found.
     *
     * @param <E>
     * @param clazz the enum you are picking from
     * @param val   the value you are looking for
     * @return guaranteed to return an Enum constant, if provided with a non-empty enum.
     */
    private <E extends Enum<E>> E safeValueOf(Class<E> clazz, String val) {
        if (val != null)
            for (E c : clazz.getEnumConstants())
                if (c.name().equalsIgnoreCase(val))
                    return c;

        return clazz.getEnumConstants()[0]; // fallback to first constant
    }

    public enum Palette {

        DEFAULT(
                Color.TOMATO,
                Color.DARKORCHID,
                // Color.VIOLETRED2,
                Color.ALICEBLUE,
                Color.PINK3,
                Color.CORNSILK,
                Color.CYAN,
                Color.YELLOW2,
                Color.LAVENDER,
                Color.PEACHPUFF4,
                Color.TURQUOISE
        ),
        FANCY(
                Color.rgb("#2E86AB"), // Blue NCS
//				Color.rgb("#A23B72"), // Maximum Red Purple
                Color.rgb("#F42A45"), // Red Munsell
                Color.rgb("#F18F01"), // Presidential Orange
                Color.rgb("#C73E1D"), // Vermilion
                Color.rgb("#3B1F2B"), // Dark Purple
                Color.rgb("#E6E6E6"), // Platinum
                Color.rgb("#BAE052"), // June Bud
                Color.rgb("#EDF7B5"), // Pale Spring Bud
                Color.rgb("#89CE94"), // Eton Blue
                Color.rgb("#FFA5AB") // Light Pink
        ),
        BLIND(
                Color.rgb("#5454A2"), // Liberty
                Color.rgb("#757464"), // Nickel
                Color.rgb("#C6C523"), // Acid Green
                Color.rgb("#8B8A24"), // Olive
                Color.rgb("#2E2E28"), // Jet
                Color.rgb("#E6E6E6"), // Platinum (colorblind friendly)
                Color.rgb("#CACA74"), // Dark Khaki
                Color.rgb("#F1F1C4"), // Pale Spring Bud (colorblind friendly)
                Color.rgb("#A6A7A2"), // Quick Silver
                Color.rgb("#474864") // Independence
        );

        private static Map<String, Color> colorHistory = new HashMap<>();
        private static int wheelPosition = 0;
        private final List<Color> colorWheel;

        private Palette(Color... wheel) {
            this.colorWheel = Arrays.asList(wheel);
        }

        public String pick(String UUID) {
            if (colorHistory.containsKey(UUID))
                return colorHistory.get(UUID).value;

            wheelPosition = wheelPosition % this.colorWheel.size();
            Color picked = this.colorWheel.get(wheelPosition++);
            colorHistory.put(UUID, picked);
            return picked.value;
        }

        public String get(int position) {
            return this.colorWheel.get(position).value;
        }

        public int size() {
            return this.colorWheel.size();
        }

        public void reset() {
            colorHistory.clear();
            wheelPosition = 0;
        }
    }

    /**
     * @see https://graphviz.org/doc/info/attrs.html#d:splines
     */
    public enum Spline {
        ortho,
        line,
        curved
    }

    /**
     * @see https://graphviz.org/doc/info/attrs.html#k:rankdir
     */
    public enum RankDIR {
        LR,
        TB,
        BT,
        RL
    }

    /**
     * @see https://graphviz.org/doc/info/attrs.html#k:arrowType
     */
    public enum ArrowHeadShape {
        normal,
        dot,
        empty,
        open,
        vee,
        halfopen
    }

    /**
     * @see https://graphviz.org/doc/info/attrs.html#k:style
     */
    public enum ArrowLineStyle {
        solid,
        dashed,
        dotted,
        bold
    }
}
