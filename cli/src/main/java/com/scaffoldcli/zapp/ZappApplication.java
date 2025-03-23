package com.scaffoldcli.zapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.command.annotation.CommandScan;

@SpringBootApplication
@CommandScan
public class ZappApplication {

	public static void main(String[] args) throws Exception {
		//========== Spring init ==========//
		SpringApplication application = new SpringApplication(ZappApplication.class);
		application.setBannerMode(Mode.OFF);
		// application.setLogStartupInfo(false);
		application.run(args);
	}
}