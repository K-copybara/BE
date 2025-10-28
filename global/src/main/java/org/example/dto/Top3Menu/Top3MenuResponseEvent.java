package org.example.dto.Top3Menu;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Top3MenuResponseEvent {
    private Long storeId;
    private List<MenuInfoDto> menus;
}

