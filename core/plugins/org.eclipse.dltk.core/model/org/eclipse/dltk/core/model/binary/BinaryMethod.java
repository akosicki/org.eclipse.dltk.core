package org.eclipse.dltk.core.model.binary;

import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.IParameter;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.internal.core.ModelElement;
import org.eclipse.dltk.utils.CorePrinter;

/**
 * @since 2.0
 */
public class BinaryMethod extends BinaryMember implements IMethod {

	public BinaryMethod(ModelElement parent, String name) {
		super(parent, name);
	}

	@Override
	public void printNode(CorePrinter output) {
	}

	public int getElementType() {
		return METHOD;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof BinaryMethod)) {
			return false;
		}
		return super.equals(o);
	}

	public String getFullyQualifiedName(String enclosingTypeSeparator) {
		try {
			return getFullyQualifiedName(enclosingTypeSeparator, false/*
																	 * don't
																	 * show
																	 * parameters
																	 */);
		} catch (ModelException e) {
			// exception thrown only when showing parameters
			return null;
		}
	}

	public String getFullyQualifiedName() {
		return getFullyQualifiedName("$"); //$NON-NLS-1$
	}

	public IParameter[] getParameters() throws ModelException {
		return ((BinaryMethodElementInfo) getElementInfo()).getArguments();
	}

	public String[] getParameterNames() throws ModelException {
		return ((BinaryMethodElementInfo) getElementInfo()).getArgumentNames();
	}

	public boolean isConstructor() throws ModelException {
		return ((BinaryMethodElementInfo) getElementInfo()).isConstructor();
	}

	public String getType() throws ModelException {
		return ((BinaryMethodElementInfo) getElementInfo()).getReturnTypeName();
	}
}
