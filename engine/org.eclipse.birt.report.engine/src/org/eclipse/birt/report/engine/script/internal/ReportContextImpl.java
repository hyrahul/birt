/*******************************************************************************
 * Copyright (c) 2005, 2026 Actuate Corporation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.engine.script.internal;

import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

import org.eclipse.birt.core.data.DataType;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.data.engine.api.IBasePreparedQuery;
import org.eclipse.birt.data.engine.api.IQueryDefinition;
import org.eclipse.birt.data.engine.api.IQueryResults;
import org.eclipse.birt.data.engine.api.IResultIterator;
import org.eclipse.birt.data.engine.api.IScriptExpression;
import org.eclipse.birt.data.engine.api.querydefn.BaseExpression;
import org.eclipse.birt.data.engine.api.querydefn.InputParameterBinding;
import org.eclipse.birt.data.engine.api.querydefn.QueryDefinition;
import org.eclipse.birt.data.engine.api.querydefn.ScriptExpressionUtil;
import org.eclipse.birt.report.data.adapter.api.DataRequestSession;
import org.eclipse.birt.report.engine.adapter.ModelDteApiAdapter;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IEngineTask;
import org.eclipse.birt.report.engine.api.IHTMLImageHandler;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.impl.Image;
import org.eclipse.birt.report.engine.api.impl.QueryUtil;
import org.eclipse.birt.report.engine.api.script.IDataSetResult;
import org.eclipse.birt.report.engine.api.script.IReportContext;
import org.eclipse.birt.report.engine.executor.ExecutionContext;
import org.eclipse.birt.report.engine.i18n.MessageConstants;
import org.eclipse.birt.report.engine.ir.Expression;
import org.eclipse.birt.report.model.api.DataSetHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.elements.structures.ResultSetColumn;

import com.ibm.icu.text.MessageFormat;
import com.ibm.icu.util.TimeZone;

/**
 * Implementation of the IReportContext interface
 */
public class ReportContextImpl implements IReportContext {

	protected ExecutionContext context;

	public ReportContextImpl(ExecutionContext context) {
		this.context = context;
	}

	@Override
	public IReportRunnable getReportRunnable() {
		return context.getRunnable();
	}

	@Override
	public Map getAppContext() {
		return context.getAppContext();
	}

	public void setAppContext(Map appContext) {
		context.setAppContext(appContext);
	}

	@Override
	public void setGlobalVariable(String name, Object obj) {
		context.registerBean(name, obj);
	}

	@Override
	public void deleteGlobalVariable(String name) {
		context.registerBean(name, null);
	}

	@Override
	public Object getGlobalVariable(String name) {
		return context.getBeans().get(name);
	}

	@Override
	public void setPersistentGlobalVariable(String name, Serializable obj) {
		context.registerGlobalBean(name, obj);
	}

	@Override
	public void deletePersistentGlobalVariable(String name) {
		context.unregisterGlobalBean(name);
	}

	@Override
	public Object getPersistentGlobalVariable(String name) {
		return context.getGlobalBeans().get(name);
	}

	public void setRegisteredPersistantObjects(Map persistantMap) {
		context.registerBeans(persistantMap);
	}

	@Override
	public Object getParameterValue(String name) {
		return context.getParameterValue(name);
	}

	@Override
	public void setParameterValue(String name, Object value) {
		context.setParameterValue(name, value);
	}

	@Override
	public Object getPageVariable(String name) {
		return context.getPageVariable(name);
	}

	@Override
	public void setPageVariable(String name, Object value) {
		context.setPageVariable(name, value);
	}

	@Override
	public Locale getLocale() {
		return context.getLocale();
	}

	@Override
	public TimeZone getTimeZone() {
		return context.getTimeZone();
	}

	@Override
	public String getOutputFormat() {
		return context.getOutputFormat();
	}

	@Override
	public IRenderOption getRenderOption() {
		return context.getRenderOption();
	}

