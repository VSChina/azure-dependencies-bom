package com.microsoft.azure.dependencies.verify.verification;

import com.microsoft.azure.dependencies.verify.exception.VerificationHttpException;
import com.microsoft.azure.dependencies.verify.pom.SimpleMavenPom;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DependencyChecker {

    private static final String MAVEN_URL_PATTERN = "https://repo1.maven.org/maven2/%s/%s/%s/%s";

    private final SimpleMavenPom pom;

    private final HttpClient httpClient;

    public DependencyChecker(@NonNull SimpleMavenPom pom) {
        this.pom = pom;
        this.httpClient = HttpClientBuilder.create().build();
    }

    public boolean check() {
        HttpResponse response = getMavenPomResponse(this.pom);
        MavenXpp3Reader reader = new MavenXpp3Reader();

        Model model = reader.read(new InputStreamReader(response.getEntity().getContent()));

        return true;
    }

    private static String getMavenPomUrl(@NonNull SimpleMavenPom pom) {
        String groupId = pom.getGroupId().replaceAll("\\.", "/");
        String artifactId = pom.getArtifactId();
        String version = pom.getVersion();
        String filename = String.format("%s-%s.pom", artifactId, version);

        return String.format(MAVEN_URL_PATTERN, groupId, artifactId, version, filename);
    }

    private HttpResponse getMavenPomResponse(@NonNull SimpleMavenPom pom) {
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
