package de.iai.ilcd.webgui.controller.util;

/**
 * <ul>
 * <li>ALL: all datasets (including any previous versions)</li>
 *
 * <li>LATEST_ONLY: the latest version of all processes, and everything else
 * (including any previous versions of non-process datasets)</li>
 *
 * <li>LATEST_ONLY_GLOBAL: only the latest versions of all datasets</li>
 * </ul>
 *
 * <i>Simply, Should previous versions be included?</i>
 *
 * <table>
 * <thead>
 * <tr>
 * <th>Export Mode</th>
 * <th>Process</th>
 * <th>Other Datasets</th>
 * </tr>
 * </thead> <tbody>
 * <tr>
 * <td>ALL</td>
 * <td>YES</td>
 * <td>YES</td>
 * </tr>
 * <tr>
 * <td>LATEST_ONLY</td>
 * <td>NO</td>
 * <td>YES</td>
 * </tr>
 * <tr>
 * <td>LATEST_ONLY (GLOBAL)</td>
 * <td>NO</td>
 * <td>NO</td>
 * </tr>
 * </tbody>
 * </table>
 *
 *
 *
 * <i>Should versions number be part of filename?</i>
 *
 * <table>
 * <thead>
 * <tr>
 * <th>Export Mode</th>
 * <th>Process</th>
 * <th>Other Datasets</th>
 * </tr>
 * </thead> <tbody>
 * <tr>
 * <td>ALL</td>
 * <td>ALWAYS</td>
 * <td>ALWAYS</td>
 * </tr>
 * <tr>
 * <td>LATEST_ONLY</td>
 * <td>NO</td>
 * <td>Older versions only</td>
 * </tr>
 * <tr>
 * <td>LATEST_ONLY (GLOBAL)</td>
 * <td>NO</td>
 * <td>NO</td>
 * </tr>
 * </tbody>
 * </table>
 */

public enum ExportMode {
    ALL(0), LATEST_ONLY(1), LATEST_ONLY_GLOBAL(2);

    private int value;

    ExportMode(int value) {
        this.value = value;
    }

    public static ExportMode fromValue(int value) {
        for (ExportMode enumValue : ExportMode.values()) {
            if (enumValue.getValue() == value) {
                return enumValue;
            }
        }
        return null;
    }

    public int getValue() {
        return this.value;
    }

    public String toString() {
        switch (this.value) {
            case 0:
                return "ALL";
            case 1:
                return "LATEST_ONLY";
            case 2:
                return "LATEST_ONLY_GLOBAL";
            default:
                return Integer.toString(this.value);
        }
    }
}