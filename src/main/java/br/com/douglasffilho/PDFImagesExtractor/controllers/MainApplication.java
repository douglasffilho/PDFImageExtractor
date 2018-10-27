package br.com.douglasffilho.PDFImagesExtractor.controllers;

import br.com.douglasffilho.PDFImagesExtractor.services.PDFImageExtractorService;
import br.com.douglasffilho.PDFImagesExtractor.services.impl.PDFImageExtractorServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

@Slf4j
public class MainApplication {
	
	private final PDFImageExtractorService pdfImageExtractorService;
	private String currentDir;
	private JPanel contentPanel;
	private JProgressBar pdfReadingProgress;
	private JButton btnChoosePDF;
	private JTextField txtOutputFolder;
	private JButton btnChooseOutputFolder;
	private JTextArea txtLog;
	
	public MainApplication() {
		this.btnChoosePDF.addActionListener(e -> MainApplication.this.btnChoosePdfOnClick());
		
		this.btnChooseOutputFolder.addActionListener(e -> MainApplication.this.btnChooseOutputFolderOnClick());
		
		this.pdfImageExtractorService = new PDFImageExtractorServiceImpl();
	}
	
	public static void main(final String[] args) {
		final JFrame mainFrame = new JFrame("PDF Images Extractor");
		
		final MainApplication mainApplication = new MainApplication();
		
		mainApplication.currentDir = new File("").getAbsolutePath() + "/";
		
		mainApplication.txtOutputFolder.setText(mainApplication.currentDir);
		
		mainApplication.txtLog.setAutoscrolls(true);
		
		mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		mainFrame.setContentPane(mainApplication.contentPanel);
		mainFrame.setMinimumSize(new Dimension(800, 50));
		mainFrame.setResizable(false);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.pack();
		
		mainFrame.setVisible(true);
	}
	
	private String saveImage(final byte[] image) throws Exception {
		final String fileName = this.currentDir + System.currentTimeMillis() + ".jpg";
		
		final BufferedImage buff = ImageIO.read(new ByteArrayInputStream(image));
		if (buff != null) {
			ImageIO.write(buff, "jpg", new File(fileName));
		}
		
		return fileName;
	}
	
	public void btnChoosePdfOnClick() {
		final JFileChooser jFileChooser = new JFileChooser();
		jFileChooser.setFileFilter(new FileNameExtensionFilter("PDF files", "pdf"));
		jFileChooser.setCurrentDirectory(new File(this.currentDir));
		jFileChooser.setAcceptAllFileFilterUsed(false);
		jFileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
		jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		final int option = jFileChooser.showOpenDialog(null);
		if (option == JFileChooser.APPROVE_OPTION) {
			final File choosedFile = jFileChooser.getSelectedFile();
			this.txtLog.append("Selected PDF file: " + choosedFile.getAbsolutePath() + "\n");
			
			try {
				final List<byte[]> images = this.pdfImageExtractorService.getImagesFromPDF(choosedFile);
				images.stream()
						.map(image -> {
							try {
								return this.saveImage(image);
							} catch (final Exception ex) {
								final String errorMessage = ExceptionUtils.getRootCauseMessage(ex);
								log.error("M=MainApplication.btnChoosePdfOnClick(), E=error trying to save image file: {}", errorMessage, ex);
								throw new RuntimeException(errorMessage);
							}
						})
						.forEach(imageName -> this.txtLog.append("Image saved as: " + imageName + "\n"));
			} catch (final Exception ex) {
				final String errorMessage = ExceptionUtils.getRootCauseMessage(ex);
				this.txtLog.append("Error trying to read file: " + errorMessage + "\n");
				log.error("M=MainApplication.btnChoosePdfOnClick(), I=error trying to get images from PDF file: {}", errorMessage, ex);
			}
		}
	}
	
	public void btnChooseOutputFolderOnClick() {
		final JFileChooser jFileChooser = new JFileChooser();
		jFileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "png, jpg"));
		jFileChooser.setCurrentDirectory(new File(this.currentDir));
		jFileChooser.setAcceptAllFileFilterUsed(false);
		jFileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
		jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		final int option = jFileChooser.showOpenDialog(null);
		if (option == JFileChooser.APPROVE_OPTION) {
			final File selectedDir = jFileChooser.getSelectedFile();
			this.currentDir = selectedDir.getAbsolutePath() + "/";
			this.txtOutputFolder.setText(this.currentDir);
			this.txtLog.append("Changed images output folder to: " + this.currentDir + "\n");
		}
	}
	
}
