package com.microsoft.azure.dependencies.verify.pom;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Project {

    private String groupId;

    private String artifactId;

    private String version;
}
