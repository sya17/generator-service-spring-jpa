package com.generator.spring.generate.util;

import com.generator.spring.generate.template.seed.TableDescription;

import java.util.List;
import java.util.Map;

public interface GenerateDDL {
    public Map<String, List<String>> generate(List<TableDescription> listTable);
    public Map<String, List<String>> generate(TableDescription tableDescription);
}
