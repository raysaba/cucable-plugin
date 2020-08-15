/*
 * Copyright 2017 trivago N.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.trivago.gherkin;

import gherkin.ast.DataTable;
import gherkin.ast.DocString;
import gherkin.ast.Examples;
import gherkin.ast.Node;
import gherkin.ast.Step;
import gherkin.ast.TableCell;
import gherkin.ast.TableRow;
import gherkin.ast.Tag;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Singleton
class GherkinToCucableConverter {

    /**
     * Converts a list of Gherkin steps to Cucable steps including data tables.
     *
     * @param gherkinSteps a {@link Step} list.
     * @return a {@link com.trivago.vo.Step} list.
     */
    List<com.trivago.vo.Step> convertGherkinStepsToCucableSteps(final List<Step> gherkinSteps) {
        List<com.trivago.vo.Step> steps = new ArrayList<>();

        for (Step gherkinStep : gherkinSteps) {
            com.trivago.vo.Step step;
            com.trivago.vo.DataTable dataTable = null;
            String docString = null;

            Node argument = gherkinStep.getArgument();
            if (argument instanceof DataTable) {
                dataTable = convertGherkinDataTableToCucableDataTable((DataTable) argument);
            } else if (argument instanceof DocString) {
                docString = ((DocString) argument).getContent();
            }

            String keywordAndName = gherkinStep.getKeyword().concat(gherkinStep.getText());
            step = new com.trivago.vo.Step(keywordAndName, dataTable, docString);
            steps.add(step);
        }
        return steps;
    }

    /**
     * Converts a Gherkin data table to a Cucable data table.
     *
     * @param gherkinDataTable a {@link DataTable}.
     * @return a {@link com.trivago.vo.DataTable}.
     */
    private com.trivago.vo.DataTable convertGherkinDataTableToCucableDataTable(
            final DataTable gherkinDataTable) {

        com.trivago.vo.DataTable dataTable = new com.trivago.vo.DataTable();
        gherkinDataTable.getRows().stream().map(TableRow::getCells)
                .map(cells -> cells.stream().map(TableCell::getValue).collect(Collectors.toList()))
                .forEachOrdered(dataTable::addRow);
        return dataTable;
    }

    /**
     * Converts a list of Gherkin tags to simple Cucable string tags.
     *
     * @param gherkinTags a {@link Tag} list.
     * @return a {@link String} list of tags.
     */
    List<String> convertGherkinTagsToCucableTags(final List<Tag> gherkinTags) {
        return gherkinTags.stream().map(Tag::getName).collect(Collectors.toList());
    }

    /**
     * Converts a Gherkin example table to a map of columns (keys) and rows (values)
     *
     * @param exampleTable a Gherkin {@link Examples} instance.
     * @return a map where the keys are the column headers and the values are lists of strings.
     */
    Map<String, List<String>> convertGherkinExampleTableToCucableExampleMap(
            final Examples exampleTable
    ) {
        Map<String, List<String>> exampleMap;

        List<TableCell> headerCells = exampleTable.getTableHeader().getCells();
        exampleMap = headerCells.stream().collect(
                Collectors.toMap(headerCell -> "<" + headerCell.getValue() + ">",
                        headerCell -> new ArrayList<>(), (a, b) -> b, LinkedHashMap::new));
        Object[] columnKeys = exampleMap.keySet().toArray();

        List<TableRow> tableBody = exampleTable.getTableBody();
        tableBody.stream().map(TableRow::getCells).forEachOrdered(
                cells -> IntStream.range(0, cells.size()).forEachOrdered(i -> {
                    String columnKey = (String) columnKeys[i];
                    List<String> values = exampleMap.get(columnKey);
                    values.add(cells.get(i).getValue());
                }));
        return exampleMap;
    }
}
