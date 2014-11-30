package org.springframework.boot.autoconfigure.drools;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.*;
import org.kie.api.runtime.KieContainer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Scanner;

@Configuration
public class DroolsAutoConfiguration {

  @Autowired
  private BeanFactory beanFactory;

  private static String convertStreamToString(java.io.InputStream is) {
    try (Scanner scanner = new Scanner(is, "UTF-8")) {
      scanner.useDelimiter("\\A");
      return scanner.hasNext() ? scanner.next() : "";
    }
  }

  @Bean
  @ConditionalOnMissingBean(KieContainer.class)
  public KieContainer kieContainer() throws IOException {
    KieServices ks = KieServices.Factory.get();
    final KieRepository kr = ks.getRepository();
    kr.addKieModule(new KieModule() {
      @Override
      public ReleaseId getReleaseId() {
        return kr.getDefaultReleaseId();
      }
    });
    KieFileSystem kfs = ks.newKieFileSystem();
    Resource[] files = new PathMatchingResourcePatternResolver().getResources("classpath*:rules/**/*.*");

    for (Resource file : files) {
      kfs.write("src/main/resources/" + file.getFilename(), convertStreamToString(file.getInputStream()));
    }

    KieBuilder kb = ks.newKieBuilder(kfs);
    kb.buildAll(); // kieModule is automatically deployed to KieRepository if successfully built.
    KieContainer kContainer = ks.newKieContainer(kr.getDefaultReleaseId());
    return kContainer;
  }

  @Bean
  @ConditionalOnMissingBean(KieBase.class)
  public KieBase kieBase() throws IOException {
    return beanFactory.getBean(KieContainer.class).getKieBase();
  }
}