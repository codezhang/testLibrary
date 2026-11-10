package org.example.web.dto;

import java.util.List;

public record LoginResponse(String userId, String username, List<String> roleList) {
}
