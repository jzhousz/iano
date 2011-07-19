package annotool.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import annotool.Annotation;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class ReportSaver {
	public void saveAnnotationReport(File file, Annotation[][] annotations) {
		Document document = new Document(PageSize.LETTER, 50, 50, 50, 50);
		try {
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
			document.open();
			Paragraph title = new Paragraph("ANNOTATION REPORT");
			title.setAlignment(Element.ALIGN_CENTER);
			title.setSpacingAfter(36);
			document.add(title);
			
			for(int model=0; model < annotations.length; model++) {
				for(int target=0; target < annotations[model].length; target++) {
				}
			}
			
			document.close();			
			System.out.println("Report saved: " + file.getAbsolutePath());
		} catch (FileNotFoundException e) {
			System.out.println("Failed to write to the file specified.");
			e.printStackTrace();
		} catch (DocumentException e) {
			System.out.println("Operation failed.");
			e.printStackTrace();
		}
	}
}
