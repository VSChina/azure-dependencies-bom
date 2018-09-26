/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.verification.gremlin.repository;

import com.microsoft.azure.verification.gremlin.domain.Question;
import com.microsoft.spring.data.gremlin.repository.GremlinRepository;

public interface QuestionRepository extends GremlinRepository<Question, String> {
}
