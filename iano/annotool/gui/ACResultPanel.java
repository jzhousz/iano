package annotool.gui;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import annotool.Annotator;
import annotool.gui.model.Chain;
import annotool.gui.model.ClassifierInfo;
import annotool.gui.model.Extractor;
import annotool.gui.model.Selector;
import annotool.gui.model.Styles;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.DefaultFontMapper;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * This class represents the result panel that is displayed after the execution of 
 * the chains in auto comparison mode.
 * 
 *
 */
public class ACResultPanel extends JPanel implements ActionListener{
	private int tabIndex; //the index of this panel in the parent tabbed pane
	private JTabbedPane parentPane = null;
	private ChartPanel pnlChart = null;
	JFreeChart chart = null;
	private JPanel pnlDesc, 
				   pnlImageInfo, pnlRunInfo, pnlDisplay;
	
	private JButton btnSaveReport;
	
	private JLabel lbImgSet, lbTestSet, 
				   lbImgExt, lbTestExt,
				   lbImgSize, 
				   lbMode, lbChannel;
	private ArrayList<Chain> selectedChains = new ArrayList<Chain>();
	
	private JTextArea taChainDetail = new JTextArea();
	private JScrollPane detailPane = new JScrollPane(taChainDetail);
	
	JFileChooser fileChooser = new JFileChooser();
	
	//Data members
	String channelName = null, mode = null,
		   imageSet = null, testSet = null;
	int imgWidth, imgHeight, imgDepth;
	boolean is3d;
	private float[][] rates;
	//private ArrayList<String> labels;
	
	public ACResultPanel(JTabbedPane parentPane, int imgWidth, int imgHeight, int imgDepth, boolean is3d, String channel, ArrayList<Chain> selectedChains, boolean cFlag) {
	   	this.parentPane = parentPane;
	   	this.tabIndex = parentPane.getTabCount();
	   	this.selectedChains = selectedChains;
	   	this.imgDepth = imgDepth;
	   	this.setLayout(new BorderLayout());
	   	this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	   	
	   	//Set data members to use later
	   	imageSet = new File(Annotator.dir).getAbsolutePath();
	   	if(Annotator.output.equals(Annotator.TT)) {
			mode = "Testing/Training";
			testSet = new File(Annotator.testdir).getAbsolutePath();
		}
		else if(Annotator.output.equals(Annotator.CV)) {
			mode = "Cross Validation " + "[Fold: " + Annotator.fold + "]";
		}
	   	
	   	if (cFlag == true){
	   	if(channel.equals("r")) 
			channelName = "Red";
		else if(channel.equals("g")) 
			channelName = "Green";
		else if(channel.equals("b")) 
			channelName = "Blue";
	   	}
	   	else
	   		channelName = "Gray Scale";
	   	
	   	this.imgWidth = imgWidth;
	   	this.imgHeight = imgHeight;
	   	this.is3d = is3d;
	   	taChainDetail.setEditable(false);
	   	taChainDetail.setMargin(new Insets(10,10,10,10));
	}
	
	public void display(float[][] rates, ArrayList<String> labels) {
		showChart(rates, labels, selectedChains);
		
		showInfo();
		
		showChainDetails();
		
		//Display current visualization tab
		parentPane.setEnabledAt(tabIndex,true);
	    parentPane.setSelectedIndex(tabIndex);
	}
	
