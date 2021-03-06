package org.gbif.validation.util;

import java.time.LocalDateTime;

/**
 * Indicates that a class can do cleanup to release persisted resources.
 */
public interface Cleanable<T> {

  /**
   * clean resources by eky
   *
   * @param key
   */
  void cleanByKey(T key);

  /**
   * Trigger a cleanup of persisted resources until the provided {@link LocalDateTime}
   *
   * @param dateTimeLimit
   */
  void cleanUntil(LocalDateTime dateTimeLimit);
}
