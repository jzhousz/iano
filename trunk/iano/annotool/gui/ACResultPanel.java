package annotool.gui;

import javax.swing.*;
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
import javax.swing.JFileChooser;
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

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.DefaultFontMapper;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import annotool.Annotator;
import annotool.gui.model.Chain;
import annotool.gui.model.Extractor;
import annotool.gui.model.Selector;

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
	int imgWidth, imgHeight;
	
	//Font objects to use while writing pdf
	public static final Font FONT_TITLE = new Font(FontFamily.HELVETICA, 18, Font.BOLD, new BaseColor(181, 0, 0));
	public static final Font FONT_HEADING = new Font(FontFamily.HELVETICA, 14, Font.BOLDITALIC, new BaseColor(230, 120, 0));
	public static final Font FONT_HEADING2 = new Font(FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(130, 60, 0));
	public static final Font FONT_LABEL = new Font(FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);
	
	public ACResultPanel(JTabbedPane parentPane, int imgWidth, int imgHeight, String channel, ArrayList<Chain> selectedChains) {
	   	this.parentPane = parentPane;
	   	this.tabIndex = parentPane.getTabCount();
	   	this.selectedChains = selectedChains;
	   	
	   	this.setLayout(new BorderLayout());
	   	this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	   	
	   	//Set data members to use later
	   	imageSet = new File(Annotator.dir).getAbsolutePath();
	   	if(Annotator.output.equals(Annotator.OUTPUT_CHOICES[0])) {
			mode = "Testing/Training";
			testSet = new File(Annotator.testdir).getAbsolutePath();
		}
		else if(Annotator.output.equals(Annotator.OUTPUT_CHOICES[1])) {
			mode = "Cross Validation " + "[Fold: " + Annotator.fold + "]";
		}
	   	
	   	if(channel.equals("r")) 
			channelName = "Red";
		else if(channel.equals("g")) 
			channelName = "Green";
		else if(channel.equals("b")) 
			channelName = "Blue";
	   	
	   	this.imgWidth = imgWidth;
	   	this.imgHeight = imgHeight;
	   	
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
		lbImgSize = new JLabel("<html><b>Image Size: </b>" + imgWidth + "x" + imgHeight + "</html>");
		
		if(Annotator.output.equals(Annotator.OUTPUT_CHOICES[0])) {
			lbTestSet = new JLabel("<html><b>Testing Image Set: </b>" + testSet + "</html>");
			lbTestExt = new JLabel("<html><b>Test Image Extension: </b>" + Annotator.testext + "</html>");
		}
		else if(Annotator.output.equals(Annotator.OUTPUT_CHOICES[1])) {
			lbTestSet = new JLabel("");
			lbTestExt = new JLabel("");
		}
		lbMode = new JLabel("<html><b>Mode: </b>" + mode + "</html>");				
		lbChannel = new JLabel("<html><b>Channel: </b>" + channelName + "</html>");
		
		//Build image info panel
		pnlImageInfo = new JPanel();
		pnlImageInfo.setLayout(new BoxLayout(pnlImageInfo, BoxLayout.PAGE_AXIS));
		pnlImageInfo.setBorder(new CompoundBorder(new TitledBorder(null, "Image Inforamtion", 
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
					Paragraph title = new Paragraph("AUTO COMPARISON REPORT", FONT_TITLE);
					title.setAlignment(Element.ALIGN_CENTER);
					document.add(title);
					
					document.add(createImageInfo());
					document.add(createOtherInfo());
					document.add(createChainInfo());
					
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
	//Content to write to pdf file
	//TODO: need to make it professional
	private Paragraph createImageInfo() {
		Paragraph info = new Paragraph();
		info.setSpacingBefore(36);
		info.add(new Paragraph("Image Information", FONT_HEADING));
		
		info.add(new Chunk("Image Set: ", FONT_LABEL));
		info.add(new Chunk(imageSet));
		info.add(Chunk.NEWLINE);
		
		info.add(new Chunk("Image Extension: ", FONT_LABEL));
		info.add(new Chunk(Annotator.ext));
		info.add(Chunk.NEWLINE);
		
		if(Annotator.output.equals(Annotator.OUTPUT_CHOICES[0])) {
			info.add(new Chunk("Test Image Set: ", FONT_LABEL));
			info.add(new Chunk(testSet));
			info.add(Chunk.NEWLINE);
			
			info.add(new Chunk("Test Image Extension: ", FONT_LABEL));
			info.add(new Chunk(Annotator.testext));
			info.add(Chunk.NEWLINE);
		}
		
		info.add(new Chunk("Image Size: ", FONT_LABEL));
		info.add(new Chunk(imgWidth + "x" + imgHeight));
		info.add(Chunk.NEWLINE);
		
		return info;
	}
	private Paragraph createOtherInfo() {
		Paragraph info = new Paragraph();
		info.setSpacingBefore(36);
		info.add(new Paragraph("Other Information", FONT_HEADING));
		
		info.add(new Chunk("Mode: ", FONT_LABEL));
		info.add(new Chunk(mode));
		info.add(Chunk.NEWLINE);
		
		info.add(new Chunk("Channel: ", FONT_LABEL));
		info.add(new Chunk(channelName));
		info.add(Chunk.NEWLINE);
		
		return info;
	}
	private Paragraph createChainInfo() {
		Paragraph info = new Paragraph();
		info.setSpacingBefore(36);
		info.add(new Paragraph("Chain Information", FONT_HEADING));
		
		info.add(new Chunk("-----------------------------------------------------------------------"));
		info.add(Chunk.NEWLINE);
		for(Chain chain : selectedChains) {
			info.add(new Chunk("CHAIN: " + chain.getName(), FONT_HEADING2));
			info.add(Chunk.NEWLINE);
			
			info.add(new Paragraph("Extractor(s)", FONT_LABEL));
			for(Extractor ex : chain.getExtractors()) {
				info.add(new Chunk(ex.getName()));
				info.add(Chunk.NEWLINE);
				for (String parameter : ex.getParams().keySet()) {
					info.add(new Chunk(parameter + "=" + ex.getParams().get(parameter)));
					info.add(Chunk.NEWLINE);
	        	}
				info.add(Chunk.NEWLINE);
			}
			
			info.add(new Paragraph("Selector(s)", FONT_LABEL));
			for(Selector sel : chain.getSelectors()) {
				info.add(new Chunk(sel.getName()));
				info.add(Chunk.NEWLINE);
				for (String parameter : sel.getParams().keySet()) {
					info.add(new Chunk(parameter + "=" + sel.getParams().get(parameter)));
					info.add(Chunk.NEWLINE);
	        	}
				info.add(Chunk.NEWLINE);
			}
			
			info.add(new Paragraph("Classifier", FONT_LABEL));
			info.add(new Chunk(chain.getClassifier()));
			info.add(Chunk.NEWLINE);
			for (String parameter : chain.getClassParams().keySet()) {
				info.add(new Chunk(parameter + "=" + chain.getClassParams().get(parameter)));
				info.add(Chunk.NEWLINE);
        	}
			info.add(Chunk.NEWLINE);
			info.add(new Chunk("-----------------------------------------------------------------------"));
			info.add(Chunk.NEWLINE);
		}		
		return info;
	}
	
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
			if(chain.getClassifier() != null) {
				taChainDetail.setText(taChainDetail.getText() + "Classifier:\n");
				taChainDetail.setText(taChainDetail.getText() + chain.getClassifier() + "\n");
				for (String parameter : chain.getClassParams().keySet()) {
					taChainDetail.setText(taChainDetail.getText() + parameter + "=" +chain.getClassParams().get(parameter) + "\n");
		    	}
			}
			taChainDetail.setText(taChainDetail.getText() + "\n\n\n");
		}
	}
}
