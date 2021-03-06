/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org.eclipse.dltk.internal.core.search.matching;

import java.io.IOException;

import org.eclipse.dltk.compiler.CharOperation;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.core.search.SearchPattern;
import org.eclipse.dltk.core.search.index.EntryResult;
import org.eclipse.dltk.core.search.index.Index;
import org.eclipse.dltk.core.search.indexing.IIndexConstants;

public class SuperTypeReferencePattern extends DLTKSearchPattern {

	public char[] superQualification;
	public char[] superSimpleName;
	public char superClassOrInterface;

	// set to CLASS_SUFFIX for only matching classes
	// set to INTERFACE_SUFFIX for only matching interfaces
	// set to TYPE_SUFFIX for matching both classes and interfaces
	public char typeSuffix;
	public char[] pkgName;
	public char[] simpleName;
	public char[] enclosingTypeName;
	public char classOrInterface;
	public int modifiers;
	public char[][] typeParameterSignatures;

	protected int superRefKind;
	public static final int ALL_SUPER_TYPES = 0;
	public static final int ONLY_SUPER_INTERFACES = 1; // used for IMPLEMENTORS
	public static final int ONLY_SUPER_CLASSES = 2; // used for hierarchy with a
	// class focus

	protected static char[][] CATEGORIES = { IIndexConstants.SUPER_REF };

	public static char[] createIndexKey(int modifiers, String namespace[],
			String typeName, String[] enclosingTypeNames,
			char[][] typeParameterSignatures, char classOrInterface,
			char[] superTypeName, char superClassOrInterface) {

		if (superTypeName == null)
			superTypeName = "(base object)".toCharArray(); //$NON-NLS-1$
		char[] superSimpleName = CharOperation.lastSegment(superTypeName, '.');
		char[] superQualification = null;
		if (superSimpleName != superTypeName) {
			int length = superTypeName.length - superSimpleName.length - 1;
			superQualification = new char[length];
			System.arraycopy(superTypeName, 0, superQualification, 0, length);
		}

		// if the supertype name contains a $, then split it into: source name
		// and append the $ prefix to the qualification
		// e.g. p.A$B ---> p.A$ + B
		char[] superTypeSourceName = CharOperation.lastSegment(superSimpleName,
				'$');
		if (superTypeSourceName != superSimpleName) {
			int start = superQualification == null ? 0
					: superQualification.length + 1;
			int prefixLength = superSimpleName.length
					- superTypeSourceName.length;
			char[] mangledQualification = new char[start + prefixLength];
			if (superQualification != null) {
				System.arraycopy(superQualification, 0, mangledQualification,
						0, start - 1);
				mangledQualification[start - 1] = '.';
			}
			System.arraycopy(superSimpleName, 0, mangledQualification, start,
					prefixLength);
			superQualification = mangledQualification;
			superSimpleName = superTypeSourceName;
		}

		String simpleName = CharOperation.lastSegment(typeName, '.');
		char[] enclosingTypeName = CharOperation.concatWith(enclosingTypeNames,
				'$');
		if (superQualification != null
				&& CharOperation.equals(superQualification,
						CharOperation.concatWith(namespace, '$')))
			namespace = IIndexConstants.ONE_ZERO_CHAR_STRINGS; // save some
		// space

		char[] typeParameters = CharOperation.NO_CHAR;
		int typeParametersLength = 0;
		if (typeParameterSignatures != null) {
			StringBuffer buffer = new StringBuffer();
			for (int i = 0, length = typeParameterSignatures.length; i < length; i++) {
				char[] typeParameter = typeParameterSignatures[i];
				buffer.append(typeParameter);
				typeParametersLength += typeParameter.length;
				if (i != length - 1) {
					buffer.append(',');
					typeParametersLength++;
				}
			}
			typeParameters = new char[typeParametersLength];
			buffer.getChars(0, typeParametersLength, typeParameters, 0);
		}

		// superSimpleName / superQualification / simpleName / enclosingTypeName
		// / typeParameters / packageName / superClassOrInterface
		// classOrInterface modifiers
		int superLength = superSimpleName == null ? 0 : superSimpleName.length;
		int superQLength = superQualification == null ? 0
				: superQualification.length;
		int simpleLength = simpleName == null ? 0 : simpleName.length();
		int enclosingLength = enclosingTypeName == null ? 0
				: enclosingTypeName.length;
		int packageLength = indexKeyLength(namespace);
		char[] result = new char[superLength + superQLength + simpleLength
				+ enclosingLength + typeParametersLength + packageLength + 9];
		int pos = 0;
		if (superLength > 0) {
			System.arraycopy(superSimpleName, 0, result, pos, superLength);
			pos += superLength;
		}
		result[pos++] = IIndexConstants.SEPARATOR;
		if (superQLength > 0) {
			System.arraycopy(superQualification, 0, result, pos, superQLength);
			pos += superQLength;
		}
		result[pos++] = IIndexConstants.SEPARATOR;
		if (simpleLength > 0) {
			simpleName.getChars(0, simpleLength, result, pos);
			pos += simpleLength;
		}
		result[pos++] = IIndexConstants.SEPARATOR;
		if (enclosingLength > 0) {
			System.arraycopy(enclosingTypeName, 0, result, pos, enclosingLength);
			pos += enclosingLength;
		}
		result[pos++] = IIndexConstants.SEPARATOR;
		if (typeParametersLength > 0) {
			System.arraycopy(typeParameters, 0, result, pos,
					typeParametersLength);
			pos += typeParametersLength;
		}
		result[pos++] = IIndexConstants.SEPARATOR;
		if (packageLength > 0) {
			pos = encodeNames(namespace, packageLength, result, pos);
		}
		result[pos++] = IIndexConstants.SEPARATOR;
		result[pos++] = superClassOrInterface;
		result[pos++] = classOrInterface;
		result[pos] = (char) modifiers;
		return result;
	}

