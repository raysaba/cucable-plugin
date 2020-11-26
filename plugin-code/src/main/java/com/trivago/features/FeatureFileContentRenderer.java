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

package com.trivago.features;

import com.trivago.vo.DataTable;
import com.trivago.vo.SingleScenario;
import com.trivago.vo.Step;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

@Singleton
class FeatureFileContentRenderer {

    private static final String LINE_SEPARATOR = System.lineSeparator();

    /**
     * Get the complete content based on multiple features that can be written to a valid feature file.
     *
     * @return the feature file content.
     */
    private String getRenderedFeatureFileContent(List<SingleScenario> singleScenarios) {
        StringBuilder renderedContent = new StringBuilder();

        SingleScenario firstScenario = singleScenarios.get(0);

        addLanguage(renderedContent, firstScenario.getFeatureLanguage());
        addTags(renderedContent, firstScenario.getFeatureTags());
        addNameAndDescription(
                renderedContent,
                firstScenario.getFeatureName(),
                firstScenario.getFeatureDescription()
        );

        addBackgroundSteps(renderedContent, firstScenario.getBackgroundSteps());

        for (SingleScenario singleScenario : singleScenarios) {
            renderedContent.append(LINE_SEPARATOR);
            List<String> scenarioTags = singleScenario.getScenarioTags();
            if (scenarioTags != null && firstScenario.getFeatureTags() != null) {
                scenarioTags.removeAll(firstScenario.getFeatureTags());
            }
            addTags(renderedContent, scenarioTags);
            addTags(renderedContent, singleScenario.getExampleTags());

            addNameAndDescription(
                    renderedContent,
                    singleScenario.getScenarioName(),
                    singleScenario.getScenarioDescription()
            );
            addSteps(renderedContent, singleScenario.getSteps());
        }

        addComments(renderedContent, firstScenario.getFeatureFilePath());
        return renderedContent.toString();
    }

    /**
     * Get the complete content based on a single feature that can be written to a valid feature file.
     *
     * @return the feature file content.
     */
    String getRenderedFeatureFileContent(SingleScenario singleScenario) {
        return getRenderedFeatureFileContent(Collections.singletonList(singleScenario));
    }

    /**
     * Adds the feature language to the generated feature file content.
     *
     * @param stringBuilder   The current feature {@link StringBuilder} instance.
     * @param featureLanguage The feature language.
     */
    private void addLanguage(final StringBuilder stringBuilder, final String featureLanguage) {
        if (featureLanguage == null || featureLanguage.isEmpty()) {
            return;
        }
        stringBuilder.append("# language: ")
                     .append(featureLanguage)
                     .append(LINE_SEPARATOR)
                     .append(LINE_SEPARATOR);
    }

    /**
     * Adds the "Generated by Cucable" line to the generated feature file content.
     *
     * @param stringBuilder   The current feature {@link StringBuilder} instance.
     * @param featureFilePath The path to the source feature file.
     */
    private void addComments(final StringBuilder stringBuilder, String featureFilePath) {
        stringBuilder.append(LINE_SEPARATOR)
                     .append("# Source feature: ")
                     .append(featureFilePath.replace("\\", "/"))
                     .append(LINE_SEPARATOR)
                     .append("# Generated by Cucable")
                     .append(LINE_SEPARATOR);
    }

    /**
     * Adds the rendered steps to the generated feature file content.
     *
     * @param stringBuilder The current feature {@link StringBuilder} instance.
     * @param steps         The {@link Step} list.
     */
    private void addSteps(final StringBuilder stringBuilder, final List<Step> steps) {
        if (steps == null) {
            return;
        }
        for (Step step : steps) {
            stringBuilder.append(step.getName()).append(LINE_SEPARATOR);
            stringBuilder.append(formatDocString(step.getDocString()));
            stringBuilder.append(formatDataTableString(step.getDataTable()));
        }
    }

    /**
     * Adds the rendered background steps to the generated feature file content.
     *
     * @param stringBuilder   The current feature {@link StringBuilder} instance.
     * @param backgroundSteps The background {@link Step} list.
     */
    private void addBackgroundSteps(StringBuilder stringBuilder, List<Step> backgroundSteps) {
        if (backgroundSteps == null || backgroundSteps.isEmpty()) {
            return;
        }
        stringBuilder.append(LINE_SEPARATOR).append("Background:").append(LINE_SEPARATOR);
        addSteps(stringBuilder, backgroundSteps);
    }

    /**
     * Adds the feature or scenario name and description to the generated feature file content.
     *
     * @param stringBuilder The current feature {@link StringBuilder} instance.
     * @param name          The feature or scenario name.
     * @param description   The feature or scenario description.
     */
    private void addNameAndDescription(
            final StringBuilder stringBuilder,
            final String name,
            final String description
    ) {
        stringBuilder.append(name);
        if (description != null && !description.isEmpty()) {
            stringBuilder.append(LINE_SEPARATOR).append(description);
        }
        stringBuilder.append(LINE_SEPARATOR);
    }

    /**
     * Adds the tags to the generated feature file content.
     *
     * @param stringBuilder The current feature {@link StringBuilder} instance.
     * @param tags          The list of {@link String} tags.
     */
    private void addTags(final StringBuilder stringBuilder, final List<String> tags) {
        if (tags == null) {
            return;
        }
        for (String tag : tags) {
            stringBuilder.append(tag).append(LINE_SEPARATOR);
        }
    }

    /**
     * Turns a {@link DataTable} instance into a printable string.
     *
     * @param dataTable the {@link DataTable} instance.
     * @return the processed data table {@link String}.
     */
    private String formatDataTableString(final DataTable dataTable) {
        if (dataTable == null) {
            return "";
        }
        char dataTableSeparator = '|';
        StringBuilder dataTableStringBuilder = new StringBuilder();
        for (List<String> rowValues : dataTable.getRows()) {
            dataTableStringBuilder.append(dataTableSeparator);
            for (String rowValue : rowValues) {
                dataTableStringBuilder.append(rowValue).append(dataTableSeparator);
            }
            dataTableStringBuilder.append(LINE_SEPARATOR);
        }
        return dataTableStringBuilder.toString();
    }

    /**
     * Turns a DocString into a printable {@link String} including quotes.
     *
     * @param docString the DocString {@link String}.
     * @return the processed DocString {@link String}.
     */
    private String formatDocString(final String docString) {
        if (docString == null || docString.isEmpty()) {
            return "";
        }
        return "\"\"\"" + LINE_SEPARATOR + docString + LINE_SEPARATOR + "\"\"\"" + LINE_SEPARATOR;
    }
}
