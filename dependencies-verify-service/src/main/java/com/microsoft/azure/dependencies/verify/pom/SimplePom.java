/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.dependencies.verify.pom;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Parent;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Builder(builderMethodName = "hiddenBuilder")
public class SimplePom {

    private String groupId;

    private String artifactId;

    @Setter
    private String version;

    private List<SimplePom> dependencies;

    public static SimplePomBuilder builder() {
        return hiddenBuilder().dependencies(new ArrayList<>());
    }

    public static SimplePom fromDependency(@NonNull Dependency dependency) {
        Assert.notNull(dependency.getVersion(), "dependency version should not be null");

        return builder()
                .groupId(dependency.getGroupId())
                .artifactId(dependency.getArtifactId())
                .version(dependency.getVersion())
                .build();
    }

    public static SimplePom fromParent(@NonNull Parent parent) {
        return builder()
                .groupId(parent.getGroupId())
                .artifactId(parent.getArtifactId())
                .version(parent.getVersion())
                .build();
    }

    public static SimplePom fromProject(@NonNull Project project) {
        return builder()
                .groupId(project.getGroupId())
                .artifactId(project.getArtifactId())
                .version(project.getVersion())
                .build();
    }

    public String signature() {
        return String.join(":", groupId, artifactId);
    }

    @Override
    public String toString() {
        return String.join(":", groupId, artifactId, version);
    }
}
