package com.yanmingchen.distributed.transaction.demo.ui.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @description swagger配置类
 */
@EnableSwagger2
@Configuration
public class Swagger2 {

	@Bean
	public Docket createRestApi() {
		ParameterBuilder ticketPar = new ParameterBuilder();
		ticketPar.name("token").description("user token")
				.modelRef(new ModelRef("string")).parameterType("header").required(false).build();
		List<Parameter> pars = new ArrayList<>();
		pars.add(ticketPar.build());

		return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).select()
				.apis(RequestHandlerSelectors.basePackage("com.yanmingchen.distrbuted.transaction")).paths(PathSelectors.any())
				.build().globalOperationParameters(pars);
	}

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder().title("springboot利用swagger构建api文档")
				.description("简单优雅的restfun风格，https://www.cnblogs.com/JoiT/p/6378086.html")
				.termsOfServiceUrl("http://ip:port/hccSale/swagger-ui.html").version("1.0").build();
	}
}
