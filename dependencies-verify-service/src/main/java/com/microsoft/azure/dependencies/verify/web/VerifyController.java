/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.dependencies.verify.web;

import com.microsoft.azure.dependencies.verify.pom.Project;
import com.microsoft.azure.dependencies.verify.verification.CheckResult;
import com.microsoft.azure.dependencies.verify.verification.DependencyChecker;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class VerifyController {

    private static final String JSON_SAMPLE = String.join(System.getProperty("line.separator"),
            "Post Json sample:",
            "{",
            "    \"groupId\": \"com.microsoft.azure\",",
            "    \"artifactId\": \"spring-data-cosmosdb\",",
            "    \"version\": \"2.0.5\"",
            "}");

    @GetMapping("/greeting")
    public String greeting() {
        return JSON_SAMPLE;
    }

    @PostMapping("/verify")
    public List<CheckResult> dependenciesVerify(@RequestBody Project project) {
        return new DependencyChecker(project).check();
    }
}
