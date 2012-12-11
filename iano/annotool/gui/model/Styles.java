package annotool.gui.model;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;

/**
 * Defines constants representing styles in pdf report generated.
 * 
 */
public class Styles {
	//Font objects to use while writing pdf
	public static final Font FONT_TITLE = new Font(FontFamily.HELVETICA, 18, Font.BOLD, new BaseColor(181, 0, 0));
	public static final Font FONT_HEADING = new Font(FontFamily.HELVETICA, 14, Font.BOLDITALIC, new BaseColor(230, 120, 0));
	public static final Font FONT_HEADING2 = new Font(FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(130, 60, 0));
	public static final Font FONT_LABEL = new Font(FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);
	public static final Font FONT_TABLE_TITLE = new Font(FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
	public static final Font FONT_TABLE_TITLE2 = new Font(FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);
	
	//Color objects : table
	public static final BaseColor COLOR_TITLE = new BaseColor(229, 120, 0);
	public static final BaseColor COLOR_TITLE2 = new BaseColor(244, 164, 96);
	public static final BaseColor COLOR_BORDER = new BaseColor(185, 100, 0);
}
