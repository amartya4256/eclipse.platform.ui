/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.workbench.texteditor.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ResourceBundle;

import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.text.tests.Accessor;

import org.eclipse.jface.util.Util;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.TextViewer;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.findandreplace.FindReplaceLogic;
import org.eclipse.ui.internal.findandreplace.SearchOptions;

/**
 * Tests the FindReplaceDialog.
 *
 * @since 3.1
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FindReplaceDialogTest {

	@Rule
	public TestName testName= new TestName();

	private Accessor fFindReplaceDialog;

	private FindReplaceLogic findReplaceLogic;

	private TextViewer fTextViewer;

	private void runEventQueue() {
		Display display= PlatformUI.getWorkbench().getDisplay();
		for (int i= 0; i < 10; i++) { // workaround for https://bugs.eclipse.org/323272
			while (display.readAndDispatch()) {
				// do nothing
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// do nothing
			}
		}
	}

	private void openFindReplaceDialog() {
		Shell shell= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		fFindReplaceDialog= new Accessor("org.eclipse.ui.texteditor.FindReplaceDialog", getClass().getClassLoader(), new Object[] { shell });
		fFindReplaceDialog.invoke("create", null);
		findReplaceLogic= (FindReplaceLogic) fFindReplaceDialog.get("findReplaceLogic");
	}

	private void openTextViewerAndFindReplaceDialog() {
		openTextViewerAndFindReplaceDialog("line\nline\nline");
	}

	private void openTextViewerAndFindReplaceDialog(String content) {
		fTextViewer= new TextViewer(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		fTextViewer.setDocument(new Document(content));
		fTextViewer.getControl().setFocus();

		Accessor fFindReplaceAction;
		fFindReplaceAction= new Accessor("org.eclipse.ui.texteditor.FindReplaceAction", getClass().getClassLoader(),
				new Class[] { ResourceBundle.class, String.class, Shell.class, IFindReplaceTarget.class },
				new Object[] { ResourceBundle.getBundle("org.eclipse.ui.texteditor.ConstructedEditorMessages"), "Editor.FindReplace.", fTextViewer.getControl().getShell(),
						fTextViewer.getFindReplaceTarget() });
		fFindReplaceAction.invoke("run", null);

		Object fFindReplaceDialogStub= fFindReplaceAction.get("fgFindReplaceDialogStub");
		if (fFindReplaceDialogStub == null)
			fFindReplaceDialogStub= fFindReplaceAction.get("fgFindReplaceDialogStubShell");
		Accessor fFindReplaceDialogStubAccessor= new Accessor(fFindReplaceDialogStub, "org.eclipse.ui.texteditor.FindReplaceAction$FindReplaceDialogStub", getClass().getClassLoader());

		fFindReplaceDialog= new Accessor(fFindReplaceDialogStubAccessor.invoke("getDialog", null), "org.eclipse.ui.texteditor.FindReplaceDialog", getClass().getClassLoader());
		findReplaceLogic= (FindReplaceLogic) fFindReplaceDialog.get("findReplaceLogic");
	}

	@After
	public void tearDown() throws Exception {
		if (fFindReplaceDialog != null) {
			fFindReplaceDialog.invoke("close", null);
			fFindReplaceDialog= null;
		}

		if (fTextViewer != null) {
			fTextViewer.getControl().dispose();
			fTextViewer= null;
		}
	}

	@Test
	public void test01InitialButtonState() {
		openFindReplaceDialog();

		assertFalse(findReplaceLogic.isActive(SearchOptions.WHOLE_WORD));
		assertTrue(findReplaceLogic.isActive(SearchOptions.WRAP));
		assertFalse(findReplaceLogic.isActive(SearchOptions.REGEX));
		Button checkbox= (Button) fFindReplaceDialog.get("fIsRegExCheckBox");
		assertTrue(checkbox.isEnabled());
		checkbox= (Button) fFindReplaceDialog.get("fWholeWordCheckBox");
		assertFalse(checkbox.isEnabled()); // there's no word in the Find field
	}

	@Test
	public void testDisableWholeWordIfRegEx() {
		openFindReplaceDialog();
		fTextViewer= new TextViewer(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		fTextViewer.setDocument(new Document("line\nline\nline"));
		fTextViewer.getControl().setFocus();

		Combo findField= (Combo) fFindReplaceDialog.get("fFindField");
		findField.setText("word");

		Button isRegExCheckBox= (Button) fFindReplaceDialog.get("fIsRegExCheckBox");
		Button wholeWordCheckbox= (Button) fFindReplaceDialog.get("fWholeWordCheckBox");

		assertTrue(isRegExCheckBox.isEnabled());
		assertTrue(wholeWordCheckbox.isEnabled());

		findReplaceLogic.updateTarget(fTextViewer.getFindReplaceTarget(), false);
		isRegExCheckBox.setSelection(true);
		wholeWordCheckbox.setSelection(true);
		fFindReplaceDialog.invoke("updateButtonState", null);

		assertTrue(isRegExCheckBox.isEnabled());
		assertFalse(wholeWordCheckbox.isEnabled());
		assertTrue(wholeWordCheckbox.getSelection());
	}

	@Test
	public void testDisableWholeWordIfNotWord() {
		openFindReplaceDialog();

		Combo findField= (Combo) fFindReplaceDialog.get("fFindField");
		Button isRegExCheckBox= (Button) fFindReplaceDialog.get("fIsRegExCheckBox");
		Button wholeWordCheckbox= (Button) fFindReplaceDialog.get("fWholeWordCheckBox");

		isRegExCheckBox.setSelection(false);
		wholeWordCheckbox.setSelection(true);
		fFindReplaceDialog.invoke("updateButtonState", null);

		findField.setText("word");
		assertTrue(isRegExCheckBox.isEnabled());
		assertTrue(wholeWordCheckbox.isEnabled());
		assertTrue(wholeWordCheckbox.getSelection());

		findField.setText("no word");
		assertTrue(isRegExCheckBox.isEnabled());
		assertFalse(wholeWordCheckbox.isEnabled());
		assertTrue(wholeWordCheckbox.getSelection());
	}

	@Test
	public void testFocusNotChangedWhenEnterPressed() {
		openTextViewerAndFindReplaceDialog();

		Combo findField= (Combo) fFindReplaceDialog.get("fFindField");
		findField.setFocus();
		findField.setText("line");
		simulateEnterInFindInputField(false);

		Shell shell= ((Shell) fFindReplaceDialog.get("fActiveShell"));
		if (shell == null && Util.isGtk()) {
			if (ScreenshotTest.isRunByGerritHudsonJob()) {
				takeScreenshot();
				return;
			} else
				fail("this test does not work on GTK unless the runtime workbench has focus. Screenshot: " + takeScreenshot());
		}

		if (Util.isMac())
			/* On the Mac, checkboxes only take focus if "Full Keyboard Access" is enabled in the System Preferences.
			 * Let's not assume that someone pressed Ctrl+F7 on every test machine... */
			return;

		assertTrue(findField.isFocusControl());

		Button wrapSearchBox= (Button) fFindReplaceDialog.get("fWrapCheckBox");
		wrapSearchBox.setFocus();
		simulateEnterInFindInputField(false);
		assertTrue(wrapSearchBox.isFocusControl());

		Button allScopeBox= (Button) fFindReplaceDialog.get("fGlobalRadioButton");
		allScopeBox.setFocus();
		simulateEnterInFindInputField(false);
		assertTrue(allScopeBox.isFocusControl());
	}

	private String takeScreenshot() {
		return ScreenshotTest.takeScreenshot(FindReplaceDialogTest.class, testName.getMethodName(), System.out);
	}

	@Test
	public void testFocusNotChangedWhenButtonMnemonicPressed() {
		if (Util.isMac())
			return; // Mac doesn't support mnemonics.

		openTextViewerAndFindReplaceDialog();

		Combo findField= (Combo) fFindReplaceDialog.get("fFindField");
		findField.setText("line");
		final Event event= new Event();

		runEventQueue();
		Shell shell= ((Shell) fFindReplaceDialog.get("fActiveShell"));
		if (shell == null && Util.isGtk())
			if (ScreenshotTest.isRunByGerritHudsonJob()) {
				takeScreenshot();
				return;
			} else
				fail("this test does not work on GTK unless the runtime workbench has focus. Screenshot: " + takeScreenshot());

		Button wrapSearchBox= (Button) fFindReplaceDialog.get("fWrapCheckBox");
		wrapSearchBox.setFocus();
		event.detail= SWT.TRAVERSE_MNEMONIC;
		event.character= 'n';
		event.doit= false;
		wrapSearchBox.traverse(SWT.TRAVERSE_MNEMONIC, event);
		runEventQueue();
		assertTrue(wrapSearchBox.isFocusControl());

		Button allScopeBox= (Button) fFindReplaceDialog.get("fGlobalRadioButton");
		allScopeBox.setFocus();
		event.detail= SWT.TRAVERSE_MNEMONIC;
		event.doit= false;
		allScopeBox.traverse(SWT.TRAVERSE_MNEMONIC, event);
		runEventQueue();
		assertTrue(allScopeBox.isFocusControl());

		event.detail= SWT.TRAVERSE_MNEMONIC;
		event.character= 'r';
		event.doit= false;
		allScopeBox.traverse(SWT.TRAVERSE_MNEMONIC, event);
		runEventQueue();
		assertTrue(allScopeBox.isFocusControl());
	}

	@Test
	public void testShiftEnterReversesSearchDirection() {
		openTextViewerAndFindReplaceDialog();

		Combo findField= (Combo) fFindReplaceDialog.get("fFindField");
		findField.setText("line");
		IFindReplaceTarget target= findReplaceLogic.getTarget();
		runEventQueue();
		Shell shell= ((Shell) fFindReplaceDialog.get("fActiveShell"));
		if (shell == null && Util.isGtk()) {
			if (ScreenshotTest.isRunByGerritHudsonJob()) {
				takeScreenshot();
				return;
			} else
				fail("this test does not work on GTK unless the runtime workbench has focus. Screenshot: " + takeScreenshot());
		}
		simulateEnterInFindInputField(false);
		assertEquals(0, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		simulateEnterInFindInputField(false);
		assertEquals(5, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		simulateEnterInFindInputField(true);
		assertEquals(0, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		Button forwardRadioButton= (Button) fFindReplaceDialog.get("fForwardRadioButton");
		forwardRadioButton.setSelection(false);
		simulateEnterInFindInputField(true);
		assertEquals(5, (target.getSelection()).x);
	}

	@Test
	public void testChangeInputForIncrementalSearch() {
		openTextViewerAndFindReplaceDialog();
		Button incremenalSearchButton= ((Button) fFindReplaceDialog.get("fIncrementalCheckBox"));
		incremenalSearchButton.setSelection(true);
		fFindReplaceDialog.invoke("updateButtonState", null);

		Combo findField= (Combo) fFindReplaceDialog.get("fFindField");
		findField.setText("lin");
		IFindReplaceTarget target= findReplaceLogic.getTarget();
		assertEquals(0, (target.getSelection()).x);
		assertEquals(findField.getText().length(), (target.getSelection()).y);

		findField.setText("line");
		assertEquals(0, (target.getSelection()).x);
		assertEquals(findField.getText().length(), (target.getSelection()).y);

		incremenalSearchButton.setSelection(false);
		fFindReplaceDialog.invoke("updateButtonState", null);
	}

	@Test
	public void testFindWithWholeWordEnabledWithMultipleWords() {
		openTextViewerAndFindReplaceDialog("two words");
		Combo findField= (Combo) fFindReplaceDialog.get("fFindField");
		findField.setText("two");
		Button wholeWordButton= ((Button) fFindReplaceDialog.get("fWholeWordCheckBox"));
		wholeWordButton.setSelection(true);
		runEventQueue();
		IFindReplaceTarget target= findReplaceLogic.getTarget();
		assertEquals(0, (target.getSelection()).x);
		assertEquals(0, (target.getSelection()).y);

		findField.setText("two wo");
		runEventQueue();
		assertFalse(wholeWordButton.getEnabled());
		assertTrue(wholeWordButton.getSelection());

		simulateEnterInFindInputField(false);
		assertEquals(0, (target.getSelection()).x);
		assertEquals(findField.getText().length(), (target.getSelection()).y);
	}

	private void simulateEnterInFindInputField(boolean shiftPressed) {
		final Event event= new Event();
		event.type= SWT.Traverse;
		event.detail= SWT.TRAVERSE_RETURN;
		event.character= SWT.CR;
		if (shiftPressed) {
			event.stateMask= SWT.SHIFT;
		}
		Combo findField= (Combo) fFindReplaceDialog.get("fFindField");
		findField.traverse(SWT.TRAVERSE_RETURN, event);
		runEventQueue();
	}


}
