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

package org.dpytel.intellij.plugin.maventest.model;

import com.intellij.execution.junit2.TestProxy;
import com.intellij.execution.junit2.states.NotFailedState;
import com.intellij.execution.junit2.ui.model.JUnitRunningModel;
import com.intellij.execution.junit2.ui.properties.JUnitConsoleProperties;
import com.intellij.mock.MockVirtualFile;
import com.intellij.openapi.util.Disposer;
import com.intellij.testFramework.PlatformUltraLiteTestFixture;
import org.jetbrains.idea.maven.project.MavenProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 *
 */
public class TestsSummaryTest {

    private JUnitRunningModel model;

    @Before
    public void setUp() throws Exception {
        PlatformUltraLiteTestFixture.getFixture().setUp();
    }

    @After
    public void tearDown() throws Exception {
        if (model != null) {
            Disposer.dispose(model);
        }
        PlatformUltraLiteTestFixture.getFixture().tearDown();
    }

    @Test
    public void testNoTests() throws Exception {
        model = ModelBuilder.newModel().build();

        TestsSummary summary = TestsSummary.createSummary(model);

        assertThat(summary.getTotal(), is(0));
        assertThat(summary.getErrors(), is(0));
        assertThat(summary.getFailed(), is(0));
        assertThat(summary.getSkipped(), is(0));
    }

    @Test
    public void testOneTestPassed() throws Exception {
        TestProxy methodTest = createPassedTest();
        model = ModelBuilder
            .newModel()
            .withSuite("org.dpytel.SingleErrorTestSuite", methodTest)
            .build();

        TestsSummary summary = TestsSummary.createSummary(model);

        assertThat(summary.getTotal(), is(1));
        assertThat(summary.getErrors(), is(0));
        assertThat(summary.getFailed(), is(0));
        assertThat(summary.getSkipped(), is(0));
    }

    @Test
    public void testOneTestError() throws Exception {
        TestProxy methodTest = createErrorTest();
        model = ModelBuilder
            .newModel()
            .withSuite("org.dpytel.SingleFailingTestSuite", methodTest)
            .build();

        TestsSummary summary = TestsSummary.createSummary(model);

        assertThat(summary.getTotal(), is(1));
        assertThat(summary.getErrors(), is(1));
        assertThat(summary.getFailed(), is(0));
        assertThat(summary.getSkipped(), is(0));
    }

    @Test
    public void testOneTestFail() throws Exception {
        TestProxy methodTest = createFailedTest();
        model = ModelBuilder
            .newModel()
            .withSuite("org.dpytel.SinglePassingTestSuite", methodTest)
            .build();

        TestsSummary summary = TestsSummary.createSummary(model);

        assertThat(summary.getTotal(), is(1));
        assertThat(summary.getErrors(), is(0));
        assertThat(summary.getFailed(), is(1));
        assertThat(summary.getSkipped(), is(0));
    }

    @Test
    public void testOneTestSkipped() throws Exception {
        TestProxy methodTest = createSkippedTest();
        model = ModelBuilder
            .newModel()
            .withSuite("org.dpytel.SingleSkippedTestSuite", methodTest)
            .build();

        TestsSummary summary = TestsSummary.createSummary(model);

        assertThat(summary.getTotal(), is(1));
        assertThat(summary.getErrors(), is(0));
        assertThat(summary.getFailed(), is(0));
        assertThat(summary.getSkipped(), is(1));
    }

    @Test
    public void testTwoSuccessfulTests() throws Exception {
        TestProxy methodTest1 = createPassedTest();
        TestProxy methodTest2 = createPassedTest();
        model = ModelBuilder
            .newModel()
            .withSuite("org.dpytel.SingleSkippedTestSuite", methodTest1, methodTest2)
            .build();

        TestsSummary summary = TestsSummary.createSummary(model);

        assertThat(summary.getTotal(), is(2));
        assertThat(summary.getErrors(), is(0));
        assertThat(summary.getFailed(), is(0));
        assertThat(summary.getSkipped(), is(0));
    }

    private TestProxy createSkippedTest() {
        TestProxy methodTest = new TestProxy(new TestMethodInfo("org.dpytel.TestClass", "testMethod"));
        methodTest.setState(new SkippedState(methodTest, ""));
        return methodTest;
    }

    private TestProxy createFailedTest() {
        TestProxy methodTest = new TestProxy(new TestMethodInfo("org.dpytel.TestClass", "testMethod"));
        methodTest.setState(FailOrErrorState.createFailedState(""));
        return methodTest;
    }

    private TestProxy createErrorTest() {
        TestProxy methodTest = new TestProxy(new TestMethodInfo("org.dpytel.TestClass", "testMethod"));
        methodTest.setState(FailOrErrorState.createErrorState(""));
        return methodTest;
    }

    private TestProxy createPassedTest() {
        TestProxy methodTest = new TestProxy(new TestMethodInfo("org.dpytel.TestClass", "testMethod"));
        methodTest.setState(NotFailedState.createPassed());
        return methodTest;
    }

    private static class ModelBuilder {

        private List<TestProxy> suites = new ArrayList<TestProxy>();

        public static ModelBuilder newModel() {
            return new ModelBuilder();
        }

        public ModelBuilder withSuite(String name, TestProxy... tests) {
            TestProxy classTest = new TestProxy(new TestSuiteInfo(name));
            classTest.setState(new AggregatedState(classTest));
            for (TestProxy test : tests) {
                classTest.addChild(test);
            }
            suites.add(classTest);
            return this;
        }

        public JUnitRunningModel build() {
            TestProxy root = RootTestBuilder.fromMavenProject(new MavenProject(new MockVirtualFile("."))).build();
            for (TestProxy suite : suites) {
                root.addChild(suite);
            }
            return new JUnitRunningModel(root, mock(JUnitConsoleProperties.class));
        }

    }
}
