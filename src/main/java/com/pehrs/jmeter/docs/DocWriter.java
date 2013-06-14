package com.pehrs.jmeter.docs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;

import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.gui.UnsharedComponent;
import org.apache.jmeter.gui.util.FilePanel;
import org.apache.jmeter.reporters.AbstractListenerElement;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.Visualizer;
import org.apache.jmeter.visualizers.gui.AbstractListenerGui;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class DocWriter extends AbstractListenerGui implements Visualizer,
		// ChangeListener, 
		UnsharedComponent, Clearable {

	private static final long serialVersionUID = 1L;

	/** Logging. */
	private static final Logger log = LoggingManager.getLoggerForClass();

	/** File Extensions */
	private static final String[] EXTS = { ".js" }; 

	/** A panel allowing results to be saved. */
	private final FilePanel filePanel;

	/** A panel allowing mapping to Play Framework routes. */
	private final FilePanel routesPanel;

	protected DocResultCollector collector = new DocResultCollector();

	protected boolean isStats = false;


	public DocWriter() {
		super();
		log.debug("new DocWriter()");

		filePanel = new FilePanel(
				JMeterUtils.getResString("file_visualizer_output_file"), EXTS); 
		filePanel.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent event) {
				log.debug("DocWriter.stateChanged() event=" + event);
				log.debug("getting new collector");
				collector = (DocResultCollector) createTestElement();
				collector.loadExistingFile();				
			}
		});

		routesPanel = new FilePanel("Match to routes File", "");
		routesPanel.addChangeListener(new ChangeListener() {		
			@Override
			public void stateChanged(ChangeEvent event) {
				log.debug("DocWriter.stateChanged() event=" + event);
				log.debug("getting new collector");
				collector = (DocResultCollector) createTestElement();
				collector.loadExistingFile();				
			}
		});

		init();
		setName(getStaticLabel());

	}

	@Override
	public boolean isStats() {
		log.debug("DocWriter.isStats() isStats=" + isStats);
		return isStats;
	}

	/**
	 * Gets the checkbox which selects whether or not only errors should be
	 * logged. Subclasses don't normally need to worry about this checkbox,
	 * because it is automatically added to the GUI in {@link #makeTitlePanel()}
	 * , and the behavior is handled in this base class.
	 * 
	 * @return the error logging checkbox
	 */
	// protected JCheckBox getErrorLoggingCheckbox() {
	// return errorLogging;
	// }

	/**
	 * Provides access to the DocResultCollector model class for extending
	 * implementations. Using this method and setModel(DocResultCollector) is
	 * only necessary if your visualizer requires a differently behaving
	 * DocResultCollector. Using these methods will allow maximum reuse of the
	 * methods provided by AbstractVisualizer in this event.
	 */
	protected DocResultCollector getModel() {
		log.debug("DocWriter.getModel() model=" + collector);
		return collector;
	}

	/**
	 * Gets the file panel which allows the user to save results to a file.
	 * Subclasses don't normally need to worry about this panel, because it is
	 * automatically added to the GUI in {@link #makeTitlePanel()}, and the
	 * behavior is handled in this base class.
	 * 
	 * @return the file panel allowing users to save results
	 */
	protected Component getFilePanel() {
		log.debug("DocWriter.getFilePanel() filePanel=" + filePanel);
		return filePanel;
	}

	protected Component getRoutesPanel() {
		log.debug("DocWriter.getRoutesPanel() routesPanel=" + routesPanel);
		return routesPanel;
	}

	public void setFile(String filename) {
		log.debug("DocWriter.setFile() filename=" + filename);
		filePanel.setFilename(filename);
	}

	public String getFile() {
		log.debug("DocWriter.getFile() filename=" + filePanel.getFilename());
		return filePanel.getFilename();
	}

	public void setRoutesFile(String filename) {
		log.debug("DocWriter.setRoutesFile() filename=" + filename);
		routesPanel.setFilename(filename);
	}

	public String getRoutesFile() {
		return routesPanel.getFilename();
	}

	/**
	 * Invoked when the target of the listener has changed its state. This
	 * implementation assumes that the target is the FilePanel, and will update
	 * the result collector for the new filename.
	 * 
	 * @param event
	 *            the event that has occurred
	 */
