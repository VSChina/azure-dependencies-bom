package com.microsoft.azure.dependencies.verify.web;

import com.microsoft.azure.dependencies.verify.pom.SimpleMavenPom;
import com.microsoft.azure.dependencies.verify.verification.DependencyChecker;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VerifyController {

    @GetMapping("/greeting")
    public String greeting() {
        return "Greetings!";
    }

    @PostMapping("/verify")
    public String dependenciesVerify() {
        SimpleMavenPom pom = SimpleMavenPom.builder()
                .groupId("com.microsoft.azure")
                .artifactId("spring-data-cosmosdb")
                .version("2.0.5")
                .build();

        DependencyChecker checker = new DependencyChecker(pom);

        checker.check();

        return "Verify Success!";
    }
}
