package org.opentripplanner.transit.raptor.rangeraptor.internalapi;

import javax.annotation.Nullable;

/**
 * The heuristics are used in the multi-criteria search and can be generated using the standard
 * search. This interface decouple these two implementations and make it possible to implement more
 * than one heuristic data provider.
 */
public interface Heuristics {
  /**
   * Is the stop reached by the heuristic search?
   */
  boolean reached(int stop);

  /**
   * The best overall travel duration from origin to the given stop.
   */
  int bestTravelDuration(int stop);

  /**
   * To plot or debug the travel duration.
   *
   * @param unreached set all unreached values to this value
   */
  int[] bestTravelDurationToIntArray(int unreached);

  /**
   * The best number of transfers to reach the given stop.
   */
  int bestNumOfTransfers(int stop);

  /**
   * To plot or debug the number of transfers.
   *
   * @param unreached set all unreached values to this value
   */
  int[] bestNumOfTransfersToIntArray(int unreached);

  /**
   * The best overall generalized cost from origin to the given stop.
   */
  int bestGeneralizedCost(int stop);

  /**
   * To plot or debug the generalized cost.
   *
   * @param unreached set all unreached values to this value
   */
  int[] bestGeneralizedCostToIntArray(int unreached);

  /**
   * The heuristic from the origin to the given stop. Returns {@link HeuristicAtStop#UNREACHED} if
   * the stop is not reached.
   */
  @Nullable
  HeuristicAtStop createHeuristicAtStop(int stop);

  /**
   * The number of stops in the heuristics. This includes all stops also stops not reached.
   */
  int size();

  /**
   * Return the best/minimum required time to travel from origin to destination.
   */
  int bestOverallJourneyTravelDuration();

  /**
   * Return the best/minimum required number of transfers from origin to destination.
   * <p>
   * Currently unused, but useful for debugging.
   */
  @SuppressWarnings("unused")
  int bestOverallJourneyNumOfTransfers();

  /**
   * Return an estimate for the shortest possible wait-time needed across all journeys reaching the
   * destination given the iteration-start-time. Note! This can NOT be used for destination pruning,
   * because there most likely exist journeys with a lower wait-time. The access is NOT time-shift
   * before computing this value.
   *
   * <p>This would be suitable for calculating a search-time-window, because it gives an estimate
   * for the expected wait-time. In a low frequency transit area the wait-time might be much larger
   * than the {@link #bestOverallJourneyTravelDuration()}, and in these cases this gives a better
   * starting point for the search-time-window calculation.
   */
  int minWaitTimeForJourneysReachingDestination();

  /**
   * Return true if the destination is reached.
   */
  boolean destinationReached();
}
