package org.sat4j.csp.intension;

public class WrongArityForOperatorException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public WrongArityForOperatorException(final String reason) {
		super(reason);
	}

}
