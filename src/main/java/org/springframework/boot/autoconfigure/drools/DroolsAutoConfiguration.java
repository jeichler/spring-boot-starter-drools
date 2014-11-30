package org.springframework.boot.autoconfigure.drools;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.*;
import org.kie.api.runtime.KieContainer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Scanner;

@Configuration
@ConditionalOnMissingBean(value = { KieBase.class, KieContainer.class })
public class DroolsAutoConfiguration {

	private static String convertStreamToString(java.io.InputStream is) {
		try (Scanner scanner = new Scanner(is, "UTF-8")) {
			scanner.useDelimiter("\\A");
			return scanner.hasNext() ? scanner.next() : "";
		}
	}

	@Bean
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
		Resource[] files = new PathMatchingResourcePatternResolver().getResources("classpath*:rules/**.*");

		for (Resource file : files) {
			String myString = convertStreamToString(file.getInputStream());
			kfs.write("src/main/resources/" + file.getFilename(), myString);
		}

		KieBuilder kb = ks.newKieBuilder(kfs);
		kb.buildAll();
		return ks.newKieContainer(kr.getDefaultReleaseId());
	}

	@Bean
	public KieBase kieBase() throws IOException {
		return kieContainer().getKieBase();
	}
}
