/**
 * Copyright (c) 2017-2018 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.net.filter.exception;

public class ParseException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public ParseException() {
    }

    public ParseException(String s) {
        super(s);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseException(Throwable cause) {
        super(cause);
    }
}
