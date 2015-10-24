package com.adamnickle.delivering;

import java.util.regex.Pattern;

public final class Validator
{
    private Validator() { }

    private static final Pattern PLAIN_MONEY_PATTERN = Pattern.compile( "^\\d*(?:\\.\\d{2})?$" );

    public static boolean money( String money )
    {
        return PLAIN_MONEY_PATTERN.matcher( money ).matches();
    }
}
