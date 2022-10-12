package com.thoughtworks.ebikecharger;

import java.io.Serializable;
import java.util.List;

public class EnergyMessage extends Message implements Serializable {

  private List<Integer> energyKnots;

  public List<Integer> getEnergyKnots() {
    return energyKnots;
  }

  public void setEnergyKnots(List<Integer> energyKnots) {
    this.energyKnots = energyKnots;
  }

  @Override
  public String toString() {
    return "EnergyMessage{" +
        "energyKnots=" + energyKnots +
        '}';
  }
}
