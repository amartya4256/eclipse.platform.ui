/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.quickdiff.compare.rangedifferencer;

import org.eclipse.jface.text.*;

/**
 * Implements the <code>IRangeComparator</code> interface for lines in a document.
 * A <code>DocLineComparator</code> is used as the input for the <code>RangeDifferencer</code>
 * engine to perform a line oriented compare on documents.
 * <p>
 * A <code>DocLineComparator</code> doesn't know anything about line separators because
 * its notion of lines is solely defined in the underlying <code>IDocument</code>.
 */
public final class DocLineComparator implements IRangeComparator {

	private final IDocument fDocument;
	private final int fLineOffset;
	private final int fLineCount;
	private final int fLength;
	private final boolean fIgnoreWhiteSpace;
	private final int fMaxOffset;
	
	
	private boolean fSkip= false;
	private int fLastOffset;
	private int fLastLength;
	
	
	/**
	 * Creates a <code>DocLineComparator</code> for the given document range.
	 * ignoreWhiteSpace controls whether comparing lines (in method
	 * <code>rangesEqual<code>) should ignore whitespace.
	 *
	 * @param document the document from which the lines are taken
	 * @param region if non-<code>null</code> only lines within this range are taken
	 * @param ignoreWhiteSpace if <code>true</code> white space is ignored when comparing lines
	 */
	public DocLineComparator(IDocument document, IRegion region, boolean ignoreWhiteSpace) {

		fDocument= document;
		fIgnoreWhiteSpace= ignoreWhiteSpace;

		if (region != null) {
			fLength= region.getLength();
			int start= region.getOffset();
			int lineOffset= 0;
			try {
				lineOffset= fDocument.getLineOfOffset(start);
			} catch (BadLocationException ex) {
			}
			fLineOffset= lineOffset;
			
			fMaxOffset= start + fLength;

			if (fLength == 0)
				fLineCount= 0;
			else {
				int endLine= fDocument.getNumberOfLines();
				try {
					endLine= fDocument.getLineOfOffset(start + fLength);
				} catch (BadLocationException ex) {
				}
				fLineCount= endLine - fLineOffset + 1;
			}

		} else {
			fLineOffset= 0;
			fLength= document.getLength();
			fLineCount= fDocument.getNumberOfLines();
			fMaxOffset= fDocument.getLength();
		}
	}

	/**
	 * Returns the number of lines in the document.
	 *
	 * @return number of lines
	 */
	public int getRangeCount() {
		return fLineCount;
	}
	
	/**
	 * Computes the length of line <code>line</code>.
	 * 
	 * @param line the line requested
	 * @return the line length or <code>0</code> if <code>line</code> is not a valid line in the document
	 */
	private int getLineLength(int line) {
		if (line >= fLineCount)
			return 0;
		try {
			int docLine= fLineOffset + line;
			String delim= fDocument.getLineDelimiter(docLine);
			int length= fDocument.getLineLength(docLine) - (delim == null ? 0 : delim.length());
			if (line == fLineCount - 1) {
				fLastOffset= fDocument.getLineOffset(docLine);
				fLastLength= Math.min(length, fMaxOffset - fLastOffset);
			} else {
				fLastOffset= -1;
				fLastLength= length;
			}
			return fLastLength;
		} catch (BadLocationException e) {
			fLastOffset= 0;
			fLastLength= 0;
			fSkip= true;
			return 0;
		}
	}

	/**
	 * Returns <code>true</code> if a line given by the first index
	 * matches a line specified by the other <code>IRangeComparator</code> and index.
	 *
	 * @param thisIndex	the number of the line within this range comparator
	 * @param other the range comparator to compare this with
	 * @param otherIndex the number of the line within the other comparator
	 * @return <code>true</code> if the lines are equal
	 */
	public boolean rangesEqual(int thisIndex, IRangeComparator other0, int otherIndex) {

		if (other0 != null && other0.getClass() == getClass()) {
			DocLineComparator other= (DocLineComparator) other0;

			if (fIgnoreWhiteSpace) {
			
				CharSequence s1= extract(thisIndex);
				CharSequence s2= other.extract(otherIndex);
				return compare(s1, s2);
			
			} else {
				
				int tlen= getLineLength(thisIndex);
				int olen= other.getLineLength(otherIndex);
				if (tlen == olen) {
					CharSequence s1= extract(thisIndex);
					CharSequence s2= other.extract(otherIndex);
					return s1.equals(s2);
				}
			}
		}
		return false;
	}

	/**
	 * Aborts the comparison if the number of tokens is too large.
	 *
	 * @return <code>true</code> to abort a token comparison
	 */
	public boolean skipRangeComparison(int length, int max, IRangeComparator other) {
		return fSkip;
	}
		
	//---- private methods
	
	/**
	 * Extract a single line from the underlying document without the line separator.
	 *
	 * @param line the number of the line to extract
	 * @return the contents of the line as a String
	 */
	private CharSequence extract(int line) {
		if (line < fLineCount) {
			try {
				int docLine= fLineOffset + line;
				if (fLastOffset == -1)
					fLastOffset= fDocument.getLineOffset(docLine);
				
				return fDocument.get(fLastOffset, fLastLength);
//				return new DocumentCharSequence(fDocument, offset, length);
			} catch(BadLocationException e) {
				fSkip= true;
			}
		}
		return ""; //$NON-NLS-1$
	}
	
	private boolean compare(CharSequence s1, CharSequence s2) {
		int l1= s1.length();
		int l2= s2.length();
		int c1= 0, c2= 0;
		int i1= 0, i2= 0;
		
		while (c1 != -1) {
			
			c1= -1;
			while (i1 < l1) {
				char c= s1.charAt(i1++);
				if (! Character.isWhitespace(c)) {
					c1= c;
					break;
				}
			}
			
			c2= -1;
			while (i2 < l2) {
				char c= s2.charAt(i2++);
				if (! Character.isWhitespace(c)) {
					c2= c;
					break;
				}
			}
				
			if (c1 != c2)
				return false;
		}
		return true;
	}

}

