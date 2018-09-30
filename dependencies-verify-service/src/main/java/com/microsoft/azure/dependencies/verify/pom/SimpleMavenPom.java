package com.microsoft.azure.dependencies.verify.pom;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder(builderMethodName = "hiddenBuilder")
public class SimpleMavenPom {

    private String groupId;

    private String artifactId;

    private String version;

    private List<SimpleMavenPom> dependencies;

    public static SimpleMavenPomBuilder builder() {
        return hiddenBuilder().dependencies(new ArrayList<>());
    }
}
