
package org.eclipse.birt.report.designer.internal.ui.views.attributes.provider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.birt.report.designer.core.model.SessionHandleAdapter;
import org.eclipse.birt.report.designer.internal.ui.editors.parts.event.IModelEventProcessor;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.internal.ui.util.UIUtil;
import org.eclipse.birt.report.designer.internal.ui.views.dialogs.provider.MapHandleProvider;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.ui.dialogs.MapRuleBuilder;
import org.eclipse.birt.report.model.api.ColumnHandle;
import org.eclipse.birt.report.model.api.CommandStack;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.GroupHandle;
import org.eclipse.birt.report.model.api.MapRuleHandle;
import org.eclipse.birt.report.model.api.PropertyHandle;
import org.eclipse.birt.report.model.api.ReportItemHandle;
import org.eclipse.birt.report.model.api.RowHandle;
import org.eclipse.birt.report.model.api.StructureHandle;
import org.eclipse.birt.report.model.api.StyleHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.elements.structures.MapRule;
import org.eclipse.birt.report.model.api.metadata.PropertyValueException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;

public class MapDescriptorProvider extends MapHandleProvider implements
		PreviewPropertyDescriptorProvider
{

	public MapDescriptorProvider( )
	{
		super( );
	}

	public MapDescriptorProvider( int expressionType )
	{
		super( expressionType );
	}

	class MapLabelProvider extends LabelProvider implements ITableLabelProvider
	{

		public Image getColumnImage( Object element, int columnIndex )
		{
			return null;
		}

		public String getColumnText( Object element, int columnIndex )
		{
			return MapDescriptorProvider.this.getColumnText( element, 1 );
		}

	}

	class MapContentProvider implements IStructuredContentProvider
	{

		private IModelEventProcessor listener;

		public MapContentProvider( IModelEventProcessor listener )
		{
			this.listener = listener;
		}

		public Object[] getElements( Object inputElement )
		{
			Object[] elements = MapDescriptorProvider.this.getElements( inputElement );

			deRegisterEventManager( );
			registerEventManager( );

			return elements;
		}

		public void inputChanged( Viewer viewer, Object oldInput,
				Object newInput )
		{
		}

		public void dispose( )
		{
			deRegisterEventManager( );
		}

		protected void deRegisterEventManager( )
		{
			if ( UIUtil.getModelEventManager( ) != null )
				UIUtil.getModelEventManager( )
						.removeModelEventProcessor( listener );
		}

		/**
		 * Registers model change listener to DE elements.
		 */
		protected void registerEventManager( )
		{
			if ( UIUtil.getModelEventManager( ) != null )
				UIUtil.getModelEventManager( )
						.addModelEventProcessor( listener );
		}

	}

	public String getColumnText( Object element, int columnIndex )
	{
		MapRuleHandle handle = (MapRuleHandle) element;

		switch ( columnIndex )
		{
			case 0 :
				String pv = handle.getDisplay( );

				return pv == null ? "" : pv; //$NON-NLS-1$

			case 1 :
				// String exp = resolveNull( getTestExpression( ) )
				String exp = resolveNull( handle.getTestExpression( ) ) + " " //$NON-NLS-1$
						+ MapRuleBuilder.getNameForOperator( handle.getOperator( ) );

				int vv = MapRuleBuilder.determineValueVisible( handle.getOperator( ) );

				if ( vv == 1 )
				{
					exp += " " + resolveNull( handle.getValue1( ) ); //$NON-NLS-1$
				}
				else if ( vv == 2 )
				{
					exp += " " //$NON-NLS-1$
							+ resolveNull( handle.getValue1( ) )
							+ " , " //$NON-NLS-1$
							+ resolveNull( handle.getValue2( ) );
				}
				else if ( vv == 3 )
				{
					exp += " "; //$NON-NLS-1$
					int count = handle.getValue1List( ).size( );
					for ( int i = 0; i < count; i++ )
					{
						if(i == 0 )
						{
							exp += handle.getValue1List( ).get( i ).toString( );
						}else
						{
							exp += "; " + handle.getValue1List( ).get( i ).toString( ); //$NON-NLS-1$
						}
					}
				}

				return exp;

			default :
				return ""; //$NON-NLS-1$
		}
	}

	private String resolveNull( String src )
	{
		if ( src == null )
		{
			return ""; //$NON-NLS-1$
		}

		return src;
	}

	public boolean doSwapItem( int pos, int direction )
			throws PropertyValueException
	{
		PropertyHandle phandle = elementHandle.getPropertyHandle( StyleHandle.MAP_RULES_PROP );

		if ( direction < 0 )
		{
			phandle.moveItem( pos, pos - 1 );
		}
		else
		{
			/**
			 * Original code: phandle.moveItem( pos, pos + 1 );
			 * 
			 * Changes due to model api changes. since property handle now
			 * treats moving from 0-0, 0-1 as the same.
			 */
			phandle.moveItem( pos, pos + 1 );
		}

		return true;
	}

	public IStructuredContentProvider getContentProvider(
			IModelEventProcessor listener )
	{
		return new MapContentProvider( listener );
	}

	public LabelProvider getLabelProvider( )
	{
		return new MapLabelProvider( );
	}

	private static final MapRuleHandle[] EMPTY = new MapRuleHandle[0];

	public Object[] getElements( Object inputElement )
	{
		if ( inputElement instanceof List )
		{
			if ( ( (List) inputElement ).size( ) > 0 )
			{
				inputElement = ( (List) inputElement ).get( 0 );
			}
			else
			{
				inputElement = null;
			}
		}

		if ( inputElement instanceof DesignElementHandle )
		{
			elementHandle = (DesignElementHandle) inputElement;

			PropertyHandle mapRules = elementHandle.getPropertyHandle( StyleHandle.MAP_RULES_PROP );

			ArrayList list = new ArrayList( );

			for ( Iterator itr = mapRules.iterator( ); itr.hasNext( ); )
			{
				Object o = itr.next( );

				list.add( o );
			}

			return (MapRuleHandle[]) list.toArray( new MapRuleHandle[0] );
		}

		return EMPTY;
	}

	public boolean doDeleteItem( int pos ) throws PropertyValueException
	{
		PropertyHandle phandle = elementHandle.getPropertyHandle( StyleHandle.MAP_RULES_PROP );

		phandle.removeItem( pos );

		return true;
	}

	public MapRuleHandle doAddItem( MapRule rule, int pos )
	{
		PropertyHandle phandle = elementHandle.getPropertyHandle( StyleHandle.MAP_RULES_PROP );

		try
		{
			phandle.addItem( rule );
		}
		catch ( SemanticException e )
		{
			ExceptionHandler.handle( e );
		}

		StructureHandle handle = rule.getHandle( phandle, pos );

		return (MapRuleHandle) handle;
	}

	public boolean edit( Object input, int handleCount )
	{
		boolean result = false;
		CommandStack stack = SessionHandleAdapter.getInstance( )
				.getCommandStack( );

		try
		{
			stack.startTrans( Messages.getString( "MapPage.transName.editMapRule" ) ); //$NON-NLS-1$

			MapRuleBuilder builder = new MapRuleBuilder( UIUtil.getDefaultShell( ),
					MapRuleBuilder.DLG_TITLE_EDIT,
					this );

			MapRuleHandle handle = (MapRuleHandle) input;

			builder.updateHandle( handle, handleCount );

			builder.setDesignHandle( getDesignElementHandle( ) );

			DesignElementHandle reportElement = getDesignElementHandle( );
			if ( getDesignElementHandle( ) instanceof RowHandle )
			{
				DesignElementHandle designElement = ( (RowHandle) getDesignElementHandle( ) ).getContainer( );
				if ( designElement instanceof ReportItemHandle )
				{
					reportElement = (ReportItemHandle) designElement;
				}
				else if ( designElement instanceof GroupHandle )
				{
					reportElement = (ReportItemHandle) ( (GroupHandle) designElement ).getContainer( );
				}
			}
			if ( reportElement instanceof ReportItemHandle )
			{
				builder.setReportElement( (ReportItemHandle) reportElement );
			}
			else if ( reportElement instanceof GroupHandle )
			{
				builder.setReportElement( (ReportItemHandle) ( (GroupHandle) reportElement ).getContainer( ) );
			}

			if ( builder.open( ) == Window.OK )
			{
				result = true;
			}
			stack.commit( );

		}
		catch ( Exception e )
		{
			stack.rollback( );
			ExceptionHandler.handle( e );
			result = false;
		}
		return result;
	}

	public boolean add( int handleCount )
	{
		boolean result = false;;
		CommandStack stack = SessionHandleAdapter.getInstance( )
				.getCommandStack( );

		try
		{
			stack.startTrans( Messages.getString( "MapPage.transName.addMapRule" ) ); //$NON-NLS-1$

			Dialog dialog = createAddDialog( handleCount );

			if ( dialog.open( ) == Window.OK )
			{
				result = true;
			}

			stack.commit( );

		}
		catch ( Exception e )
		{
			stack.rollback( );
			ExceptionHandler.handle( e );
			result = false;
		}
		return result;
	}

	protected MapRuleBuilder createAddDialog( int handleCount )
	{
		MapRuleBuilder builder = new MapRuleBuilder( UIUtil.getDefaultShell( ),
				MapRuleBuilder.DLG_TITLE_NEW, //$NON-NLS-1$
				this );

		builder.updateHandle( null, handleCount );

		builder.setDesignHandle( getDesignElementHandle( ) );

		DesignElementHandle reportElement = getDesignElementHandle( );
		if ( reportElement instanceof RowHandle )
		{
			DesignElementHandle designElement = ( (RowHandle) reportElement ).getContainer( );
			if ( designElement instanceof ReportItemHandle )
			{
				reportElement = (ReportItemHandle) designElement;
			}
			else if ( designElement instanceof GroupHandle )
			{
				reportElement = (ReportItemHandle) ( (GroupHandle) designElement ).getContainer( );
			}
		}else
		if( reportElement instanceof ColumnHandle)
		{
			DesignElementHandle designElement = ( (ColumnHandle)reportElement ).getContainer( );
			if ( designElement instanceof ReportItemHandle )
			{
				reportElement = (ReportItemHandle) designElement;
			}
			else if ( designElement instanceof GroupHandle )
			{
				reportElement = (ReportItemHandle) ( (GroupHandle) designElement ).getContainer( );
			}
		}
		if ( reportElement instanceof ReportItemHandle )
		{
			builder.setReportElement( (ReportItemHandle) reportElement );
		}
		else if ( reportElement instanceof GroupHandle )
		{
			builder.setReportElement( (ReportItemHandle) ( (GroupHandle) reportElement ).getContainer( ) );
		}

		return builder;
	}

	public boolean delete( int index )
	{
		boolean result = false;
		CommandStack stack = SessionHandleAdapter.getInstance( )
				.getCommandStack( );

		try
		{
			stack.startTrans( Messages.getString( "MapPage.transName.deleteMapRule" ) ); //$NON-NLS-1$

			doDeleteItem( index );

			stack.commit( );

			result = true;

		}
		catch ( Exception e )
		{
			stack.rollback( );
			ExceptionHandler.handle( e );
			result = false;
		}
		return result;
	}

	public boolean moveUp( int index )
	{
		boolean result = false;
		CommandStack stack = SessionHandleAdapter.getInstance( )
				.getCommandStack( );

		try
		{
			stack.startTrans( Messages.getString( "MapPage.transName.moveUpMapRule" ) ); //$NON-NLS-1$

			doSwapItem( index, -1 );

			stack.commit( );

			result = true;

		}
		catch ( Exception e )
		{
			stack.rollback( );
			ExceptionHandler.handle( e );
			result = false;
		}
		return result;
	}

	public boolean moveDown( int index )
	{

		boolean result = false;
		CommandStack stack = SessionHandleAdapter.getInstance( )
				.getCommandStack( );

		try
		{
			stack.startTrans( Messages.getString( "MapPage.transName.moveDownRule" ) ); //$NON-NLS-1$

			doSwapItem( index, 1 );

			stack.commit( );

			result = true;

		}
		catch ( Exception e )
		{
			stack.rollback( );
			ExceptionHandler.handle( e );
			result = false;
		}
		return result;
	}

	protected Object input;

	public void setInput( Object input )
	{
		this.input = input;
	}

	public String getDisplayName( )
	{
		// TODO Auto-generated method stub
		return Messages.getString( "MapPage.label.mapList" ); //$NON-NLS-1$
	}

	public Object load( )
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void save( Object value ) throws SemanticException
	{
		// TODO Auto-generated method stub

	}

	public String getText( int key )
	{
		switch ( key )
		{
			case 0 :
				return Messages.getString( "MapPage.label.mapList" ); //$NON-NLS-1$
			case 1 :
				return Messages.getString( "MapPage.label.add" ); //$NON-NLS-1$
			case 2 :
				return Messages.getString( "MapPage.label.delete" ); //$NON-NLS-1$
			case 3 :
				return Messages.getString( "FormPage.Button.Up" ); //$NON-NLS-1$
			case 4 :
				return Messages.getString( "MapPage.toolTipText.moveUp" ); //$NON-NLS-1$
			case 5 :
				return Messages.getString( "FormPage.Button.Down" ); //$NON-NLS-1$
			case 6 :
				return Messages.getString( "MapPage.toolTipText.moveDown" ); //$NON-NLS-1$
			case 7 :
				return Messages.getString( "MapPage.label.displayValue" ); //$NON-NLS-1$
			case 8 :
				return Messages.getString( "MapPage.label.condition" ); //$NON-NLS-1$
			case 9 :
				return Messages.getString( "" ); //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}

	public String getDisplayText( Object handle )
	{
		return ( (MapRuleHandle) handle ).getDisplay( );
	}

}
