/*
 * org.openmicroscopy.shoola.agents.util.classifier.view.ClassifierView 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.classifier.view;



//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;


/** 
 * The {@link Classifier}'s View. Embeds the <code>AnnotatorUI</code>
 * to let users interact with annotations. Also provides statusBar
 * and a working pane. 
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
class ClassifierView
	extends JDialog
{

	/** The title of the window. */
	private static final String 	TITLE = "Categorise";
	
    /** Text displayed in the title panel. */
    private static final String     ADD_PANEL_TITLE = "Categorise";
    
    /** The default note. */
    private static final String     PANEL_NOTE = "Expand list to select the " +
    		"categories to add the images to.";
    
    /** Text displayed in the note panel. */
    private static final String     REMOVE_PANEL_NOTE = "The image is " +
            "currently classified under the following categories. ";
    
    /** Text displayed in the text panel. */
    private static final String     ADD_PANEL_TEXT = "Categorise the " +
    										"selected images."; 
    
    /** Text displayed in the title panel. */
    private static final String     REMOVE_PANEL_TITLE = "Decategorise";
    
    /** Text displayed in the text panel. */
    private static final String     REMOVE_PANEL_TEXT = "Decategorise the " +
                                                        "following image: ";
    
    /** Text displayed in the text panel. */
    private static final String     BULK_PANEL_TEXT = "Categorise the " +
    									"images  contained in the selected " +
    										"folders."; 
    
	/** The default size of the window. */
	private static final Dimension DEFAULT_SIZE = new Dimension(700, 500);
    
    /** 
     * The size of the invisible components used to separate buttons
     * horizontally.
     */
    private static final Dimension  H_SPACER_SIZE = new Dimension(5, 10);
    
	/** The Controller. */
    private ClassifierControl	controller;

    /** The Model. */
    private ClassifierModel		model;
    
    /** The UI component displaying the classification paths. */
    private ClassifierUI		classifierUI;
    
    /** The status bar. */
    private StatusBar			statusBar;
    
    /** Button to select all nodes. */
    private JButton				selectAll;
    
    /** Button to deselect all nodes. */
    private JButton				deselectAll;
    
    /** 
     * Flag indicating to invoke the {@link #pack()} method or the 
     * {@link #setSize(Dimension)} method.
     */
    private boolean				pack;
    
    /** Initializes the UI components. */
    private void initComponents()
    {
    	selectAll = new JButton("Select All");
    	selectAll.setToolTipText("Select all categories");
    	selectAll.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent e) {
				classifierUI.selectAll();
			}
		
		});
    	selectAll.setEnabled(false);
    	deselectAll = new JButton("Deselect All");
    	deselectAll.setToolTipText("Deselect all categories");
    	deselectAll.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent e) {
				classifierUI.deselectAll();
			}
		
		});
    	deselectAll.setEnabled(false);
    }
    
    /**
     * Builds the tool bar hosting the selection buttons.
     * 
     * @return See above.
     */
    private JPanel buildSelectToolBar()
    {
    	JPanel bar = new JPanel();
    	bar.setBorder(null);
    	bar.add(selectAll);
        bar.add(Box.createRigidArea(H_SPACER_SIZE));
        bar.add(deselectAll);
    	return UIUtilities.buildComponentPanel(bar);
    }
    
    /** 
     * Builds the UI component hosting the controls.
     * 
     * @return See above.
     */
    private JPanel buildControlsToolBar()
    {
    	JPanel bar = new JPanel();
    	bar.setBorder(null);
    	JButton b = new JButton(controller.getAction(ClassifierControl.FINISH));
        bar.add(b);
        getRootPane().setDefaultButton(b);
        bar.add(Box.createRigidArea(H_SPACER_SIZE));
        b = new JButton(controller.getAction(ClassifierControl.CANCEL));
        bar.add(b);
        return UIUtilities.buildComponentPanelRight(bar);
    }
    
    /**
     * Builds the UI component displaying the annotations.
     * 
     * @return See above.
     */
    private JPanel buildBody()
    {
    	JPanel p = new JPanel();
    	p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    	p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    	p.add(classifierUI);
    	p.add(new JSeparator());
    	JPanel bars = new JPanel();
    	bars.setLayout(new BoxLayout(bars, BoxLayout.X_AXIS));
    	bars.add(buildSelectToolBar());
    	bars.add(buildControlsToolBar());
    	p.add(bars);
    	return p;
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
    	IconManager icons = IconManager.getInstance();
    	TitlePanel tp = new TitlePanel(getWindowTitle(), getWindowText(), 
    						getWindowNote(),
    						icons.getIcon(IconManager.CLASSIFICATION_48));
    	
    	Container c = getContentPane();
    	//c.setLayout(new BorderLayout(0, 0));
        c.add(tp, BorderLayout.NORTH);
        c.add(buildBody(), BorderLayout.CENTER);
        c.add(statusBar, BorderLayout.SOUTH);
    }
    
    /** 
     * Returns the title displayed in the titlePanel.
     * 
     * @return See above.
     */
    private String getWindowTitle()
    {
        switch (model.getMode()) {
            case Classifier.CLASSIFY_MODE: 
            case Classifier.BULK_CLASSIFY_MODE:
            	return ADD_PANEL_TITLE;
            case Classifier.DECLASSIFY_MODE: return REMOVE_PANEL_TITLE;
        }
        return "";
    }
    
    /** 
     * Returns the text displayed in the titlePanel.
     * 
     * @return See above.
     */
    private String getWindowText()
    {
        switch (model.getMode()) {
            case Classifier.CLASSIFY_MODE: return ADD_PANEL_TEXT;
            case Classifier.DECLASSIFY_MODE: return REMOVE_PANEL_TEXT;
            case Classifier.BULK_CLASSIFY_MODE: return BULK_PANEL_TEXT;
        }
        return "";
    }
    
    /**
     * Returns the note displayed in the titlePanel.
     * 
     * @return See above.
     */
    private String getWindowNote()
    {
        switch (model.getMode()) {
            case Classifier.CLASSIFY_MODE: 
            case Classifier.BULK_CLASSIFY_MODE:
            	return PANEL_NOTE;
            case Classifier.DECLASSIFY_MODE: return REMOVE_PANEL_NOTE;
        }
        return "";
    }
    
    /** Sets the properties of the window. */
    private void setProperties()
    {
    	setModal(true);
    	setTitle(TITLE);
    }
    
    /** Creates a new instance. */
    ClassifierView()
    {
    	super(ClassifierFactory.getOwner());
    	setProperties();
    }
    
    /**
     * Links this View to its Controller.
     * 
     * @param model 		The Model. Mustn't be <code>null</code>. 
     * @param controller 	The Controller. Mustn't be <code>null</code>.
     */
	void initialize(ClassifierModel model, ClassifierControl controller)
	{
		if (model == null) throw new IllegalArgumentException("No model.");
		if (controller == null) 
			throw new IllegalArgumentException("No control.");
		this.controller = controller;
        this.model = model;
        statusBar = new StatusBar();
        classifierUI = new ClassifierUI(model, controller);
        initComponents();
        setTitle(getWindowTitle());
    	buildGUI();
	}
    
	/**
	 * Sets the status of the {@link #statusBar}.
	 * 
	 * @param text 	The status message.
	 * @param hide	Pass <code>true</code> to hide the progress bar, 
     *              <code>false</otherwise>.
	 */
	void setStatus(String text, boolean hide)
	{
		statusBar.setStatus(text);
		statusBar.setProgress(hide);
	}
	
	/** Displays the classifications. */
	void showClassifications()
	{
		Set paths = model.getClassificationPaths();
		boolean b = true;
		if (paths == null || paths.size() == 0) {
			b = false;
			pack = true;
		}
		selectAll.setEnabled(b);
		deselectAll.setEnabled(b);
		classifierUI.showClassifications();
	}
	
    /**
     * Returns the collection of selected paths or <code>null</code>
     * if no path selected.
     * 
     * @return See above.
     */
    Set getSelectedPaths() { return classifierUI.getSelectedPaths(); }
    
    /** 
     * Sets the <code>enabled</code> flag of the selection buttons to 
     * <code>false</code>.
     */
	void finish()
	{
		selectAll.setEnabled(false);
		deselectAll.setEnabled(false);
	}
	
	/** Sets the window on screen. */
	void setOnScreen()
	{
		if (pack) pack();
		else setSize(DEFAULT_SIZE);
		UIUtilities.centerAndShow(this);
	}
	
}
