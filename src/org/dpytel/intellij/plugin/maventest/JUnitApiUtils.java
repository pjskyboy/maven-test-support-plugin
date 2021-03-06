/*
 * Copyright 2014 Dawid Pytel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dpytel.intellij.plugin.maventest;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.junit.JUnitConfiguration;
import com.intellij.execution.junit.JUnitConfigurationType;
import com.intellij.execution.junit2.ui.properties.JUnitConsoleProperties;
import com.intellij.openapi.project.Project;

/**
 *
 */
public class JUnitApiUtils {
    public static JUnitConsoleProperties createConsoleProperties(Project project) {
        JUnitConfigurationType jUnitConfigurationType = JUnitConfigurationType.getInstance();
        if (jUnitConfigurationType == null) {
            return null;
        }
        ConfigurationFactory configurationFactory = jUnitConfigurationType.getConfigurationFactories()[0];
        JUnitConfiguration myConfiguration = new JUnitConfiguration("maven", project, configurationFactory);
        Executor executor = new DefaultRunExecutor();
        final JUnitConsoleProperties consoleProperties = new JUnitConsoleProperties(myConfiguration, executor);
        return consoleProperties;
    }
}
