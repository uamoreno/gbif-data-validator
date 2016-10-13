package org.gbif.occurrence.validation.evaluator;

import org.gbif.occurrence.validation.api.RecordEvaluator;
import org.gbif.occurrence.validation.api.model.EvaluationType;
import org.gbif.occurrence.validation.api.model.RecordEvaluationResult;

import javax.annotation.Nullable;

/**
 * Class to evaluate the structure of a record.
 */
public class RecordStructureEvaluator implements RecordEvaluator {

  private static int expectedColumnCount;

  public RecordStructureEvaluator(String[] fields) {
    expectedColumnCount = fields.length;
  }

  @Override
  public RecordEvaluationResult evaluate(
          @Nullable Long lineNumber, String[] record) {
    if (record.length != expectedColumnCount) {
      return toColumnCountMismatchResult(lineNumber, expectedColumnCount, record.length);
    }
    return null;
  }

  /**
   * Creates a RecordStructureEvaluationResult instance for a column count mismatch.
   *
   * @param lineNumber
   * @param expectedColumnCount
   * @param actualColumnCount
   * @return
   */
  private static RecordEvaluationResult toColumnCountMismatchResult(Long lineNumber, int expectedColumnCount,
                                                                             int actualColumnCount) {
    return new RecordEvaluationResult.Builder()
            .withLineNumber(lineNumber)
            .addBaseDetail(EvaluationType.COLUMN_MISMATCH, Integer.toString(expectedColumnCount),
                    Integer.toString(actualColumnCount),null).build();
  }
}