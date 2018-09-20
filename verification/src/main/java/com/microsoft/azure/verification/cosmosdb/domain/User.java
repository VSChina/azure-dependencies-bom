package com.microsoft.azure.verification.cosmosdb.domain;

import com.microsoft.azure.spring.data.cosmosdb.core.mapping.Document;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
@Document
public class User {

    @Id
    private String id;

    private String name;
}