	@Override
	public Object getHttpServletRequest() {
		return getAppContext().get(EngineConstants.APPCONTEXT_BIRT_VIEWER_HTTPSERVET_REQUEST);
	}

	@Override
	public String getMessage(String key) {
		return context.getDesign().getMessage(key);
	}

	@Override
	public String getMessage(String key, Locale locale) {
		return context.getDesign().getMessage(key, locale);
	}

	@Override
	public String getMessage(String key, Object[] params) {
		String msg = context.getDesign().getMessage(key);
		if (msg == null) {
			return "";
		}
		return MessageFormat.format(msg, params);
	}

	@Override
	public String getMessage(String key, Locale locale, Object[] params) {
		String msg = context.getDesign().getMessage(key, locale);
		if (msg == null) {
			return "";
		}
		MessageFormat formatter = new MessageFormat(msg, locale);
		return formatter.format(params, new StringBuffer(), null).toString();
	}

	@Override
	public Object getParameterDisplayText(String name) {
		return context.getParameterDisplayText(name);
	}

	@Override
	public void setParameterDisplayText(String name, String displayText) {
		context.setParameterDisplayText(name, displayText);
	}

	@Override
	public int getTaskType() {
		IEngineTask task = context.getEngineTask();
		if (task != null) {
			return task.getTaskType();
		}
		return IEngineTask.TASK_UNKNOWN;
	}

	@Override
	public ReportDesignHandle getDesignHandle() {
		return (ReportDesignHandle) getReportRunnable().getDesignHandle();
	}

	@Override
	public URL getResource(String resourceName) {
		return context.getResource(resourceName);
	}

	@Override
	public String getResourceRenderURL(String resourceName) {
		IRenderOption option = context.getRenderOption();
		if (option != null) {
			IHTMLImageHandler imageHandler = option.getImageHandler();
			if (imageHandler != null) {
				URL resourceUrl = context.getResource(resourceName);
				if (resourceUrl != null) {
					Image image = new Image(resourceUrl.toExternalForm());
					if (image.getSource() == Image.FILE_IMAGE) {
						return imageHandler.onFileImage(image, this);
					}
					return imageHandler.onURLImage(image, this);
				}
			}
		}
		return resourceName;
	}

	@Override
	public Object evaluate(String script) throws BirtException {
		if (null != script && script.length() > 0) {
			return context.evaluate(script);
		}
		return null;
	}

	@Override
	public Object evaluate(String language, String script) throws BirtException {
		if (null != script && script.length() > 0) {
			return context.evaluateInlineScript(language, script);
		}
		return null;
	}

	@Override
	public ClassLoader getApplicationClassLoader() {
		return context.getApplicationClassLoader();
	}

	@Override
	public Object evaluate(Expression script) throws BirtException {
		return context.evaluate(script);
	}

	@Override
	public void cancel() {
		cancel(null);
	}

	@Override
	public void cancel(String msg) {
		IEngineTask task = context.getEngineTask();
		if (task != null) {
			task.cancel(msg);
		}
	}

	@Override
	public boolean isReportDocumentFinished() {
		return context.isReportDocumentFinished();
	}

