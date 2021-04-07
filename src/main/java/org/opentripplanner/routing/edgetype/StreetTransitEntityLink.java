package org.opentripplanner.routing.edgetype;

import java.util.Locale;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.opentripplanner.common.geometry.GeometryUtils;
import org.opentripplanner.model.Trip;
import org.opentripplanner.routing.api.request.RoutingRequest;
import org.opentripplanner.routing.core.CarPickupState;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.vertextype.StreetVertex;

/**
 * This represents the connection between a street vertex and a transit vertex.
 */
public abstract class StreetTransitEntityLink<T extends Vertex> extends Edge {

    private static final long serialVersionUID = -3311099256178798981L;
    static final int STEL_TRAVERSE_COST = 1;

    private final T transitEntityVertex;

    private final boolean wheelchairAccessible;

    public StreetTransitEntityLink(StreetVertex fromv, T tov, boolean wheelchairAccessible) {
    	super(fromv, tov);
    	this.transitEntityVertex = tov;
        this.wheelchairAccessible = wheelchairAccessible;
    }

    public StreetTransitEntityLink(T fromv, StreetVertex tov, boolean wheelchairAccessible) {
        super(fromv, tov);
        this.transitEntityVertex = fromv;
        this.wheelchairAccessible = wheelchairAccessible;
    }

    protected abstract int getStreetToStopTime();

    protected T getTransitEntityVertex() {
        return transitEntityVertex;
    }

    public String getDirection() {
        return null;
    }

    public double getDistanceMeters() {
        return 0;
    }

    public LineString getGeometry() {
        Coordinate[] coordinates = new Coordinate[] { fromv.getCoordinate(), tov.getCoordinate()};
        return GeometryUtils.getGeometryFactory().createLineString(coordinates);
    }

    public String getName() {
        return this.transitEntityVertex.getName();
    }

    public String getName(Locale locale) {
        //TODO: localize
        return getName();
    }

    public State traverse(State s0) {

        // Forbid taking shortcuts composed of two street-transit links associated with the same stop in a row. Also
        // avoids spurious leg transitions. As noted in https://github.com/opentripplanner/OpenTripPlanner/issues/2815,
        // it is possible that two stops can have the same GPS coordinate thus creating a possibility for a
        // legitimate StreetTransitLink > StreetTransitLink sequence, so only forbid two StreetTransitLinks to be taken
        // if they are for the same stop.
        if (
            s0.backEdge instanceof StreetTransitEntityLink &&
                ((StreetTransitEntityLink<?>) s0.backEdge).transitEntityVertex
                    == this.transitEntityVertex
        ) {
            return null;
        }

        RoutingRequest req = s0.getOptions();
        if (s0.getOptions().wheelchairAccessible && !wheelchairAccessible) {
            return null;
        }

        if (s0.getOptions().bikeParkAndRide && !s0.isBikeParked()) {
            // Forbid taking your own bike in the station if bike P+R activated.
            return null;
        }
        if (s0.isBikeRenting()) {
            // Forbid taking a rented bike on any transit.
            // TODO Check this condition, does this always make sense?
            return null;
        }

        // Do not check here whether any transit modes are selected. A check for the presence of
        // transit modes will instead be done in the following PreBoard edge.
        // This allows searching for nearby transit stops using walk-only options.
        StateEditor s1 = s0.edit(this);

        /* Only enter stations in CAR mode if parking is not required (kiss and ride) */
        /* Note that in arriveBy searches this is double-traversing link edges to fork the state into both WALK and CAR mode. This is an insane hack. */
        if (s0.getNonTransitMode() == TraverseMode.CAR) {
            if (req.carPickup && s0.getCarPickupState() == CarPickupState.IN_CAR) {
                s1.setTaxiState(s0.getOptions().arriveBy ? CarPickupState.WALK_TO_PICKUP : CarPickupState.WALK_FROM_DROP_OFF);
            }
        }

        // We do not increase the time here, so that searching from the stop coordinates instead of
        // the stop id catch transit departing at that exact search time.
        int streetToStopTime = getStreetToStopTime();
        s1.incrementTimeInSeconds(streetToStopTime);
        s1.incrementWeight(STEL_TRAVERSE_COST + streetToStopTime);
        return s1.makeState();
    }

    public State optimisticTraverse(State s0) {
        StateEditor s1 = s0.edit(this);
        s1.incrementWeight(STEL_TRAVERSE_COST);
        return s1.makeState();
    }
    
    // anecdotally, the lower bound search is about 2x faster when you don't reach stops
    // and therefore don't even consider boarding
    @Override
    public double weightLowerBound(RoutingRequest options) {
        return options.transitAllowed() ? 0 : Double.POSITIVE_INFINITY;
    }

    public Vertex getFromVertex() {
        return fromv;
    }

    public Vertex getToVertex() {
        return tov;
    }

    public Trip getTrip() {
        return null;
    }

    public boolean isRoundabout() {
        return false;
    }

    public String toString() {
        return "StreetTransitLink(" + fromv + " -> " + tov + ")";
    }


}
