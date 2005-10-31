/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.ui.navigator.internal.extensions;

import java.util.Comparator;
/**
* <p>
* <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
* progress. There is a guarantee neither that this API will work nor that it will remain the same.
* Please do not use this API without consulting with the Platform/UI team.
* </p>
* 
* @since 3.2
*/
public class IdentityComparator implements Comparator {

	public static final IdentityComparator INSTANCE = new IdentityComparator();
	
	public int compare(Object lvalue, Object rvalue) {
		return 0;
	}
	
	public boolean equals(Object anObject) {
		return this == anObject; 
	}
}

