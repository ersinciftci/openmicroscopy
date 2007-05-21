/*
 * org.openmicroscopy.shoola.util.roi.ROIComponent 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.roi;


//Java imports
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.TreeMap;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.roi.exception.NoSuchShapeException;
import org.openmicroscopy.shoola.util.roi.exception.ROICreationException;
import org.openmicroscopy.shoola.util.roi.exception.ROIShapeCreationException;
import org.openmicroscopy.shoola.util.roi.io.XMLFileIOStrategy;
import org.openmicroscopy.shoola.util.roi.io.XMLIOStrategy;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROICollection;
import org.openmicroscopy.shoola.util.roi.model.ROIRelationship;
import org.openmicroscopy.shoola.util.roi.model.ROIRelationshipList;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.ROIShapeRelationship;
import org.openmicroscopy.shoola.util.roi.model.ROIShapeRelationshipList;
import org.openmicroscopy.shoola.util.roi.model.ShapeList;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public 	class ROIComponent 
		extends Component 
		implements PropertyChangeListener
{
	private ROICollection		roiCollection;
	private XMLIOStrategy		ioStrategy;
	
	public ROIComponent()
	{
		roiCollection = new ROICollection();
		ioStrategy = new XMLFileIOStrategy();
	}
	
	public void saveROI(String filename) throws IOException
	{
		ioStrategy.write(filename, this);
	}
	
	public void loadROI(String filename) throws IOException, ROIShapeCreationException, NoSuchROIException, ROICreationException
	{
		ioStrategy.read(filename, this);
	}
	
	public long getNextID()
	{
		return roiCollection.getNextID();
	}
	
	public ROI createROI(long id) 				throws ROICreationException
	{
		return roiCollection.createROI(id);
	}
	
	public ROI createROI() 						throws 	ROICreationException
	{
		return roiCollection.createROI();
	}
	
	public TreeMap<Long, ROI>  getROIMap()
	{
		return roiCollection.getROIMap();
	}
	
	public ROI getROI(long id) throws NoSuchROIException
	{
		return roiCollection.getROI(id);
	}
	
	public ROIShape getShape(long id, Coord3D coord) 
												throws 	NoSuchROIException, 
														NoSuchShapeException
	{
		return roiCollection.getShape(id, coord);
	}
	
	public ShapeList getShapeList(Coord3D coord) throws
														NoSuchShapeException
	{
		return roiCollection.getShapeList(coord);
	}
	
	public void deleteROI(long id) throws NoSuchROIException
	{
		roiCollection.deleteROI(id);
	}
	
	public void deleteShape(long id, Coord3D coord) 	
												throws 	NoSuchROIException, 
														NoSuchShapeException
	{
		roiCollection.deleteShape(id, coord);
	}
	
	public 	void addShape(long id, Coord3D coord, ROIShape shape) 
												throws ROIShapeCreationException, 
													   NoSuchROIException
	{
		roiCollection.addShape(id, coord, shape);
	}	
	
	public void propagateShape(long id, Coord3D selectedShape, Coord3D start, 
															   Coord3D end) 
												throws ROIShapeCreationException, 
												       NoSuchROIException, 
												       NoSuchShapeException
	{
		roiCollection.propagateShape(id, selectedShape, start, end);
	}
	
	public void deleteShape(long id, Coord3D start, Coord3D end) 
												throws 	NoSuchROIException, 
														NoSuchShapeException
	{
		roiCollection.deleteShape(id, start, end);
	}
	
	public void addROIRelationship(ROIRelationship relationship)
	{
		roiCollection.addROIRelationship(relationship);
	}
	
	public void addROIShapeRelationship(ROIShapeRelationship relationship)
	{
		roiCollection.addROIShapeRelationship(relationship);
	}
	
	public void removeROIRelationship(long relationship)
	{
		roiCollection.removeROIRelationship(relationship);
	}
	
	public void removeROIShapeRelationship(long relationship)
	{
		roiCollection.removeROIShapeRelationship(relationship);
	}
	
	public boolean containsROIRelationship(long relationship)
	{
		return roiCollection.containsROIRelationship(relationship);
	}
	
	public boolean containsROIShapeRelationship(long relationship)
	{
		return roiCollection.containsROIShapeRelationship(relationship);
	}
	
	public ROIRelationshipList getROIRelationshipList(long roiID)
	{
		return roiCollection.getROIRelationshipList(roiID);
	}
	
	public ROIShapeRelationshipList getROIShapeRelationshipList(long roiID)
	{
		return roiCollection.getROIShapeRelationshipList(roiID);
	}
	
	
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
		
	}

}


