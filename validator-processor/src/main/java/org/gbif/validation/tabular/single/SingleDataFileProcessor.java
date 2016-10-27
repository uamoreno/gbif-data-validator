package org.gbif.validation.tabular.single;

import org.gbif.dwc.terms.Term;
import org.gbif.validation.api.DataFile;
import org.gbif.validation.api.DataFileProcessor;
import org.gbif.validation.api.RecordEvaluator;
import org.gbif.validation.api.RecordSource;
import org.gbif.validation.api.model.ValidationResult;
import org.gbif.validation.collector.InterpretedTermsCountCollector;
import org.gbif.validation.tabular.RecordSourceFactory;

import java.io.File;

public class SingleDataFileProcessor implements DataFileProcessor {

  private final DataValidationProcessor dataValidationProcessor;

  //TODO Should interpretedTermsCountCollector be nullable?
  public SingleDataFileProcessor(Term[] terms, RecordEvaluator recordEvaluator,
                                 InterpretedTermsCountCollector interpretedTermsCountCollector) {
    dataValidationProcessor = new DataValidationProcessor(terms,recordEvaluator,interpretedTermsCountCollector);
  }

  @Override
  public ValidationResult process(DataFile dataFile) {

    try (RecordSource recordSource = RecordSourceFactory.fromDelimited(new File(dataFile.getFileName()), dataFile.getDelimiterChar(),
            dataFile.isHasHeaders())) {
      String[] record;
      while ((record = recordSource.read()) != null) {
        dataValidationProcessor.process(record);
      }
      //FIXME the Status and indexeable should be decided by a another class somewhere
      return dataValidationProcessor.getValidationResult();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }


}
