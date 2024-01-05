package com.bmghunter;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import net.studioblueplanet.fitreader.FitMessage;
import net.studioblueplanet.fitreader.FitMessageRepository;
import net.studioblueplanet.fitreader.FitReader;
public class FitToCsv extends JFrame
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 149410522439047625L;

	public FitToCsv()
	{
		super("MainApplicationFrame");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(300,300);
		makeGui();
	}
	public void makeGui()
	{
    	JLabel pathLabel = new JLabel("Input File");
    	JTextField pathTextField = new JTextField(new java.io.File(".").getAbsolutePath().toString());
		pathTextField.setToolTipText("Select a file.");
		JButton browseButton = new JButton("Browse");
		JButton convert = new JButton("Convert");
		JCheckBox metricCheckbox = new JCheckBox("Metric");
		metricCheckbox.setSelected(false);
		JPanel pathPanel = new JPanel();
		pathPanel.setLayout(new BoxLayout(pathPanel, BoxLayout.LINE_AXIS));
		pathPanel.add(pathLabel);
		pathPanel.add(pathTextField);
		pathPanel.add(browseButton);
		pathPanel.add(convert);
		pathPanel.add(metricCheckbox);
		getContentPane().add(pathPanel);

		browseButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				JFileChooser chooser = new JFileChooser();
				
				
				try {
					chooser.setFileFilter(new FitFileFilter());			
				}
				catch(Exception e){
					e.printStackTrace();
				}
				
				
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				chooser.setCurrentDirectory(new java.io.File("."));
				chooser.setDialogTitle("Select your FIT file. ");

				if (chooser.showOpenDialog(FitToCsv.this) == JFileChooser.APPROVE_OPTION)
				{
					pathTextField.setText(chooser.getSelectedFile().toString());
				}
			
			}
		});
		convert.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				FitMessageRepository contents = FitReader.getInstance().readFile(pathTextField.getText(), true);
				List<String> names = contents.getMessageNames();
				List< List<FitMessage> > allmsgs = new ArrayList< List<FitMessage> >();
				for(String name : names)
				{
					allmsgs.add(contents.getAllMessages(name));
				}
				List<FitMessage> speeds = allmsgs.get(4);
				for(FitMessage fit : speeds)
				{
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					PrintStream ps = new PrintStream(baos);
					// IMPORTANT: Save the old System.out!
					PrintStream old = System.out;
					// Tell Java to use your special stream
					System.setOut(ps);
					String speedoutput = "fps";
					if(metricCheckbox.isSelected())
						speedoutput = "m/s";
					
					// Print some output: goes to your special stream
					fit.dumpRecordsToCsv();
					System.out.flush();
					System.setOut(old);
					List<List<String>> records = new ArrayList<>();
					String[] baosLines = baos.toString().split("\n");
				    for(String line : baosLines)
				    {
				        String[] values = line.split(",");
				        records.add(Arrays.asList(values));
				    }
				    List<List<String>> cleanedRecords = new ArrayList<>();
					for(List<String> record : records)
					{
						String timestamp = record.get(0);
						String velocity = record.get(1);
						String index = record.get(2);
						try {
							int time = Integer.parseInt(timestamp.trim());
							double velInt = Double.parseDouble(velocity.trim());
							if(!metricCheckbox.isSelected())
							{
								velInt = velInt / 304.8;
							}
							else
							{
								velInt = velInt / 1000;
							}
							record.set(1,  String.format("%.2f",velInt));
							cleanedRecords.add(record);
						}
						catch(Exception e)
						{
							continue;
						}
					}
					

			        try 
			        { 
			        	File originalFile = new File(pathTextField.getText());
			        	String newname = originalFile.getPath() + ".csv";
			            // attach a file to FileWriter 
			            FileWriter fw 
			                = new FileWriter(newname); 
			  
			            // read each character from string and write 
			            // into FileWriter 
			            fw.write("timestamp,velocity("+speedoutput+"),shot number\n");
						for(List<String> record : cleanedRecords)
						{
				            fw.write(record.get(0) + "," + record.get(1) + "," +record.get(2) + "\n"); 
				  

				        } 
			            fw.flush();
						  
			            // close the file 
			            fw.close(); 
						//System.out.println(record.get(0) + "," + record.get(1) + "," +record.get(2));
					}
			        catch (Exception e) { 
			            e.getStackTrace(); 
			        } 
					// Put things back

					// Show what happened
					//System.out.println("Here: " + baos.toString());
				}				
			}
		});		
		pack();
		setVisible(true);
	}

    public static void main(String[] args)
	{
    	FitToCsv mainApp = new FitToCsv();
		
		/*System.out.println("Hello FitToCsv World!");
		FitMessageRepository contents = FitReader.getInstance().readFile("D:/garmin-chrono-csv/FitReader-master/FitReader-master/test.fit", true);
		List<String> names = contents.getMessageNames();
		List< List<FitMessage> > allmsgs = new ArrayList< List<FitMessage> >();
		for(String name : names)
		{
			allmsgs.add(contents.getAllMessages(name));
		}
		List<FitMessage> speeds = allmsgs.get(4);
		for(FitMessage fit : speeds)
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			// IMPORTANT: Save the old System.out!
			PrintStream old = System.out;
			// Tell Java to use your special stream
			System.setOut(ps);
			// Print some output: goes to your special stream
			fit.dumpRecordsToCsv();
			// Put things back
			System.out.flush();
			System.setOut(old);
			// Show what happened
			System.out.println("Here: " + baos.toString());
		}*/
	}

	private class FitFileFilter extends FileFilter
	{
		public boolean accept(File pathname)
		{
			return /*pathname.isDirectory() || */pathname.toString().toLowerCase().endsWith(".fit");
		}

		public String getDescription()
		{
			return "Garmin Fit Files (.fit)";
		}
	};

}