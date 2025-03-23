package com.scaffoldcli.zapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.shell.command.annotation.CommandScan;

@SpringBootApplication
@CommandScan
@ComponentScan
public class ZappApplication {

	public static void main(String[] args) throws Exception {
		//========== Spring init ==========//
		SpringApplication application = new SpringApplication(ZappApplication.class);
		application.setBannerMode(Mode.OFF);
		application.run(args);
	}
}