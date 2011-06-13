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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;

import gnu.getopt.Getopt;

//import oracle.jdbc.driver.OracleResultSet;
//import oracle.sql.DATE;
//import oracle.sql.OPAQUE;
//import oracle.sql.TIMESTAMP;
//import oracle.xdb.XMLType;

import net.sourceforge.jtds.jdbc.BlobImpl;
import net.sourceforge.jtds.jdbc.ClobImpl;

/**
 * @author Rodrigo Hjort <rodrigo.hjort@gmail.com>
 * @see http://dump2pg.googlecode.com
 */
public class Dump {
    
	private Connection conn;
    
    private PrintWriter pr = null;
    private String catalog = null, scheme = null, table = null, filename = null, filter = null;
    
    private boolean isOracle = false;
    private boolean isMSSQL = false;
    
	private boolean isNoBlob = false;
	private boolean isByteArray = false;
	private long limit = 0, offset = 0;
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        
        boolean list = false, help = false, create = false, bytea = false, noblob = false;
        String driver = null, url = null, user = null, password = null;
        String catalog = null, scheme = null, table = null, filename = null, filter = null;
        long limit = 0, offset = 0;
        
        //new ShowUs().goFirst();
        //new DumpIt().go();
        
        //http://www.urbanophile.com/arenn/hacking/getopt/gnu.getopt.Getopt.html
        Getopt g = new Getopt("dump2pg", args, "hlf:d:r:u:p:c:e:t:d:bm:o:kw:");
        
