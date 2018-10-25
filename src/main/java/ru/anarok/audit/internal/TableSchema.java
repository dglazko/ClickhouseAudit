package ru.anarok.audit.internal;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class TableSchema {

    private final String name;
    private List<ColumnSchema> columnSchemaList = new ArrayList<>();

}
