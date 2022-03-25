package org.opentripplanner.routing.algorithm.raptoradapter.transit.cost;


import org.opentripplanner.model.AccessibilityRequirements;

/**
 * Mutable version of the {@link McCostParams}.
 */
@SuppressWarnings("UnusedReturnValue")
public class McCostParamsBuilder {
    private int boardCost;
    private int transferCost;
    private double[] transitReluctanceFactors;
    private double waitReluctanceFactor;
    private AccessibilityRequirements accessibilityRequirements;

    public McCostParamsBuilder() {
        this(McCostParams.DEFAULTS);
    }

    private McCostParamsBuilder(McCostParams other) {
        this.boardCost = other.boardCost();
        this.transferCost = other.transferCost();
        this.transitReluctanceFactors = other.transitReluctanceFactors();
        this.waitReluctanceFactor = other.waitReluctanceFactor();
        this.accessibilityRequirements = other.accessibilityRequirements();
    }

    public int boardCost() {
        return boardCost;
    }

    public McCostParamsBuilder boardCost(int boardCost) {
        this.boardCost = boardCost;
        return this;
    }

    public int transferCost() {
        return transferCost;
    }

    public McCostParamsBuilder transferCost(int transferCost) {
        this.transferCost = transferCost;
        return this;
    }

    public double[] transitReluctanceFactors() {
        return transitReluctanceFactors;
    }

    public McCostParamsBuilder transitReluctanceFactors(double[] transitReluctanceFactors) {
        this.transitReluctanceFactors = transitReluctanceFactors;
        return this;
    }

    public double waitReluctanceFactor() {
        return waitReluctanceFactor;
    }

    public McCostParamsBuilder waitReluctanceFactor(double waitReluctanceFactor) {
        this.waitReluctanceFactor = waitReluctanceFactor;
        return this;
    }

    public AccessibilityRequirements accessibilityMode() {
        return accessibilityRequirements;
    }

    public McCostParamsBuilder accessibilityMode(AccessibilityRequirements mode) {
        accessibilityRequirements = mode;
        return this;
    }

    public McCostParams build() {
        return new McCostParams(this);
    }
}
