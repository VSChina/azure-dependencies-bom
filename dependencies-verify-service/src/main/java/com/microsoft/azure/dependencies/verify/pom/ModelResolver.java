/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.dependencies.verify.pom;

import com.microsoft.azure.dependencies.verify.exception.VerificationHttpException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
public class ModelResolver {

    private static final String MAVEN_URL_PATTERN = "https://repo1.maven.org/maven2/%s/%s/%s/%s";

    private final HttpClient httpClient;

    public ModelResolver() {
        this.httpClient = HttpClientBuilder.create().build();
    }

    private static String getMavenPomUrl(@NonNull SimplePom pom) {
        String groupId = pom.getGroupId().replaceAll("\\.", "/");
        String artifactId = pom.getArtifactId();
        String version = pom.getVersion();
        String filename = String.format("%s-%s.pom", artifactId, version);

        return String.format(MAVEN_URL_PATTERN, groupId, artifactId, version, filename);
    }

    public Model resolve(@NonNull SimplePom pom) {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        HttpResponse response = getModelPomResponse(pom);

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new VerificationHttpException("Failed to get http response.");
        }

        try {
            return reader.read(new InputStreamReader(response.getEntity().getContent()));
        } catch (IOException e) {
            throw new VerificationHttpException("Failed to get http content.", e);
        } catch (XmlPullParserException e) {
            throw new VerificationHttpException("Failed to parse target pom.", e);
        }
    }

    private HttpResponse getModelPomResponse(@NonNull SimplePom pom) {
        String url = getMavenPomUrl(pom);
        HttpGet request = new HttpGet(url);
        HttpResponse response;

        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            throw new VerificationHttpException("Failed to execute http request.", e);
        }

        return response;
    }
}
