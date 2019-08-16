/*
 * Copyright (C) 2011-2019 ShenZhen zbjf soft Information Technology Co.,Ltd.
 *
 * All right reserved.
 *
 * This software is the confidential and proprietary
 * information of test Company of China.
 * ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only
 * in accordance with the terms of the contract agreement
 * you entered into with test inc.
 *
 */

package com.yanmingchen.distributed.transaction.demo.stock.config;

import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.FileOutConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.TemplateConfig;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author: zhouwei
 * @since: 2019年1月25日
 * @version:
 */
public class MyBatisPlusGenerator {

	private static Scanner scanner;
	private static final String PROJECT_PATH = "D:/code/tx_stock";

	/**
	 * 读取控制台内容
	 */
	public static String scanner(String tip) {
		scanner = new Scanner(System.in);
		StringBuilder help = new StringBuilder();
		help.append("请输入" + tip + "：");
		System.out.println(help.toString());
		if (scanner.hasNext()) {
			String ipt = scanner.next();
			if (StringUtils.isNotEmpty(ipt)) {
				return ipt;
			}
		}
		throw new MybatisPlusException("请输入正确的" + tip + "！");
	}

	public static void main(String[] args) {
		// 代码生成器
		AutoGenerator mpg = new AutoGenerator();

		// 全局配置
		globalConfig(mpg);

		// 数据源配置
		dataSourceConfig(mpg);

		// 包配置
		packageConfig(mpg);

		// 自定义配置
		config(mpg);

		// 配置模板
		TemplateConfig templateConfig = new TemplateConfig();

		// 配置自定义输出模板

		templateConfig.setXml(null);
		mpg.setTemplate(templateConfig);

		// 策略配置
		strategyConfig(mpg);

		mpg.setTemplateEngine(new FreemarkerTemplateEngine());
		mpg.execute();
	}

	private static void strategyConfig(AutoGenerator mpg) {
		StrategyConfig strategy = new StrategyConfig();
		strategy.setNaming(NamingStrategy.underline_to_camel);
		strategy.setColumnNaming(NamingStrategy.underline_to_camel);
		strategy.setEntityLombokModel(true);
		strategy.setRestControllerStyle(true);
		strategy.setInclude(scanner("表名"));
		strategy.setControllerMappingHyphenStyle(true);
		mpg.setStrategy(strategy);
	}

	private static void config(AutoGenerator mpg) {
		InjectionConfig cfg =
				new InjectionConfig() {

					@Override
					public void initMap() {
						// to do nothing
					}
				};

		// 如果模板引擎是 freemarker
		String templatePath = "/templates/mapper.xml.ftl";

		// 自定义输出配置
		List<FileOutConfig> focList = new ArrayList<>();
		// 自定义配置会被优先输出
		focList.add(
				new FileOutConfig(templatePath) {

					@Override
					public String outputFile(TableInfo tableInfo) {
						// 自定义输出文件名
						return PROJECT_PATH
								+ "/src/main/resources/mapper/"
								+ tableInfo.getEntityName()
								+ "Mapper"
								+ StringPool.DOT_XML;
					}
				});

		cfg.setFileOutConfigList(focList);
		mpg.setCfg(cfg);
	}

	private static void packageConfig(AutoGenerator mpg) {
		PackageConfig pc = new PackageConfig();
		pc.setParent("com.yanmingchen.distributed.transaction.demo.stock");
		pc.setEntity("entity");
		pc.setMapper("mapper");
		mpg.setPackageInfo(pc);
	}

	private static void dataSourceConfig(AutoGenerator mpg) {
		DataSourceConfig dsc = new DataSourceConfig();
		dsc.setUrl(
				"jdbc:mysql://47.104.171.230:3306/tx_stock?useUnicode=true&useSSL=false&characterEncoding=utf8");
		dsc.setDriverName("com.mysql.jdbc.Driver");
		dsc.setUsername("root");
		dsc.setPassword("root");
		mpg.setDataSource(dsc);
	}

	private static void globalConfig(AutoGenerator mpg) {
		GlobalConfig gc = new GlobalConfig();
		// 是否覆盖文件
		gc.setOutputDir(PROJECT_PATH + "/src/main/java")
				.setFileOverride(true)
				.setActiveRecord(true)
				// XML 二级缓存
				.setEnableCache(false)
				// XML ResultMap
				.setBaseResultMap(true)
				// XML columList
				.setBaseColumnList(true)
				// 创建人
				.setAuthor(scanner("作者姓名"))
				// 打开文件夹
				.setOpen(false);
		mpg.setGlobalConfig(gc);
	}
}
