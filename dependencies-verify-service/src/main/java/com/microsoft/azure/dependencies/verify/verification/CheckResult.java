/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.dependencies.verify.verification;

import com.microsoft.azure.dependencies.verify.pom.SimplePom;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CheckResult {

    private String groupId;

    private String artifactId;

    private String version;

    private String hiddenVersion;

    public static CheckResult from(@NonNull SimplePom pom, @NonNull String hiddenVersion) {
        CheckResult result = new CheckResult();

        result.groupId = pom.getGroupId();
        result.artifactId = pom.getArtifactId();
        result.version = pom.getVersion();
        result.hiddenVersion = hiddenVersion;

        return result;
    }
}
