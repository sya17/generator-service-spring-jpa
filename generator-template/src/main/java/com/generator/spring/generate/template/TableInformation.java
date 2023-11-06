package com.generator.spring.generate.template;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableInformation {
    private String tableName;
    private List<ColumnInformation> columns;
}
