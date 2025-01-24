package com.aknopov.config_logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Component that logs injected `@Value` fields. This code -
 * <ol>
 * <li>Gets all bean classes in "org.example.*" packages</li>
 * <li>Browses their constructors</li>
 * <li>Gets from {@code Environment} values of parameters with {@code Value} annotation, or uses their default value</li>
 * </ol>
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class ConfigurationLogger {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationLogger.class);
    private static final String SPEL_PFX = "${";

    private final String appPackage;
    private final String lineHeading;
    @Autowired
    private Environment environment;

    /**
     * Constructor
     *
     * @param appPackage - a root package to scan for beans, e.g. "org.example"
     * @param lineHeading - a heading in the log entry for the settings output, e.g. "Service configuration"
     */
    public ConfigurationLogger(
            @Value("${config.logging.package:}") String appPackage,
            @Value("${config.logging.prefix:Service configuration}") String lineHeading) {
        this.appPackage = appPackage;
        this.lineHeading = lineHeading;
    }

    /**
     * Logs configuration parameters in all application beans
     *
     */
    @PostConstruct
    public void logSettings() throws ClassNotFoundException {
        // https://stackoverflow.com/posts/21430849/revisions
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));
        Set<BeanDefinition> beanDefs = provider.findCandidateComponents(appPackage);

        Properties allSettings = new Properties();

        for (var beanDef : beanDefs) {
            Class<?> klaz = Class.forName(beanDef.getBeanClassName());
            allSettings.putAll(extractInjectedValues(klaz));
        }

        allSettings.entrySet()
                .stream()
                .map(e -> Map.entry(e.getKey().toString(), e.getValue()))
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> logger.info("{}: {} = {}", lineHeading, e.getKey(), e.getValue()));
    }

    private Properties extractInjectedValues(Class<?> klaz) {
        Properties ret = new Properties();

        for (Constructor<?> ctor : klaz.getConstructors()) {
            for (Parameter parm : ctor.getParameters()) {
                Value annotation = parm.getAnnotation(Value.class);
                if (annotation != null) {
                    Map.Entry<String, String> entry = parseAnnotation(annotation);
                    String envValue = environment.getProperty(entry.getKey());
                    String realValue = envValue != null ? envValue : entry.getValue();
                    ret.setProperty(entry.getKey(), realValue != null ? realValue : entry.getValue());
                }
            }
        }

        return ret;
    }

    private Map.Entry<String, String> parseAnnotation(Value annotation) {
        String raw = annotation.value();
        if (!raw.startsWith(SPEL_PFX)) {
            return Map.entry("<unkn>", raw);
        }
        // These are defaults
        String[] parts = raw.replace(SPEL_PFX, "")
                .replace("}", "")
                .split(":", -1);
        return Map.entry(parts[0], parts.length == 2 ? parts[1] : "N/A");
    }
}
