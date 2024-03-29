/*
 Copyright (C) 2007 MySQL AB

 This program is free software; you can redistribute it and/or modify
 it under the terms of version 2 of the GNU General Public License as 
 published by the Free Software Foundation.

 There are special exceptions to the terms and conditions of the GPL 
 as it is applied to this software. View the full text of the 
 exception in file EXCEPTIONS-CONNECTOR-J in the directory of this 
 software distribution.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

*/

package com.mysql.jdbc;

import java.sql.SQLException;

/**
 * This interface contains methods that are considered the "vendor extension"
 * to the JDBC API for MySQL's implementation of java.sql.Statement.
 * 
 * For those looking further into the driver implementation, it is not
 * an API that is used for plugability of implementations inside our driver
 * (which is why there are still references to StatementImpl throughout the
 * code).
 * 
 * @version $Id: $
 *
 */
public interface Statement extends java.sql.Statement {

	/**
	 * Workaround for containers that 'check' for sane values of
	 * Statement.setFetchSize() so that applications can use
	 * the Java variant of libmysql's mysql_use_result() behavior.
	 * 
	 * @throws SQLException
	 */
	public abstract void enableStreamingResults() throws SQLException;
	
	/**
	 * Resets this statements fetch size and result set type to the values
	 * they had before enableStreamingResults() was called.
	 * 
	 * @throws SQLException
	 */
	public abstract void disableStreamingResults() throws SQLException;

}