        //TODO: opção para quebrar arquivo de dump a cada N linhas ou bytes
        
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'h':
                    help = true;
                    break;
                case 'l':
                    list = true;
                    break;
                case 'f':
                    create = true;
                    filename = g.getOptarg();
                    break;
                case 'd':
                    driver = g.getOptarg();
                    break;
                case 'r':
                    url = g.getOptarg();
                    break;
                case 'u':
                    user = g.getOptarg();
                    break;
                case 'p':
                    password = g.getOptarg();
                    break;
                case 'c':
                    catalog = g.getOptarg();
                    break;
                case 'e':
                    scheme = g.getOptarg();
                    break;
                case 't':
                    table = g.getOptarg();
                    break;
                case 'w':
                    filter = g.getOptarg();
                    break;
                case 'b':
                    bytea = true;
                    break;
                case 'k':
                    noblob = true;
                    break;
                case 'm':
                    limit = Long.parseLong(g.getOptarg());
                    break;
                case 'o':
                    offset = Long.parseLong(g.getOptarg());
                    break;
                default:
                   System.out.print("getopt() returned " + c + "\n");
            }
        }

        try {
            if (!list && !create) {
            	
                System.out.println("Usage: dump2pg [-h | -l | -f] <options>");
                System.out.println();
                System.out.println("Options:");
                System.out.println("\t -h \t\tshow this help message");
                System.out.println("\t -l \t\tshow list of tables");
                System.out.println("\t -f [file] \tcreate insert script \t(eg: \"db-dump.sql\")");
                System.out.println();
                System.out.println("\t -d [driver] \tdefine JDBC driver \t(eg: \"net.sourceforge.jtds.jdbc.Driver\")");
                System.out.println("\t -r [url] \tdefine connection URL \t(eg: \"jdbc:jtds:sqlserver://myserver\")");
                System.out.println("\t -u [user] \tdefine username \t(eg: \"sa_guest\")");
                System.out.println("\t -p [password] \tdefine password \t(eg: \"thepass\")");
                System.out.println();
                System.out.println("\t -c [catalog] \tdefine catalog \t\t(eg: \"maindb\")");
                System.out.println("\t -e [scheme] \tdefine scheme \t\t(eg: \"SA_%\")");
                System.out.println("\t -t [table] \tdefine table \t\t(eg: \"Author%\")");
                System.out.println();
                System.out.println("\t -k \t\tignore \"BLOB\" datatypes");
                System.out.println("\t -b \t\t\"BLOB\" type exported as \"bytea\"");
                System.out.println();
                System.out.println("\t -w [filter] \tdefine a filter for the rows (\"WHERE\" statement)");
                System.out.println("\t -m [limit] \tdefine maximum amount of rows to retrieve (limit)");
                System.out.println("\t -o [skip] \tdefine number of rows to be skipped (offset)");
                System.out.println();
                System.out.println("Examples:");
                System.out.println("\t dump2pg -d net.sourceforge.jtds.jdbc.Driver -r jdbc:jtds:sqlserver://host:1433 \\");
                System.out.println("\t         -u user -p pass -c maindb -e dbo -t Client% -f dump-eg1.sql");
                System.out.println("\t dump2pg -d oracle.jdbc.driver.OracleDriver -r jdbc:oracle:oci:@mydb \\");
                System.out.println("\t         -u user -p pass -e SA_% -t TB_PRODUCT -f dump-eg2.sql");
                System.out.println();
                System.out.println("Report bugs and give us feedback on: http://dump2pg.googlecode.com/");
                
                if (!help)
                    throw new Exception("What you want me to do? Please be more specific!");
            }
            if (help)
            	return;
            if (driver == null)
                throw new Exception("Database driver not supplied!");
            if (url == null)
                throw new Exception("Connection URL not supplied!");
            if (user == null)
                throw new Exception("User not supplied!");
            if (password == null)
                throw new Exception("Password not supplied!");
            if (create && filename == null)
                throw new Exception("Script filename not supplied!");
        } catch (Exception e) {
            System.out.println();
            System.err.println(e.getMessage());
            return;
        }
        
        Dump dump = new Dump(driver, url, user, password);
        dump.catalog = catalog;
        dump.scheme = scheme;
        dump.table = table;
        dump.filename = filename;
        dump.filter = filter;
        dump.limit = limit;
        dump.offset = offset;
        dump.isNoBlob = noblob;
        dump.isByteArray = bytea;
        
        if (list)
            dump.listTables();
        else if (create)
            dump.createDump();
    }

    public Dump(final String driver, final String url, final String user, final String pwd) {
        try {
            
            //Establish Communication between Oracle and JDBC by Registering the Oracle Driver
            //DriverManager.registerDriver(new net.sourceforge.jtds.jdbc.Driver());
            //DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver()); 
            //DriverManager.registerDriver(new com.microsoft.jdbc.sqlserver.SQLServerDriver());
            //Class.forName("net.sourceforge.jtds.jdbc.Driver");
            Class.forName(driver);
            
            //conn = DriverManager.getConnection("jdbc:jtds:sqlserver://server:1433", "user", "pass");
            //conn = DriverManager.getConnection("jdbc:oracle:oci8:@dtran", "user", "pass");
            //conn = DriverManager.getConnection("jdbc:oracle:oci8:@gprev", "user", "pass");
            //conn = DriverManager.getConnection("jdbc:microsoft:sqlserver://server:1433;SelectMethod=Cursor;DatabaseName=db", "user", "pass");
            conn = DriverManager.getConnection(url, user, pwd);
            
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        
        //verificar se é Oracle ou MS SQL
        this.isOracle = driver.contains("oracle");
        this.isMSSQL = driver.contains("jtds") || driver.contains("microsoft");
    }

    /**
     * e247. Listing All Table Names in a Database
     * http://www.javaalmanac.com/egs/java.sql/GetTables.html
     */
    public void listTables() {
        try {
            // Gets the database metadata
        	final DatabaseMetaData dbmd = conn.getMetaData();
            
            // Specify the type of object; in this case we want tables
        	final String[] types = {"TABLE"};
        	final ResultSet resultSet = dbmd.getTables(catalog, scheme, table != null ? table : "%", types);
            
            while (resultSet.next()) {
            	final String tableName = resultSet.getString(3);
            	final String tableCatalog = resultSet.getString(1);
            	final String tableSchema = resultSet.getString(2);
                System.out.println(tableCatalog + "." + tableSchema + "." + tableName);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html
     * @param tableSchema
     * @param tableName
     */
    private void readTable(final String tableSchema, final String tableName) {
    	
    	final String fullName = (tableSchema != null ? tableSchema + "." : "") + tableName;
    	String selectStr = "SELECT * FROM " + fullName;
    	String copyStr = "copy " + fullName.toUpperCase() + " (";
    	
    	boolean headerRead = false;
    	long offsetCount = this.offset;
        
        System.out.println(fullName);
        
        //TODO: fazer todo esse controle de uma maneira mais profissional!!!
        if (limit > 0) {
        	if (isOracle)
        		selectStr += " WHERE rownum <= " + limit +
        			(filter != null ? " AND " + filter : "");
        	else if (isMSSQL)
        		selectStr = selectStr.replace(" * ", " TOP " + limit + " * ") +
        			(filter != null ? " WHERE " + filter : "");
        	//TODO: implementar para o resto dos SGBDs!!!
        	else
        		selectStr += (filter != null ? " WHERE " + filter : "") + " LIMIT " + limit;
        } else if (filter != null) {
    		selectStr += " WHERE " + filter;
        }
        
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(selectStr);
            ResultSetMetaData rsmd = rs.getMetaData();
//            OracleResultSet ors = null;
            
            while (offsetCount > 0 && rs.next()) offsetCount--;
            
            while (rs.next()) {
                
                //ler cabeçalho (só na primeira vez!)
                if (!headerRead) {
                    for (int ii = 0; ii < rsmd.getColumnCount(); ii++) {
                        String columnName = rsmd.getColumnName(ii + 1);
                        copyStr += (ii > 0 ? ", " : "") + columnName.toLowerCase();
                    }
                    copyStr += ") from stdin;";
                    pr.println();
                    pr.println("-- Schema: " + tableSchema + ", Table: " + tableName);
                    if (filter != null)
                    	pr.println("-- Filter: " + filter);
                    pr.println(copyStr);
                    headerRead = true;
                }
                
//                if (isOracle)
//                    ors = (OracleResultSet) rs;
                
                for (int ii = 0; ii < rsmd.getColumnCount(); ii++) {
                    Object obj = rs.getObject(ii + 1);
                    String value = "\\N"; //nulo
                    
                    if (ii > 0)
                        pr.print("\t");
                    
//                    if (ors != null && obj instanceof DATE)
//                        obj = ors.getOracleObject(ii + 1);
                    
                    if (obj != null) {
                        
                    	/*
                        // tipos de data/hora específicos do Oracle
                        if (obj instanceof TIMESTAMP) {
                            Timestamp ts = ((TIMESTAMP) obj).timestampValue();
                            value = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S").format(ts);
                        } else if (obj instanceof DATE) {
                            Timestamp ts = ((DATE) obj).timestampValue();
                            value = new SimpleDateFormat("yyyy-MM-dd").format(ts);
                        
                        // XMLType
                        } else if (obj instanceof OPAQUE && 
                        	((OPAQUE) obj).getSQLTypeName().equals("SYS.XMLTYPE")) {
                            
                            XMLType xml = XMLType.createXML((OPAQUE) obj);
                           	value = Stuff.escaparString(xml.getStringVal());
                                                        
                        } else if (obj instanceof Timestamp) {
                        */
                    	if (obj instanceof Timestamp) {
                            value = obj.toString();
                            //valor = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S").format(obj);
                        } else if (obj instanceof Date) {
                            value = new SimpleDateFormat("yyyy-MM-dd").format(obj);
                        
                        //tipo nvarchar específico do SQL Server
                        } else if (obj instanceof ClobImpl) {
                            
                        	if (!isNoBlob) {
	                            value = "";
	                            ClobImpl clob = ((ClobImpl) obj);
	                            
	                            //dessa forma funciona a acentuação!
	                            Reader cs = clob.getCharacterStream();
	                            @SuppressWarnings("unused")
	                            int numRead = 0;
	                            char[] vetor = new char[1];
	                            while ((numRead = cs.read(vetor)) != -1) {
	                                for (int jj = 0; jj < vetor.length; jj++) {
	                                    char ch = (char) vetor[jj];
	                                    String str = String.valueOf(ch);
	                                    pr.print(Stuff.escapeString(str));
	                                }
	                            }
                        	}
                        	
                            //perde acentuação!
                            /*
                            InputStream is = clob.getAsciiStream();
                            byte[] vetor = new byte[(int) clob.length()];
                            int offset = 0, numRead = 0;
                            while (offset < vetor.length && (numRead = is.read(vetor, offset, vetor.length - offset)) >= 0)
                                offset += numRead;
                            
                            for (int jj = 0; jj < vetor.length; jj++) {
                                char ch = (char) vetor[jj];
                                String str = String.valueOf(ch);
                                pr.print(Stuff.escaparString(str));
                            }
                            */
                            
                        //tipo "image" específico do SQL Server
                        } else if (obj instanceof BlobImpl) {
                            
                        	if (!isNoBlob) {
	                            value = "";
	                            BlobImpl blob = ((BlobImpl) obj);
	                            
	                            byte[] vector = blob.getBytes(1, (int) blob.length());
	                            
	                            //FileOutputStream fos = new FileOutputStream("/tmp/f-"+blob.length()+".jpg");
	                            //fos.write(bytes);
	                            //fos.close();
	                            
	                            if (!isByteArray) {
	                            	value = Stuff.escapeString(new String(vector));
	                            
	                            //tratar como binário
	                            } else {
		                            value = "";
		                            for (int jj = 0; jj < vector.length; jj++)
			                            pr.print(Stuff.escapeByte(vector[jj]));
	                            }
                        	}
                            
                        //vetor de bytes (blob, bytea)
                        } else if (obj instanceof byte[]) {
                            
                        	if (!isNoBlob) {
	                            byte[] vetor = (byte[]) obj;
	                            
	                            //TODO	verificar se BLOB é do tipo texto ou binário
	                            //tratar como texto
	                            if (!isByteArray) {
	                            	value = Stuff.escapeString(new String(vetor));
	                            
	                            //tratar como binário
	                            } else {
		                            value = "";
		                            for (int jj = 0; jj < vetor.length; jj++) {
		                                byte bnum = vetor[jj];
		                                long lnum = Stuff.toLong(bnum);
		                                String octal = Stuff.toOctalString((int) lnum);
		                                pr.print("\\\\" + octal);
		                            }
	                            }
                        	}
                            
                        } else if (obj instanceof String) {
                            value = Stuff.escapeString((String) obj);
                            
                        } else
                            value = obj.toString();
                    }
                    
                    pr.print(value);
                }
                
                pr.print("\n");
            }
            rs.close();
            
            if (headerRead)
                pr.println("\\.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Creates the dump file in PostgreSQL format.
     */
    public void createDump() {
        
        //criar arquivo de inclusão
        try {
            pr = new PrintWriter(new BufferedWriter(new FileWriter(filename, false)));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        
        //ler lista de tabelas do esquema especificado
        try {
        	final DatabaseMetaData dbmd = conn.getMetaData();
        	final String[] types = {"TABLE"};
            
            //ResultSet resultSet = dbmd.getTables(null, null, "%", types);
            //ResultSet resultSet = dbmd.getTables(null, null, "H_Emp%", types);
            //ResultSet resultSet = dbmd.getTables(null, "SA_%", "%", types);
        	final ResultSet resultSet = dbmd.getTables(catalog, scheme, table != null ? table : "%", types);
            
            while (resultSet.next()) {
                //String tableCatalog = resultSet.getString(1);
            	final String tableSchema = resultSet.getString(2);
            	final String tableName = resultSet.getString(3);
                
                //readTable(null, tableName);
                //readTable(tableSchema, tableName);
                readTable(scheme != null ? tableSchema : null, tableName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        pr.close();
        pr = null;
    }

	public void setCatalog(final String catalog) {
		this.catalog = catalog;
	}
	
	public void setFilter(final String filter) {
		this.filter = filter;
	}
	
	public void setFilename(final String filename) {
		this.filename = filename;
	}

	public void setTable(final String table) {
		this.table = table;
	}
	
}
