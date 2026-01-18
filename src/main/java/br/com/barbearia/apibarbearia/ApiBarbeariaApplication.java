package br.com.barbearia.apibarbearia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class ApiBarbeariaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiBarbeariaApplication.class, args);
        System.out.println(new BCryptPasswordEncoder().encode("dev123"));
    }


}
