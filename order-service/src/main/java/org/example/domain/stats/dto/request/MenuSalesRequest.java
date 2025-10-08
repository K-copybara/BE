package org.example.domain.stats.dto.request;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MenuSalesRequest {
    private List<Long> menuIds;
}
