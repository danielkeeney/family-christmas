package org.dkeeney.injector;

import com.google.inject.AbstractModule;
import org.joda.time.DateTime;

import java.util.Random;

public class RandomModule extends AbstractModule{
  @Override
  protected void configure() {
    bind(Random.class).toInstance(getConfiguredRandom());
  }

  public Random getConfiguredRandom() {
    return new Random(DateTime.now().getYear());
  }
}
