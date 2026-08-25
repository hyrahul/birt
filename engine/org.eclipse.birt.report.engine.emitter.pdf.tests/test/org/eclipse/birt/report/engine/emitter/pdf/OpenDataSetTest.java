
/*******************************************************************************
 * Copyright (c) 2026 Actuate Corporation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation (issue #1351)
 *******************************************************************************/
package org.eclipse.birt.report.engine.emitter.pdf;

import java.io.File;
import java.util.List;

import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.PDFRenderOption;

/**
 * Covers {@code reportContext.openDataSet(...)} (issue #1351): reading a data
 * set from a beforeFactory/onPrepare script, before the report is laid out.
 * <p>
 * These tests assert on render completion and on engine errors recorded via
 * {@code task.getErrors()} - script exceptions are caught internally rather
 * than thrown out of {@code run()}. They do not verify glyph-level PDF content,
 * since this module has no PDF text extraction available. The actual effect of
 * a script-driven design change (opendataset-regionconfig.rptdesign: an
 * onPrepare script reads the first row of "RegionConfig" and rewrites a label's
 * text with it) was verified manually - see the PR description for the console
 * output and rendered PDF.
 */
public class OpenDataSetTest extends EngineCase {

	private static final String PKG = "test/org/eclipse/birt/report/engine/emitter/pdf/";

	private File outputDir;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		outputDir = new File(System.getProperty("java.io.tmpdir"), "birt-opendataset-test");
		outputDir.mkdirs();
	}

	/**
	 * A design whose onPrepare/beforeFactory scripts call openDataSet and use the
	 * result to modify the design must render without error, record no engine
	 * errors, and produce a non-empty PDF. Uses the default (empty) value of the
	 * countryFilter parameter, which the query filters on - the onPrepare script
	 * handles the resulting empty result set safely.
	 *
	 * @throws Exception
	 */
	public void testRendersWithoutError() throws Exception {
		IRunAndRenderTask task = createRunAndRenderTask(PKG + "opendataset-regionconfig.rptdesign");
		try {
			File out = renderTo(task, "regionconfig.pdf");
			assertTrue("Missing output: " + out, out.isFile());
			assertTrue("Empty output: " + out, out.length() > 0L);
			assertTrue("Expected no errors: " + task.getErrors(), task.getErrors().isEmpty());
		} finally {
			task.close();
		}
	}

	/**
	 * A data set opened from script that does not exist must be reported clearly.
	 * The exception is not thrown out of run() - it is caught internally and
	 * recorded as an engine error - so this checks task.getErrors() rather than
	 * expecting an exception.
	 *
	 * @throws Exception
	 */
	public void testMissingDataSetFailsClearly() throws Exception {
		IRunAndRenderTask task = createRunAndRenderTask(PKG + "opendataset-missing.rptdesign");
		try {
			renderTo(task, "missing.pdf");
			List<?> errors = task.getErrors();
			assertFalse("Expected an error for a non-existent data set", errors.isEmpty());
			String combined = errors.toString();
			assertTrue("Expected the error to name the missing data set: " + combined,
					combined.contains("NoSuchDataSet"));
		} finally {
			task.close();
		}
	}

	/**
	 * A script that opens a data set and never calls close() must not prevent the
	 * render from completing or cause a script error.
	 *
	 * @throws Exception
	 */
	public void testMissingCloseDoesNotBreakRender() throws Exception {
		IRunAndRenderTask task = createRunAndRenderTask(PKG + "opendataset-noclose.rptdesign");
		try {
			File out = renderTo(task, "noclose.pdf");
			assertTrue("Missing output: " + out, out.isFile());
			assertTrue("Empty output: " + out, out.length() > 0L);
			assertTrue("Expected no errors: " + task.getErrors(), task.getErrors().isEmpty());
		} finally {
			task.close();
		}
	}

	/**
	 * The RegionConfig data set is bound to a report parameter (countryFilter, used
	 * in the query as "where COUNTRY like ?"). Setting the parameter before running
	 * and reading it from openDataSet must filter the result as expected,
	 * confirming the report's resolved parameter values reach a query executed from
	 * script. Verified manually (console output) that the query returns 0 rows with
	 * the default empty value and 2 rows ("A%") - this test checks the render
	 * completes cleanly with the parameter set.
	 *
	 * @throws Exception
	 */
	public void testOpenDataSetWithParameter() throws Exception {
		IRunAndRenderTask task = createRunAndRenderTask(PKG + "opendataset-regionconfig.rptdesign");
		try {
			task.setParameterValue("countryFilter", "A%");
			File out = renderTo(task, "param.pdf");
			assertTrue(out.isFile());
			assertTrue("Expected no errors: " + task.getErrors(), task.getErrors().isEmpty());
		} finally {
			task.close();
		}
	}

	/** Render the given task to a PDF in the test output directory. */
	private File renderTo(IRunAndRenderTask task, String fileName) throws Exception {
		File out = new File(outputDir, fileName);
		PDFRenderOption options = new PDFRenderOption();
		options.setOutputFormat("pdf");
		options.setOutputFileName(out.getAbsolutePath());
		task.setRenderOption(options);
		task.run();
		return out;
	}
}