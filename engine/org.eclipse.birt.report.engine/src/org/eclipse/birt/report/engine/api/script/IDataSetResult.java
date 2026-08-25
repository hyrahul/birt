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

package org.eclipse.birt.report.engine.api.script;

import org.eclipse.birt.core.exception.BirtException;

/**
 * A read-only cursor over the rows of a data set opened from script via
 * {@link IReportContext#openDataSet(String)}.
 */
public interface IDataSetResult {

	/**
	 * Move to the next row.
	 *
	 * @return {@code true} if a row is available
	 * @throws ScriptException
	 * @throws BirtException
	 */
	boolean next() throws ScriptException, BirtException;

	/**
	 * Get the value of a column in the current row.
	 *
	 * @param columnName the column name
	 * @return the value, or {@code null}
	 * @throws ScriptException
	 * @throws BirtException
	 */
	Object getValue(String columnName) throws ScriptException, BirtException;

	/**
	 * Get the value of a column in the current row as a string.
	 *
	 * @param columnName the column name
	 * @return the value as a string, or {@code null}
	 * @throws ScriptException
	 * @throws BirtException
	 */
	String getString(String columnName) throws ScriptException, BirtException;

	/**
	 * Release the underlying result set.
	 *
	 * @throws ScriptException
	 * @throws BirtException
	 */
	void close() throws ScriptException, BirtException;
}
