package org.gbif.validation.collector;

import org.gbif.validation.api.ResultsCollector;
import org.gbif.validation.api.model.EvaluationType;
import org.gbif.validation.api.model.RecordEvaluationResult;
import org.gbif.validation.api.result.ValidationResultDetails;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Basic implementation of a {@link ResultsCollector}.
 */
public class RecordEvaluationResultCollector implements ResultsCollector, Serializable {

  public static final int DEFAULT_MAX_NUMBER_OF_SAMPLE = 10;

  private final int maxNumberOfSample;
  private final InnerRecordEvaluationResultCollector innerImpl;

  /**
   *
   * @param maxNumberOfSample
   * @param useConcurrentMap if this {@link ResultsCollector} will be used in a concurrent context
   */
  public RecordEvaluationResultCollector(Integer maxNumberOfSample, boolean useConcurrentMap) {
    this.maxNumberOfSample = maxNumberOfSample != null ? maxNumberOfSample : DEFAULT_MAX_NUMBER_OF_SAMPLE;
    innerImpl = useConcurrentMap ? new RecordEvaluationResultCollectorConcurrent() :
            new RecordEvaluationResultCollectorSingleThread();
  }

  /**
   * Internal interface that defined the behavior of an internal RecordEvaluationResultCollector.
   */
  private interface InnerRecordEvaluationResultCollector extends Serializable {
    void countAndPrepare(EvaluationType type);
    void computeSampling(EvaluationType type, BiFunction<EvaluationType, Collection<ValidationResultDetails>,
            Collection<ValidationResultDetails>> samplingFunction);
    Map<EvaluationType, Long> getAggregatedCounts();
    Map<EvaluationType, List<ValidationResultDetails>> getSamples();
  }

  /**
   * InnerRecordEvaluationResultCollector implementation with support for single-thread access.
   */
  private static class RecordEvaluationResultCollectorSingleThread implements InnerRecordEvaluationResultCollector {
    private final Map<EvaluationType, Long> issueCounter;
    private final Map<EvaluationType, Collection<ValidationResultDetails>> issueSampling;

    RecordEvaluationResultCollectorSingleThread() {
      issueCounter = new EnumMap<>(EvaluationType.class);
      issueSampling = new EnumMap<>(EvaluationType.class);
    }

    @Override
    public void countAndPrepare(EvaluationType type) {
      issueCounter.compute(type, (k, v) -> (v == null) ? 1 : ++v);
      issueSampling.putIfAbsent(type, new ArrayList<>());
    }

    @Override
    public void computeSampling(EvaluationType type, BiFunction<EvaluationType, Collection<ValidationResultDetails>,
            Collection<ValidationResultDetails>> samplingFunction) {
      issueSampling.compute(type, samplingFunction);
    }

    @Override
    public Map<EvaluationType, List<ValidationResultDetails>> getSamples() {
      return issueSampling.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
              rec ->  new ArrayList<>(rec.getValue())));
    }

    @Override
    public Map<EvaluationType, Long> getAggregatedCounts() {
      return issueCounter.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
              Map.Entry::getValue));
    }
  }

  /**
   * InnerRecordEvaluationResultCollector implementation with support for concurrent access.
   */
  private static class RecordEvaluationResultCollectorConcurrent implements InnerRecordEvaluationResultCollector {
    private final Map<EvaluationType, LongAdder> issueCounter;
    private final Map<EvaluationType, Collection<ValidationResultDetails>> issueSampling;

    RecordEvaluationResultCollectorConcurrent() {
      issueCounter = new ConcurrentHashMap<>(EvaluationType.values().length);
      issueSampling = new ConcurrentHashMap<>(EvaluationType.values().length);
    }

    @Override
    public void countAndPrepare(EvaluationType type) {
      issueCounter.computeIfAbsent(type, k -> new LongAdder()).increment();
      issueSampling.putIfAbsent(type, new ConcurrentLinkedQueue<>());
    }

    @Override
    public void computeSampling(EvaluationType type, BiFunction<EvaluationType, Collection<ValidationResultDetails>,
            Collection<ValidationResultDetails>> samplingFunction) {
      issueSampling.compute(type, samplingFunction);
    }

    /**
     * @return a copy of the inter aggregated counts.
     */
    @Override
    public Map<EvaluationType, Long> getAggregatedCounts() {
      return issueCounter.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
              rec -> rec.getValue().longValue()));
    }

    /**
     *
     * @return a copy of the internal evaluation samples.
     */
    @Override
    public Map<EvaluationType, List<ValidationResultDetails>> getSamples() {
      return issueSampling.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
              rec ->  new ArrayList<>(rec.getValue())));
    }
  }

  @Override
  public void collect(RecordEvaluationResult result) {
    if (result !=null && result.getDetails() != null) {
      result.getDetails().forEach(detail -> {
        innerImpl.countAndPrepare(detail.getEvaluationType());
        innerImpl.computeSampling(detail.getEvaluationType(), (type, queue) -> {
          if (queue.size() < maxNumberOfSample) {
            queue.add(new ValidationResultDetails(result.getLineNumber(), result.getRecordId(), detail.getExpected(),
                    detail.getFound(), detail.getRelatedData()));
          }
          return queue;
        });
      });
    }
  }


  public Map<EvaluationType, List<ValidationResultDetails>> getSamples() {
    return innerImpl.getSamples();
  }

  public Map<EvaluationType, Long> getAggregatedCounts() {
    return innerImpl.getAggregatedCounts();
  }

}
