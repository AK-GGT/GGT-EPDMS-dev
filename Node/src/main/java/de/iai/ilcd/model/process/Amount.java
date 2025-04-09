package de.iai.ilcd.model.process;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Amount declaration with module description
 */
@Embeddable
public class Amount {

    /**
     * The value
     */
    @Basic
    @Column(name = "value")
    private Double value = null;

    /**
     * Phase
     */
    @Basic
    @Column(name = "phase")
    private String phase;

    /**
     * Module description
     */
    @Basic
    @Column(name = "module")
    private String module;

    /**
     * Scenario
     */
    @Basic
    @Column(name = "scenario")
    private String scenario;

    @Basic
    @Column(name = "scaled_value")
    private Double scaledValue;

    /**
     * Get the value
     *
     * @return value
     */
    public Double getValue() {
        return this.value;
    }

    /**
     * Set the value
     *
     * @param value value to set
     */
    public void setValue(Double value) {
        this.value = value;
    }

    /**
     * Get the module description
     *
     * @return module description
     */
    public String getModule() {
        return this.module;
    }

    /**
     * Set the module description
     *
     * @param module module description to set
     */
    public void setModule(String module) {
        this.module = module;
    }

    /**
     * Get the phase
     *
     * @return the phase
     */
    public String getPhase() {
        return this.phase;
    }

    /**
     * Set the phase
     *
     * @param phase the phase to set
     */
    public void setPhase(String phase) {
        this.phase = phase;
    }

    /**
     * Get the scenario
     *
     * @return the scenario
     */
    public String getScenario() {
        return this.scenario;
    }

    /**
     * Set the scenario
     *
     * @param scenario the scenario to set
     */
    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    /**
     * Get scaled value
     *
     * @return the scaled value
     */
    public Double getScaledValue() {
        return scaledValue;
    }

    /**
     * Set scaled value
     *
     * @param scaledValue The scaled value to set
     */
    public void setScaledValue(Double scaledValue) {
        this.scaledValue = scaledValue;
    }

    /**
     * Helper function to round and format the original value as String (using scientific notation if an exponent is
     * needed and without any trailing zeros)
     *
     * @return the rounded and formatted value as String
     */
    public String getRoundedAndFormattedValue() {
        // TODO: add DecimalFormat.format(...)
        return new BigDecimal(this.value, new MathContext(4)).stripTrailingZeros().toString();
    }

}
