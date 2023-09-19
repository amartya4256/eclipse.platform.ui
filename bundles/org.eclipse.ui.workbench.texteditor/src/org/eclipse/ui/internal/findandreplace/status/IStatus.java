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

/**
 * Interface for statuses that can occur while performing
 * Find/Replace-operations.
 */
public interface IStatus {
	public <T> T accept(IStatusVisitor<T> visitor);

	/**
	 * Whether the status represents an Error in the Find/Replace-Operation.
	 *
	 * @return the error state.
	 */
	public boolean isError();
}
