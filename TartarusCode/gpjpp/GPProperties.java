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
import java.util.Enumeration;
import java.util.Properties;

/**
 * Extends the standard Properties class. GPProperties fixes mishandling
 * of tab and space in the load() method of JDK 1.0 (fixed in JDK 1.1). 
 * It makes keynames case-insensitive ala Windows ini files. 
 * And it allows comments at the end of each line, delimited by the 
 * first space, tab, #, or ! character after the value string. 
 * This means that the value string cannot contain any of these 
 * characters, of course, but this does not impose an important 
 * limitation for this package.
 *
 * @version 1.0
 */
public class GPProperties extends Properties {

    /**
     * Creates an empty property set.
     */
    public GPProperties() { super(null); }

    /**
     * Creates an empty property set with specified default values.
     */
    public GPProperties(Properties defaults) { super(defaults); }

    /**
     * Looks up a property value based on a key. The key is forced
     * to lowercase before calling Properties.getProperty.
     */
    public String getProperty(String key) {
        return super.getProperty(key.toLowerCase());
    }

    /**
     * Loads a property set from an InputStream. This completely
     * replaces Properties.load() with the following differences:
     * fixes bugs in handling embedded tab and space characters
     * in JDK 1.0; terminates the value string with the first
     * space, tab, !, or # character, treating the rest of the 
     * line as a comment; and forces key strings to lowercase
     * before adding them to the property table.
     *
     * @exception IOException
     *              if an error occurs while reading the stream.
     */
    public synchronized void load(InputStream in)
        throws IOException {

        //deprecated in JDK 1.1, but JDK 1.1 still uses it!
        in = Runtime.getRuntime().getLocalizedInputStream(in);

        int ch = in.read();
        while (true) {
            //skip comments
            switch (ch) {
                case -1:
                    return;

                case '#':
                case '!':
                    do {
                        ch = in.read();
                    } while ((ch >= 0) && (ch != '\n') && (ch != '\r'));
                    continue;

                case '\n':
                case '\r':
                case ' ':
                case '\t':
                    ch = in.read();
                    continue;
            }

            // Read the key
            StringBuffer key = new StringBuffer();
            while ((ch >= 0) && (ch != '=') && (ch != ':') &&
                (ch != ' ') && (ch != '\t') && (ch != '\n') && (ch != '\r')) {
                key.append((char)ch);
                ch = in.read();
            }
            //skip space to =
            while ((ch == ' ') || (ch == '\t')) //!! krk 970422
                ch = in.read();
            //skip =
            if ((ch == '=') || (ch == ':'))
                ch = in.read();
            //skip space after =
            while ((ch == ' ') || (ch == '\t')) //!! krk 970422
                ch = in.read();

            // Read the value
            StringBuffer val = new StringBuffer();
readval:    while ((ch >= 0) && (ch != '\n') && (ch != '\r')) {
                switch (ch) {
                    //value terminated by any of these comment delimiters
                    case '#':
                    case '!':
                    case ' ':
                    case '\t':
                        //skip to end of line
                        do {
                            ch = in.read();
                        } while ((ch >= 0) && (ch != '\n') && (ch != '\r'));
                        break readval;

                    case '\\':
                        //interpret escapes
                        switch (ch = in.read()) {
                            case '\r':
                                if (((ch = in.read()) == '\n') ||
                                    (ch == ' ') || (ch == '\t')) {
                                    // fall thru to '\n' case
                                } else
                                    continue;
                            case '\n':
                                while (((ch = in.read()) == ' ') || (ch == '\t'));
                                continue;
                            case 't': ch = '\t'; break;
                            case 'n': ch = '\n'; break;
                            case 'r': ch = '\r'; break;
                            case 'u': {
                                while ((ch = in.read()) == 'u');
                                int d = 0;
                                loop:
                                for (int i = 0 ; i < 4 ; i++, ch = in.read()) {
                                    switch (ch) {
                                        case '0': case '1': case '2': case '3': case '4':
                                        case '5': case '6': case '7': case '8': case '9':
                                            d = (d << 4) + ch - '0';
                                            break;
                                        case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
                                            d = (d << 4) + 10 + ch - 'a';
                                            break;
                                        case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
                                            d = (d << 4) + 10 + ch - 'A';
                                            break;
                                        default:
                                            break loop;
                                    }
                                }
                                ch = d;
                            }
                        }
                        break;
                } //switch (ch)

                val.append((char)ch);
                ch = in.read();
            }

            //System.out.println(key + " = '" + val + "'");
            put(key.toString().toLowerCase(), val.toString());
        }
    }

    /**
     * Writes the contents of the properties to System.out. For
     * testing and debugging.
     */
    public void show() {
        for (Enumeration e = propertyNames(); e.hasMoreElements();) {
            String key = (String)e.nextElement();
            String val = (String)getProperty(key);
            System.out.println(key+"="+val);
        }
    }
}
