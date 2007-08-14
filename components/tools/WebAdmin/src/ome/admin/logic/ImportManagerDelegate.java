/*
 * ome.admin.controller
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.admin.logic;

// Java imports
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// Third-party libraries
import org.apache.commons.beanutils.BeanUtils;

// Application-internal dependencies
import ome.admin.data.CSVWorkbookReader;
import ome.admin.data.HSSFWorkbookReader;
import ome.admin.model.MyExperimenter;
import ome.model.meta.Experimenter;

public class ImportManagerDelegate implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * {@link java.lang.String}
	 */
	private String sortByProperty = "firstName";

	/**
	 * {@link java.lang.String} - whole path is required
	 */
	private String filePath;

	/**
	 * {@link java.util.List}<{@link ome.model.meta.Experimenter}>.
	 */
	private List<MyExperimenter> experimenters = Collections.EMPTY_LIST;

	/**
	 * {@link java.util.Comparator}
	 */
	private transient final Comparator propertyAscendingComparator = new Comparator() {
		public int compare(Object object1, Object object2) {
			try {
				String property1 = BeanUtils.getProperty(object1,
						ImportManagerDelegate.this.sortByProperty);
				String property2 = BeanUtils.getProperty(object2,
						ImportManagerDelegate.this.sortByProperty);

				return property1.toLowerCase().compareTo(
						property2.toLowerCase());
			} catch (Exception e) {
				return 0;
			}
		}
	};

	/**
	 * {@link java.util.Comparator}
	 */
	private transient final Comparator propertyDescendingComparator = new Comparator() {
		public int compare(Object object1, Object object2) {
			try {
				String property1 = BeanUtils.getProperty(object1,
						ImportManagerDelegate.this.sortByProperty);
				String property2 = BeanUtils.getProperty(object2,
						ImportManagerDelegate.this.sortByProperty);

				return property2.toLowerCase().compareTo(
						property1.toLowerCase());
			} catch (Exception e) {
				return 0;
			}
		}
	};

	/**
	 * Gets type of file. Last three signs.
	 * 
	 * @return {@link java.lang.String} last three signs.
	 */
	public String getType() {
		return this.filePath.substring((this.filePath.length()) - 3);
	}

	/**
	 * Gets {@link java.util.List} of {@link ome.admin.model.MyExperimenter}
	 * from the specified file. There is possible to import users from XLS, CSV
	 * and XML. Not supported format throws IOException
	 * 
	 * @return {@link java.util.List}<{@link ome.admin.model.MyExperimenter}>.
	 * @throws FileNotFoundException,
	 *             IOException
	 */
	public List<MyExperimenter> lookupImportingExperimenters()
			throws FileNotFoundException, IOException {

		if (getType().equals("xls")) {
			HSSFWorkbookReader hssf = new HSSFWorkbookReader(this.filePath);
			this.experimenters = hssf.importingExperimenters();
		} else if (getType().equals("csv")) {
			CSVWorkbookReader csv = new CSVWorkbookReader(this.filePath);
			this.experimenters = csv.importingExperimenters();
		} else if (getType().equals("xml")) {
			this.experimenters = null;
		} else
			throw new IOException("File type is not support.");

		return experimenters;
	}

	/**
	 * Gets {@link ome.admin.logic.ImportManagerDelegate#filePath}
	 * 
	 * @return String.
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * Sets {@link ome.admin.logic.ImportManagerDelegate#filePath}
	 * 
	 * @param filePath
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * Sort {@link java.util.List} items
	 * 
	 * @param sortItem
	 *            {@link java.lang.String}
	 * @param sort
	 *            {@link java.lang.String}
	 * @return {@link java.util.List}<{@link ome.admin.model.MyExperimenter}>
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws Exception
	 */
	public List<MyExperimenter> sortItems(String sortItem, String sort)
			throws FileNotFoundException, IOException {
		this.experimenters = lookupImportingExperimenters();
		sortByProperty = sortItem;
		if (sort.equals("asc"))
			sort(propertyAscendingComparator);
		else if (sort.equals("dsc"))
			sort(propertyDescendingComparator);
		return experimenters;
	}

	/**
	 * Sort {@link ome.admin.model.MyExperimenter} by
	 * {@link java.util.Comparator}
	 * 
	 * @param comparator
	 *            {@link java.util.Comparator}
	 */
	private void sort(Comparator comparator) {
		Collections.sort(experimenters, comparator);
	}

	/**
	 * Creates {@link ome.model.meta.Experimenter} in database
	 * 
	 * @param list
	 *            {@link java.util.List}<{@link ome.admin.model.MyExperimenter}>
	 */
	public void createExperimenters(List<MyExperimenter> list) {
		for (int i = 0; i < list.size(); i++) {
			MyExperimenter mexp = list.get(i);
			if (!mexp.isSelectBooleanCheckboxValue()) {
				list.remove(i);
				i--;
			}
		}
		toExperimenterList(list);
		
	}

	/**
	 * Converts {@link java.util.List}<{@link ome.admin.model.MyExperimenter}>
	 * to {@link java.util.List}<{@link ome.model.meta.Experimenter}>
	 * 
	 * @param list
	 *            {@link java.util.List}<{@link ome.admin.model.MyExperimenter}>
	 * @return {@link java.util.List}<{@link ome.model.meta.Experimenter}>
	 */
	private List<Experimenter> toExperimenterList(List<MyExperimenter> list) {
		List<Experimenter> exps = new ArrayList<Experimenter>();
		for (Experimenter exp : this.experimenters) {
			exps.add(exp);
			System.out.println(exp.getFirstName() + "," + exp.getLastName()
					+ "," + exp.getOmeName() + "," + exp.getEmail());
		}
		return exps;
	}

}
