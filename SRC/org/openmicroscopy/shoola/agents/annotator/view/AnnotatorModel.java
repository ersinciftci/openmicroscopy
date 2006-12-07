/*
 * org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorModel 
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.util.annotator.view;


//Java imports
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.annotator.AnnotationsLoader;
import org.openmicroscopy.shoola.agents.util.annotator.AnnotationsSaver;
import org.openmicroscopy.shoola.agents.util.annotator.AnnotatorLoader;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;

/** 
 * The Model component in the <code>Annotator</code> MVC triad.
 * This class tracks the <code>Annotator</code>'s state and knows how to
 * initiate data retrievals. It also knows how to store and manipulate
 * the results. This class  provide  a suitable data loader. 
 * The {@link AnnotatorComponent} intercepts the 
 * results of data loadings, feeds them back to this class and fires state
 * transitions as appropriate.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class AnnotatorModel
{

    /** Holds one of the state flags defined by {@link Annotator}. */
    private int				state;
    
	/** Collection of <code>DataObject</code>s to annotate. */
	private Set 			toAnnotate;
	
	/** Collection of <code>DataObject</code>s already annotated. */
	private Set				annotated;
	
	/** The type of <code>DataObject</code>s to annotate. */
	private Class			type;
	
	/** The annotations retrieved for the annotated <code>DataObject</code>s. */
	private Map				annotations;
	
	/** The number of objects to handle. */
	private int				maxObjects;
	
    /** 
     * Will either be a data loader or
     * <code>null</code> depending on the current state. 
     */
	private AnnotatorLoader	currentLoader;
	
    /** Reference to the component that embeds this model. */
    protected Annotator		component;
    
    /**
     * Returns <code>true</code> if the <code>DataObject</code> has been 
     * annotated by the current user, <code>false</code> otherwise.
     * 
     * @param data The <code>DataObject</code> to handle.
     * @return See above.
     */
    private boolean isObjectAnnotated(DataObject data)
    {
    	Long n = null;
    	if (data instanceof ImageData)  {
    		type = ImageData.class;
    		n = ((ImageData) data).getAnnotationCount();
    	} else if (data instanceof DatasetData) {
    		type = DatasetData.class;
    		n = ((DatasetData) data).getAnnotationCount();
    	}
    	if (n == null) return false;
    	return (n.longValue() != 0);
    }
    
    /**
     * Returns <code>true</code> if the <code>DataObject</code> can be
     * annotated, <code>false</code> otherwise.
     * 
     * @param data The <code>DataObject</code> to handle. 
     * @return See above.
     */
    private boolean isAnnotatable(DataObject data)
    {
    	if ((data instanceof ImageData)) {
    		type = ImageData.class;
    		return true;
    	} else if (data instanceof DatasetData) {
    		type = DatasetData.class;
    		return true;
    	}
    	return false;
    }
    
    /**
     * Returns the partial name of the image's name
     * 
     * @param originalName The original name.
     * @return See above.
     */
    private String getPartialName(String originalName)
    {
        if (Pattern.compile("/").matcher(originalName).find()) {
            String[] l = originalName.split("/", 0);
            int n = l.length;
            if (n == 1) return l[0];
            return UIUtilities.DOTS+l[n-2]+"/"+l[n-1]; 
        } else if (Pattern.compile("\\\\").matcher(originalName).find()) {
            String[] l = originalName.split("\\\\", 0);
            int n = l.length;
            if (n == 1) return l[0];
            return UIUtilities.DOTS+l[n-2]+"\\"+l[n-1];
        } 
        return originalName;
    }

    /** 
     * Returns the last annotation.
     * 
     * @param list   Collection of {@link AnnotationData} linked to 
     *               a <code>Dataset</code> or an <code>Image</code>.
     * @return See above.
     */
    private AnnotationData getLastAnnotation(List list)
    {
        if (list == null || list.size() == 0) return null;
        Comparator c = new Comparator() {
            public int compare(Object o1, Object o2)
            {
                Timestamp t1 = ((AnnotationData) o1).getLastModified(),
                          t2 = ((AnnotationData) o2).getLastModified();
                long n1 = t1.getTime();
                long n2 = t2.getTime();
                int v = 0;
                if (n1 < n2) v = -1;
                else if (n1 > n2) v = 1;
                return v;
            }
        };
        Collections.sort(list, c);
        return (AnnotationData) list.get(list.size()-1);
    }
    
	/**
	 * Builds the map whose key is the annotated object and value
	 * the object to update.
	 * 
	 * @param data The annotation object.
	 * @return See above.
	 */
	private Map getAnnotatedObjects(AnnotationData data)
	{
		Map m = new HashMap(annotated.size());
		Iterator i = annotated.iterator();
		DataObject object;
		List l;
		AnnotationData d;
		while (i.hasNext()) {
			object = (DataObject) i.next();
			l = (List) annotations.get(new Long(object.getId()));
			d = getLastAnnotation(l);
			d.setText(data.getText());
			m.put(object, d);
		}
		return m;
	}
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param objects Collection of <code>DataObject</code>s to annotate.
	 */
	AnnotatorModel(Set objects)
	{
		maxObjects = objects.size();
		annotated = new HashSet();
		toAnnotate  = new HashSet();
		state = Annotator.NEW;
		Iterator i = objects.iterator();
		DataObject data;
		while (i.hasNext()) {
			data = (DataObject) i.next();
			if (isObjectAnnotated(data)) annotated.add(data);
			else {
				if (isAnnotatable(data)) toAnnotate.add(data);
			}
		}
	}
	
	/**
     * Called by the <code>Annotator</code> after creation to allow this
     * object to store a back reference to the embedding component.
     * 
     * @param component The embedding component.
     */
	void initialize(Annotator component) { this.component = component; }
	
	/**
     * Returns the current state.
     * 
     * @return One of the flags defined by the {@link Annotator} interface.  
     */
	int getState() { return state; }    
   
	/**
     * Sets the object in the {@link Annotator#DISCARDED} state.
     * Any ongoing data loading will be cancelled.
     */
	void discard()
	{
		cancel();
        state = Annotator.DISCARDED;
    }
   
	/**
	 * Sets the object in the {@link Annotator#READY} state.
	 * Any ongoing data loading will be cancelled.
	 */
	void cancel()
	{
       if (currentLoader != null) {
           currentLoader.cancel();
           currentLoader = null;
       }
       state = Annotator.READY;
    }
	
	/**
	 * Returns the name of the specified <code>DataObject</code>.
	 * 
	 * @param data The object to handle.
	 * @return See above.
	 */
	String getDataObjectName(DataObject data)
	{
		if (data instanceof ImageData) {
			return getPartialName(((ImageData) data).getName());
		} else if (data instanceof DatasetData) 
			return ((DatasetData) data).getName();
		return null;
	}
	
	/**
	 * Loads asynchronously the annotations for the annotated 
	 * <code>DataObject</code>s.
	 */
	void fireAnnotationsRetrieval()
	{
		if (annotated.size() == 0) state = Annotator.READY;
		else {
			currentLoader = new AnnotationsLoader(component, annotated, type);
			currentLoader.load();
			state = Annotator.LOADING;
		}
	}
	
	/** 
	 * Saves asynchronously the annotation. 
	 * 
	 * @param data The annotation.
	 */
	void fireAnnotationSaving(AnnotationData data)
	{ 
		if (annotated.size() == maxObjects)
			currentLoader = new AnnotationsSaver(component,  
											getAnnotatedObjects(data));
		else if (toAnnotate.size() == maxObjects) 
			currentLoader = new AnnotationsSaver(component, toAnnotate, data);
		else 
			currentLoader = new AnnotationsSaver(component, 
								 getAnnotatedObjects(data), toAnnotate, data);
		currentLoader.load();
		state = Annotator.SAVING;
	}
	
	/**
	 * Sets the annotations found to the annotated <code>DataObject</code>s
	 * 
	 * @param map The value to set.
	 */
	void setAnnotations(Map map)
	{
        ViewerSorter sorter = new ViewerSorter();
        sorter.setAscending(false);
        HashMap sortedAnnotations = new HashMap();
        Set set;
        Long index;
        Iterator i = map.keySet().iterator(), l;
        Iterator j;
        AnnotationData data;
        HashMap m;
        List timestamps, results, list;
        while (i.hasNext()) {
            index = (Long) i.next();
            set = (Set) map.get(index);
            j = set.iterator();
            m = new HashMap(set.size());
            timestamps = new ArrayList(set.size());
            while (j.hasNext()) {
                data = (AnnotationData) j.next();
                m.put(data.getLastModified(), data);
                timestamps.add(data.getLastModified()); 
            }
            results = sorter.sort(timestamps);
            l = results.iterator();
            list = new ArrayList(results.size());
            while (l.hasNext())
                list.add(m.get(l.next()));
            sortedAnnotations.put(index, list);
        }
        
        this.annotations = sortedAnnotations;
		state = Annotator.READY;
	}
	
	/** 
	 * Returns the annotations retrieved for the annotated 
	 * <code>DataObject</code>s.
	 * 
	 * @return See above.
	 */
	Map getAnnotations() { return annotations; }
	
	/**
	 * Returns the type of annotations to handle.
	 * 
	 * @return See above.
	 */
	AnnotationData getAnnotationType()
	{ 
		if (type.equals(DatasetData.class))
			return new AnnotationData(AnnotationData.DATASET_ANNOTATION); 
		return new AnnotationData(AnnotationData.IMAGE_ANNOTATION); 
		
	}
	
}