	public SuperTypeReferencePattern(char[] superQualification,
			char[] superSimpleName, int superRefKind, int matchRule,
			IDLTKLanguageToolkit toolkit) {

		this(matchRule, toolkit);

		this.superQualification = isCaseSensitive() ? superQualification
				: CharOperation.toLowerCase(superQualification);
		this.superSimpleName = (isCaseSensitive() || isCamelCase()) ? superSimpleName
				: CharOperation.toLowerCase(superSimpleName);
		// ((InternalSearchPattern) this).mustResolve = superQualification !=
		// null;
		this.superRefKind = superRefKind;
	}

	public SuperTypeReferencePattern(char[] superQualification,
			char[] superSimpleName, int superRefKind, char typeSuffix,
			int matchRule, IDLTKLanguageToolkit toolkit) {

		this(superQualification, superSimpleName, superRefKind, matchRule,
				toolkit);
		this.typeSuffix = typeSuffix;
		// ((InternalSearchPattern) this).mustResolve = superQualification !=
		// null
		// || typeSuffix != TYPE_SUFFIX;
	}

	SuperTypeReferencePattern(int matchRule, IDLTKLanguageToolkit toolkit) {
		super(IIndexConstants.SUPER_REF_PATTERN, matchRule, toolkit);
	}

	/*
	 * superSimpleName / superQualification / simpleName / enclosingTypeName /
	 * typeParameters / pkgName / superClassOrInterface classOrInterface
	 * modifiers
	 */
	public void decodeIndexKey(char[] key) {
		int slash = CharOperation.indexOf(IIndexConstants.SEPARATOR, key, 0);
		this.superSimpleName = CharOperation.subarray(key, 0, slash);

		// some values may not have been know when indexed so decode as null
		int start = slash + 1;
		slash = CharOperation.indexOf(IIndexConstants.SEPARATOR, key, start);
		this.superQualification = slash == start ? null : CharOperation
				.subarray(key, start, slash);

		slash = CharOperation.indexOf(IIndexConstants.SEPARATOR, key,
				start = slash + 1);
		this.simpleName = CharOperation.subarray(key, start, slash);

		start = ++slash;
		if (key[start] == IIndexConstants.SEPARATOR) {
			this.enclosingTypeName = null;
		} else {
			slash = CharOperation
					.indexOf(IIndexConstants.SEPARATOR, key, start);
			if (slash == (start + 1) && key[start] == IIndexConstants.ZERO_CHAR) {
				this.enclosingTypeName = IIndexConstants.ONE_ZERO;
			} else {
				char[] names = CharOperation.subarray(key, start, slash);
				this.enclosingTypeName = names;
			}
		}

		start = ++slash;
		if (key[start] == IIndexConstants.SEPARATOR) {
			this.typeParameterSignatures = null;
		} else {
			slash = CharOperation
					.indexOf(IIndexConstants.SEPARATOR, key, start);
			this.typeParameterSignatures = CharOperation.splitOn(',', key,
					start, slash);
		}

		start = ++slash;
		if (key[start] == IIndexConstants.SEPARATOR) {
			this.pkgName = null;
		} else {
			slash = CharOperation
					.indexOf(IIndexConstants.SEPARATOR, key, start);
			if (slash == (start + 1) && key[start] == IIndexConstants.ZERO_CHAR) {
				this.pkgName = this.superQualification;
			} else {
				char[] names = CharOperation.subarray(key, start, slash);
				this.pkgName = names;
			}
		}

		this.superClassOrInterface = key[slash + 1];
		this.classOrInterface = key[slash + 2];
		this.modifiers = key[slash + 3]; // implicit cast to int type
	}

