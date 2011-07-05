package annotool.gui;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.*;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ChainPanel extends JPanel implements ActionListener, ListSelectionListener{
	private JPanel pnlTable, pnlControl, pnlDetail;
	private JTable tblChain = null;
	private JScrollPane scrollPane = null;
	
	private JButton btnNew, btnRemove;
	private ChainTableModel tableModel = new ChainTableModel();
	
	//Details
	JTextArea taDetail = new JTextArea(6,40);
	JScrollPane detailPane = new JScrollPane(taDetail);
	
	public ChainPanel() {
		this.setLayout(new BorderLayout());
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		pnlTable = new JPanel(new BorderLayout());
		pnlControl = new JPanel();
		pnlDetail = new JPanel(new BorderLayout());
		pnlDetail.setBorder(new CompoundBorder(new TitledBorder(null, "Selected Chain", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(10, 10, 10, 10)));
		pnlDetail.add(detailPane, BorderLayout.CENTER);
		pnlDetail.setPreferredSize(new java.awt.Dimension(400, 300));
		taDetail.setMargin(new Insets(10,10,10,10));
		taDetail.setEditable(false);
		
		tblChain = new JTable(tableModel);
		tblChain.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblChain.getSelectionModel().addListSelectionListener(this);
		    
		//tblChain.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblChain.getColumnModel().getColumn(0).setPreferredWidth(30);
		tblChain.getColumnModel().getColumn(1).setPreferredWidth(420);
		scrollPane = new JScrollPane(tblChain);
		pnlTable.add(scrollPane, BorderLayout.CENTER);
		
		btnNew = new JButton("New");
		btnNew.addActionListener(this);
		btnRemove = new JButton("Remove");
		btnRemove.setEnabled(false);
		btnRemove.addActionListener(this);
		//pnlControl.setLayout(new BoxLayout(pnlControl, BoxLayout.PAGE_AXIS));
		pnlControl.add(btnNew);
		pnlControl.add(btnRemove);
		
		this.add(pnlTable, BorderLayout.CENTER);
		this.add(pnlControl, BorderLayout.EAST);
		this.add(pnlDetail, BorderLayout.SOUTH);
	}
	
	public void actionPerformed(ActionEvent ev) {
		if(ev.getSource().equals(btnNew)) {
			Object[] rowData = {new Boolean(false), new Chain()};
			tableModel.insertNewRow(rowData);
			tblChain.changeSelection(tableModel.getRowCount() - 1, 1, false, false);
		}
		else if(ev.getSource().equals(btnRemove)) {
			tableModel.removeRow(tblChain.getSelectedRow());
			taDetail.setText("");
		}
		if(tableModel.getRowCount() > 0)
			btnRemove.setEnabled(true);
		else
			btnRemove.setEnabled(false);
	}
	public void valueChanged(ListSelectionEvent ev) {
		if (ev.getValueIsAdjusting()) {
            return;
        }
		showItemDetail();
	}
	public void addExtractor(Extractor ex) {
		int currentRow = tblChain.getSelectedRow();
		if(currentRow < 0)
			return;
		Chain chain = (Chain)tblChain.getValueAt(currentRow, 1);
		chain.addExtractor(ex);
		tblChain.repaint();
		showItemDetail();
	}
	public void addSelector(String name, HashMap<String, String> params) {
		int currentRow = tblChain.getSelectedRow();
		if(currentRow < 0)
			return;
		Chain chain = (Chain)tblChain.getValueAt(currentRow, 1);
		chain.setSelector(name);
		chain.setSelParams(params);
		tblChain.repaint();
		showItemDetail();
	}
	public void addClassifier(String name, HashMap<String, String> params) {
		int currentRow = tblChain.getSelectedRow();
		if(currentRow < 0)
			return;
		Chain chain = (Chain)tblChain.getValueAt(currentRow, 1);
		chain.setClassifier(name);
		chain.setClassParams(params);
		tblChain.repaint();
		showItemDetail();
	}
	private void showItemDetail() {
		int currentRow = tblChain.getSelectedRow();
		if(currentRow < 0)
			return;
		
		final Chain chain = (Chain)tblChain.getValueAt(currentRow, 1);
		taDetail.setText("Chain Details: \n");
		if(chain.getExtractors().size() > 0) {
			taDetail.setText(taDetail.getText() + "---------------------------------------\n");
			taDetail.setText(taDetail.getText() + "FEATURE EXTRACTOR(S):\n");
			taDetail.setText(taDetail.getText() + "---------------------------------------\n");
			for(Extractor ex : chain.getExtractors()) {
				taDetail.setText(taDetail.getText() + "Name: " + ex.getName() + "\n");
				for (String parameter : ex.getParams().keySet()) {
					taDetail.setText(taDetail.getText() + parameter + "=" +ex.getParams().get(parameter) + "\n");
	        	}
			}
		}
		if(chain.getSelector() != null) {
			taDetail.setText(taDetail.getText() + "---------------------------------------\n");
			taDetail.setText(taDetail.getText() + "FEATURE SELECTOR:\n");
			taDetail.setText(taDetail.getText() + "---------------------------------------\n");
			taDetail.setText(taDetail.getText() + "Name: " + chain.getSelector() + "\n");
			for (String parameter : chain.getSelParams().keySet()) {
				taDetail.setText(taDetail.getText() + parameter + "=" +chain.getSelParams().get(parameter) + "\n");
	    	}
		}
		if(chain.getClassifier() != null) {
			taDetail.setText(taDetail.getText() + "---------------------------------------\n");
			taDetail.setText(taDetail.getText() + "CLASSIFIER:\n");
			taDetail.setText(taDetail.getText() + "---------------------------------------\n");
			taDetail.setText(taDetail.getText() + "Name: " + chain.getClassifier() + "\n");
			for (String parameter : chain.getClassParams().keySet()) {
				taDetail.setText(taDetail.getText() + parameter + "=" +chain.getClassParams().get(parameter) + "\n");
	    	}
		}
		
		taDetail.setCaretPosition(taDetail.getText().length());
	}
}
