package org.opentripplanner.routing.api.request;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opentripplanner.model.base.ToStringBuilder;
import org.opentripplanner.model.modes.AllowedTransitMode;

public class RequestModes {

  public static RequestModes defaultRequestModes = new RequestModes(
    StreetMode.WALK,
    StreetMode.WALK,
    StreetMode.WALK,
    StreetMode.WALK,
    new HashSet<>(AllowedTransitMode.getAllTransitModes())
  );

  @Nonnull
  public StreetMode accessMode;

  @Nonnull
  public StreetMode transferMode;

  @Nonnull
  public StreetMode egressMode;

  @Nonnull
  public StreetMode directMode;

  @Nonnull
  public Set<AllowedTransitMode> transitModes;

  public RequestModes(
    StreetMode accessMode,
    StreetMode transferMode,
    StreetMode egressMode,
    StreetMode directMode,
    Collection<AllowedTransitMode> transitModes
  ) {
    this.accessMode = (accessMode != null && accessMode.access) ? accessMode : StreetMode.NOT_SET;
    this.transferMode =
      (transferMode != null && transferMode.transfer) ? transferMode : StreetMode.NOT_SET;
    this.egressMode = (egressMode != null && egressMode.egress) ? egressMode : StreetMode.NOT_SET;
    this.directMode = directMode != null ? directMode : StreetMode.NOT_SET;
    this.transitModes = transitModes != null ? new HashSet<>(transitModes) : Set.of();
  }

  public boolean contains(StreetMode streetMode) {
    return (
      streetMode.equals(accessMode) ||
      streetMode.equals(egressMode) ||
      streetMode.equals(directMode)
    );
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    RequestModes that = (RequestModes) o;

    if (accessMode != that.accessMode) return false;
    if (egressMode != that.egressMode) return false;
    if (directMode != that.directMode) return false;
    return transitModes.equals(that.transitModes);
  }

  @Override
  public String toString() {
    return ToStringBuilder
      .of(RequestModes.class)
      .addEnum("accessMode", accessMode)
      .addEnum("egressMode", egressMode)
      .addEnum("directMode", directMode)
      .addCol("transitModes", transitModes)
      .toString();
  }
}