	public SearchPattern getBlankPattern() {
		return new SuperTypeReferencePattern(R_EXACT_MATCH | R_CASE_SENSITIVE,
				getToolkit());
	}

	public char[][] getIndexCategories() {
		return CATEGORIES;
	}

	public boolean matchesDecodedKey(SearchPattern decodedPattern) {
		SuperTypeReferencePattern pattern = (SuperTypeReferencePattern) decodedPattern;
		if (this.superRefKind == ONLY_SUPER_CLASSES
				&& pattern.enclosingTypeName != IIndexConstants.ONE_ZERO/*
																		 * not
																		 * an
																		 * anonymous
																		 */)
			// consider enumerations as classes, reject interfaces and
			// annotations
			if (pattern.superClassOrInterface == IIndexConstants.ANNOTATION_TYPE_SUFFIX)
				return false;

		if (pattern.superQualification != null)
			if (!matchesName(this.superQualification,
					pattern.superQualification))
				return false;

		return matchesName(this.superSimpleName, pattern.superSimpleName);
	}

	public EntryResult[] queryIn(Index index) throws IOException {
		char[] key = this.superSimpleName; // can be null
		int matchRule = getMatchRule();

		// cannot include the superQualification since it may not exist in the
		// index
		switch (getMatchMode()) {
		case R_EXACT_MATCH:
			if (this.isCamelCase)
				break;
			// do a prefix query with the superSimpleName
			matchRule &= ~R_EXACT_MATCH;
			matchRule |= R_PREFIX_MATCH;
			if (this.superSimpleName != null)
				key = CharOperation.append(this.superSimpleName,
						IIndexConstants.SEPARATOR);
			break;
		case R_PREFIX_MATCH:
			// do a prefix query with the superSimpleName
			break;
		case R_PATTERN_MATCH:
			// do a pattern query with the superSimpleName
			break;
		case R_REGEXP_MATCH:
			// TODO (frederic) implement regular expression match
			break;
		}

		return index.query(getIndexCategories(), key, matchRule); // match
		// rule is
		// irrelevant
		// when the
		// key is
		// null
	}

	protected StringBuffer print(StringBuffer output) {
		switch (this.superRefKind) {
		case ALL_SUPER_TYPES:
			output.append("SuperTypeReferencePattern: <"); //$NON-NLS-1$
			break;
		case ONLY_SUPER_INTERFACES:
			output.append("SuperInterfaceReferencePattern: <"); //$NON-NLS-1$
			break;
		case ONLY_SUPER_CLASSES:
			output.append("SuperClassReferencePattern: <"); //$NON-NLS-1$
			break;
		}
		if (superSimpleName != null)
			output.append(superSimpleName);
		else
			output.append("*"); //$NON-NLS-1$
		output.append(">"); //$NON-NLS-1$
		return super.print(output);
	}
}
