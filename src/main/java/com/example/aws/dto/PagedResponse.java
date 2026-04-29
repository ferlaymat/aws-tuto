package com.example.aws.dto;

import java.util.List;

public record PagedResponse(List<String> keys, String nextContinuationToken, boolean hasNext) {}