package org.example.dto.Top3Menu;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MenuInfoDto {
    private Long menuId;
    private String menuName;
    private String menuInfo;
    private Long menuPrice;
}
