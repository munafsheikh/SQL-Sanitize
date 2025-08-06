package org.example.sqlsanitize;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SqlSanitizeApplication {

	/**
	 * TODO: Figure out postman sanitize bug (funny chars maybe??)
	 * 	test swagger
	 * 	Check dup code
	 * 	Finish javadocs
	 * 	check imports
	 * 	create tests
 	 */

	public static void main(String[] args) {
		SpringApplication.run(SqlSanitizeApplication.class, args);
	}

}
