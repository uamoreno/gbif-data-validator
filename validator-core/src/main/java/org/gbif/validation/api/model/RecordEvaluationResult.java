package org.gbif.validation.api.model;

import org.gbif.dwc.terms.Term;
import org.gbif.validation.api.result.EvaluationResultDetails;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Represents the result of an evaluation of a single record.
 * Immutable once built.
 */
public class RecordEvaluationResult implements Serializable {

  //currently stored inside the details
  private final String recordId;
  private final Map<Term, Object> interpretedData;
  private final List<EvaluationResultDetails> details;

  /**
   * Use {@link Builder} to get an instance.
   * @param recordId
   * @param details
   * @param interpretedData
   */
  private RecordEvaluationResult(String recordId, List<EvaluationResultDetails> details, Map<Term, Object> interpretedData){
    this.recordId = recordId;
    this.details = details;
    this.interpretedData = interpretedData;
  }

  public List<EvaluationResultDetails> getDetails(){
    return details;
  }

  /**
   *
   * @return suggested data or null
   */
  public Map<Term, Object> getInterpretedData() {
    return interpretedData;
  }

  @Override
  public String toString() {
    return "recordId: " + recordId + ", details: " + details;
  }

  /**
   * Builder class to build a RecordEvaluationResult instance.
   */
  public static class Builder {
    private Long lineNumber;
    private String recordId;
    private Map<Term, Object> interpretedData;
    private List<EvaluationResultDetails> details;

    /**
     * Merge 2 @{link RecordEvaluationResult} into a single one.
     * The @{link RecordEvaluationResult} are assumed to come from the same record therefore no validation is performed
     * on the recordId and the one from rer1 will be taken.
     * @param rer1
     * @param rer2
     * @return
     */
    public static RecordEvaluationResult merge(RecordEvaluationResult rer1, RecordEvaluationResult rer2) {
      // if something is null, try to return the other one, if both null, null is returned
      if(rer1 == null || rer2 == null) {
        return rer1 == null ? rer2 : rer1;
      }
      return new Builder()
              .withExisting(rer1)
              .addDetails(rer2.getDetails()).build();
    }

    public Builder withLineNumber(@Nullable Long lineNumber){
      this.lineNumber = lineNumber;
      return this;
    }

    public Builder withRecordId(@Nullable String recordId){
      this.recordId = recordId;
      return this;
    }

    public Builder withInterpretedData(Map<Term, Object> interpretedData) {
      this.interpretedData = interpretedData;
      return this;
    }

    public Builder withExisting(RecordEvaluationResult recordEvaluationResult){
      this.recordId = recordEvaluationResult.recordId;

      if(recordEvaluationResult.getDetails() != null){
        details = new ArrayList<>(recordEvaluationResult.getDetails());
      }
      return this;
    }

    /**
     * Internal operation to copy details.
     *
     * @param details
     * @return
     */
    private Builder addDetails(List<EvaluationResultDetails> details) {
      if(this.details == null){
        this.details = new ArrayList<>();
      }
      this.details.addAll(details);
      return this;
    }

    public Builder addInterpretationDetail(EvaluationType issueFlag, Map<Term, String> relatedData) {
      if(details == null){
        details = new ArrayList<>();
      }
      details.add(new RecordInterpretationResultDetails(lineNumber, recordId, issueFlag, relatedData));
      return this;
    }

    /**
     * Base detail is in the form of expected:found with an optional message but ideally we should be able to
     *
     * @param evaluationType
     * @param expected
     * @param found
     * @return
     */
    public Builder addBaseDetail(EvaluationType evaluationType, String expected, String found) {
      if(details == null){
        details = new ArrayList<>();
      }
      details.add(new BaseEvaluationResultDetails(lineNumber, recordId, evaluationType, expected, found));
      return this;
    }

    public RecordEvaluationResult build(){
      return new RecordEvaluationResult(recordId, details == null ? new ArrayList<>() : details, interpretedData);
    }
  }


  /**
   * Base evaluation result details with "expected" and "found".
   */
  public static class BaseEvaluationResultDetails implements EvaluationResultDetails {
    protected final Long lineNumber;
    protected final String recordId;
    protected final EvaluationType evaluationType;

    protected final String expected;
    protected final String found;

    BaseEvaluationResultDetails(Long lineNumber, String recordId, EvaluationType evaluationType){
      this(lineNumber, recordId, evaluationType, null, null);
    }

    BaseEvaluationResultDetails(Long lineNumber, String recordId, EvaluationType evaluationType,
                                String expected, String found){
      this.lineNumber = lineNumber;
      this.recordId = recordId;
      this.evaluationType = evaluationType;
      this.expected = expected;
      this.found = found;
    }

    public Long getLineNumber(){
      return lineNumber;
    }

    public String getRecordId() {
      return recordId;
    }

    public String getExpected() {
      return expected;
    }

    public String getFound() {
      return found;
    }


    @Override
    public EvaluationType getEvaluationType() {
      return evaluationType;
    }

    @Override
    public String toString() {
      return "evaluationType: " + evaluationType;
    }
  }


  /**
   * TODO decide if we should keep it
   * Evaluation result related to completeness.
   */
  public static class CompletenessEvaluationResultDetails extends BaseEvaluationResultDetails {
    private Term[] terms;

    CompletenessEvaluationResultDetails(Long lineNumber, String recordId, EvaluationType issueFlag,
                                        Term[] terms) {
      super(lineNumber, recordId, issueFlag);
      this.terms = terms;
    }

    public Term[] getTerms() {
      return terms;
    }
  }

  /**
   * Contains details of a RecordInterpretationResult.
   */
  public static class RecordInterpretationResultDetails extends BaseEvaluationResultDetails {

    private final Map<Term, String> relatedData;

    RecordInterpretationResultDetails(Long lineNumber, String recordId, EvaluationType issueFlag,
                                             Map<Term, String> relatedData) {
      super(lineNumber, recordId, issueFlag);
      this.relatedData = relatedData;
    }

    public Map<Term, String> getRelatedData() {
      return relatedData;
    }
  }

}
