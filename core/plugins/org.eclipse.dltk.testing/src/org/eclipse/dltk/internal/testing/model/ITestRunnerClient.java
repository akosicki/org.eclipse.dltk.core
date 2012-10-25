/*******************************************************************************
 * Copyright (c) 2008 xored software, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
 *******************************************************************************/
package org.eclipse.dltk.internal.testing.model;

import org.eclipse.dltk.testing.model.ITestElement;

public interface ITestRunnerClient {

	/**
	 * Answers if this client is still running.
	 */
	boolean isRunning();

	/**
	 * 
	 */
	void stopTest();

	/**
	 * Advises this client to stop waiting for additional events after the test
	 * run was completed. Is called by the {@link TestRunSession} when the
	 * corresponding ILaunch is terminated.
	 */
	void stopWaiting();

	/**
	 * @param testId
	 * @param className
	 * @param testName
	 *
	 * @deprecated please use the {@link ITestRunnerClient#rerunTest(ITestElement, String)} instead
	 */
	@Deprecated
	void rerunTest(String testId, String className, String testName);

	/**
	 * Advise this client to rerun the selected test element.
	 * 
	 * @param element
	 */
	void rerunTest(ITestElement element, String launchMode);

	/**
	 * @param listeners
	 */
	void startListening(ITestRunListener2 listener);

}
