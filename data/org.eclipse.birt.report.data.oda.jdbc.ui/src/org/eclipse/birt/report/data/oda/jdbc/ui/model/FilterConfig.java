/*******************************************************************************
 * Copyright (c) 2008 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/
package org.eclipse.birt.report.data.oda.jdbc.ui.model;

import java.util.ArrayList;

import org.eclipse.birt.report.data.oda.jdbc.ui.JdbcPlugin;


public class FilterConfig
{
	public static enum Type{ ALL, TABLE, VIEW, PROCEDURE };
	public final static String JDBC_TYPE_TABLE = "TABLE";
	public final static String JDBC_TYPE_VIEW = "VIEW";
	public final static String JDBC_TYPE_SYSTEM_TABLE = "SYSTEM TABLE";
	public final static String JDBC_TYPE_GLOBAL_TEMPORARY = "GLOBAL TEMPORARY";
	public final static String JDBC_TYPE_LOCAL_TEMPORARY = "LOCAL TEMPORARY";
	public final static String JDBC_TYPE_ALIAS = "ALIAS";
	public final static String JDBC_TYPE_SYNONYM = "SYNONYM";
	
	private Type type;
	private boolean isShowSystemTable;
	private boolean isShowAlias;
	private String schemaName; //null if no schema specified
	private String namePattern;
	
	private int maxSchemaCount;
	private int maxTableCountPerSchema;
	
	public FilterConfig( String schemaName, Type type, String namePattern,
			 boolean isShowSystemTable, boolean isShowAlias,
			 int maxSchemaCount, 
			 int maxTableCountPerSchema )
	{
		assert type != null && maxSchemaCount > 0 && maxTableCountPerSchema > 0;
		this.schemaName = schemaName;
		this.type = type;
		this.isShowSystemTable = isShowSystemTable;
		this.isShowAlias = isShowAlias;
		this.namePattern = generatePattern( namePattern );
		this.maxSchemaCount = maxSchemaCount;
		this.maxTableCountPerSchema = maxTableCountPerSchema;
	}

	public Type getType( )
	{
		return type;
	}
	
	public boolean isShowSystemTable( )
	{
		return isShowSystemTable;
	}
	
	public boolean isShowAlias( )
	{
		return isShowAlias;
	}
	
	public String getNamePattern( )
	{
		return namePattern;
	}
	
	public String getSchemaName( )
	{
		return schemaName;
	}

	
	public int getMaxSchemaCount( )
	{
		return maxSchemaCount;
	}

	
	public int getMaxTableCountPerSchema( )
	{
		return maxTableCountPerSchema;
	}
	
	/**
	 * @return null if no table/view needed to query
	 */
	public String[] getTableTypesForJDBC( )
	{
		ArrayList<String> types = new ArrayList<String>();
		switch ( type )
		{
			case PROCEDURE:
				break;
			case TABLE:
				types.add( JDBC_TYPE_TABLE );
				populateSystemTableOption( types );
				populateAliasOption( types );
				break;
			case VIEW:
				types.add( JDBC_TYPE_VIEW );
				populateAliasOption( types );
				break;
			case ALL:
				types.add( JDBC_TYPE_TABLE );
				types.add( JDBC_TYPE_VIEW );
				populateSystemTableOption( types );
				populateAliasOption( types );
				break;
			default:
				//should never goes here
				assert false;
				break;
		}
		return types.isEmpty( ) ? null : types.toArray( new String[]{} );
	}
	
	private void populateSystemTableOption( final ArrayList<String> types )
	{
		if ( isShowSystemTable( ) )
		{
			types.add( JDBC_TYPE_SYSTEM_TABLE );
		}
	}
	
	private void populateAliasOption( final ArrayList<String> types )
	{
		if ( isShowAlias( ) )
		{
			types.add( JDBC_TYPE_ALIAS );
			types.add( JDBC_TYPE_SYNONYM );
		}
	}
	
	private static String generatePattern( String input )
	{
		if ( input != null )
		{
			if ( input.lastIndexOf( '%' ) == -1 )
			{
				input = input + "%";
			}
		}
		else
		{
			input = "%";
		}
		return input;
	}
	
	public static String getTypeDisplayText( Type type )
	{
		assert type != null;
		switch ( type )
		{
			case PROCEDURE:
				return JdbcPlugin.getResourceString( "tablepage.text.procedure" );
			case TABLE:
				return JdbcPlugin.getResourceString( "tablepage.text.tabletype" );
			case VIEW:	
				return JdbcPlugin.getResourceString( "tablepage.text.viewtype" );
			case ALL:
				return JdbcPlugin.getResourceString( "tablepage.text.All" );
			default:
				//should never goes here
				assert false;
				return "";
		}
	}
}
