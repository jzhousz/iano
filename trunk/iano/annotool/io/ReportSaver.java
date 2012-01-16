package annotool.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import annotool.Annotation;
import annotool.gui.model.Styles;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class ReportSaver {
	public boolean saveAnnotationReport(File file, Annotation[][] annotations, HashMap<String, String> classNames, 
			String[] modelLabels, boolean[] supportsProb, String[] imageNames,
			boolean isBinary) {
		
		int numModels = annotations.length;
		int numTargets = annotations[0].length;
		
		Document document = new Document(PageSize.LETTER, 50, 50, 50, 50);
		try {
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
			document.open();
			Paragraph title = new Paragraph("ANNOTATION REPORT", Styles.FONT_TITLE);
			title.setAlignment(Element.ALIGN_CENTER);
			title.setSpacingAfter(36);
			document.add(title);
			
			//Build stats paragraph for each annotation label
			for(int i=0; i < annotations.length; i++)
				document.add(getStats(annotations[i], classNames, modelLabels[i]));
			
			//Create and display table of annotation results
			PdfPTable table = null;
			if(isBinary)
				table = new PdfPTable(numModels + 2); //extra columns for image names and summary
			else
				table = new PdfPTable(numModels + 1);
			table.setWidthPercentage(100);
			table.setSpacingBefore(5);
			table.setSpacingAfter(5);
			
			//Set relative width of columns
			int[] colWidths = null;
			if(isBinary)
				colWidths = new int[numModels + 2];
			else
				colWidths = new int[numModels + 1];
			
			colWidths[0] = 2;							//First column is for image name and it gets twice the normal width
			for(int i=1; i < colWidths.length; i++)
				colWidths[i] = 1;						//Rest gets normal width
			table.setWidths(colWidths);
				
			
			//First row for header
			PdfPCell titleCell = new PdfPCell(new Phrase("Annotation Results: ", Styles.FONT_TABLE_TITLE));
			if(isBinary)
				titleCell.setColspan(numModels + 2);
			else
				titleCell.setColspan(numModels + 1);
			titleCell.setMinimumHeight(24f);
			titleCell.setPadding(4);
			titleCell.setBackgroundColor(Styles.COLOR_TITLE);
			titleCell.setBorderColor(Styles.COLOR_BORDER);
			
			//Add title
			table.addCell(titleCell);
			
			//Create title cell for image names
			PdfPCell imgTitle = new PdfPCell(new Phrase("Image", Styles.FONT_TABLE_TITLE2));
			imgTitle.setBorderColor(Styles.COLOR_BORDER);
			imgTitle.setPadding(3);
			imgTitle.setBackgroundColor(Styles.COLOR_TITLE2);
			
			table.addCell(imgTitle);
			
			//Create title cells for each model
			for(int model=0; model < annotations.length; model++) {
				PdfPCell modelTitle = new PdfPCell(new Phrase(modelLabels[model], Styles.FONT_TABLE_TITLE2));
				modelTitle.setBorderColor(Styles.COLOR_BORDER);
				modelTitle.setPadding(3);
				modelTitle.setBackgroundColor(Styles.COLOR_TITLE2);
				
				table.addCell(modelTitle);
			}
			
			//For binary case, add summary column
			if(isBinary) {
				PdfPCell modelTitle = new PdfPCell(new Phrase("Summary", Styles.FONT_TABLE_TITLE2));
				modelTitle.setBorderColor(Styles.COLOR_BORDER);
				modelTitle.setPadding(3);
				modelTitle.setBackgroundColor(Styles.COLOR_TITLE2);
				
				table.addCell(modelTitle);
			}
			
			//Create all the rows
			//Traverse annotation results in inverted way : go through all the models in one target and then next target and so on
			for(int target=0; target < numTargets; target++) {										//Rows are targets
				String summary = "";
				
				//First column in each row is the image name
				PdfPCell nameCell = new PdfPCell(new Phrase(imageNames[target]));
				nameCell.setBorderColor(Styles.COLOR_BORDER);
				nameCell.setPadding(3);
				table.addCell(nameCell);
				for(int model = 0; model < numModels; model++) {									//Columns are models (and hence annotation label)
					String value = String.valueOf(annotations[model][target].anno);					//Annotation result has target along second dimension, model along first
					//Also, show probability if the particular model supports probability
					if(supportsProb[model])
						value += String.format(" (%.2f%%)", 100 * annotations[model][target].prob);
					
					PdfPCell cell = new PdfPCell(new Phrase(value));
					cell.setBorderColor(Styles.COLOR_BORDER);
					cell.setPadding(3);
					table.addCell(cell);
					
					if(isBinary && annotations[model][target].anno == 1)
						summary += modelLabels[model] + " ";
				}
				if(isBinary) {
					PdfPCell cell = new PdfPCell(new Phrase(summary));
					cell.setBorderColor(Styles.COLOR_BORDER);
					cell.setPadding(3);
					table.addCell(cell);
				}
			}
			
			document.add(table);
			document.close();		
			System.out.println("Report saved: " + file.getAbsolutePath());
			return true;
		} catch (FileNotFoundException e) {
			System.out.println("Failed to write to the file specified.");
			e.printStackTrace();
			return false;
		} catch (DocumentException e) {
			System.out.println("Operation failed.");
			e.printStackTrace();
			return false;
		}
	}
	private Paragraph getStats(Annotation[] annotations, HashMap<String, String> classNames, String modelLabel) {
		//Initialize treemap(sorted) to store count for each class
		TreeMap<String, Integer> counts = new TreeMap<String, Integer>();
		for(String key : classNames.keySet())
			counts.put(key, 0);
		
		for(int i = 0; i < annotations.length; i++) {
			Integer count = counts.get(String.valueOf(annotations[i].anno));
			count++;
			counts.put(String.valueOf(annotations[i].anno), count);
		}

		Paragraph info = new Paragraph();
		info.add(new Chunk(modelLabel, Styles.FONT_HEADING));
		info.add(Chunk.NEWLINE);
		
		for(String key : counts.keySet()) {
			info.add(new Chunk(key +" (" + classNames.get(key) + ") : ", Styles.FONT_LABEL ));//eg. 1(classA) = 10
			info.add(new Chunk(counts.get(key).toString()));
			info.add(Chunk.NEWLINE);
		}
		
		info.setSpacingAfter(36);
		
		return info;
	}
}