	/**
	 * Open a data set defined in the report and read its rows, using the report's
	 * own data engine and already-resolved parameters. All columns of the data set
	 * are selected; no filters, sorts, or row limits are applied beyond any input
	 * parameter bindings supplied.
	 * <p>
	 * Optionally binds input parameter values by name, independent of any
	 * report-level parameter of the same name. Values are bound the same way the
	 * ODA driver's own parameter placeholders are - via the data engine's parameter
	 * binding, not by building the query text - so this does not carry the SQL
	 * injection risk of assembling a WHERE clause manually.
	 * <p>
	 * This is available from script exit points where the report design and
	 * parameters are already resolved, such as {@code beforeFactory} and
	 * {@code onPrepare} - both of which run before the report is laid out, so the
	 * result can be used to shape the design.
	 *
	 * @param name   the data set name
	 * @param params input parameter values keyed by parameter name, or {@code null}
	 *               if the data set has no parameters
	 * @return a cursor over the data set rows; the caller must close it
	 * @throws BirtException if the data set cannot be found or executed
	 */
	@Override
	public IDataSetResult openDataSet(String name, Map<String, Object> params) throws BirtException {
		ReportDesignHandle design = context.getReportDesign();
		if (design == null) {
			throw new EngineException(MessageConstants.REPORT_DESIGN_NOT_AVAILABLE_EXCEPTION);
		}
		DataSetHandle dataSet = design.findDataSet(name);
		if (dataSet == null) {
			throw new EngineException(MessageConstants.DATA_SET_NOT_FOUND_EXCEPTION, name);
		}

		context.openDataEngine();
		DataRequestSession session = context.getDataEngine().getDTESession();

		if (dataSet.getCachedMetaDataHandle() == null) {
			session.refreshMetaData(dataSet);
		}
		QueryDefinition query = new QueryDefinition();
		query.setDataSetName(dataSet.getQualifiedName());
		for (ResultSetColumn column : QueryUtil.getResultSetColumns(dataSet).values()) {
			QueryUtil.addBinding(query, column);
		}

		if (params != null) {
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				if (entry.getValue() == null) {
					continue;
				}
				query.addInputParamBinding(new InputParameterBinding(entry.getKey(), toExpression(entry.getValue())));
			}
		}

		new ModelDteApiAdapter(context).defineDataSet(dataSet, session);
		session.registerQueries(new IQueryDefinition[] { query });
		IBasePreparedQuery prepared = session.prepare(query);
		IQueryResults results = (IQueryResults) session.execute(prepared, null, context.getScriptContext());

		return new DataSetResult(results);
	}

	/**
	 * Build a constant expression from an input parameter value, with the data type
	 * set according to the value's Java type. {@code createConstantExpression}
	 * leaves the data type as {@code UNKNOWN_TYPE} by default, which is not
	 * reliable for non-string values.
	 *
	 * @param value the parameter value; never null
	 * @return a typed constant expression
	 */
	private IScriptExpression toExpression(Object value) {
		IScriptExpression expr = ScriptExpressionUtil.createConstantExpression(String.valueOf(value));
		int dataType;
		if (value instanceof Integer || value instanceof Long) {
			dataType = DataType.INTEGER_TYPE;
		} else if (value instanceof Double || value instanceof Float) {
			dataType = DataType.DOUBLE_TYPE;
		} else if (value instanceof BigDecimal) {
			dataType = DataType.DECIMAL_TYPE;
		} else if (value instanceof java.util.Date) {
			dataType = DataType.DATE_TYPE;
		} else if (value instanceof Boolean) {
			dataType = DataType.BOOLEAN_TYPE;
		} else {
			dataType = DataType.STRING_TYPE;
		}
		((BaseExpression) expr).setDataType(dataType);
		return expr;
	}

	/**
	 * Default {@link IDataSetResult} implementation, wrapping the query results
	 * produced by {@link #openDataSet(String)}.
	 */
	private static class DataSetResult implements IDataSetResult {
		private final IQueryResults results;
		private IResultIterator iterator;

		DataSetResult(IQueryResults results) throws BirtException {
			this.results = results;
			this.iterator = results.getResultIterator();
		}

		@Override
		public boolean next() throws BirtException {
			return iterator != null && iterator.next();
		}

		@Override
		public Object getValue(String columnName) throws BirtException {
			return iterator.getValue(columnName);
		}

		@Override
		public String getString(String columnName) throws BirtException {
			return iterator.getString(columnName);
		}

		@Override
		public void close() throws BirtException {
			if (iterator != null) {
				iterator.close();
				iterator = null;
			}
			results.close();
		}
	}
}
