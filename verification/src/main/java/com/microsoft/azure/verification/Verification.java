/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.verification;

import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.ConsistencyLevel;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.credentials.AppServiceMSICredentials;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.authentication.KeyVaultCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.ServiceResponseBuilder;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.microsoft.rest.serializer.JacksonAdapter;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.services.media.MediaConfiguration;
import com.microsoft.windowsazure.services.media.MediaContract;
import com.microsoft.windowsazure.services.media.MediaService;
import com.microsoft.windowsazure.services.media.authentication.*;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RestController
public class Verification {
    @Value("${azure.management.baseUrl}")
    private String managementBaseUrl;

    @Bean
    public Azure getAzure() throws IOException {
        RestClient restClient = new RestClient.Builder()
                .withBaseUrl(managementBaseUrl)
                .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                .withSerializerAdapter(new AzureJacksonAdapter())
                .build();

        return Azure.authenticate(restClient, "Fake-domain").withDefaultSubscription();
    }

    @Bean
    public KeyVaultClient getAuthenticationClient() {
        return new KeyVaultClient(createCredentials());
    }

    private ServiceClientCredentials createCredentials() {
        return new KeyVaultCredentials() {
            @Override
            @SneakyThrows
            public String doAuthenticate(String auth, String resource, String scope) {
                final AuthenticationResult result;

                result = getAccessToken(auth, resource);

                return result.getAccessToken();
            }
        };
    }

    @SneakyThrows
    private AuthenticationResult getAccessToken(String auth, String resource) {
        final ExecutorService service = Executors.newFixedThreadPool(1);
        final AuthenticationContext context = new AuthenticationContext(auth, false, service);
        final ClientCredential credential = new ClientCredential("fake-id", "fake-key");
        final Future<AuthenticationResult> future = context.acquireToken(resource, credential, null);
        final AuthenticationResult result = future.get();

        service.shutdown();

        return result;
    }

    @Bean
    public TelemetryClient getTelemetryClient() {
        return new TelemetryClient();
    }

    @Bean
    public QueueClient getQueueClient() throws InterruptedException, ServiceBusException {
        com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder builder = new
                com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder("fake-string", "fake-queue-name");

        return new QueueClient(builder, ReceiveMode.RECEIVEANDDELETE);
    }

    @Bean
    public RestClient getRestClient() {
        return new RestClient.Builder()
                .withBaseUrl("http://localhost")
                .withResponseBuilderFactory(new ServiceResponseBuilder.Factory())
                .withSerializerAdapter(new JacksonAdapter())
                .build();
    }

    @Bean
    public CloudBlobContainer getCloudBlobContainer() throws InvalidKeyException, URISyntaxException, StorageException {
        CloudStorageAccount account = CloudStorageAccount.parse("fake-string");
        CloudBlobClient serviceClient = account.createCloudBlobClient();
        CloudBlobContainer container = serviceClient.getContainerReference("myimages");

        container.createIfNotExists();

        return container;
    }

    @Bean
    public Vault getVault() throws IOException {
        return this.getAzure().vaults().define("fake-name")
                .withRegion(Region.US_WEST)
                .withNewResourceGroup("pli-test-group")
                .defineAccessPolicy()
                .forServicePrincipal("fake-sp")
                .allowKeyAllPermissions()
                .allowSecretAllPermissions()
                .attach()
                .create();
    }

    @Bean
    public MediaContract createMediaContract() throws MalformedURLException {
        final AzureAdTokenCredentials tokenCredentials = new AzureAdTokenCredentials("fake-tenant",
                new AzureAdClientSymmetricKey("fake-client-id", "fake-client-key"),
                AzureEnvironments.AZURE_CLOUD_ENVIRONMENT);
        final ExecutorService executorService = Executors.newFixedThreadPool(1);
        final TokenProvider tokenProvider = new AzureAdTokenProvider(tokenCredentials, executorService);
        final Configuration configuration = Configuration.getInstance();

        configuration.setProperty(MediaConfiguration.AZURE_AD_API_SERVER, "http://fake.account.api.url");
        configuration.setProperty(MediaConfiguration.AZURE_AD_TOKEN_PROVIDER, tokenProvider);

        return MediaService.create(configuration);
    }

    @Bean
    public AppServiceMSICredentials getAppServiceMSICredentials() {
        return new AppServiceMSICredentials(AzureEnvironment.AZURE);
    }
}
