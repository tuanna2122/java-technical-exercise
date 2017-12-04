/**
 * ApplicationException aims to wrap {@link RuntimeException} for some tweaks and modification
 */
package com.treasuredata.exercise;

/**
 * @author Tuan Nguyen
 *
 */
public class ApplicationException extends RuntimeException {

	/**
	 * @serialField
	 */
	private static final long serialVersionUID = -5233156256651997562L;

	public ApplicationException(String message) {
		super(message);
	}

	public ApplicationException(String message, Throwable cause) {
		super(message, cause);
	}

	// You can make some tweaks or modification here to adapt your requirements

}
