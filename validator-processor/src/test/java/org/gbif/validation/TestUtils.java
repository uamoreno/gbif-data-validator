package org.gbif.validation;

import org.gbif.checklistbank.cli.normalizer.NormalizerConfiguration;
import org.gbif.dwc.extensions.ExtensionManager;
import org.gbif.utils.HttpUtil;
import org.gbif.utils.file.FileUtils;
import org.gbif.utils.file.properties.PropertiesUtil;
import org.gbif.validation.api.DataFile;
import org.gbif.validation.api.model.EvaluationType;
import org.gbif.validation.api.result.ValidationIssue;
import org.gbif.validation.api.result.ValidationIssues;
import org.gbif.validation.api.result.ValidationResultElement;
import org.gbif.validation.api.vocabulary.FileFormat;
import org.gbif.validation.conf.ValidatorConfiguration;
import org.gbif.validation.dwc.extensions.ExtensionManagerFactoryTestAdapter;
import org.gbif.validation.evaluator.EvaluatorFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.http.client.HttpClient;

/**
 *
 */
public class TestUtils {


  public static final File XML_CATALOG = FileUtils.getClasspathFile("xml/xml-catalog.xml");

  private static ValidatorConfiguration testConfig = loadValidatorConfiguration();
  private static final String APP_CONF_FILE = "validation.properties";
  private static final String NORMALIZER_CONF_FILE = "clb-normalizer.yaml";

  public static final HttpClient HTTP_CLIENT = HttpUtil.newMultithreadedClient(6000, 2, 1);

  //This ExtensionManager only servers 2 extensions (Description and Occurrence)
  public static final ExtensionManager EXTENSION_MANAGER =
          ExtensionManagerFactoryTestAdapter.getTestExtensionManager(HTTP_CLIENT);


  private static ValidatorConfiguration loadValidatorConfiguration() {
    try {
      Properties p = PropertiesUtil.readFromFile(FileUtils.getClasspathFile(APP_CONF_FILE).getAbsolutePath());
      return ValidatorConfiguration.builder()
              .setApiUrl(p.getProperty("validation.apiUrl"))
              .setNormalizerConfiguration(loadNormalizerConfiguration())
              .setExtensionListURL(new URL(p.getProperty("validation.extensionDiscoveryUrl")))
              .build();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private static NormalizerConfiguration loadNormalizerConfiguration() {
    try {
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      return mapper.readValue(Thread.currentThread().getContextClassLoader().getResource(NORMALIZER_CONF_FILE),
              NormalizerConfiguration.class);
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }
  }

  public static ValidatorConfiguration getValidatorConfiguration() {
    return testConfig;
  }

  public static EvaluatorFactory getEvaluatorFactory(){
    return new EvaluatorFactory(TestUtils.getValidatorConfiguration(), EXTENSION_MANAGER);
  }


  /**
   * Get a DataFile instance for a file in the classpath.
   * @param resourcePath
   * @param sourceFileName
   * @return
   */
  public static DataFile getDwcaDataFile(String resourcePath, String sourceFileName) {
    File dwcaFolder = FileUtils.getClasspathFile(resourcePath);
    return new DataFile(UUID.randomUUID(), dwcaFolder.toPath(), sourceFileName, FileFormat.DWCA, "", "");
  }

  /**
   * Get a DataFile instance for a file in the classpath.
   * @param resourcePath
   * @param sourceFileName
   * @return
   */
  public static DataFile getDataFile(String resourcePath, String sourceFileName, FileFormat fileFormat) {
    File dwcaFolder = FileUtils.getClasspathFile(resourcePath);
    return new DataFile(UUID.randomUUID(), dwcaFolder.toPath(), sourceFileName, fileFormat, "", "");
  }

  /**
   * Utility method to get the first {@link ValidationIssue} from a list of {@link ValidationResultElement}.
   * This method doesn't check if it exits first.
   *
   * @param validationResultElementList
   *
   * @return
   */
  public static ValidationIssue getFirstValidationIssue(List<ValidationResultElement> validationResultElementList) {
    return validationResultElementList.get(0).getIssues().get(0);
  }

  public static List<ValidationResultElement> mockMetadataValidationResultElementList(EvaluationType type) {
    List<ValidationResultElement> validationResultElements = new ArrayList<>();
    validationResultElements.add(ValidationResultElement.forMetadata("myfile",
            Collections.singletonList(ValidationIssues.withEvaluationTypeOnly(type))
            , null));
    return validationResultElements;
  }

  /**
   * Get the first {@link ValidationResultElement} from the provided list where at least one issues is matching
   * the provided {@link EvaluationType}.
   *
   * @param type
   * @param validationResultElementList
   *
   * @return
   */
  public static ValidationResultElement getFirstValidationResultElement(EvaluationType type, List<ValidationResultElement> validationResultElementList) {
    return validationResultElementList.stream()
            .filter(vre -> vre.contains(type))
            .findFirst().orElse(null);
  }

}