	private void showInfo() {
		pnlDesc = new JPanel();
		pnlDesc.setLayout(new BoxLayout(pnlDesc, BoxLayout.PAGE_AXIS));
		
		//Labels
		lbImgSet = new JLabel("<html><b>Image Set: </b>" + imageSet + "</html>");
		lbImgExt = new JLabel("<html><b>Image Extension: </b>" + Annotator.ext + "</html>");
		if (is3d)
		{
			lbImgSize = new JLabel("<html><b>Image Size: </b>" + imgWidth + "x" + imgHeight + "x" + imgDepth + "</html>");
		}
		else
		{
			lbImgSize = new JLabel("<html><b>Image Size: </b>" + imgWidth + "x" + imgHeight + "</html>");
		}
		if(Annotator.output.equals(Annotator.TT)) {
			lbTestSet = new JLabel("<html><b>Testing Image Set: </b>" + testSet + "</html>");
			lbTestExt = new JLabel("<html><b>Test Image Extension: </b>" + Annotator.testext + "</html>");
		}
		else if(Annotator.output.equals(Annotator.CV)) {
			lbTestSet = new JLabel("");
			lbTestExt = new JLabel("");
		}
		lbMode = new JLabel("<html><b>Mode: </b>" + mode + "</html>");				
		lbChannel = new JLabel("<html><b>Channel: </b>" + channelName + "</html>");
		
		//Build image info panel
		pnlImageInfo = new JPanel();
		pnlImageInfo.setLayout(new BoxLayout(pnlImageInfo, BoxLayout.PAGE_AXIS));
		pnlImageInfo.setBorder(new CompoundBorder(new TitledBorder(null, "Image Information", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(10, 10, 10, 10)));
		pnlImageInfo.setAlignmentX(RIGHT_ALIGNMENT);
		pnlImageInfo.add(lbImgSet);
		pnlImageInfo.add(lbImgExt);
		pnlImageInfo.add(lbTestSet);
		pnlImageInfo.add(lbTestExt);
		pnlImageInfo.add(lbImgSize);
		
		//Build info panel for other data
		pnlRunInfo = new JPanel();
		pnlRunInfo.setLayout(new BoxLayout(pnlRunInfo, BoxLayout.PAGE_AXIS));
		pnlRunInfo.setBorder(new CompoundBorder(new TitledBorder(null, "Other Inforamtion", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(10, 10, 10, 10)));
		pnlRunInfo.setAlignmentX(RIGHT_ALIGNMENT);
		pnlRunInfo.add(lbMode);
		pnlRunInfo.add(lbChannel);
		
		//Build chain details panel
		pnlDisplay = new JPanel(new java.awt.GridLayout(1,1));
		pnlDisplay.setBorder(new CompoundBorder(new TitledBorder(null, "Chain Inforamtion", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(10, 10, 10, 10)));
		pnlDisplay.add(detailPane);
		pnlDisplay.setAlignmentX(RIGHT_ALIGNMENT);
		
		//Buttons
		btnSaveReport = new JButton("Save Report");
		btnSaveReport.addActionListener(this);
		btnSaveReport.setAlignmentX(RIGHT_ALIGNMENT);
		
		pnlDesc.add(pnlImageInfo);
		pnlDesc.add(pnlRunInfo);
		pnlDesc.add(pnlDisplay);
		pnlDesc.add(btnSaveReport);
		this.add(pnlDesc, BorderLayout.EAST);
	}
	/*
	 * Argument: float[][] rates: recognition rate for each label for each chain
	 */
	private void showChart(float[][] rates, ArrayList<String> labels, ArrayList<Chain> selectedChains) {
		// create the dataset...
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        this.rates = rates;
        //this.labels = labels;
        this.selectedChains = selectedChains;
        
        for(int i=0; i < rates.length; i++){			//Each chain 
        	for(int j=0; j < rates[i].length; j++) {	//Each label
        		dataset.addValue(100.0*rates[i][j], labels.get(j), selectedChains.get(i).getName());
        	}
        }
		
		chart = ChartFactory.createBarChart3D("Auto Comparison Result", "Chain", "Rate (%)", dataset, PlotOrientation.VERTICAL, true, true, false);
		
		CategoryPlot plot = chart.getCategoryPlot();
		ValueAxis axis = plot.getRangeAxis();
		axis.setAutoRange(false);
		axis.setRange(0, 100);
		
		//CategoryItemRenderer r = plot.getRenderer();
		//r.setSeriesPaint(0, new Color(76, 182, 73)); 
		//r.setSeriesPaint(1, new Color(223, 34, 39));
		
		pnlChart = new ChartPanel(chart);
		pnlChart.setBorder(BorderFactory.createEtchedBorder());
		this.add(pnlChart, BorderLayout.CENTER);		
	}
	
	public void actionPerformed(ActionEvent ev) {
		if(ev.getSource() == btnSaveReport) {
			int returnVal = fileChooser.showSaveDialog(this);
			
	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            File file = fileChooser.getSelectedFile();
	            String filePath = file.getPath();
	            if(!filePath.toLowerCase().endsWith(".pdf")) {
	            	file = new File(filePath + ".pdf");
	            }
				Document document = new Document(PageSize.LETTER, 50, 50, 50, 50);
				try {
					PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
					document.open();
					Paragraph title = new Paragraph("AUTO COMPARISON REPORT", Styles.FONT_TITLE);
					title.setAlignment(Element.ALIGN_CENTER);
					title.setSpacingAfter(36);
					document.add(title);
					
					document.add(createImageInfo());
					document.add(createOtherInfo());
					
					document.add(new Paragraph("Chain Information", Styles.FONT_HEADING));				
					//Add chain table for each chain
					for(Chain chain : selectedChains) {
						document.add(createChainTable(chain));
					}
					
					document.newPage();//TODO: Or I can check if there is enough space in the page for image first
					
					//Add chart to pdf
					int width = 520;
					int height = 450;
					 
					// get the direct pdf content
					PdfContentByte canvas = writer.getDirectContent();
					 
					// get a pdf template from the direct content
					PdfTemplate tp = canvas.createTemplate(width, height);
					 
					// create an AWT renderer from the pdf template
					Graphics2D g2 = tp.createGraphics(width, height, new DefaultFontMapper() );
					Rectangle2D r2D = new Rectangle2D.Double(0,0, width,height);
					chart.draw(g2,r2D,null);
					g2.dispose();
					 
					// add the rendered pdf template to the direct content
					// 38 is just a typical left margin - the chart is absolutely positioned
					// docWriter.getVerticalPosition(true) will approximate the position that the content above the chart ended
					canvas.addTemplate(tp, 38, writer.getVerticalPosition(true)- height - 20);
					
					document.close();
					
					System.out.println("Report saved: " + file.getAbsolutePath());
				} catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(this,
						    "Failed to write to the file specified.", 
						    "Save Failed",
						    JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				} catch (DocumentException e) {
					JOptionPane.showMessageDialog(this,
						    "Operation failed.", 
						    "Error!",
						    JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
	        }
		}
	}
	/*
	 * Creates and returns the paragraph with image information
	 */
	private Paragraph createImageInfo() {
		Paragraph info = new Paragraph();
		info.setSpacingAfter(36);
		info.add(new Paragraph("Image Information", Styles.FONT_HEADING));
		
		info.add(new Chunk("Image Set: ", Styles.FONT_LABEL));
		info.add(new Chunk(imageSet));
		info.add(Chunk.NEWLINE);
		
		info.add(new Chunk("Image Extension: ", Styles.FONT_LABEL));
		info.add(new Chunk(Annotator.ext));
		info.add(Chunk.NEWLINE);
		
		if(Annotator.output.equals(Annotator.TT)) {
			info.add(new Chunk("Test Image Set: ", Styles.FONT_LABEL));
			info.add(new Chunk(testSet));
			info.add(Chunk.NEWLINE);
			
			info.add(new Chunk("Test Image Extension: ", Styles.FONT_LABEL));
			info.add(new Chunk(Annotator.testext));
			info.add(Chunk.NEWLINE);
		}
		
		info.add(new Chunk("Image Size: ", Styles.FONT_LABEL));
		if ( is3d ) {
			info.add(new Chunk(imgWidth + "x" + imgHeight + "x" + imgDepth));
		}
		else {
			info.add(new Chunk(imgWidth + "x" + imgHeight));
		}
		info.add(Chunk.NEWLINE);
		
		return info;
	}
	/*
	 * Creates and returns the paragraph with other information
	 */
	private Paragraph createOtherInfo() {
		Paragraph info = new Paragraph();
		info.setSpacingAfter(36);
		info.add(new Paragraph("Other Information", Styles.FONT_HEADING));
		
		info.add(new Chunk("Mode: ", Styles.FONT_LABEL));
		info.add(new Chunk(mode));
		info.add(Chunk.NEWLINE);
		
		info.add(new Chunk("Channel: ", Styles.FONT_LABEL));
		info.add(new Chunk(channelName));
		info.add(Chunk.NEWLINE);
		
		return info;
	}
	
	/*
	 * Creates and return table of chain information for the passed in chain
	 */
	private PdfPTable createChainTable(Chain chain) throws DocumentException {
		PdfPTable table = new PdfPTable(4);
		table.setWidthPercentage(100);
		table.setSpacingBefore(5);
		table.setSpacingAfter(5);
		
		//First row for chain name
		PdfPCell titleCell = new PdfPCell(new Phrase("Chain: " + chain.getName(), Styles.FONT_TABLE_TITLE));
		titleCell.setColspan(4);
		titleCell.setMinimumHeight(24f);
		titleCell.setPadding(4);
		titleCell.setBackgroundColor(Styles.COLOR_TITLE);
		titleCell.setBorderColor(Styles.COLOR_BORDER);
		
		//Second row has three column titles
		PdfPCell exTitleCell, selTitleCell, classTitleCell, rateTitleCell;
		exTitleCell = new PdfPCell(new Phrase("Extractor(s)", Styles.FONT_TABLE_TITLE2));
		selTitleCell = new PdfPCell(new Phrase("Selector(s)", Styles.FONT_TABLE_TITLE2));
		classTitleCell = new PdfPCell(new Phrase("Classifier", Styles.FONT_TABLE_TITLE2));
		rateTitleCell = new PdfPCell(new Phrase("Recognition Rate", Styles.FONT_TABLE_TITLE2));
		exTitleCell.setBorderColor(Styles.COLOR_BORDER);
		exTitleCell.setPadding(3);
		exTitleCell.setBackgroundColor(Styles.COLOR_TITLE2);
		selTitleCell.setBorderColor(Styles.COLOR_BORDER);
		selTitleCell.setPadding(3);
		selTitleCell.setBackgroundColor(Styles.COLOR_TITLE2);
		classTitleCell.setBorderColor(Styles.COLOR_BORDER);
		classTitleCell.setPadding(3);
		classTitleCell.setBackgroundColor(Styles.COLOR_TITLE2);
		rateTitleCell.setBorderColor(Styles.COLOR_BORDER);
		rateTitleCell.setPadding(3);
		rateTitleCell.setBackgroundColor(Styles.COLOR_TITLE2);
		
		//Lastly, the content for each column
		PdfPCell exCell, selCell, classCell, rateCell;
		exCell = new PdfPCell(getExtractorInfo(chain));
		selCell = new PdfPCell(getSelectorInfo(chain));
		classCell = new PdfPCell(getClassifierInfo(chain));
		rateCell = new PdfPCell(getRateInfo(chain));
		exCell.setBorderColor(Styles.COLOR_BORDER);
		exCell.setPadding(3);
		selCell.setBorderColor(Styles.COLOR_BORDER);
		selCell.setPadding(3);
		classCell.setBorderColor(Styles.COLOR_BORDER);
		classCell.setPadding(3);
		rateCell.setBorderColor(Styles.COLOR_BORDER);
		rateCell.setPadding(3);
		
		//Add title cells to table
		table.addCell(titleCell);
		table.addCell(exTitleCell);
		table.addCell(selTitleCell);
		table.addCell(classTitleCell);
		table.addCell(rateTitleCell);
		
		//Add content cells
		table.addCell(exCell);
		table.addCell(selCell);
		table.addCell(classCell);
		table.addCell(rateCell);
		
		return table;
	}
	
	/*
	 *  Creates and returns the paragraph of recognition rate info
	 */
	private Paragraph getRateInfo(Chain chain) {
		Paragraph info = new Paragraph();
		
		for (int i = 0; i < rates.length; i++) {
			for (int j = 0; j < rates[i].length; j++){
				if (selectedChains.get(i).getName().equals(chain.getName())) {
					double rate = (100.0*rates[i][j]);
					String rate_str = String.valueOf(rate);
					info.add(rate_str);
					break;
				}
			}
		}
		
		return info;
	}
	
	/*
	 * Creates and returns the paragraph of extractor info for passed in chain
	 */
	private Paragraph getExtractorInfo(Chain chain) {
		Paragraph info = new Paragraph();
		for(Extractor ex : chain.getExtractors()) {
			info.add(new Chunk(ex.getName()));
			info.add(Chunk.NEWLINE);
			for (String parameter : ex.getParams().keySet()) {
				info.add(new Chunk(parameter + "=" + ex.getParams().get(parameter)));
				info.add(Chunk.NEWLINE);
        	}
		}
		return info;
	}
	/*
	 * Creates and returns the paragraph of selector info for passed in chain
	 */
	private Paragraph getSelectorInfo(Chain chain) {
		Paragraph info = new Paragraph();
		for(Selector sel : chain.getSelectors()) {
			info.add(new Chunk(sel.getName()));
			info.add(Chunk.NEWLINE);
			for (String parameter : sel.getParams().keySet()) {
				info.add(new Chunk(parameter + "=" + sel.getParams().get(parameter)));
				info.add(Chunk.NEWLINE);
        	}
		}
		return info;
	}
	/*
	 * Creates and returns the paragraph of classifier info for passed in chain
	 */
	private Paragraph getClassifierInfo(Chain chain) {
		
		/* Added 1/16/2014 */
		Paragraph info = new Paragraph();
		for(ClassifierInfo classinfo : chain.getClassifierInfo()) {
			info.add(new Chunk(classinfo.getName()));
			info.add(Chunk.NEWLINE);
			for (String parameter : classinfo.getParams().keySet()) {
				info.add(new Chunk(parameter + "=" + classinfo.getParams().get(parameter)));
				info.add(Chunk.NEWLINE);
        	}
		}
		return info;
		
		/* Remove 1/16/2014
		Paragraph info = new Paragraph();		
		info.add(new Chunk(chain.getClassifier()));
		info.add(Chunk.NEWLINE);
		for (String parameter : chain.getClassParams().keySet()) {
			info.add(new Chunk(parameter + "=" + chain.getClassParams().get(parameter)));
			info.add(Chunk.NEWLINE);
    	}
		return info;
		*/
	}
	
	/*
	 * Displays the list of algorithms from the chains in the gui
	 */
	private void showChainDetails() {
		taChainDetail.setText("");
		for(Chain chain : selectedChains) {
			taChainDetail.setText(taChainDetail.getText() + "CHAIN:"+ chain.getName() +"\n");
			taChainDetail.setText(taChainDetail.getText() + "=========================================================\n");
			if(chain.getExtractors().size() > 0) {
				taChainDetail.setText(taChainDetail.getText() + "Feature Extractor(s):\n");
				for(Extractor ex : chain.getExtractors()) {
					taChainDetail.setText(taChainDetail.getText() + ex.getName() + "\n");
					for (String parameter : ex.getParams().keySet()) {
						taChainDetail.setText(taChainDetail.getText() + parameter + "=" + ex.getParams().get(parameter) + "\n");
		        	}
					taChainDetail.setText(taChainDetail.getText() + "\n");
				}
			}
			if(chain.getSelectors().size() > 0) {
				taChainDetail.setText(taChainDetail.getText() + "Feature Selector(s):\n");
				for(Selector sel : chain.getSelectors()) {
					taChainDetail.setText(taChainDetail.getText() + sel.getName() + "\n");
					for (String parameter : sel.getParams().keySet()) {
						taChainDetail.setText(taChainDetail.getText() + parameter + "=" + sel.getParams().get(parameter) + "\n");
		        	}
					taChainDetail.setText(taChainDetail.getText() + "\n");
				}
			}
			
			/* added 1/16/2014 */
			if(chain.getClassifierInfo() != null) {
				taChainDetail.setText(taChainDetail.getText() + "Classifier(s):\n");
				for(ClassifierInfo classInfo : chain.getClassifierInfo()) {
					taChainDetail.setText(taChainDetail.getText() + classInfo.getName() + "\n");
					for (String parameter : classInfo.getParams().keySet()) {
						taChainDetail.setText(taChainDetail.getText() + parameter + "=" + classInfo.getParams().get(parameter) + "\n");
		        	}
					taChainDetail.setText(taChainDetail.getText() + "\n");
				}
			}
			
			/* Removed 1/16/2014
			if(chain.getClassifier() != null) {
				taChainDetail.setText(taChainDetail.getText() + "Classifier:\n");
				taChainDetail.setText(taChainDetail.getText() + chain.getClassifier() + "\n");
				for (String parameter : chain.getClassParams().keySet()) {
					taChainDetail.setText(taChainDetail.getText() + parameter + "=" +chain.getClassParams().get(parameter) + "\n");
		    	}
			}
			*/
			
			for (int i = 0; i < rates.length; i++) {
				for (int j = 0; j < rates[i].length; j++){
					if (selectedChains.get(i).getName().equals(chain.getName())) {
						double rate = (100.0*rates[i][j]);
						String rate_str = String.valueOf(rate);
						taChainDetail.setText(taChainDetail.getText() + "\nRecognition Rate: ");
						taChainDetail.setText(taChainDetail.getText() + rate_str + "\n");
						break;
					}
				}
			}
			
			taChainDetail.setText(taChainDetail.getText() + "\n\n\n");
		}
	}
}
