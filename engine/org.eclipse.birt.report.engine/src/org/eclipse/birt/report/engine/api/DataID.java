/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.engine.api;

public class DataID
{

	protected DataSetID dataSet;
	protected long rowId;

	public DataID( DataSetID dataSet, long rowId )
	{
		this.dataSet = dataSet;
		this.rowId = rowId;
	}

	public DataSetID getDataSetID( )
	{
		return dataSet;
	}
	
	public long getRowID( )
	{
		return rowId;
	}
	
	public void append( StringBuffer buffer )
	{
		dataSet.append( buffer );
		buffer.append( ":" );
		buffer.append( rowId );
	}

	public String toString( )
	{
		StringBuffer buffer = new StringBuffer( );
		append( buffer );
		return buffer.toString( );
	}

	static DataID parse( String dataId )
	{
		return parse( dataId.toCharArray( ), 0, dataId.length( ) );
	}

	static DataID parse( char[] buffer, int offset, int length )
	{
		int ptr = offset + length - 1;
		while ( ptr >= offset && buffer[ptr] != ':' )
		{
			ptr--;
		}
		if ( ptr >= offset && buffer[ptr] == ':' )
		{
			// we found the row Id
			String strRowId = new String( buffer, ptr + 1, offset + length
					- ptr - 1 );
			long rowId = Long.parseLong( strRowId );
			ptr--; // skip the current ':'
			if ( ptr >= offset )
			{
				DataSetID dataSetId = DataSetID.parse( buffer, offset, ptr
						- offset + 1 );
				if ( dataSetId != null )
				{
					return new DataID( dataSetId, rowId );
				}
			}
		}
		return null;
	}
}
