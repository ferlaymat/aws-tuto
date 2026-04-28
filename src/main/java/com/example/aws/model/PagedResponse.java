package com.example.aws.model;

import java.util.List;

public record PagedResponse(List<String> keys, String nextContinuationToken, boolean hasNext) {}