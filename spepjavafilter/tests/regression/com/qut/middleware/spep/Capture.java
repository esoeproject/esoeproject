package com.qut.middleware.spep;


import static org.easymock.EasyMock.reportMatcher;

import java.util.Vector;

import org.easymock.IArgumentMatcher;

public class Capture<T> implements IArgumentMatcher
{
    private Vector<T> captured = new Vector<T>();

    public void appendTo(StringBuffer buffer)
    {
        buffer.append("capture()");
    }

    public boolean matches(Object parameter)
    {
        captured.add( (T) parameter );

        return true;
    }

    public Vector<T> getCaptured()
    {
        return captured;
    }

    public static <T> T capture(Capture<T> capture)
    {
        reportMatcher(capture);

        return null;
    }

}
