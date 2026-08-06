package com.coltomex.test_hsm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.ComponentScan;

import org.springframework.boot.Banner;

@SpringBootApplication
@ComponentScan(
		basePackages = {"com.coltomex.test_hsm", "com.coltomex.arc.jwe**", "com.coltomex.arc.common**", "com.coltomex.arc.hsm**"}
		//Specifies which types are not eligible for component scanning.
		//excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = {})}
		)
public class TestHsmApplication  {
@Autowired 
private ValidacionHSM validador;

private static String alias;

public static void main( String[] args ) {	
	alias = args[0];

	System.out.println("============================================================================");
	System.out.println("");
	System.out.println("        I N I C I A N D O    P R U E B A    D E    C O N E X I O N     Y");
	System.out.println("        C O N F I G U R A C I O N    H S M");
	System.out.println("");
	System.out.println("        ALIAS: " + args[0]);
	System.out.println("");
	System.out.println("============================================================================");
	
	ConfigurableApplicationContext c = new SpringApplicationBuilder(TestHsmApplication.class)
            .web(WebApplicationType.NONE) // No iniciar servidor web
            .headless(false)			  // Permitir interacción con el sistema gráfico si es necesario
            .bannerMode(Banner.Mode.OFF)  // Desactivar el banner de Spring Boot
            .run(args);
        
        System.exit(SpringApplication.exit(c, () -> 0)); // Finalizar la aplicación correctamente
}


    @Bean
    CommandLineRunner validacionCommand() {
    	return args -> {   
    		System.out.println(validador.validacion(alias));
    		System.out.println("");
    		System.out.println("========================================================================================");
    		System.out.println("");
    	};
    }
    

}


