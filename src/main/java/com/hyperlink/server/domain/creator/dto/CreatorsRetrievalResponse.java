package com.hyperlink.server.domain.creator.dto;

import java.util.List;

public record CreatorsRetrievalResponse(List<CreatorResponse> creators, boolean hasNext) {



}
