package com.thoughtworks.ebikecharger.model;

import static com.thoughtworks.ebikecharger.MainWorld.HOUR_AS_MILLIS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Answers.CALLS_REAL_METHODS;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChargerTest {

  @Mock(answer = CALLS_REAL_METHODS)
  private Charger charger;

  @Test
  public void when_generate_knots_should_return_empty_list_given_gap_9() {
    // when
    List<Integer> knots = charger.generateEnergyKnots(9 * HOUR_AS_MILLIS, 0);

    // then
    assertEquals(0, knots.size());
  }

  @Test
  public void when_generate_knots_should_return_half_empty_list_given_gap_8_6() {
    // when
    List<Integer> knots = charger.generateEnergyKnots((long) (8.6 * HOUR_AS_MILLIS), 0);

    // then
    assertEquals(List.of(10, 10, 10, 10, 0, 0, 0, 0, 0, 0), knots);
  }

  @Test
  public void when_generate_knots_should_return_full_list_given_gap_7() {
    // when
    List<Integer> knots = charger.generateEnergyKnots(7 * HOUR_AS_MILLIS, 0);

    // then
    assertEquals(List.of(10, 10, 10, 10, 10, 10, 10, 10, 10, 10), knots);
  }

}
