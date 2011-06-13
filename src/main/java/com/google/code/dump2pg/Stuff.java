/**
 * This file is part of dump2pg.
 * 
 * dump2pg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * dump2pg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with dump2pg. If not, see <http://www.gnu.org/licenses/>.
 */
package com.google.code.dump2pg;

/**
 * @author Rodrigo Hjort <rodrigo.hjort@gmail.com>
 * @see http://dump2pg.googlecode.com
 */
public class Stuff {
    
    public static long toLong(final byte value) {
    	long v = value;
        if (value < 0)
            v += (1L << 8);
        return(v);
    }
    
    public static String toOctalString(final int numero) {
    	final String octal = Integer.toOctalString(numero);
    	final String zeroes = "000".substring(0, 3 - octal.length());
        return(zeroes + octal);
    }
    
    public static String escapeString(final String value) {
        String ret = value;
        
        //substituir "\" por "\\"
        ret = ret.replaceAll("\\\\", "\\\\\\\\");
        
        //substituir sequencias especiais de escapes: \b, \f, \n, \r, \t, \v
        //ascii: 8 (backspace), 12 (form feed), 10 (lf), 13 (cr), 9 (tab), 11 (vtab)
        ret = ret.replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r").replaceAll("\t", "\\\\t");
        
        return(ret);
    }
 
    public static String escapeBytes(final byte[] array) {
    	final StringBuffer sb = new StringBuffer();
        for (int jj = 0; jj < array.length; jj++) {
        	final byte bnum = array[jj];
            if ((bnum >= 'a' && bnum <= 'z') ||
                	(bnum >= 'A' && bnum <= 'Z')||
            		(bnum >= '0' && bnum <= '9')) {
            	sb.append((char) bnum);
            } else {
            	final long lnum = toLong(bnum);
            	final String octal = toOctalString((int) lnum);
                sb.append("\\\\" + octal);
            }
        }
        return sb.toString();
    }

    public static String escapeByte(byte bnum) {
    	String ret = "";
    	if (bnum >= 32 && bnum <= 126) {
			ret = (bnum != 92 ? String.valueOf((char) bnum) : "\\\\\\\\");
        } else {
            long lnum = toLong(bnum);
            String octal = toOctalString((int) lnum);
            ret = "\\\\" + octal;
        }
        return ret;
    }

}
