package com.generator.spring.generate.template.seed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableDescription {
    private String tableName;
    private String fileName;
    private String className;
    private Map mapContex;
    private Map mapContexEntityEmbededId;
    private List<ColumnDescription> columnDescriptions = new ArrayList<>();
    private Map<String, ColumnDescription> mapPK = new HashMap<>();

    public void addColumnDescription(ColumnDescription columnDescription) {
        columnDescriptions.add(columnDescription);
    }

    public List<ColumnDescription> getColumnDescriptions() {
        return columnDescriptions;
    }

}
