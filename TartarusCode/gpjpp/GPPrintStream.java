// gpjpp (genetic programming package for Java)
// Copyright (c) 1997, Kim Kokkonen
//
// This program is free software; you can redistribute it and/or 
// modify it under the terms of version 2 of the GNU General Public 
// License as published by the Free Software Foundation.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// Send comments, suggestions, problems to kimk@turbopower.com

package gpjpp;

import java.io.*;

/**
 * Extends the standard PrintStream class to output a
 * platform-specific line separator. This is not needed for JDK 1.1,
 * which outputs the System property line.separator automatically 
 * at the end of each line. GPPrintStream does not interfere with
 * the operation of PrintStream under JDK 1.1 (which deprecates 
 * PrintStream and completely changes its implementation).
 *
 * @version 1.0
 */
public class GPPrintStream extends PrintStream {
    /**
     * The line separator string obtained by calling
     * System.getProperty("line.separator").
     */
    protected String lineSeparator = 
        System.getProperty("line.separator", "\r\n");

    /**
     * Creates a GPPrintStream filter over an OutputStream.
     */
    public GPPrintStream(OutputStream out) { super(out); }

    /**
     * Creates a GPPrintStream filter over an OutputStream.
     */
    public GPPrintStream(OutputStream out, boolean autoflush) {
        super(out, autoflush);
    }

    /**
     * Writes the line separator string to the PrintStream.
     */
    public void writeEol() {
        for (int i = 0; i < lineSeparator.length(); i++)
            super.write(lineSeparator.charAt(i));
    }

    /**
     * Detects '\n' and expands it to a full line separator string.
     * All other characters are passed on to the superclass.
     */
    public void write(int b) {
        if (b == '\n')
            writeEol();
        else
            super.write(b);
    }

}
