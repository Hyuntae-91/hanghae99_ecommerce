// UserResponse.java
package kr.hhplus.be.server.dto.point;

import java.util.List;

public record PointResponse(
        Long userId,
        Long point
) {}
