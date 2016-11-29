package org.gbif.validation.ws;

import org.gbif.service.guice.PrivateServiceModule;
import org.gbif.utils.HttpUtil;
import org.gbif.utils.file.properties.PropertiesUtil;
import org.gbif.validation.ResourceEvaluationManager;
import org.gbif.validation.api.result.ValidationResult;
import org.gbif.validation.evaluator.EvaluatorFactory;
import org.gbif.validation.jobserver.JobServer;
import org.gbif.validation.jobserver.impl.DataValidationActorPropsMapping;
import org.gbif.validation.jobserver.impl.FileJobStorage;
import org.gbif.validation.ws.conf.ConfKeys;
import org.gbif.validation.ws.conf.ValidationConfiguration;
import org.gbif.ws.app.ConfUtils;
import org.gbif.ws.mixin.Mixins;
import org.gbif.ws.server.guice.GbifServletListener;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.common.collect.Lists;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Server listener. Contains
 */
public class ValidationWsListener extends GbifServletListener {

  //Default configuration file
  private static final String APP_CONF_FILE = "validation.properties";

  /**
   * Private module that installs all the required settings and exposes the service configuration.
   */
  private static class ValidationModule extends PrivateServiceModule {

    private static final String PROPERTIES_PREFIX = "validation.";

    private static final int DEFAULT_SPLIT_SIZE = 10000;

    private static final int HTTP_CLIENT_TO = 60000;
    private static final int HTTP_CLIENT_THREADS = 20;
    private static final int HTTP_CLIENT_THREADS_PER_ROUTE = 20;

    private static final TypeLiteral<JobServer<ValidationResult>> JOB_SERVER_TYPE_LITERAL =
      new TypeLiteral<JobServer<ValidationResult>>(){};

    ValidationModule(Properties properties) {
      super(PROPERTIES_PREFIX,properties);
    }

    private static ValidationConfiguration getConfFromProperties(Properties properties){
      ValidationConfiguration configuration = new ValidationConfiguration();
      configuration.setApiUrl(properties.getProperty(ConfKeys.API_URL_CONF_KEY));
      configuration.setWorkingDir(properties.getProperty(ConfKeys.WORKING_DIR_CONF_KEY));
      configuration.setFileSplitSize(NumberUtils.toInt(properties.getProperty(ConfKeys.FILE_SPLIT_SIZE),
                                                       DEFAULT_SPLIT_SIZE));
      configuration.setApiDataValidationPath(properties.getProperty(ConfKeys.VALIDATION_API_PATH_CONF_KEY));
      configuration.setJobResultStorageDir(properties.getProperty(ConfKeys.RESULT_STORAGE_DIR_CONF_KEY));
      return configuration;
    }

    @Override
    protected void configureService() {
      ValidationConfiguration configuration = getConfFromProperties(getProperties());

      HttpUtil httpUtil = new HttpUtil(HttpUtil.newMultithreadedClient(HTTP_CLIENT_TO, HTTP_CLIENT_THREADS,
                                                                       HTTP_CLIENT_THREADS_PER_ROUTE));

      bind(HttpUtil.class).toInstance(httpUtil);
      bind(JOB_SERVER_TYPE_LITERAL).toInstance(new JobServer<>(new FileJobStorage(Paths.get(configuration.getJobResultStorageDir())),
                                                               buildActorPropsMapping(configuration)));
      bind(ValidationConfiguration.class).toInstance(configuration);
      bind(ResourceEvaluationManager.class).toInstance(new ResourceEvaluationManager(configuration.getApiUrl(),
                                                                                     configuration.getFileSplitSize()));

      expose(JOB_SERVER_TYPE_LITERAL);
      expose(ValidationConfiguration.class);
      expose(ResourceEvaluationManager.class);
      expose(HttpUtil.class);
    }

    /**
     * Builds an instance of DataValidationActorPropsMapping which is used by the Akka components.
     */
    private static DataValidationActorPropsMapping buildActorPropsMapping(ValidationConfiguration configuration) {
      return new DataValidationActorPropsMapping(new EvaluatorFactory(configuration.getApiUrl()),
                                                 configuration.getFileSplitSize(),
                                                 configuration.getWorkingDir());
    }
  }


  public ValidationWsListener() throws IOException {
    super(PropertiesUtil.readFromFile(ConfUtils.getAppConfFile(APP_CONF_FILE)), "org.gbif.validation.ws", false);
  }

  @Override
  protected List<Module> getModules(Properties properties) {
    List<Module> modules = Lists.newArrayList();
    modules.add(new ValidationModule(properties));
    return modules;
  }

  /**
   * Installs predefined mixins.
   */
  @Override
  protected Map<Class<?>, Class<?>> getMixIns() {
    return Mixins.getPredefinedMixins();
  }

}
