/*******************************************************************************
 * Copyright (c) 2023 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vector Informatik GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.findandreplace.status;

public class FindStatus implements IStatus {

	public enum StatusCode {
		NO_MATCH,
		WRAPPED, READONLY,
	}

	private StatusCode messageCode;
	private boolean isError;

	public FindStatus(StatusCode errorCode, boolean isError) {
		this.messageCode = errorCode;
		this.isError = isError;
	}

	public StatusCode getMessageCode() {
		return messageCode;
	}

	@Override
	public <T> T accept(IStatusVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public boolean isError() {
		return this.isError;
	}

}
