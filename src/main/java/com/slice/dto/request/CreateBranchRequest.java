package com.slice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBranchRequest {

    @NotBlank(message = "Branch name is required")
    @Size(max = 255, message = "Branch name must not exceed 255 characters")
    private String name;
}