//	@Override
//	public void stateChanged(ChangeEvent event) {
//		log.debug("DocWriter.stateChanged() event=" + event);
//		log.debug("getting new collector");
//		collector = (DocResultCollector) createTestElement();
//		collector.loadExistingFile();
//	}

	/* Implements JMeterGUIComponent.createTestElement() */
	@Override
	public TestElement createTestElement() {
		log.debug("DocWriter.createTestElement()");
		if (collector == null) {
			collector = new DocResultCollector();
		}
		modifyTestElement(collector);
		return (TestElement) collector.clone();
	}

	/* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
	@Override
	public void modifyTestElement(TestElement testElement) {
		log.debug("DocWriter.modifyTestElement() testElement=" + testElement);
		configureTestElement((AbstractListenerElement) testElement);
		if (testElement instanceof DocResultCollector) {
			DocResultCollector rc = (DocResultCollector) testElement;
			rc.setFilename(getFile());
			rc.setRoutesFilename(getRoutesFile());
			collector = rc;
		}
	}

	/* Overrides AbstractJMeterGuiComponent.configure(TestElement) */
	@Override
	public void configure(TestElement testElement) {
		log.debug("DocWriter.configure() testElement=" + testElement);
		super.configure(testElement);
		setFile(testElement.getPropertyAsString(DocResultCollector.FILENAME));
		setRoutesFile(testElement.getPropertyAsString(DocResultCollector.ROUTES_FILENAME));
		DocResultCollector rc = (DocResultCollector) testElement;
		if (collector == null) {
			collector = new DocResultCollector();
		}
		collector.setSaveConfig((SampleSaveConfiguration) rc.getSaveConfig()
				.clone());
	}

	/**
	 * This provides a convenience for extenders when they implement the
	 * {@link org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()}
	 * method. This method will set the name, gui class, and test class for the
	 * created Test Element. It should be called by every extending class when
	 * creating Test Elements, as that will best assure consistent behavior.
	 * 
	 * @param mc
	 *            the TestElement being created.
	 */
	protected void configureTestElement(AbstractListenerElement mc) {
		log.debug("DocWriter.configureTestElement() testElement=" + mc);
		super.configureTestElement(mc);
		mc.setListener(this);
	}

	/**
	 * Create a standard title section for JMeter components. This includes the
	 * title for the component and the Name Panel allowing the user to change
	 * the name for the component. The AbstractVisualizer also adds the
	 * FilePanel allowing the user to save the results, and the error logging
	 * checkbox, allowing the user to choose whether or not only errors should
	 * be logged.
	 * <p>
	 * This method is typically added to the top of the component at the
	 * beginning of the component's init method.
	 * 
	 * @return a panel containing the component title, name panel, file panel,
	 *         and error logging checkbox
	 */
	@Override
	protected Container makeTitlePanel() {
		log.debug("DocWriter.makeTitlePanel()");
		Container panel = super.makeTitlePanel();
		panel.add(getFilePanel());
		panel.add(getRoutesPanel());
		return panel;
	}

	/**
	 * Provides extending classes the opportunity to set the DocResultCollector
	 * model for the Visualizer. This is useful to allow maximum reuse of the
	 * methods from AbstractVisualizer.
	 * 
	 * @param collector
	 */
	protected void setModel(DocResultCollector collector) {
		log.debug("DocWriter.setModel() collector=" + collector);
		this.collector = collector;
	}

	@Override
	public void clearGui() {
		log.debug("DocWriter.clearGui()");
		super.clearGui();
		filePanel.clearGui();
		routesPanel.clearGui();
	}

	private void init() {
		log.debug("DocWriter.init()");
		setLayout(new BorderLayout());
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);
	}

	/**
	 * Does nothing, but required by interface.
	 */

	@Override
	public void clearData() {
		log.debug("DocWriter.clearData()");
	}

	/**
	 * Does nothing, but required by interface.
	 * 
	 * @param sample
	 *            ignored
	 */
	@Override
	public void add(SampleResult sample) {
		log.debug("DocWriter.add() sample=" + sample);
	}

	@Override
	public String getLabelResource() {
		return "oc_writer";
	}

	@Override
	public String getStaticLabel() {
		return "JSON Doc Writer";
	}
}
